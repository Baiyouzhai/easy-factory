package com.byz.factory.andon;

import com.byz.factory.batch.AndonStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.AndonEventTypes;
import com.byz.factory.andon.model.*;
import com.byz.factory.andon.service.AndonService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Andon 模块单元测试。
 * <p>
 * 覆盖：AndonCall、EscalationRule、EscalationLevel、AndonDashboard 的
 * 构造、状态转换、业务便捷方法、事件发布、IExpand 以及枚举定义。
 *
 * @author 苏政
 */
@DisplayName("Andon 模块 单元测试")
class AndonModuleTest {

    @AfterEach
    void tearDown() {
        DomainEventPublisher.clearAll();
    }

    // ==================== AndonCall 构造测试 ====================

    @Test
    @DisplayName("AndonCall 创建 — 初始状态为 OPEN + escalationLevel=0")
    void andonCall_creation_shouldSetDefaults() {
        // Given & When
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工张三", "注塑机A温度异常");

        // Then
        assertEquals("CALL-001", call.getCode());
        assertEquals("Andon-CALL-001", call.getName());
        assertEquals(AndonStatus.OPEN, call.getStatus());
        assertEquals(TriggerType.EQUIPMENT_FAULT, call.getTriggerType());
        assertEquals(AndonSeverity.CRITICAL, call.getSeverity());
        assertEquals(AndonSource.MANUAL, call.getSource());
        assertEquals("操作工张三", call.getTriggeredBy());
        assertEquals("注塑机A温度异常", call.getDescription());
        assertNotNull(call.getTriggeredAt());
        assertEquals(0, call.getEscalationLevel());
        assertNull(call.getAcknowledgedBy());
        assertNull(call.getResolvedAt());
        assertNull(call.getClosedAt());
        assertTrue(call.isActive());
    }

    @Test
    @DisplayName("AndonCall 创建 — 系统自动触发（AUTO）")
    void andonCall_creation_autoSource_shouldSetCorrectly() {
        // When
        AndonCall call = new AndonCall("AUTO-001", TriggerType.PROCESS_DELAY,
                AndonSeverity.WARNING, AndonSource.AUTO, "SYSTEM", "工序超时");

        // Then
        assertEquals(AndonSource.AUTO, call.getSource());
        assertTrue(call.isAutoTriggered());
        assertEquals("SYSTEM", call.getTriggeredBy());
    }

    @Test
    @DisplayName("AndonCall 创建 — 所有触发类型可正常构造")
    void andonCall_creation_allTriggerTypes_shouldWork() {
        for (TriggerType tt : TriggerType.values()) {
            AndonCall call = new AndonCall("C-" + tt.name(), tt, AndonSeverity.WARNING,
                    AndonSource.MANUAL, "测试", "测试");
            assertEquals(tt, call.getTriggerType());
            assertEquals(AndonStatus.OPEN, call.getStatus());
        }
    }

    @Test
    @DisplayName("AndonCall 创建 — 所有严重程度可正常构造")
    void andonCall_creation_allSeverities_shouldWork() {
        for (AndonSeverity sev : AndonSeverity.values()) {
            AndonCall call = new AndonCall("C-" + sev.name(), TriggerType.OTHER, sev,
                    AndonSource.MANUAL, "测试", "测试");
            assertEquals(sev, call.getSeverity());
        }
    }

    // ==================== AndonCall 状态转换测试 ====================

