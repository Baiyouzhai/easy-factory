package com.byz.factory.andon;

import com.byz.factory.batch.AndonStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.andon.model.*;
import com.byz.factory.andon.repository.AndonCallRepository;
import com.byz.factory.andon.repository.EscalationRuleRepository;
import com.byz.factory.andon.service.impl.AndonServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = AndonTestConfig.class)
@Import(AndonServiceImpl.class)
@DisplayName("AndonService 实现集成测试")
class AndonServiceImplTest {

    @Autowired private AndonCallRepository callRepo;
    @Autowired private EscalationRuleRepository ruleRepo;
    @Autowired private AndonServiceImpl service;

    @BeforeEach
    void setUp() {
        ruleRepo.deleteAll();
        callRepo.deleteAll();
    }

    @AfterEach
    void tearDown() {
        DomainEventPublisher.clearAll();
    }

    // ==================== trigger ====================

    @Test
    @DisplayName("trigger — 创建呼叫并持久化 + 发布事件")
    void trigger_shouldCreateAndPersist() {
        AndonCall call = service.trigger(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL,
                AndonSource.MANUAL, "操作工张三", "注塑机A温度异常");

        assertNotNull(call);
        assertNotNull(call.getCode());
        assertTrue(call.getCode().startsWith("ANDON-"));
        assertEquals(AndonStatus.OPEN, call.getStatus());
        assertEquals(TriggerType.EQUIPMENT_FAULT, call.getTriggerType());
        assertEquals("操作工张三", call.getTriggeredBy());

        // 验证持久化
        AndonCall found = callRepo.findByCode(call.getCode());
        assertNotNull(found);
    }

    @Test
    @DisplayName("trigger — 自动触发来源")
    void trigger_autoSource_shouldWork() {
        AndonCall call = service.trigger(TriggerType.PROCESS_DELAY, AndonSeverity.WARNING,
                AndonSource.AUTO, "SYSTEM", "工序超时");

        assertEquals(AndonSource.AUTO, call.getSource());
        assertTrue(call.isAutoTriggered());
    }

    @Test
    @DisplayName("trigger — 所有触发类型均可创建")
    void trigger_allTriggerTypes_shouldWork() {
        for (TriggerType tt : TriggerType.values()) {
            AndonCall call = service.trigger(tt, AndonSeverity.WARNING,
                    AndonSource.MANUAL, "测试", "测试-" + tt.name());
            assertEquals(tt, call.getTriggerType());
        }
    }

    // ==================== acknowledge ====================

    @Test
    @DisplayName("acknowledge — 确认呼叫并持久化状态变更")
    void acknowledge_shouldTransitionAndPersist() {
        AndonCall call = service.trigger(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL,
                AndonSource.MANUAL, "操作工", "故障");

        AndonCall result = service.acknowledge(call.getCode(), "班组长李四");

        assertEquals(AndonStatus.ACKNOWLEDGED, result.getStatus());
        assertEquals("班组长李四", result.getAcknowledgedBy());
        assertNotNull(result.getAcknowledgedAt());

        AndonCall found = callRepo.findByCode(call.getCode());
        assertEquals(AndonStatus.ACKNOWLEDGED, found.getStatus());
    }

