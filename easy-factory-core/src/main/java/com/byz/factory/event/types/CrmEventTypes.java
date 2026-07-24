package com.byz.factory.event.types;

/**
 * CRM 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>MPS</b> — 订阅 {@link #ORDER_CONFIRMED}，
 *       销售订单确认后注册需求来源，触发生成生产计划</li>
 *   <li><b>QMS</b> — 订阅 {@link #COMPLAINT_RECEIVED}，
 *       客户投诉创建后生成偏差/CAPA</li>
 *   <li><b>BI</b> — 订阅 {@link #ORDER_CONFIRMED} / {@link #ORDER_COMPLETED} / {@link #ORDER_CANCELLED}，
 *       销售看板与交付统计</li>
 *   <li><b>Andon</b> — 订阅 {@link #COMPLAINT_RECEIVED}，
 *       重大投诉触发安灯呼叫</li>
 *   <li><b>WMS</b> — 订阅 {@link #ORDER_SHIPPED}，
 *       成品出库发货通知</li>
 * </ul>
 *
 * @author 苏政
 */
public final class CrmEventTypes {

    private CrmEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "crm";

    // ── 客户管理 ──

    /** 客户已注册 — 载荷: customerCode + name + industry */
    public static final String CUSTOMER_REGISTERED = "crm.customer.registered";

    /** 客户 GMP 审计状态已更新 — 载荷: customerCode + gmpAuditStatus + gmpAuditDate */
    public static final String CUSTOMER_AUDIT_UPDATED = "crm.customer.audit_updated";

    // ── 销售订单 ──

    /** 销售订单已创建 — 载荷: orderNo + customerCode + productCode + quantity */
    public static final String ORDER_CREATED = "crm.order.created";

    /** 销售订单已确认 — 载荷: orderNo + customerCode + confirmedDate */
    public static final String ORDER_CONFIRMED = "crm.order.confirmed";

    /** 销售订单已发货 — 载荷: orderNo + shippedDate */
    public static final String ORDER_SHIPPED = "crm.order.shipped";

    /** 销售订单已完成 — 载荷: orderNo + completedDate */
    public static final String ORDER_COMPLETED = "crm.order.completed";

    /** 销售订单已取消 — 载荷: orderNo + cancelReason */
    public static final String ORDER_CANCELLED = "crm.order.cancelled";

    // ── 客户投诉 ──

    /** 客户投诉已接收 — 载荷: complaintNo + customerCode + orderNo + batchNo + type + description */
    public static final String COMPLAINT_RECEIVED = "crm.complaint.received";

    /** 客户投诉已解决 — 载荷: complaintNo + resolution */
    public static final String COMPLAINT_RESOLVED = "crm.complaint.resolved";

    /** 客户投诉已关闭 — 载荷: complaintNo + closedDate */
    public static final String COMPLAINT_CLOSED = "crm.complaint.closed";

}