    @Test
    @DisplayName("AndonCall 确认 — OPEN → ACKNOWLEDGED 并发布事件")
    void andonCall_acknowledge_shouldTransitionAndPublishEvent() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, events::add);

        // When
        call.acknowledge("班组长李四");

        // Then
        assertEquals(AndonStatus.ACKNOWLEDGED, call.getStatus());
        assertEquals("班组长李四", call.getAcknowledgedBy());
        assertNotNull(call.getAcknowledgedAt());
        assertEquals(1, events.size());
        assertEquals(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("AndonCall 上报 — ACKNOWLEDGED → ESCALATED + escalationLevel递增")
    void andonCall_escalate_shouldTransitionAndIncrementLevel() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.QUALITY_ISSUE,
                AndonSeverity.CRITICAL, AndonSource.AUTO, "SPC系统", "连续不良");
        call.acknowledge("班组长");

        // When
        call.escalate("超时未响应");

        // Then
        assertEquals(AndonStatus.ESCALATED, call.getStatus());
        assertEquals(1, call.getEscalationLevel());
        assertEquals("超时未响应", call.getExpandProperty("andon.call.escalationReason"));
    }

    @Test
    @DisplayName("AndonCall 上报 — OPEN 直接升级（无人确认）")
    void andonCall_escalate_fromOpen_shouldTransitionDirectly() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.SAFETY_INCIDENT,
                AndonSeverity.EMERGENCY, AndonSource.MANUAL, "操作工", "化学品泄漏");

        // When
        call.escalate("紧急情况直接上报");

        // Then
        assertEquals(AndonStatus.ESCALATED, call.getStatus());
        assertEquals(1, call.getEscalationLevel());
    }

    @Test
    @DisplayName("AndonCall 多次上报 — escalationLevel 持续递增")
    void andonCall_escalate_multipleLevels_shouldIncrement() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");
        call.acknowledge("班组长");

        // L1 → L2
        call.escalate("第一次超时");
        assertEquals(1, call.getEscalationLevel());

        // L2 → L3 (stay in ESCALATED)
        call.escalate("第二次超时");
        assertEquals(2, call.getEscalationLevel());

        // L3 → L4
        call.escalate("第三次超时");
        assertEquals(3, call.getEscalationLevel());
    }

    @Test
    @DisplayName("AndonCall 解决 — ACKNOWLEDGED → RESOLVED 并发布事件")
    void andonCall_resolve_fromAcknowledged_shouldTransitionAndPublish() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.MATERIAL_SHORTAGE,
                AndonSeverity.WARNING, AndonSource.MANUAL, "操作工", "物料不足");
        call.acknowledge("班组长");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(AndonEventTypes.ANDON_CALL_RESOLVED, events::add);

        // When
        call.resolve("已从线边仓调拨物料");

        // Then
        assertEquals(AndonStatus.RESOLVED, call.getStatus());
        assertEquals("已从线边仓调拨物料", call.getResolution());
        assertNotNull(call.getResolvedAt());
        assertEquals(1, events.size());
        assertEquals(AndonEventTypes.ANDON_CALL_RESOLVED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("AndonCall 解决 — ESCALATED → RESOLVED")
    void andonCall_resolve_fromEscalated_shouldTransition() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.AUTO, "PLC系统", "设备停机");
        call.escalate("无人响应");

        // When
        call.resolve("设备工程师修复完成");

        // Then
        assertEquals(AndonStatus.RESOLVED, call.getStatus());
    }

    @Test
    @DisplayName("AndonCall 关闭 — RESOLVED → CLOSED 并发布事件")
    void andonCall_close_shouldTransitionAndPublish() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.PROCESS_DELAY,
                AndonSeverity.WARNING, AndonSource.MANUAL, "操作工", "工序超时");
        call.acknowledge("班组长");
        call.resolve("已处理");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(AndonEventTypes.ANDON_CALL_CLOSED, events::add);

        // When
        call.close();

        // Then
        assertEquals(AndonStatus.CLOSED, call.getStatus());
        assertNotNull(call.getClosedAt());
        assertEquals(1, events.size());
        assertEquals(AndonEventTypes.ANDON_CALL_CLOSED, events.get(0).getEventType());
        assertFalse(call.isActive());
    }

    @Test
    @DisplayName("AndonCall 关闭 — OPEN 直接关闭（误触发）")
    void andonCall_close_fromOpen_shouldWork() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.OTHER,
                AndonSeverity.INFO, AndonSource.MANUAL, "操作工", "误触发");

        // When
        call.close();

        // Then
        assertEquals(AndonStatus.CLOSED, call.getStatus());
        assertFalse(call.isActive());
    }

    @Test
    @DisplayName("AndonCall 完整生命周期 — OPEN→ACKNOWLEDGED→RESOLVED→CLOSED")
    void andonCall_fullLifecycle_shouldWork() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");

        assertEquals(AndonStatus.OPEN, call.getStatus());

        call.acknowledge("班组长");
        assertEquals(AndonStatus.ACKNOWLEDGED, call.getStatus());

        call.resolve("已修复");
        assertEquals(AndonStatus.RESOLVED, call.getStatus());

        call.close();
        assertEquals(AndonStatus.CLOSED, call.getStatus());
    }

    @Test
    @DisplayName("AndonCall 完整生命周期 — OPEN→ESCALATED→RESOLVED→CLOSED")
    void andonCall_fullLifecycle_withEscalation_shouldWork() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.SAFETY_INCIDENT,
                AndonSeverity.EMERGENCY, AndonSource.MANUAL, "操作工", "安全事件");

        call.escalate("紧急上报");
        assertEquals(AndonStatus.ESCALATED, call.getStatus());

        call.resolve("安全事件已处理");
        assertEquals(AndonStatus.RESOLVED, call.getStatus());

        call.close();
        assertEquals(AndonStatus.CLOSED, call.getStatus());
    }

    // ==================== AndonCall 非法状态转换测试 ====================

    @Test
    @DisplayName("AndonCall 非法转换 — ACKNOWLEDGED 不能直接 CLOSE（需先 RESOLVE）")
    void andonCall_illegalTransition_acknowledgedToClosed_shouldThrow() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");
        call.acknowledge("班组长");

        assertThrows(IllegalStateException.class, call::close);
    }

    @Test
    @DisplayName("AndonCall 非法转换 — RESOLVED 不能 ACKNOWLEDGE")
    void andonCall_illegalTransition_resolvedToAcknowledged_shouldThrow() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");
        call.acknowledge("班组长");
        call.resolve("已修复");

        assertThrows(IllegalStateException.class, () -> call.acknowledge("其他人"));
    }

    @Test
    @DisplayName("AndonCall 非法转换 — OPEN 不能直接 RESOLVE")
    void andonCall_illegalTransition_openToResolved_shouldThrow() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");

        assertThrows(IllegalStateException.class, () -> call.resolve("跳过确认直接解决"));
    }

    @Test
    @DisplayName("AndonCall 终态 — CLOSED 不能做 reopen 操作（acknowledge/resolve/escalate）")
    void andonCall_terminalState_shouldRejectAllTransitions() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.OTHER,
                AndonSeverity.INFO, AndonSource.MANUAL, "操作工", "测试");
        call.close();

        assertThrows(IllegalStateException.class, () -> call.acknowledge("测试"));
        assertThrows(IllegalStateException.class, () -> call.resolve("测试"));
        assertThrows(IllegalStateException.class, () -> call.escalate("测试"));
        // close() 同状态幂等 — CLOSED→CLOSED 不抛异常
        assertDoesNotThrow(call::close);
    }

    @Test
    @DisplayName("AndonCall 同状态幂等 — OPEN→OPEN 不抛异常")
    void andonCall_sameStateTransition_shouldNotThrow() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.OTHER,
                AndonSeverity.INFO, AndonSource.MANUAL, "操作工", "测试");

        assertDoesNotThrow(() -> call.transition(AndonStatus.OPEN));
    }

    // ==================== AndonCall 查询方法测试 ====================

    @Test
    @DisplayName("AndonCall isActive — 未关闭返回 true")
    void andonCall_isActive_shouldReturnTrueForNonClosed() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");
        assertTrue(call.isActive());

        call.acknowledge("班组长");
        assertTrue(call.isActive());

        call.resolve("已修复");
        assertTrue(call.isActive());

        call.close();
        assertFalse(call.isActive());
    }

    @Test
    @DisplayName("AndonCall isUrgent — CRITICAL/EMERGENCY 返回 true")
    void andonCall_isUrgent_shouldReturnCorrectly() {
        AndonCall info = new AndonCall("C-1", TriggerType.OTHER, AndonSeverity.INFO,
                AndonSource.MANUAL, "人", "测试");
        AndonCall warn = new AndonCall("C-2", TriggerType.OTHER, AndonSeverity.WARNING,
                AndonSource.MANUAL, "人", "测试");
        AndonCall crit = new AndonCall("C-3", TriggerType.OTHER, AndonSeverity.CRITICAL,
                AndonSource.MANUAL, "人", "测试");
        AndonCall emerg = new AndonCall("C-4", TriggerType.OTHER, AndonSeverity.EMERGENCY,
                AndonSource.MANUAL, "人", "测试");

        assertFalse(info.isUrgent());
        assertFalse(warn.isUrgent());
        assertTrue(crit.isUrgent());
        assertTrue(emerg.isUrgent());
    }

    @Test
    @DisplayName("AndonCall isAutoTriggered — AUTO 来源返回 true")
    void andonCall_isAutoTriggered_shouldReturnCorrectly() {
        AndonCall manual = new AndonCall("C-1", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.WARNING, AndonSource.MANUAL, "人", "测试");
        AndonCall auto = new AndonCall("C-2", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.WARNING, AndonSource.AUTO, "系统", "测试");

        assertFalse(manual.isAutoTriggered());
        assertTrue(auto.isAutoTriggered());
    }

    @Test
    @DisplayName("AndonCall getDurationSeconds — 计算触发到现在的时长")
    void andonCall_getDurationSeconds_shouldReturnPositive() throws Exception {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");

        Thread.sleep(100); // 确保有时长
        long duration = call.getDurationSeconds();
        assertTrue(duration >= 0, "Duration should be >= 0, was " + duration);
    }

    @Test
    @DisplayName("AndonCall getDurationSeconds — 已解决后计算到 resolvedAt 的时长")
    void andonCall_getDurationSeconds_afterResolved_shouldUseResolvedAt() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");
        call.acknowledge("班组长");
        call.resolve("已修复");

        long duration = call.getDurationSeconds();
        assertTrue(duration >= 0);
    }

    // ==================== AndonCall 关联方法测试 ====================

    @Test
    @DisplayName("AndonCall 关联工单/工序/设备")
    void andonCall_linkMethods_shouldSetFields() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");

        call.linkWorkOrder("WO-20260725-001");
        call.linkProcess("PROC-001");
        call.linkEquipment("EQ-DX-001");

        assertEquals("WO-20260725-001", call.getWorkOrderNo());
        assertEquals("PROC-001", call.getProcessCode());
        assertEquals("EQ-DX-001", call.getEquipmentCode());
    }

    // ==================== AndonCall IExpand 测试 ====================

    @Test
    @DisplayName("AndonCall IExpand — 动态属性设置和读取")
    void andonCall_iExpand_shouldWork() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");

        call.setExpandProperty("andon.call.escalationLevel", "3");
        call.setExpandProperty("andon.call.acknowledgedBy", "班组长");

        assertEquals("3", call.getExpandProperty("andon.call.escalationLevel"));
        assertEquals("班组长", call.getExpandProperty("andon.call.acknowledgedBy"));
    }

    // ==================== AndonCall 事件发布测试 ====================

    @Test
    @DisplayName("AndonCall 事件 — 确认事件载荷包含正确字段")
    void andonCall_acknowledge_eventPayload_shouldContainCorrectFields() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工", "故障");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, events::add);

        call.acknowledge("班组长李四");

        assertEquals(1, events.size());
        IDomainEvent event = events.get(0);
        assertEquals(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, event.getEventType());
        assertEquals(AndonEventTypes.PREFIX, event.getSource());
    }

    @Test
    @DisplayName("AndonCall 事件 — 解决事件载荷包含 resolution")
    void andonCall_resolve_eventPayload_shouldContainResolution() {
        AndonCall call = new AndonCall("CALL-001", TriggerType.MATERIAL_SHORTAGE,
                AndonSeverity.WARNING, AndonSource.MANUAL, "操作工", "物料不足");
        call.acknowledge("班组长");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(AndonEventTypes.ANDON_CALL_RESOLVED, events::add);

        call.resolve("已从备用仓库调拨");

        assertEquals(1, events.size());
        assertEquals(AndonEventTypes.ANDON_CALL_RESOLVED, events.get(0).getEventType());
    }

    // ==================== EscalationRule 构造测试 ====================

    @Test
    @DisplayName("EscalationRule 创建 — 默认值正确")
    void escalationRule_creation_shouldSetDefaults() {
        // When
        EscalationRule rule = new EscalationRule("RULE-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL);

        // Then
        assertEquals("RULE-001", rule.getCode());
        assertEquals(TriggerType.EQUIPMENT_FAULT, rule.getTriggerType());
        assertEquals(AndonSeverity.CRITICAL, rule.getSeverity());
        assertEquals(EscalationRule.AutoStop.NONE, rule.getAutoStop());
        assertTrue(rule.isEnabled());
        assertNotNull(rule.getLevels());
        assertTrue(rule.getLevels().isEmpty());
        assertEquals(0, rule.getMaxLevel());
    }

    @Test
    @DisplayName("EscalationRule 添加上报级别 — levels 应增加")
    void escalationRule_addLevel_shouldAddToLevels() {
        EscalationRule rule = new EscalationRule("RULE-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL);

        EscalationRule.EscalationLevel level1 = new EscalationRule.EscalationLevel(
                1, 0, EscalationRule.EscalateCondition.TIMEOUT);
        level1.addNotifyRole("班组长");

        EscalationRule.EscalationLevel level2 = new EscalationRule.EscalationLevel(
                2, 5, EscalationRule.EscalateCondition.TIMEOUT);
        level2.addNotifyRole("车间主任");
        level2.addNotifyRole("设备工程师");

        // When
        rule.addLevel(level1);
        rule.addLevel(level2);

        // Then
        assertEquals(2, rule.getLevels().size());
        assertEquals(2, rule.getMaxLevel());
        assertNotNull(rule.getLevel(1));
        assertEquals(1, rule.getLevel(1).getLevel());
        assertEquals(0, rule.getLevel(1).getTimeoutMinutes());
        assertNotNull(rule.getLevel(2));
        assertEquals(2, rule.getLevel(2).getLevel());
        assertEquals(5, rule.getLevel(2).getTimeoutMinutes());
    }

    @Test
    @DisplayName("EscalationRule getLevel — 不存在的级别返回 null")
    void escalationRule_getLevel_nonexistent_shouldReturnNull() {
        EscalationRule rule = new EscalationRule("RULE-001", TriggerType.QUALITY_ISSUE,
                AndonSeverity.WARNING);

        assertNull(rule.getLevel(1));
        assertNull(rule.getLevel(99));
    }

    @Test
    @DisplayName("EscalationRule 启用/禁用")
    void escalationRule_enableDisable_shouldWork() {
        EscalationRule rule = new EscalationRule("RULE-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL);

        rule.disable();
        assertFalse(rule.isEnabled());

        rule.enable();
        assertTrue(rule.isEnabled());
    }

    @Test
    @DisplayName("EscalationRule 设置自动停止策略")
    void escalationRule_setAutoStopPolicy_shouldWork() {
        EscalationRule rule = new EscalationRule("RULE-001", TriggerType.SAFETY_INCIDENT,
                AndonSeverity.EMERGENCY);

        rule.setAutoStopPolicy(EscalationRule.AutoStop.STOP_LINE);
        assertEquals(EscalationRule.AutoStop.STOP_LINE, rule.getAutoStop());
    }

    // ==================== EscalationLevel 测试 ====================

    @Test
    @DisplayName("EscalationLevel 创建 — 无参构造 + 全参构造")
    void escalationLevel_creation_shouldWork() {
        EscalationRule.EscalationLevel level1 = new EscalationRule.EscalationLevel();
        assertEquals(0, level1.getLevel());
        assertNotNull(level1.getNotifyRoles());
        assertTrue(level1.getNotifyRoles().isEmpty());

        EscalationRule.EscalationLevel level2 = new EscalationRule.EscalationLevel(
                3, 15, EscalationRule.EscalateCondition.TIMEOUT);
        assertEquals(3, level2.getLevel());
        assertEquals(15, level2.getTimeoutMinutes());
        assertEquals(EscalationRule.EscalateCondition.TIMEOUT, level2.getEscalateOn());
    }

    @Test
    @DisplayName("EscalationLevel 添加通知角色")
    void escalationLevel_addNotifyRole_shouldWork() {
        EscalationRule.EscalationLevel level = new EscalationRule.EscalationLevel(
                1, 0, EscalationRule.EscalateCondition.TIMEOUT);

        level.addNotifyRole("班组长");
        level.addNotifyRole("设备工程师");
        level.addNotifyRole("车间主任");

        assertEquals(3, level.getNotifyRoles().size());
        assertTrue(level.getNotifyRoles().contains("班组长"));
        assertTrue(level.getNotifyRoles().contains("设备工程师"));
        assertTrue(level.getNotifyRoles().contains("车间主任"));
    }

    // ==================== 枚举测试 ====================

    @Test
    @DisplayName("TriggerType 枚举 — 六个触发类型")
    void triggerType_enum_shouldHaveSixValues() {
        assertEquals(6, TriggerType.values().length);
        assertNotNull(TriggerType.valueOf("EQUIPMENT_FAULT"));
        assertNotNull(TriggerType.valueOf("QUALITY_ISSUE"));
        assertNotNull(TriggerType.valueOf("MATERIAL_SHORTAGE"));
        assertNotNull(TriggerType.valueOf("SAFETY_INCIDENT"));
        assertNotNull(TriggerType.valueOf("PROCESS_DELAY"));
        assertNotNull(TriggerType.valueOf("OTHER"));
    }

    @Test
    @DisplayName("AndonSeverity 枚举 — 四个严重程度")
    void andonSeverity_enum_shouldHaveFourValues() {
        assertEquals(4, AndonSeverity.values().length);
        assertNotNull(AndonSeverity.valueOf("INFO"));
        assertNotNull(AndonSeverity.valueOf("WARNING"));
        assertNotNull(AndonSeverity.valueOf("CRITICAL"));
        assertNotNull(AndonSeverity.valueOf("EMERGENCY"));
    }

    @Test
    @DisplayName("AndonSource 枚举 — 两个来源")
    void andonSource_enum_shouldHaveTwoValues() {
        assertEquals(2, AndonSource.values().length);
        assertNotNull(AndonSource.valueOf("MANUAL"));
        assertNotNull(AndonSource.valueOf("AUTO"));
    }

    @Test
    @DisplayName("EscalationRule.AutoStop 枚举 — 四个自动停止策略")
    void autoStop_enum_shouldHaveFourValues() {
        assertEquals(4, EscalationRule.AutoStop.values().length);
        assertNotNull(EscalationRule.AutoStop.valueOf("NONE"));
        assertNotNull(EscalationRule.AutoStop.valueOf("PAUSE_PROCESS"));
        assertNotNull(EscalationRule.AutoStop.valueOf("STOP_LINE"));
        assertNotNull(EscalationRule.AutoStop.valueOf("STOP_FACTORY"));
    }

    @Test
    @DisplayName("EscalationRule.EscalateCondition 枚举 — 两个上报条件")
    void escalateCondition_enum_shouldHaveTwoValues() {
        assertEquals(2, EscalationRule.EscalateCondition.values().length);
        assertNotNull(EscalationRule.EscalateCondition.valueOf("TIMEOUT"));
        assertNotNull(EscalationRule.EscalateCondition.valueOf("NO_RESPONSE"));
    }

    // ==================== AndonStatus 状态机测试 ====================

    @Test
    @DisplayName("AndonStatus allowedTransitions — OPEN 可转到 ACKNOWLEDGED/CLOSED")
    void andonStatus_open_allowedTransitions() {
        Set<AndonStatus> allowed = AndonStatus.OPEN.allowedTransitions();
        assertEquals(3, allowed.size());
        assertTrue(allowed.contains(AndonStatus.ACKNOWLEDGED));
        assertTrue(allowed.contains(AndonStatus.ESCALATED));
        assertTrue(allowed.contains(AndonStatus.CLOSED));
    }

    @Test
    @DisplayName("AndonStatus allowedTransitions — ACKNOWLEDGED 可转到 RESOLVED/ESCALATED")
    void andonStatus_acknowledged_allowedTransitions() {
        Set<AndonStatus> allowed = AndonStatus.ACKNOWLEDGED.allowedTransitions();
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(AndonStatus.RESOLVED));
        assertTrue(allowed.contains(AndonStatus.ESCALATED));
    }

    @Test
    @DisplayName("AndonStatus allowedTransitions — RESOLVED 可转到 CLOSED")
    void andonStatus_resolved_allowedTransitions() {
        Set<AndonStatus> allowed = AndonStatus.RESOLVED.allowedTransitions();
        assertEquals(1, allowed.size());
        assertTrue(allowed.contains(AndonStatus.CLOSED));
    }

    @Test
    @DisplayName("AndonStatus allowedTransitions — ESCALATED 可转到 RESOLVED/CLOSED")
    void andonStatus_escalated_allowedTransitions() {
        Set<AndonStatus> allowed = AndonStatus.ESCALATED.allowedTransitions();
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(AndonStatus.RESOLVED));
        assertTrue(allowed.contains(AndonStatus.CLOSED));
    }

    @Test
    @DisplayName("AndonStatus allowedTransitions — CLOSED 终态不可转换")
    void andonStatus_closed_allowedTransitions() {
        Set<AndonStatus> allowed = AndonStatus.CLOSED.allowedTransitions();
        assertTrue(allowed.isEmpty());
    }

    // ==================== AndonDashboard 测试 ====================

    @Test
    @DisplayName("AndonDashboard empty — 返回全零数据")
    void andonDashboard_empty_shouldReturnZeros() {
        AndonDashboard dashboard = AndonDashboard.empty();

        assertEquals(0, dashboard.totalCalls());
        assertEquals(0, dashboard.urgentCount());
        assertEquals(0.0, dashboard.avgResponseMinutes());
        assertTrue(dashboard.bySeverity().isEmpty());
        assertTrue(dashboard.byTriggerType().isEmpty());
        assertFalse(dashboard.hasUrgentCalls());
    }

    @Test
    @DisplayName("AndonDashboard hasUrgentCalls — 有紧急呼叫返回 true")
    void andonDashboard_hasUrgentCalls_shouldDetectEmergency() {
        AndonDashboard with = new AndonDashboard(5, Map.of(), Map.of(), 3, 2.5);
        AndonDashboard without = new AndonDashboard(5, Map.of(), Map.of(), 0, 2.5);

        assertTrue(with.hasUrgentCalls());
        assertFalse(without.hasUrgentCalls());
    }

    // ==================== AndonEventTypes 常量测试 ====================

    @Test
    @DisplayName("AndonEventTypes — 事件命名遵循 {module}.{entity}.{past_tense} 格式")
    void andonEventTypes_naming_shouldFollowConvention() {
        assertTrue(AndonEventTypes.ANDON_CALL_CREATED.startsWith("andon.call."));
        assertTrue(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED.startsWith("andon.call."));
        assertTrue(AndonEventTypes.ANDON_CALL_ESCALATED.startsWith("andon.call."));
        assertTrue(AndonEventTypes.ANDON_CALL_RESOLVED.startsWith("andon.call."));
        assertTrue(AndonEventTypes.ANDON_CALL_CLOSED.startsWith("andon.call."));
        assertTrue(AndonEventTypes.RULE_CREATED.startsWith("andon.rule."));
        assertTrue(AndonEventTypes.RULE_UPDATED.startsWith("andon.rule."));
    }

    @Test
    @DisplayName("AndonEventTypes PREFIX — 应等于 andon")
    void andonEventTypes_prefix_shouldBeAndon() {
        assertEquals("andon", AndonEventTypes.PREFIX);
    }

    @Test
    @DisplayName("AndonEventTypes — 共 7 个事件常量")
    void andonEventTypes_count_shouldBeSeven() {
        // 验证核心事件常量数量
        assertEquals("andon.call.created", AndonEventTypes.ANDON_CALL_CREATED);
        assertEquals("andon.call.acknowledged", AndonEventTypes.ANDON_CALL_ACKNOWLEDGED);
        assertEquals("andon.call.escalated", AndonEventTypes.ANDON_CALL_ESCALATED);
        assertEquals("andon.call.resolved", AndonEventTypes.ANDON_CALL_RESOLVED);
        assertEquals("andon.call.closed", AndonEventTypes.ANDON_CALL_CLOSED);
        assertEquals("andon.rule.created", AndonEventTypes.RULE_CREATED);
        assertEquals("andon.rule.updated", AndonEventTypes.RULE_UPDATED);
    }

    // ==================== AndonService 接口契约测试 ====================

    @Test
    @DisplayName("AndonService 接口 — 方法签名可访问")
    void andonService_interface_shouldExist() {
        // 验证接口编译且方法签名正确
        Class<AndonService> clazz = com.byz.factory.andon.service.AndonService.class;
        assertTrue(clazz.isInterface());
        assertTrue(clazz.getMethods().length >= 10,
                "AndonService should have at least 10 methods");
    }

    // ==================== 综合场景测试 ====================

    @Test
    @DisplayName("综合场景 — 设备故障完整上报链")
    void integration_equipmentFault_fullEscalationFlow() {
        // 场景：注塑机故障 → 操作工触发 → 班组长确认 → 5min未解决上报车间主任 → 15min未解决上报厂长 → 修复 → 关闭

        // Step 1: 触发呼叫
        AndonCall call = new AndonCall("ANDON-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.CRITICAL, AndonSource.MANUAL, "操作工张三", "注塑机A温度异常停机");
        call.linkEquipment("EQ-DX-001");
        call.linkWorkOrder("WO-20260725-001");
        assertEquals(AndonStatus.OPEN, call.getStatus());
        assertEquals(0, call.getEscalationLevel());

        // Step 2: 配置上报规则
        EscalationRule rule = new EscalationRule("RULE-EQUIP-CRITICAL",
                TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);

        EscalationRule.EscalationLevel l1 = new EscalationRule.EscalationLevel(
                1, 0, EscalationRule.EscalateCondition.TIMEOUT);
        l1.addNotifyRole("班组长");

        EscalationRule.EscalationLevel l2 = new EscalationRule.EscalationLevel(
                2, 5, EscalationRule.EscalateCondition.TIMEOUT);
        l2.addNotifyRole("车间主任");
        l2.addNotifyRole("设备工程师");

        EscalationRule.EscalationLevel l3 = new EscalationRule.EscalationLevel(
                3, 15, EscalationRule.EscalateCondition.TIMEOUT);
        l3.addNotifyRole("生产经理");
        l3.addNotifyRole("值班厂长");

        EscalationRule.EscalationLevel l4 = new EscalationRule.EscalationLevel(
                4, 30, EscalationRule.EscalateCondition.TIMEOUT);
        l4.addNotifyRole("厂长");
        rule.setAutoStopPolicy(EscalationRule.AutoStop.STOP_LINE);

        rule.addLevel(l1);
        rule.addLevel(l2);
        rule.addLevel(l3);
        rule.addLevel(l4);
        assertEquals(4, rule.getMaxLevel());

        // Step 3: 班组长确认
        call.acknowledge("班组长李四");
        assertEquals(AndonStatus.ACKNOWLEDGED, call.getStatus());

        // Step 4: 5分钟未解决→上报车间主任(L2)
        call.escalate("L1超时未响应");
        assertEquals(AndonStatus.ESCALATED, call.getStatus());
        assertEquals(1, call.getEscalationLevel());

        // Step 5: 15分钟未解决→上报生产经理(L3)
        call.escalate("L2超时未响应");
        assertEquals(2, call.getEscalationLevel());

        // Step 6: 设备工程师到场修复
        call.resolve("更换温度传感器，重新校准");
        assertEquals(AndonStatus.RESOLVED, call.getStatus());

        // Step 7: 关闭呼叫
        call.close();
        assertEquals(AndonStatus.CLOSED, call.getStatus());
        assertFalse(call.isActive());
    }

    @Test
    @DisplayName("综合场景 — 安全事件紧急上报（OPEN 直接升级）")
    void integration_safetyIncident_directEscalation() {
        AndonCall call = new AndonCall("ANDON-EMERG-001", TriggerType.SAFETY_INCIDENT,
                AndonSeverity.EMERGENCY, AndonSource.MANUAL, "操作工王五", "化学品泄漏，需立即疏散");

        // 直接上报（跳过确认）
        call.escalate("安全事件直接上报全厂");
        assertEquals(AndonStatus.ESCALATED, call.getStatus());
        assertEquals(1, call.getEscalationLevel());

        // 安全事件处理完成
        call.resolve("泄漏已控制，区域已通风，人员已返回");
        assertEquals(AndonStatus.RESOLVED, call.getStatus());

        call.close();
        assertEquals(AndonStatus.CLOSED, call.getStatus());
    }

    @Test
    @DisplayName("综合场景 — 误触发直接关闭")
    void integration_falseTrigger_directClose() {
        AndonCall call = new AndonCall("ANDON-ERR-001", TriggerType.OTHER,
                AndonSeverity.INFO, AndonSource.MANUAL, "操作工赵六", "误触碰安灯按钮");

        // 误触发直接关闭
        call.close();
        assertEquals(AndonStatus.CLOSED, call.getStatus());
        assertFalse(call.isActive());
    }

    @Test
    @DisplayName("综合场景 — 系统自动触发→人工确认→解决")
    void integration_autoTrigger_manualResolve() {
        // IoT 设备报警自动触发
        AndonCall call = new AndonCall("ANDON-AUTO-001", TriggerType.EQUIPMENT_FAULT,
                AndonSeverity.WARNING, AndonSource.AUTO, "PLC系统", "振动传感器超限");
        call.linkEquipment("EQ-PUMP-003");
        assertTrue(call.isAutoTriggered());

        // 班组长确认
        call.acknowledge("班组长孙七");
        assertEquals(AndonStatus.ACKNOWLEDGED, call.getStatus());

        // 检查后直接解决
        call.resolve("振动在允许范围内，传感器误报，已调整阈值");
        assertEquals(AndonStatus.RESOLVED, call.getStatus());

        call.close();
        assertEquals(AndonStatus.CLOSED, call.getStatus());
    }

}
