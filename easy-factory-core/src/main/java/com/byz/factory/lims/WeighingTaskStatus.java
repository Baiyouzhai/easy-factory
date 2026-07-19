package com.byz.factory.lims;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 称量任务生命周期状态。
 * <p>
 * 线性流程：PENDING → WEIGHING → VERIFIED → COMPLETE
 *
 * <pre>
 * PENDING ──→ WEIGHING ──→ VERIFIED ──→ COMPLETE
 * </pre>
 *
 * @author 苏政
 */
public enum WeighingTaskStatus implements ILifecycle.StatusEnum {

    /** 待称量 — 任务已创建，等待操作 */
    PENDING,
    /** 称量中 — 正在进行称量操作 */
    WEIGHING,
    /** 已复核 — 称量完成，复核人已确认 */
    VERIFIED,
    /** 已完成 — 全部称量工作结束 */
    COMPLETE;

    @Override
    public Set<WeighingTaskStatus> allowedTransitions() {
        return switch (this) {
            case PENDING   -> Set.of(WEIGHING);
            case WEIGHING  -> Set.of(VERIFIED);
            case VERIFIED  -> Set.of(COMPLETE);
            case COMPLETE  -> Set.of();
        };
    }

}
