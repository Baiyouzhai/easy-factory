package com.byz.factory.scm.model;

import com.byz.factory.batch.PurchaseOrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PurchaseOrder 采购订单测试")
class PurchaseOrderTest {

    // ==================== 构造 ====================

    @Test
    @DisplayName("构造 — poNo/supplierCode 正确赋值，初始状态 DRAFT，items 为空")
    void constructor_shouldSetRequiredFields() {
        PurchaseOrder po = new PurchaseOrder("PO-2026-001", "SUP-001");

        assertEquals("PO-2026-001", po.getPoNo());
        assertEquals("SUP-001", po.getSupplierCode());
        assertEquals(PurchaseOrderStatus.DRAFT, po.getStatus());
        assertNotNull(po.getItems());
        assertTrue(po.getItems().isEmpty());
        assertNotNull(po.getCreatedAt());
    }

    // ==================== 正常生命周期 ====================

    @Test
    @DisplayName("正常链 — DRAFT → APPROVED → SENT → RECEIVING → COMPLETED")
    void lifecycle_normal_shouldTransitionCorrectly() {
        PurchaseOrder po = new PurchaseOrder("PO-001", "SUP-001");

        po.approve("张三");
        assertEquals(PurchaseOrderStatus.APPROVED, po.getStatus());
        assertEquals("张三", po.getApprovedBy());

        po.send();
        assertEquals(PurchaseOrderStatus.SENT, po.getStatus());

        po.startReceiving();
        assertEquals(PurchaseOrderStatus.RECEIVING, po.getStatus());

        po.complete();
        assertEquals(PurchaseOrderStatus.COMPLETED, po.getStatus());
    }

    @Test
    @DisplayName("快捷完成 — SENT → COMPLETED（跳过 RECEIVING，如一次到齐）")
    void lifecycle_skipReceiving_shouldWork() {
        PurchaseOrder po = new PurchaseOrder("PO-002", "SUP-001");
        po.approve("李四");
        po.send();

        // 全部一次性到货，直接从 SENT 到 COMPLETED
        po.complete();
        assertEquals(PurchaseOrderStatus.COMPLETED, po.getStatus());
    }

    // ==================== 取消 ====================

    @Test
    @DisplayName("取消 — DRAFT → CANCELLED")
    void cancel_fromDraft_shouldWork() {
        PurchaseOrder po = new PurchaseOrder("PO-003", "SUP-001");
        po.cancel();

        assertEquals(PurchaseOrderStatus.CANCELLED, po.getStatus());
    }

    @Test
    @DisplayName("取消 — APPROVED → CANCELLED")
    void cancel_fromApproved_shouldWork() {
        PurchaseOrder po = new PurchaseOrder("PO-004", "SUP-001");
        po.approve("王五");
        po.cancel();

        assertEquals(PurchaseOrderStatus.CANCELLED, po.getStatus());
    }

    @Test
    @DisplayName("SENT 之后不能取消")
    void sent_cannotBeCancelled() {
        PurchaseOrder po = new PurchaseOrder("PO-005", "SUP-001");
        po.approve("赵六");
        po.send();

        assertThrows(IllegalStateException.class, po::cancel,
                "已发送给供应商的订单不能取消");
    }

    // ==================== 终态 ====================

    @Test
    @DisplayName("COMPLETED 是终态 — 任何转换都抛异常")
    void completed_isTerminal_shouldThrow() {
        PurchaseOrder po = new PurchaseOrder("PO-006", "SUP-001");
        po.approve("张三");
        po.send();
        po.complete();

        assertThrows(IllegalStateException.class, po::cancel);
        assertThrows(IllegalStateException.class, () -> po.approve("李四"));
        assertThrows(IllegalStateException.class, po::send);
    }

    @Test
    @DisplayName("CANCELLED 是终态 — 任何转换都抛异常")
    void cancelled_isTerminal_shouldThrow() {
        PurchaseOrder po = new PurchaseOrder("PO-007", "SUP-001");
        po.cancel();

        assertThrows(IllegalStateException.class, () -> po.approve("张三"));
        assertThrows(IllegalStateException.class, po::send);
    }

