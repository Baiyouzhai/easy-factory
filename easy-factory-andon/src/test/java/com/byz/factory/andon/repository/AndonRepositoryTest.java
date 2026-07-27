package com.byz.factory.andon.repository;

import com.byz.factory.batch.AndonStatus;
import com.byz.factory.andon.AndonTestConfig;
import com.byz.factory.andon.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = AndonTestConfig.class)
@DisplayName("Andon Repository 集成测试")
class AndonRepositoryTest {

    @Autowired private AndonCallRepository callRepo;
    @Autowired private EscalationRuleRepository ruleRepo;

    @BeforeEach
    void setUp() {
        ruleRepo.deleteAll();
        callRepo.deleteAll();
    }

    // ==================== AndonCallRepository ====================

    @Test
    @DisplayName("保存并查询安灯呼叫 — findByCode")
    void call_saveAndFindByCode() {
        AndonCall call = new AndonCall("ANDON-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "测试");
        callRepo.save(call);

        AndonCall found = callRepo.findByCode("ANDON-001");
        assertNotNull(found);
        assertEquals(TriggerType.EQUIPMENT_FAULT, found.getTriggerType());
        assertEquals(AndonStatus.OPEN, found.getStatus());
        assertEquals("操作工", found.getTriggeredBy());
    }

    @Test
    @DisplayName("按工单号查找安灯呼叫")
    void call_findByWorkOrderNo() {
        AndonCall c1 = new AndonCall("ANDON-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "人", "故障");
        c1.linkWorkOrder("WO-001");
        AndonCall c2 = new AndonCall("ANDON-002", TriggerType.QUALITY_ISSUE,
                AndonSeverity.WARNING, AndonSource.AUTO, "系统", "质量");
        c2.linkWorkOrder("WO-001");
        AndonCall c3 = new AndonCall("ANDON-003", TriggerType.PROCESS_DELAY,
                AndonSeverity.WARNING, AndonSource.AUTO, "系统", "超时");
        c3.linkWorkOrder("WO-002");
        callRepo.saveAll(List.of(c1, c2, c3));

        List<AndonCall> results = callRepo.findByWorkOrderNo("WO-001");
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("按设备编码查找安灯呼叫")
    void call_findByEquipmentCode() {
        AndonCall c1 = new AndonCall("ANDON-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "人", "故障");
        c1.linkEquipment("EQ-001");
        callRepo.save(c1);

        List<AndonCall> results = callRepo.findByEquipmentCode("EQ-001");
        assertEquals(1, results.size());
        assertEquals("ANDON-001", results.get(0).getCode());
    }

    @Test
    @DisplayName("按状态查找安灯呼叫")
    void call_findByStatus() {
        AndonCall c1 = new AndonCall("ANDON-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "人", "故障");
        callRepo.save(c1);

        AndonCall c2 = new AndonCall("ANDON-002", TriggerType.QUALITY_ISSUE,
                AndonSeverity.WARNING, AndonSource.AUTO, "系统", "质量");
        c2.acknowledge("班组长");
        callRepo.save(c2);

        assertEquals(1, callRepo.findByStatus(AndonStatus.OPEN).size());
        assertEquals(1, callRepo.findByStatus(AndonStatus.ACKNOWLEDGED).size());
    }

    @Test
    @DisplayName("状态变更后持久化 — ACKNOWLEDGED 状态")
    void call_statusTransition_acknowledge_shouldPersist() {
        AndonCall call = new AndonCall("ANDON-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");
        callRepo.save(call);

        call.acknowledge("班组长");
        callRepo.save(call);

        AndonCall found = callRepo.findByCode("ANDON-001");
        assertEquals(AndonStatus.ACKNOWLEDGED, found.getStatus());
        assertEquals("班组长", found.getAcknowledgedBy());
        assertNotNull(found.getAcknowledgedAt());
    }

    @Test
    @DisplayName("状态变更后持久化 — 完整生命周期 OPEN→ACK→RESOLVED→CLOSED")
    void call_fullLifecycle_shouldPersist() {
        AndonCall call = new AndonCall("ANDON-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");
        callRepo.save(call);

        call.acknowledge("班组长"); callRepo.save(call);
        assertEquals(AndonStatus.ACKNOWLEDGED, callRepo.findByCode("ANDON-001").getStatus());

        call.resolve("已修复"); callRepo.save(call);
        assertEquals(AndonStatus.RESOLVED, callRepo.findByCode("ANDON-001").getStatus());

        call.close(); callRepo.save(call);
        assertEquals(AndonStatus.CLOSED, callRepo.findByCode("ANDON-001").getStatus());
    }

    // ==================== EscalationRuleRepository ====================

    @Test
    @DisplayName("保存并查询上报规则 — findByCode")
    void rule_saveAndFindByCode() {
        EscalationRule rule = new EscalationRule("RULE-EQ-CRITICAL",
                TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);
        ruleRepo.save(rule);

        EscalationRule found = ruleRepo.findByCode("RULE-EQ-CRITICAL");
        assertNotNull(found);
        assertEquals(TriggerType.EQUIPMENT_FAULT, found.getTriggerType());
        assertEquals(AndonSeverity.CRITICAL, found.getSeverity());
        assertTrue(found.isEnabled());
    }

    @Test
    @DisplayName("按触发类型和严重程度查找唯一规则")
    void rule_findByTriggerTypeAndSeverity() {
        EscalationRule rule = new EscalationRule("RULE-001",
                TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);
        ruleRepo.save(rule);

        EscalationRule found = ruleRepo.findByTriggerTypeAndSeverity(
                TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);
        assertNotNull(found);
        assertEquals("RULE-001", found.getCode());
    }

    @Test
    @DisplayName("查找所有启用的规则")
    void rule_findByEnabledTrue() {
        EscalationRule r1 = new EscalationRule("RULE-001",
                TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);
        EscalationRule r2 = new EscalationRule("RULE-002",
                TriggerType.QUALITY_ISSUE, AndonSeverity.WARNING);
        r2.disable();
        EscalationRule r3 = new EscalationRule("RULE-003",
                TriggerType.SAFETY_INCIDENT, AndonSeverity.EMERGENCY);
        ruleRepo.saveAll(List.of(r1, r2, r3));

        List<EscalationRule> active = ruleRepo.findByEnabledTrue();
        assertEquals(2, active.size());
    }

    @Test
    @DisplayName("上报规则自动停止策略持久化")
    void rule_autoStop_shouldPersist() {
        EscalationRule rule = new EscalationRule("RULE-001",
                TriggerType.SAFETY_INCIDENT, AndonSeverity.EMERGENCY);
        rule.setAutoStopPolicy(EscalationRule.AutoStop.STOP_LINE);
        ruleRepo.save(rule);

        EscalationRule found = ruleRepo.findByCode("RULE-001");
        assertEquals(EscalationRule.AutoStop.STOP_LINE, found.getAutoStop());
    }

    @Test
    @DisplayName("上报规则禁用后 enabled=false 持久化")
    void rule_disable_shouldPersist() {
        EscalationRule rule = new EscalationRule("RULE-001",
                TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);
        assertTrue(rule.isEnabled());
        ruleRepo.save(rule);

        rule.disable();
        ruleRepo.save(rule);

        EscalationRule found = ruleRepo.findByCode("RULE-001");
        assertFalse(found.isEnabled());
    }

    @Test
    @DisplayName("搜索不存在的实体返回 null")
    void findNonExistent_shouldReturnNull() {
        assertNull(callRepo.findByCode("NON-EXISTENT"));
        assertNull(ruleRepo.findByCode("NON-EXISTENT"));
    }

}
