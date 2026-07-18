package com.byz.factory.erp.model;

import com.byz.factory.batch.TransactionStatus;
import com.byz.factory.batch.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction 事务回传记录测试")
class TransactionTest {

    // ==================== 构造 ====================

    @Test
    @DisplayName("构造 — code/transactionType/materialCode/quantity 正确赋值，初始状态 PENDING")
    void constructor_shouldSetRequiredFields() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));

        assertEquals("TX-001", tx.getCode());
        assertEquals(TransactionType.GOODS_ISSUE, tx.getTransactionType());
        assertEquals("MAT-001", tx.getMaterialCode());
        assertEquals(new BigDecimal("100.00"), tx.getQuantity());
        assertEquals(TransactionStatus.PENDING, tx.getStatus());
    }

    @Test
    @DisplayName("构造 — retryCount 初始为 0，postingDate 为当天")
    void constructor_shouldSetDefaults() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_RECEIPT,
                "MAT-001", new BigDecimal("50.00"));

        assertEquals(0, tx.getRetryCount());
        assertNotNull(tx.getPostingDate());
    }

    // ==================== 正常生命周期 ====================

    @Test
    @DisplayName("正常链 — PENDING → SENT → CONFIRMED")
    void lifecycle_normal_shouldTransitionCorrectly() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));

        tx.markSent();
        assertEquals(TransactionStatus.SENT, tx.getStatus());

        tx.markConfirmed();
        assertEquals(TransactionStatus.CONFIRMED, tx.getStatus());
    }

    @Test
    @DisplayName("markSent — PENDING → SENT，同状态重复调用不报错（no-op）")
    void markSent_shouldTransitionFromPending() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));

        tx.markSent();
        assertEquals(TransactionStatus.SENT, tx.getStatus());

        // 同状态 transition 是 no-op，不报错（ILifecycle 规范）
        tx.markSent();
        assertEquals(TransactionStatus.SENT, tx.getStatus());
    }

    @Test
    @DisplayName("markConfirmed — SENT → CONFIRMED")
    void markConfirmed_shouldTransitionFromSent() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        tx.markSent();

        tx.markConfirmed();
        assertEquals(TransactionStatus.CONFIRMED, tx.getStatus());
    }

    @Test
    @DisplayName("CONFIRMED 是终态 — 任何转换都抛异常")
    void confirmed_isTerminal_shouldThrow() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        tx.markSent();
        tx.markConfirmed();

        assertThrows(IllegalStateException.class, () -> tx.markFailed("error"));
        assertThrows(IllegalStateException.class, tx::retry);
        assertThrows(IllegalStateException.class, tx::cancel);
    }

    // ==================== 失败重试 ====================

    @Test
    @DisplayName("失败链 — PENDING → SENT → FAILED → PENDING（重试）")
    void lifecycle_failureAndRetry_shouldWork() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        tx.markSent();
        tx.markFailed("ERP 连接超时");

        assertEquals(TransactionStatus.FAILED, tx.getStatus());
        assertEquals(1, tx.getRetryCount());
        assertEquals("ERP 连接超时", tx.getErrorMessage());

        // 重试：FAILED → PENDING
        tx.retry();
        assertEquals(TransactionStatus.PENDING, tx.getStatus());
        assertNull(tx.getErrorMessage(), "重试时应清空错误信息");
    }

    @Test
    @DisplayName("多次失败 — retryCount 递增")
    void multipleFailures_shouldIncrementRetryCount() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));

        for (int i = 1; i <= 3; i++) {
            tx.markSent();
            tx.markFailed("失败 #" + i);
            tx.retry();
        }

        assertEquals(3, tx.getRetryCount());
        assertEquals(TransactionStatus.PENDING, tx.getStatus());
    }

    @Test
    @DisplayName("FAILED 只能转到 PENDING")
    void failed_onlyToPending() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        tx.markSent();
        tx.markFailed("error");

        // FAILED → CANCELLED 不合法
        assertThrows(IllegalStateException.class, tx::cancel);
        // FAILED → SENT 不合法
        assertThrows(IllegalStateException.class, () -> tx.markSent());
    }

    // ==================== 取消 ====================

    @Test
    @DisplayName("取消 — PENDING → CANCELLED")
    void cancel_fromPending_shouldWork() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));

        tx.cancel();
        assertEquals(TransactionStatus.CANCELLED, tx.getStatus());
    }

    @Test
    @DisplayName("CANCELLED 是终态 — 任何转换都抛异常")
    void cancelled_isTerminal_shouldThrow() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        tx.cancel();

        assertThrows(IllegalStateException.class, tx::retry);
        assertThrows(IllegalStateException.class, () -> tx.markSent());
    }

    @Test
    @DisplayName("SENT 状态不能取消 — 只能进入 CONFIRMED 或 FAILED")
    void sent_cannotBeCancelled() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        tx.markSent();

        assertThrows(IllegalStateException.class, tx::cancel,
                "SENT 状态不能直接取消");
    }

    // ==================== 所有事务类型 ====================

    @Test
    @DisplayName("GOODS_RECEIPT 类型 — 正确创建")
    void goodsReceipt_shouldCreateCorrectly() {
        Transaction tx = new Transaction("TX-002", TransactionType.GOODS_RECEIPT,
                "MAT-002", new BigDecimal("200.00"));

        assertEquals(TransactionType.GOODS_RECEIPT, tx.getTransactionType());
        assertEquals(TransactionStatus.PENDING, tx.getStatus());
    }

    @Test
    @DisplayName("TRANSFER 类型 — 正确创建")
    void transfer_shouldCreateCorrectly() {
        Transaction tx = new Transaction("TX-003", TransactionType.TRANSFER,
                "MAT-003", new BigDecimal("50.00"));

        assertEquals(TransactionType.TRANSFER, tx.getTransactionType());
        assertEquals(TransactionStatus.PENDING, tx.getStatus());
    }

    // ==================== 附加字段 ====================

    @Test
    @DisplayName("附加字段 — batchNo/movementType/referenceDoc/unit 可设可读")
    void optionalFields_shouldBeReadWrite() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        tx.setBatchNo("BATCH-2026-001");
        tx.setMovementType("261");
        tx.setReferenceDoc("WO-2026-001");
        tx.setUnit("KG");

        assertEquals("BATCH-2026-001", tx.getBatchNo());
        assertEquals("261", tx.getMovementType());
        assertEquals("WO-2026-001", tx.getReferenceDoc());
        assertEquals("KG", tx.getUnit());
    }

}
