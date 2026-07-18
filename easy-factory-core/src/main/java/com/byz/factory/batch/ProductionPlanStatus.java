package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 生产计划状态
 *
 * @author 苏政
 */
public enum ProductionPlanStatus implements ILifecycle.StatusEnum {

    /** 草稿 */
    DRAFT,
    /** 已审批 */
    APPROVED,
    /** 已下达 */
    RELEASED,
    /** 执行中 */
    IN_PROGRESS,
    /** 已完成 */
    COMPLETED,
    /** 已关闭 */
    CLOSED;

    @Override
    public Set<ProductionPlanStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT       -> Set.of(APPROVED);
            case APPROVED    -> Set.of(RELEASED, DRAFT);
            case RELEASED    -> Set.of(IN_PROGRESS);
            case IN_PROGRESS -> Set.of(COMPLETED);
            case COMPLETED   -> Set.of(CLOSED);
            case CLOSED      -> Set.of();
        };
    }

}
