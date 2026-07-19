package com.byz.factory.lims;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 配方生命周期状态。
 * <p>
 * 正向链：DRAFT → APPROVED → ACTIVE → RETIRED<br>
 * 驳回链：APPROVED → DRAFT
 *
 * <pre>
 * DRAFT ──→ APPROVED ──→ ACTIVE ──→ RETIRED
 *   ↑          │
 *   └──────────┘  (驳回)
 * </pre>
 *
 * @author 苏政
 */
public enum FormulaStatus implements ILifecycle.StatusEnum {

    /** 草稿 — 配方创建中，可编辑 */
    DRAFT,
    /** 已批准 — 审批通过，待激活 */
    APPROVED,
    /** 已激活 — 当前生产用版本 */
    ACTIVE,
    /** 已退役 — 不再使用，历史追溯保留 */
    RETIRED;

    @Override
    public Set<FormulaStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT    -> Set.of(APPROVED);
            case APPROVED -> Set.of(ACTIVE, DRAFT);
            case ACTIVE   -> Set.of(RETIRED);
            case RETIRED  -> Set.of();
        };
    }

}
