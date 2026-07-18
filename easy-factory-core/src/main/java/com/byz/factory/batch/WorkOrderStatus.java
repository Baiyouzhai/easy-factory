package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 工单生命周期状态
 *
 * @author 苏政
 */
public enum WorkOrderStatus implements ILifecycle.StatusEnum {

    /** 已创建 */
    CREATED,
    /** 已下达 */
    RELEASED,
    /** 进行中 */
    IN_PROGRESS,
    /** 已完成 */
    COMPLETED,
    /** 已关闭 */
    CLOSED,
    /** 已取消 */
    CANCELLED;

    @Override
    public Set<WorkOrderStatus> allowedTransitions() {
        return switch (this) {
            case CREATED     -> Set.of(RELEASED, CANCELLED);
            case RELEASED    -> Set.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> Set.of(COMPLETED, CANCELLED);
            case COMPLETED   -> Set.of(CLOSED);
            case CLOSED      -> Set.of();
            case CANCELLED   -> Set.of();
        };
    }

}
