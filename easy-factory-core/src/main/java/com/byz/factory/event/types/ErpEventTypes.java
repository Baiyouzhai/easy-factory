package com.byz.factory.event.types;

/**
 * ERP 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>BI</b> — 订阅 {@link #TRANSACTION_CONFIRMED}，更新成本看板</li>
 * </ul>
 *
 * @author 苏政
 */
public final class ErpEventTypes {

    private ErpEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "erp";

    // ── 物料主数据 ──

    /** 物料主数据同步完成 — 载荷: materialCode + sourceSystem */
    public static final String MATERIAL_SYNCED = "erp.material.synced";

    // ── 事务生命周期 ──

    /** 事务已创建（加入发送队列） — 载荷: transactionCode + transactionType */
    public static final String TRANSACTION_CREATED = "erp.transaction.created";

    /** 事务已发送 — 载荷: transactionCode + sentTime */
    public static final String TRANSACTION_SENT = "erp.transaction.sent";

    /** 事务已确认（ERP 过账成功） — 载荷: transactionCode + confirmedTime */
    public static final String TRANSACTION_CONFIRMED = "erp.transaction.confirmed";

    /** 事务发送失败 — 载荷: transactionCode + errorMessage + retryCount */
    public static final String TRANSACTION_FAILED = "erp.transaction.failed";

    /** 事务已取消 — 载荷: transactionCode + cancelReason */
    public static final String TRANSACTION_CANCELLED = "erp.transaction.cancelled";

    // ── 库存 ──

    /** 库存快照已更新 — 载荷: materialCode + plantCode */
    public static final String INVENTORY_UPDATED = "erp.inventory.updated";

}
