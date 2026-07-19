package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 工序记录生命周期状态 — 工序在工单中的执行状态。
 * <p>
 * 与 {@link WorkOrderStatus} 互补：工单状态是工单级视图，ProcessStatus 是工序级视图。
 * QMS 检验触发中断时，工序进入 INTERRUPTED 状态等待恢复。
 *
 * <h3>状态流转</h3>
 * <pre>
 * PENDING ──→ IN_PROGRESS ──→ COMPLETED
 *                 │
 *                 ├──→ INTERRUPTED ──→ IN_PROGRESS (恢复)
 *                 │         │
 *                 │         └──→ CANCELLED
 *                 │
 * PENDING ──→ SKIPPED
 * </pre>
 *
 * @author 苏政
 * @see WorkOrderStatus
 */
public enum ProcessStatus implements ILifecycle.StatusEnum {

    /** 待执行 — 工单已下达但该工序尚未开始 */
    PENDING,
    /** 执行中 — 工序正在执行动作链 */
    IN_PROGRESS,
    /** 已中断 — QMS 检验 / 设备故障 / 人工暂停触发中断 */
    INTERRUPTED,
    /** 已完成 — 工序所有动作执行完毕 */
    COMPLETED,
    /** 已跳过 — 工艺变更或条件不满足时跳过该工序 */
    SKIPPED,
    /** 已取消 — 工序被取消（工单取消联动或单独取消） */
    CANCELLED;

    @Override
    public Set<ProcessStatus> allowedTransitions() {
        return switch (this) {
            case PENDING      -> Set.of(IN_PROGRESS, SKIPPED, CANCELLED);
            case IN_PROGRESS  -> Set.of(COMPLETED, INTERRUPTED, CANCELLED);
            case INTERRUPTED  -> Set.of(IN_PROGRESS, CANCELLED);
            case COMPLETED    -> Set.of();
            case SKIPPED      -> Set.of();
            case CANCELLED    -> Set.of();
        };
    }

}