    @Test
    @DisplayName("acknowledge — 不存在的呼叫抛出异常")
    void acknowledge_nonexistentCall_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.acknowledge("NON-EXISTENT", "测试"));
    }

    // ==================== escalate ====================

    @Test
    @DisplayName("escalate — ACKNOWLEDGED → ESCALATED + 级别递增")
    void escalate_shouldTransitionAndIncrementLevel() {
        AndonCall call = service.trigger(TriggerType.QUALITY_ISSUE, AndonSeverity.CRITICAL,
                AndonSource.AUTO, "SPC系统", "连续不良");
        service.acknowledge(call.getCode(), "班组长");

        AndonCall result = service.escalate(call.getCode(), "超时未响应");

        assertEquals(AndonStatus.ESCALATED, result.getStatus());
        assertEquals(1, result.getEscalationLevel());

        AndonCall found = callRepo.findByCode(call.getCode());
        assertEquals(AndonStatus.ESCALATED, found.getStatus());
    }

    @Test
    @DisplayName("escalate — OPEN 直接升级（紧急情况）")
    void escalate_fromOpen_shouldWork() {
        AndonCall call = service.trigger(TriggerType.SAFETY_INCIDENT, AndonSeverity.EMERGENCY,
                AndonSource.MANUAL, "操作工", "化学品泄漏");

        AndonCall result = service.escalate(call.getCode(), "紧急上报");

        assertEquals(AndonStatus.ESCALATED, result.getStatus());
        assertEquals(1, result.getEscalationLevel());
    }

    @Test
    @DisplayName("escalate — 多次上报级别持续递增")
    void escalate_multiple_shouldIncrementLevel() {
        AndonCall call = service.trigger(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL,
                AndonSource.MANUAL, "操作工", "故障");
        service.acknowledge(call.getCode(), "班组长");

        service.escalate(call.getCode(), "L1超时");
        assertEquals(1, callRepo.findByCode(call.getCode()).getEscalationLevel());

        service.escalate(call.getCode(), "L2超时");
        assertEquals(2, callRepo.findByCode(call.getCode()).getEscalationLevel());
    }

    @Test
    @DisplayName("escalate — 不存在的呼叫抛出异常")
    void escalate_nonexistentCall_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.escalate("NON-EXISTENT", "测试"));
    }

    // ==================== resolve ====================

    @Test
    @DisplayName("resolve — ACKNOWLEDGED → RESOLVED 并持久化")
    void resolve_shouldTransitionAndPersist() {
        AndonCall call = service.trigger(TriggerType.MATERIAL_SHORTAGE, AndonSeverity.WARNING,
                AndonSource.MANUAL, "操作工", "物料不足");
        service.acknowledge(call.getCode(), "班组长");

        AndonCall result = service.resolve(call.getCode(), "已调拨物料");

        assertEquals(AndonStatus.RESOLVED, result.getStatus());
        assertEquals("已调拨物料", result.getResolution());
        assertNotNull(result.getResolvedAt());

        AndonCall found = callRepo.findByCode(call.getCode());
        assertEquals(AndonStatus.RESOLVED, found.getStatus());
    }

    @Test
    @DisplayName("resolve — 不存在的呼叫抛出异常")
    void resolve_nonexistentCall_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.resolve("NON-EXISTENT", "已解决"));
    }

    // ==================== close ====================

    @Test
    @DisplayName("close — RESOLVED → CLOSED 并持久化")
    void close_shouldTransitionAndPersist() {
        AndonCall call = service.trigger(TriggerType.PROCESS_DELAY, AndonSeverity.WARNING,
                AndonSource.MANUAL, "操作工", "工序超时");
        service.acknowledge(call.getCode(), "班组长");
        service.resolve(call.getCode(), "已处理");

        AndonCall result = service.close(call.getCode());

        assertEquals(AndonStatus.CLOSED, result.getStatus());
        assertNotNull(result.getClosedAt());
        assertFalse(result.isActive());

        AndonCall found = callRepo.findByCode(call.getCode());
        assertEquals(AndonStatus.CLOSED, found.getStatus());
    }

    @Test
    @DisplayName("close — OPEN 直接关闭（误触发）")
    void close_fromOpen_shouldWork() {
        AndonCall call = service.trigger(TriggerType.OTHER, AndonSeverity.INFO,
                AndonSource.MANUAL, "操作工", "误触发");

        AndonCall result = service.close(call.getCode());

        assertEquals(AndonStatus.CLOSED, result.getStatus());
        assertFalse(result.isActive());
    }

    @Test
    @DisplayName("close — 不存在的呼叫抛出异常")
    void close_nonexistentCall_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.close("NON-EXISTENT"));
    }

    // ==================== 查询方法 ====================

    @Test
    @DisplayName("findCall — 按编码查找")
    void findCall_shouldReturnCall() {
        AndonCall call = service.trigger(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL,
                AndonSource.MANUAL, "操作工", "故障");

        AndonCall found = service.findCall(call.getCode());
        assertNotNull(found);
        assertEquals(call.getCode(), found.getCode());
    }

    @Test
    @DisplayName("findCall — 不存在的编码返回 null")
    void findCall_nonexistent_shouldReturnNull() {
        assertNull(service.findCall("NON-EXISTENT"));
    }

    @Test
    @DisplayName("getActiveCalls — 不包含已关闭呼叫")
    void getActiveCalls_shouldExcludeClosed() {
        AndonCall open = service.trigger(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL,
                AndonSource.MANUAL, "操作工", "故障1");
        AndonCall ack = service.trigger(TriggerType.QUALITY_ISSUE, AndonSeverity.WARNING,
                AndonSource.AUTO, "系统", "质量");
        service.acknowledge(ack.getCode(), "班组长");
        AndonCall closed = service.trigger(TriggerType.OTHER, AndonSeverity.INFO,
                AndonSource.MANUAL, "操作工", "误触发");
        service.close(closed.getCode());

        List<AndonCall> active = service.getActiveCalls();
        assertEquals(2, active.size());
    }

    @Test
    @DisplayName("findCallsByWorkOrder — 按工单号查询")
    void findCallsByWorkOrder_shouldFilterByWO() {
        AndonCall c1 = service.trigger(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL,
                AndonSource.MANUAL, "操作工", "故障");
        c1.linkWorkOrder("WO-001"); callRepo.save(c1);
        AndonCall c2 = service.trigger(TriggerType.PROCESS_DELAY, AndonSeverity.WARNING,
                AndonSource.AUTO, "系统", "超时");
        c2.linkWorkOrder("WO-001"); callRepo.save(c2);
        AndonCall c3 = service.trigger(TriggerType.QUALITY_ISSUE, AndonSeverity.WARNING,
                AndonSource.AUTO, "系统", "质量");
        c3.linkWorkOrder("WO-002"); callRepo.save(c3);

        assertEquals(2, service.findCallsByWorkOrder("WO-001").size());
        assertEquals(1, service.findCallsByWorkOrder("WO-002").size());
    }

    @Test
    @DisplayName("getUrgentCalls — 只返回 CRITICAL/EMERGENCY 活跃呼叫")
    void getUrgentCalls_shouldFilterBySeverityAndActive() {
        AndonCall critical = service.trigger(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL,
                AndonSource.MANUAL, "操作工", "严重故障");
        AndonCall emergency = service.trigger(TriggerType.SAFETY_INCIDENT, AndonSeverity.EMERGENCY,
                AndonSource.MANUAL, "操作工", "安全事件");
        AndonCall warning = service.trigger(TriggerType.PROCESS_DELAY, AndonSeverity.WARNING,
                AndonSource.AUTO, "系统", "警告");
        AndonCall info = service.trigger(TriggerType.OTHER, AndonSeverity.INFO,
                AndonSource.MANUAL, "操作工", "信息");

        List<AndonCall> urgent = service.getUrgentCalls();
        assertEquals(2, urgent.size());
    }

    @Test
    @DisplayName("getDashboard — 返回正确的看板统计")
    void getDashboard_shouldReturnStats() {
        service.trigger(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL,
                AndonSource.MANUAL, "操作工", "故障");
        service.trigger(TriggerType.QUALITY_ISSUE, AndonSeverity.WARNING,
                AndonSource.AUTO, "系统", "质量");
        service.trigger(TriggerType.MATERIAL_SHORTAGE, AndonSeverity.WARNING,
                AndonSource.MANUAL, "操作工", "物料");

        AndonDashboard dashboard = service.getDashboard();
        assertEquals(3, dashboard.totalCalls());
        assertEquals(1, dashboard.urgentCount());
        assertTrue(dashboard.bySeverity().containsKey("CRITICAL"));
        assertTrue(dashboard.bySeverity().containsKey("WARNING"));
        assertTrue(dashboard.byTriggerType().containsKey("EQUIPMENT_FAULT"));
        assertTrue(dashboard.byTriggerType().containsKey("QUALITY_ISSUE"));
        assertTrue(dashboard.byTriggerType().containsKey("MATERIAL_SHORTAGE"));
    }

    // ==================== 规则管理 ====================

    @Test
    @DisplayName("createRule — 创建规则并持久化")
    void createRule_shouldPersist() {
        EscalationRule rule = service.createRule(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);

        assertNotNull(rule);
        assertTrue(rule.getCode().startsWith("RULE-"));
        assertEquals(TriggerType.EQUIPMENT_FAULT, rule.getTriggerType());
        assertEquals(AndonSeverity.CRITICAL, rule.getSeverity());
        assertTrue(rule.isEnabled());

        EscalationRule found = ruleRepo.findByCode(rule.getCode());
        assertNotNull(found);
    }

    @Test
    @DisplayName("addEscalationLevel — 添加级别并发布事件")
    void addEscalationLevel_shouldAddAndPersist() {
        EscalationRule rule = service.createRule(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);

        EscalationRule.EscalationLevel level = new EscalationRule.EscalationLevel(
                1, 0, EscalationRule.EscalateCondition.TIMEOUT);
        level.addNotifyRole("班组长");

        service.addEscalationLevel(rule.getCode(), level);

        EscalationRule found = ruleRepo.findByCode(rule.getCode());
        assertNotNull(found);
        assertEquals(1, found.getMaxLevel());
    }

    @Test
    @DisplayName("findRule — 按触发类型和严重程度查询")
    void findRule_shouldReturnMatchingRule() {
        service.createRule(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);

        EscalationRule found = service.findRule(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);
        assertNotNull(found);
        assertEquals(TriggerType.EQUIPMENT_FAULT, found.getTriggerType());
    }

    @Test
    @DisplayName("findRule — 没有匹配返回 null")
    void findRule_nonexistent_shouldReturnNull() {
        assertNull(service.findRule(TriggerType.OTHER, AndonSeverity.INFO));
    }

    @Test
    @DisplayName("getActiveRules — 返回所有启用的规则")
    void getActiveRules_shouldReturnEnabledRules() {
        EscalationRule r1 = service.createRule(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);
        EscalationRule r2 = service.createRule(TriggerType.QUALITY_ISSUE, AndonSeverity.WARNING);
        service.disableRule(r2.getCode());

        List<EscalationRule> active = service.getActiveRules();
        assertEquals(1, active.size());
    }

    @Test
    @DisplayName("disableRule / enableRule — 切换启用状态")
    void disableAndEnableRule_shouldToggleState() {
        EscalationRule rule = service.createRule(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);

        service.disableRule(rule.getCode());
        assertFalse(ruleRepo.findByCode(rule.getCode()).isEnabled());

        service.enableRule(rule.getCode());
        assertTrue(ruleRepo.findByCode(rule.getCode()).isEnabled());
    }

    @Test
    @DisplayName("disableRule — 不存在的规则抛出异常")
    void disableRule_nonexistent_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.disableRule("NON-EXISTENT"));
    }

    // ==================== 综合场景 ====================

    @Test
    @DisplayName("综合场景 — 设备故障完整上报链（触发→确认→上报→解决→关闭）")
    void integration_fullEscalationFlow() {
        // Step 1: 触发
        AndonCall call = service.trigger(TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL,
                AndonSource.MANUAL, "操作工张三", "注塑机A温度异常");
        call.linkEquipment("EQ-DX-001");
        call.linkWorkOrder("WO-20260725-001");
        callRepo.save(call);
        assertEquals(AndonStatus.OPEN, call.getStatus());

        // Step 2: 班组长确认
        service.acknowledge(call.getCode(), "班组长李四");
        assertEquals(AndonStatus.ACKNOWLEDGED, callRepo.findByCode(call.getCode()).getStatus());

        // Step 3: 超时上报
        service.escalate(call.getCode(), "L1超时未响应");
        assertEquals(AndonStatus.ESCALATED, callRepo.findByCode(call.getCode()).getStatus());

        // Step 4: 修复解决
        service.resolve(call.getCode(), "更换温度传感器");
        assertEquals(AndonStatus.RESOLVED, callRepo.findByCode(call.getCode()).getStatus());

        // Step 5: 关闭
        service.close(call.getCode());
        assertEquals(AndonStatus.CLOSED, callRepo.findByCode(call.getCode()).getStatus());
        assertFalse(callRepo.findByCode(call.getCode()).isActive());
    }

    @Test
    @DisplayName("综合场景 — 误触发直接关闭")
    void integration_falseTrigger_directClose() {
        AndonCall call = service.trigger(TriggerType.OTHER, AndonSeverity.INFO,
                AndonSource.MANUAL, "操作工赵六", "误触碰安灯按钮");

        service.close(call.getCode());

        AndonCall found = callRepo.findByCode(call.getCode());
        assertEquals(AndonStatus.CLOSED, found.getStatus());
        assertFalse(found.isActive());
    }

}
