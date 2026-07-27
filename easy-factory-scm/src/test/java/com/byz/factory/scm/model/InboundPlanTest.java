package com.byz.factory.scm.model;

import com.byz.factory.scm.InboundPlanStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InboundPlan 来料计划测试")
class InboundPlanTest {

    // ==================== 构造 ====================

    @Test
    @DisplayName("构造 — code/poNo/supplierCode 正确赋值，初始状态 CREATED")
    void constructor_shouldSetRequiredFields() {
        InboundPlan plan = new InboundPlan("IB-001", "PO-001", "SUP-001");

        assertEquals("IB-001", plan.getCode());
        assertEquals("PO-001", plan.getPoNo());
        assertEquals("SUP-001", plan.getSupplierCode());
        assertEquals(InboundPlanStatus.CREATED, plan.getStatus());
        assertFalse(plan.isNotifyWarehouse());
        assertNotNull(plan.getCreatedAt());
    }

    // ==================== 正常生命周期 ====================

    @Test
    @DisplayName("正常链 — CREATED → NOTIFIED → RECEIVING → COMPLETED")
    void lifecycle_normal_shouldTransitionCorrectly() {
        InboundPlan plan = new InboundPlan("IB-002", "PO-002", "SUP-001");

        plan.notifyWarehouse();
        assertEquals(InboundPlanStatus.NOTIFIED, plan.getStatus());
        assertTrue(plan.isNotifyWarehouse());

        plan.startReceiving();
        assertEquals(InboundPlanStatus.RECEIVING, plan.getStatus());

        plan.complete();
        assertEquals(InboundPlanStatus.COMPLETED, plan.getStatus());
    }

    // ==================== 取消 ====================

    @Test
    @DisplayName("取消 — CREATED → CANCELLED")
    void cancel_fromCreated_shouldWork() {
        InboundPlan plan = new InboundPlan("IB-003", "PO-003", "SUP-001");
        plan.cancel();

        assertEquals(InboundPlanStatus.CANCELLED, plan.getStatus());
    }

    @Test
    @DisplayName("取消 — NOTIFIED → CANCELLED")
    void cancel_fromNotified_shouldWork() {
        InboundPlan plan = new InboundPlan("IB-004", "PO-004", "SUP-001");
        plan.notifyWarehouse();
        plan.cancel();

        assertEquals(InboundPlanStatus.CANCELLED, plan.getStatus());
    }

    @Test
    @DisplayName("RECEIVING 之后不能取消")
    void receiving_cannotBeCancelled() {
        InboundPlan plan = new InboundPlan("IB-005", "PO-005", "SUP-001");
        plan.notifyWarehouse();
        plan.startReceiving();

        assertThrows(IllegalStateException.class, plan::cancel,
                "已开始收货的计划不能取消");
    }

    // ==================== 终态 ====================

    @Test
    @DisplayName("COMPLETED 是终态 — 任何转换都抛异常")
    void completed_isTerminal_shouldThrow() {
        InboundPlan plan = new InboundPlan("IB-006", "PO-006", "SUP-001");
        plan.notifyWarehouse();
        plan.startReceiving();
        plan.complete();

        assertThrows(IllegalStateException.class, plan::cancel);
        assertThrows(IllegalStateException.class, plan::notifyWarehouse);
    }

    @Test
    @DisplayName("CANCELLED 是终态 — 任何转换都抛异常")
    void cancelled_isTerminal_shouldThrow() {
        InboundPlan plan = new InboundPlan("IB-007", "PO-007", "SUP-001");
        plan.cancel();

        assertThrows(IllegalStateException.class, plan::notifyWarehouse);
        assertThrows(IllegalStateException.class, plan::startReceiving);
    }

    // ==================== 非法转换 ====================

    @Test
    @DisplayName("CREATED 不能直接开始收货（跳过通知）")
    void created_cannotStartReceiving() {
        InboundPlan plan = new InboundPlan("IB-008", "PO-008", "SUP-001");

        assertThrows(IllegalStateException.class, plan::startReceiving);
    }

    @Test
    @DisplayName("同状态转换 — 幂等，不抛异常")
    void sameStateTransition_shouldBeIdempotent() {
        InboundPlan plan = new InboundPlan("IB-009", "PO-009", "SUP-001");

        assertDoesNotThrow(() -> plan.transition(InboundPlanStatus.CREATED));
        assertEquals(InboundPlanStatus.CREATED, plan.getStatus());
    }

    // ==================== 扩展字段 ====================

    @Test
    @DisplayName("扩展字段 — expectedDate 可设可读")
    void expectedDate_shouldBeReadWrite() {
        InboundPlan plan = new InboundPlan("IB-010", "PO-010", "SUP-001");
        java.time.LocalDate date = java.time.LocalDate.of(2026, 8, 15);
        plan.setExpectedDate(date);

        assertEquals(date, plan.getExpectedDate());
    }

    // ==================== InboundPlanStatus 枚举 ====================

    @Test
    @DisplayName("InboundPlanStatus — 各状态允许的转换正确")
    void inboundPlanStatus_allowedTransitions_shouldBeCorrect() {
        // CREATED
        assertTrue(InboundPlanStatus.CREATED.allowedTransitions().contains(InboundPlanStatus.NOTIFIED));
        assertTrue(InboundPlanStatus.CREATED.allowedTransitions().contains(InboundPlanStatus.CANCELLED));
        assertEquals(2, InboundPlanStatus.CREATED.allowedTransitions().size());

        // NOTIFIED
        assertTrue(InboundPlanStatus.NOTIFIED.allowedTransitions().contains(InboundPlanStatus.RECEIVING));
        assertTrue(InboundPlanStatus.NOTIFIED.allowedTransitions().contains(InboundPlanStatus.CANCELLED));
        assertEquals(2, InboundPlanStatus.NOTIFIED.allowedTransitions().size());

        // RECEIVING
        assertTrue(InboundPlanStatus.RECEIVING.allowedTransitions().contains(InboundPlanStatus.COMPLETED));
        assertEquals(1, InboundPlanStatus.RECEIVING.allowedTransitions().size());

        // COMPLETED — 终端
        assertTrue(InboundPlanStatus.COMPLETED.allowedTransitions().isEmpty());

        // CANCELLED — 终端
        assertTrue(InboundPlanStatus.CANCELLED.allowedTransitions().isEmpty());
    }

}
