package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 排程状态
 *
 * @author 苏政
 */
public enum ScheduleStatus implements ILifecycle.StatusEnum {

    /** 草稿 */
    DRAFT,
    /** 已优化 */
    OPTIMIZED,
    /** 已下发 */
    DISPATCHED,
    /** 执行中 */
    IN_PROGRESS,
    /** 已完成 */
    COMPLETED,
    /** 已取消 */
    CANCELLED;

    @Override
    public Set<ScheduleStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT      -> Set.of(OPTIMIZED, CANCELLED);
            case OPTIMIZED  -> Set.of(DISPATCHED, CANCELLED);
            case DISPATCHED -> Set.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> Set.of(COMPLETED, CANCELLED);
            case COMPLETED  -> Set.of();
            case CANCELLED  -> Set.of();
        };
    }

}
