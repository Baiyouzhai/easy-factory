package com.byz.factory.factory;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 蓝图状态 — PLM 工艺路线的生命周期。
 * <p>
 * 蓝图从草稿开始，经评审、批准后发布到 MES，最终可被废弃。
 *
 * <pre>
 * DRAFT → UNDER_REVIEW → APPROVED → RELEASED → OBSOLETED
 *                                      ↑              │
 *                                      └──────────────┘
 * </pre>
 *
 * @author 苏政
 */
public enum BlueprintStatus implements ILifecycle.StatusEnum {

    /** 草稿 — 工艺设计中 */
    DRAFT,
    /** 评审中 — DMS 审批流程 */
    UNDER_REVIEW,
    /** 已批准 — 待发布到 MES */
    APPROVED,
    /** 已发布 — MES 可引用执行 */
    RELEASED,
    /** 已废弃 — 不再使用 */
    OBSOLETED;

    @Override
    public Set<BlueprintStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT         -> Set.of(UNDER_REVIEW, OBSOLETED);
            case UNDER_REVIEW  -> Set.of(APPROVED, DRAFT);
            case APPROVED      -> Set.of(RELEASED, DRAFT);
            case RELEASED      -> Set.of(OBSOLETED);
            case OBSOLETED     -> Set.of();
        };
    }

}
