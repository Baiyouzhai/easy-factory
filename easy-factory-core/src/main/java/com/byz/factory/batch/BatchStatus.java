package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 批次生命周期状态
 *
 * @author 苏政
 */
public enum BatchStatus implements ILifecycle.StatusEnum {

    /** 已创建（工单下达，但未开工） */
    CREATED,
    /** 生产中（至少一个工序已开始） */
    IN_PROGRESS,
    /** 生产完成（所有工序执行完毕） */
    COMPLETED,
    /** 审核中（批记录审核） */
    UNDER_REVIEW,
    /** 已批准（批记录已签批，待放行） */
    APPROVED,
    /** 已放行（质量受权人批准放行，可销售/使用） */
    RELEASED,
    /** 已拒绝（批记录审核不通过，需返工或报废） */
    REJECTED,
    /** 已归档（批记录归档，不可修改） */
    ARCHIVED,
    /** 已取消（工单取消/作废） */
    CANCELLED;

    @Override
    @SuppressWarnings("unchecked")
    public Set<BatchStatus> allowedTransitions() {
        return switch (this) {
            case CREATED       -> Set.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS   -> Set.of(COMPLETED, CANCELLED);
            case COMPLETED     -> Set.of(UNDER_REVIEW);
            case UNDER_REVIEW  -> Set.of(APPROVED, REJECTED);
            case APPROVED      -> Set.of(RELEASED);
            case RELEASED      -> Set.of(ARCHIVED);
            case REJECTED      -> Set.of(CANCELLED, IN_PROGRESS);  // 可以返工
            case ARCHIVED      -> Set.of();
            case CANCELLED     -> Set.of();
        };
    }

}
