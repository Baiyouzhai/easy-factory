package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 检验状态
 *
 * @author 苏政
 */
public enum InspectionStatus implements ILifecycle.StatusEnum {

    /** 待检验 */
    PENDING,
    /** 检验中 */
    IN_PROGRESS,
    /** 合格 */
    PASSED,
    /** 不合格 */
    FAILED,
    /** 已关闭 */
    CLOSED;

    @Override
    public Set<InspectionStatus> allowedTransitions() {
        return switch (this) {
            case PENDING     -> Set.of(IN_PROGRESS);
            case IN_PROGRESS -> Set.of(PASSED, FAILED);
            case PASSED      -> Set.of(CLOSED);
            case FAILED      -> Set.of(CLOSED);
            case CLOSED      -> Set.of();
        };
    }

}
