package com.byz.factory.scm;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 采购订单状态 — SCM 采购订单生命周期。
 * <p>
 * 正向链：DRAFT → APPROVED → SENT → RECEIVING → COMPLETED<br>
 * 取消链：DRAFT → CANCELLED / APPROVED → CANCELLED
 *
 * <pre>
 * DRAFT ──→ APPROVED ──→ SENT ──→ RECEIVING ──→ COMPLETED
 *   │          │
 *   └──────────┴──→ CANCELLED
 * </pre>
 *
 * @author 苏政
 */
public enum PurchaseOrderStatus implements ILifecycle.StatusEnum {

    /** 草稿 — 采购申请创建，待审批 */
    DRAFT,
    /** 已审批 — 审批通过，待发送供应商 */
    APPROVED,
    /** 已发送 — 已发送给供应商，等待交货 */
    SENT,
    /** 收货中 — 部分到货，正在入库 */
    RECEIVING,
    /** 已完成 — 全部收货完成 */
    COMPLETED,
    /** 已取消 — 订单取消 */
    CANCELLED;

    @Override
    public Set<PurchaseOrderStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT      -> Set.of(APPROVED, CANCELLED);
            case APPROVED   -> Set.of(SENT, CANCELLED);
            case SENT       -> Set.of(RECEIVING, COMPLETED);
            case RECEIVING  -> Set.of(COMPLETED);
            case COMPLETED  -> Set.of();
            case CANCELLED  -> Set.of();
        };
    }

}
