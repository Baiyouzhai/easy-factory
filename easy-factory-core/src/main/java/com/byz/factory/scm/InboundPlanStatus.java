package com.byz.factory.scm;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 来料计划状态 — SCM 来料计划生命周期。
 * <p>
 * CREATED → NOTIFIED → RECEIVING → COMPLETED<br>
 * CREATED → CANCELLED / NOTIFIED → CANCELLED
 *
 * <pre>
 * CREATED ──→ NOTIFIED ──→ RECEIVING ──→ COMPLETED
 *    │            │
 *    └────────────┴──→ CANCELLED
 * </pre>
 *
 * @author 苏政
 */
public enum InboundPlanStatus implements ILifecycle.StatusEnum {

    /** 已创建 — 来料计划生成，待通知仓库 */
    CREATED,
    /** 已通知 — 已通知仓库准备收货 */
    NOTIFIED,
    /** 收货中 — 仓库正在收货 */
    RECEIVING,
    /** 已完成 — 全部入库完毕 */
    COMPLETED,
    /** 已取消 — 计划取消 */
    CANCELLED;

    @Override
    public Set<InboundPlanStatus> allowedTransitions() {
        return switch (this) {
            case CREATED    -> Set.of(NOTIFIED, CANCELLED);
            case NOTIFIED   -> Set.of(RECEIVING, CANCELLED);
            case RECEIVING  -> Set.of(COMPLETED);
            case COMPLETED  -> Set.of();
            case CANCELLED  -> Set.of();
        };
    }

}