    // ==================== 非法转换 ====================

    @Test
    @DisplayName("DRAFT 不能直接发送（跳过审批）")
    void draft_cannotSend() {
        PurchaseOrder po = new PurchaseOrder("PO-008", "SUP-001");

        assertThrows(IllegalStateException.class, po::send);
    }

    @Test
    @DisplayName("APPROVED 不能直接完成")
    void approved_cannotComplete() {
        PurchaseOrder po = new PurchaseOrder("PO-009", "SUP-001");
        po.approve("张三");

        assertThrows(IllegalStateException.class, po::complete);
    }

    @Test
    @DisplayName("同状态转换 — 幂等，不抛异常")
    void sameStateTransition_shouldBeIdempotent() {
        PurchaseOrder po = new PurchaseOrder("PO-010", "SUP-001");

        assertDoesNotThrow(() -> po.transition(PurchaseOrderStatus.DRAFT));
        assertEquals(PurchaseOrderStatus.DRAFT, po.getStatus());
    }

    // ==================== 采购明细 ====================

    @Test
    @DisplayName("添加明细 — addItem(PurchaseOrderItem)")
    void addItem_byObject_shouldAddToItems() {
        PurchaseOrder po = new PurchaseOrder("PO-011", "SUP-001");
        PurchaseOrderItem item = new PurchaseOrderItem("MAT-001", new BigDecimal("100"));
        item.setUnitPrice(new BigDecimal("25.50"));
        po.addItem(item);

        assertEquals(1, po.getItems().size());
        assertEquals("MAT-001", po.getItems().get(0).getMaterialCode());
    }

