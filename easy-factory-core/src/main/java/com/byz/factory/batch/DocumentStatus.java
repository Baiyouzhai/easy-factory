package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 文档审批状态
 *
 * @author 苏政
 */
public enum DocumentStatus implements ILifecycle.StatusEnum {

    /** 草稿 */
    DRAFT,
    /** 审核中 */
    UNDER_REVIEW,
    /** 已批准 */
    APPROVED,
    /** 已拒绝 */
    REJECTED,
    /** 已作废 */
    OBSOLETE;

    @Override
    public Set<DocumentStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT        -> Set.of(UNDER_REVIEW, OBSOLETE);
            case UNDER_REVIEW -> Set.of(APPROVED, REJECTED);
            case APPROVED     -> Set.of(OBSOLETE);
            case REJECTED     -> Set.of(DRAFT);  // 退回修改
            case OBSOLETE     -> Set.of();
        };
    }

}
