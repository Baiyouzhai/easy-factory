package com.byz.factory.dms;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 文档审批状态 — DMS 内部枚举（不暴露到 core）。
 *
 * @author 苏政
 */
public enum DocumentStatus implements ILifecycle.StatusEnum {

    /** 草稿 */
    DRAFT,
    /** 审核中 */
    UNDER_REVIEW,
    /** 已批准（待生效） */
    APPROVED,
    /** 已生效 */
    EFFECTIVE,
    /** 已拒绝 */
    REJECTED,
    /** 已作废 */
    OBSOLETE;

    @Override
    public Set<DocumentStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT        -> Set.of(UNDER_REVIEW, OBSOLETE);
            case UNDER_REVIEW -> Set.of(APPROVED, REJECTED);
            case APPROVED     -> Set.of(EFFECTIVE, OBSOLETE);
            case EFFECTIVE    -> Set.of(OBSOLETE);
            case REJECTED     -> Set.of(DRAFT);
            case OBSOLETE     -> Set.of();
        };
    }

}