    @Test
    @DisplayName("添加明细 — addItem(String, BigDecimal) 便捷方法")
    void addItem_byConvenience_shouldCreateAndAdd() {
        PurchaseOrder po = new PurchaseOrder("PO-012", "SUP-001");
        po.addItem("MAT-002", new BigDecimal("200"));

        assertEquals(1, po.getItems().size());
        assertEquals("MAT-002", po.getItems().get(0).getMaterialCode());
        assertEquals(new BigDecimal("200"), po.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("多条明细行 — 可添加多行")
    void multipleItems_shouldBeSupported() {
        PurchaseOrder po = new PurchaseOrder("PO-013", "SUP-001");
        po.addItem("MAT-001", new BigDecimal("100"));
        po.addItem("MAT-002", new BigDecimal("200"));
        po.addItem("MAT-003", new BigDecimal("50"));

        assertEquals(3, po.getItems().size());
    }

    @Test
    @DisplayName("采购总金额 — 自动计算")
    void totalAmount_shouldCalculateCorrectly() {
        PurchaseOrder po = new PurchaseOrder("PO-014", "SUP-001");
        PurchaseOrderItem item1 = new PurchaseOrderItem("MAT-001", new BigDecimal("100"));
        item1.setUnitPrice(new BigDecimal("25.50"));
        PurchaseOrderItem item2 = new PurchaseOrderItem("MAT-002", new BigDecimal("200"));
        item2.setUnitPrice(new BigDecimal("10.00"));
        po.addItem(item1);
        po.addItem(item2);

        // 100*25.50 + 200*10.00 = 2550 + 2000 = 4550
        assertEquals(new BigDecimal("4550.00"), po.totalAmount());
    }

    @Test
    @DisplayName("单价为 null 时 — totalAmount 按零计算")
    void totalAmount_withNullPrice_shouldTreatAsZero() {
        PurchaseOrder po = new PurchaseOrder("PO-015", "SUP-001");
        po.addItem("MAT-001", new BigDecimal("100"));

        assertEquals(BigDecimal.ZERO, po.totalAmount());
    }

    // ==================== 收货 ====================

    @Test
    @DisplayName("PurchaseOrderItem 收货 — receivedQty 累加")
    void item_receive_shouldAccumulate() {
        PurchaseOrderItem item = new PurchaseOrderItem("MAT-001", new BigDecimal("100"));

        item.receive(new BigDecimal("30"));
        assertEquals(new BigDecimal("30"), item.getReceivedQty());
        assertEquals(new BigDecimal("70"), item.remaining());

        item.receive(new BigDecimal("70"));
        assertEquals(new BigDecimal("100"), item.getReceivedQty());
        assertEquals(BigDecimal.ZERO, item.remaining());
    }

    @Test
    @DisplayName("PurchaseOrderItem — isFullyReceived 判断")
    void item_isFullyReceived_shouldCheckCorrectly() {
        PurchaseOrderItem item = new PurchaseOrderItem("MAT-001", new BigDecimal("100"));

        assertFalse(item.isFullyReceived());

        item.receive(new BigDecimal("100"));
        assertTrue(item.isFullyReceived());
    }

    @Test
    @DisplayName("PurchaseOrder — isFullyReceived 所有行都收齐才为 true")
    void po_isFullyReceived_shouldCheckAllItems() {
        PurchaseOrder po = new PurchaseOrder("PO-016", "SUP-001");
        PurchaseOrderItem item1 = po.addItem("MAT-001", new BigDecimal("100"));
        PurchaseOrderItem item2 = po.addItem("MAT-002", new BigDecimal("50"));

        assertFalse(po.isFullyReceived());

        item1.receive(new BigDecimal("100"));
        assertFalse(po.isFullyReceived());

        item2.receive(new BigDecimal("50"));
        assertTrue(po.isFullyReceived());
    }

    // ==================== 扩展字段 ====================

    @Test
    @DisplayName("扩展字段 — expectedDelivery 可设可读")
    void expectedDelivery_shouldBeReadWrite() {
        PurchaseOrder po = new PurchaseOrder("PO-017", "SUP-001");
        LocalDate delivery = LocalDate.of(2026, 8, 15);
        po.setExpectedDelivery(delivery);

        assertEquals(delivery, po.getExpectedDelivery());
    }

    // ==================== PurchaseOrderStatus 枚举 ====================

    @Test
    @DisplayName("PurchaseOrderStatus — 各状态允许的转换正确")
    void purchaseOrderStatus_allowedTransitions_shouldBeCorrect() {
        // DRAFT
        assertTrue(PurchaseOrderStatus.DRAFT.allowedTransitions().contains(PurchaseOrderStatus.APPROVED));
        assertTrue(PurchaseOrderStatus.DRAFT.allowedTransitions().contains(PurchaseOrderStatus.CANCELLED));
        assertEquals(2, PurchaseOrderStatus.DRAFT.allowedTransitions().size());

        // APPROVED
        assertTrue(PurchaseOrderStatus.APPROVED.allowedTransitions().contains(PurchaseOrderStatus.SENT));
        assertTrue(PurchaseOrderStatus.APPROVED.allowedTransitions().contains(PurchaseOrderStatus.CANCELLED));
        assertEquals(2, PurchaseOrderStatus.APPROVED.allowedTransitions().size());

        // SENT
        assertTrue(PurchaseOrderStatus.SENT.allowedTransitions().contains(PurchaseOrderStatus.RECEIVING));
        assertTrue(PurchaseOrderStatus.SENT.allowedTransitions().contains(PurchaseOrderStatus.COMPLETED));
        assertEquals(2, PurchaseOrderStatus.SENT.allowedTransitions().size());

        // RECEIVING
        assertTrue(PurchaseOrderStatus.RECEIVING.allowedTransitions().contains(PurchaseOrderStatus.COMPLETED));
        assertEquals(1, PurchaseOrderStatus.RECEIVING.allowedTransitions().size());

        // COMPLETED — 终端
        assertTrue(PurchaseOrderStatus.COMPLETED.allowedTransitions().isEmpty());

        // CANCELLED — 终端
        assertTrue(PurchaseOrderStatus.CANCELLED.allowedTransitions().isEmpty());
    }

}
