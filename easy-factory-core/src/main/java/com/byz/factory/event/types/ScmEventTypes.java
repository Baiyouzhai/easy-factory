package com.byz.factory.event.types;

/**
 * SCM 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>WMS</b> — 订阅 {@link #PO_CREATED} / {@link #PO_SENT} / {@link #INBOUND_PLAN_CREATED}，
 *       生成收货单、预置储位策略</li>
 *   <li><b>ERP</b> — 订阅 {@link #PO_COMPLETED} / {@link #INBOUND_COMPLETED}，
 *       同步采购成本到财务应付</li>
 *   <li><b>QMS</b> — 订阅 {@link #SUPPLIER_QUALIFIED} / {@link #SUPPLIER_DISQUALIFIED}，
 *       更新来料检验（IQC）策略</li>
 *   <li><b>BI</b> — 订阅 {@link #PO_CREATED} / {@link #PO_COMPLETED} / {@link #SUPPLIER_BLACKLISTED}，
 *       更新采购看板、供应商绩效统计</li>
 * </ul>
 *
 * @author 苏政
 */
public final class ScmEventTypes {

    private ScmEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "scm";

    // ── 供应商生命周期 ──

    /** 供应商已注册 — 载荷: supplierCode + name + category */
    public static final String SUPPLIER_REGISTERED = "scm.supplier.registered";

    /** 供应商资质审核通过 — 载荷: supplierCode */
    public static final String SUPPLIER_QUALIFIED = "scm.supplier.qualified";

    /** 供应商资质被取消 — 载荷: supplierCode */
    public static final String SUPPLIER_DISQUALIFIED = "scm.supplier.disqualified";

    /** 供应商暂停合作 — 载荷: supplierCode */
    public static final String SUPPLIER_DEACTIVATED = "scm.supplier.deactivated";

    /** 供应商恢复合作 — 载荷: supplierCode */
    public static final String SUPPLIER_REACTIVATED = "scm.supplier.reactivated";

    /** 供应商已拉黑 — 载荷: supplierCode */
    public static final String SUPPLIER_BLACKLISTED = "scm.supplier.blacklisted";

    // ── 采购订单生命周期 ──

    /** 采购订单已创建 — 载荷: poNo + supplierCode + itemCount */
    public static final String PO_CREATED = "scm.po.created";

    /** 采购订单已审批 — 载荷: poNo + approvedBy */
    public static final String PO_APPROVED = "scm.po.approved";

    /** 采购订单已发送供应商 — 载荷: poNo + supplierCode（WMS 订阅此事件生成收货单） */
    public static final String PO_SENT = "scm.po.sent";

    /** 采购订单开始收货 — 载荷: poNo + materialCode + receivedQty */
    public static final String PO_RECEIVING = "scm.po.receiving";

    /** 采购订单已完成（全部收货） — 载荷: poNo + totalAmount（ERP 订阅同步应付） */
    public static final String PO_COMPLETED = "scm.po.completed";

    /** 采购订单已取消 — 载荷: poNo + cancelReason */
    public static final String PO_CANCELLED = "scm.po.cancelled";

    // ── 来料计划 ──

    /** 来料计划已创建 — 载荷: inboundPlanCode + poNo（WMS 订阅此事件准备收货） */
    public static final String INBOUND_PLAN_CREATED = "scm.inbound.created";

    /** 来料计划已完成入库 — 载荷: inboundPlanCode + completedTime（ERP 订阅更新库存） */
    public static final String INBOUND_COMPLETED = "scm.inbound.completed";

}
