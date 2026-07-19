package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 拣料任务状态 — WMS 拣料任务生命周期。
 * <p>
 * 正向链：PENDING → IN_PROGRESS → PICKED → DELIVERED<br>
 * 取消链：PENDING → CANCELLED / IN_PROGRESS → CANCELLED
 *
 * <pre>
 * PENDING ──→ IN_PROGRESS ──→ PICKED ──→ DELIVERED
 *    │             │
 *    └─────────────┴──→ CANCELLED
 * </pre>
 *
 * @author 苏政
 */
public enum PickingTaskStatus implements ILifecycle.StatusEnum {

    /** 待拣料 */
    PENDING,
    /** 拣料中 */
    IN_PROGRESS,
    /** 已拣完 */
    PICKED,
    /** 已送达（线边仓） */
    DELIVERED,
    /** 已取消 */
    CANCELLED;

    @Override
    public Set<PickingTaskStatus> allowedTransitions() {
        return switch (this) {
            case PENDING     -> Set.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> Set.of(PICKED, CANCELLED);
            case PICKED      -> Set.of(DELIVERED);
            case DELIVERED   -> Set.of();
            case CANCELLED   -> Set.of();
        };
    }

}
