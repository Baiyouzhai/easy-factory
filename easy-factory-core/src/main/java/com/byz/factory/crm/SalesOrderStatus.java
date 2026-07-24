package com.byz.factory.crm;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 销售订单状态 — CRM 销售订单生命周期。
 * <p>
 * 正向链：DRAFT → CONFIRMED → IN_PRODUCTION → SHIPPED → COMPLETED<br>
 * 取消链：DRAFT → CANCELLED / CONFIRMED → CANCELLED
 *
 * <pre>
 * DRAFT ──→ CONFIRMED ──→ IN_PRODUCTION ──→ SHIPPED ──→ COMPLETED
 *   │           │
 *   └───────────┴──→ CANCELLED
 * </pre>
 *
 * @author 苏政
 */
public enum SalesOrderStatus implements ILifecycle.StatusEnum {

    /** 草稿 — 订单已录入，待确认 */
    DRAFT,
    /** 已确认 — 客户确认，等待排产 */
    CONFIRMED,
    /** 生产中 — MPS 已接收，正在制造 */
    IN_PRODUCTION,
    /** 已发货 — 成品已出库，运输中 */
    SHIPPED,
    /** 已完成 — 客户签收，订单结清 */
    COMPLETED,
    /** 已取消 — 订单取消 */
    CANCELLED;

    @Override
    public Set<SalesOrderStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT         -> Set.of(CONFIRMED, CANCELLED);
            case CONFIRMED     -> Set.of(IN_PRODUCTION, CANCELLED);
            case IN_PRODUCTION -> Set.of(SHIPPED);
            case SHIPPED       -> Set.of(COMPLETED);
            case COMPLETED     -> Set.of();
            case CANCELLED     -> Set.of();
        };
    }

}
