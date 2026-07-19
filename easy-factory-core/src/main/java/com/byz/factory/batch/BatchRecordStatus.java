package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 批记录生命周期状态。
 * <p>
 * 正向链：IN_PROGRESS → REVIEW → APPROVED → ARCHIVED<br>
 * 驳回链：REVIEW → IN_PROGRESS
 *
 * <pre>
 * IN_PROGRESS ──→ REVIEW ──→ APPROVED ──→ ARCHIVED
 *      ↑            │
 *      └────────────┘  (驳回重审)
 * </pre>
 *
 * @author 苏政
 */
public enum BatchRecordStatus implements ILifecycle.StatusEnum {

    /** 进行中 — 批记录随生产实时生成 */
    IN_PROGRESS,
    /** 审核中 — 批记录已完成，等待审核 */
    REVIEW,
    /** 已批准 — 审核通过，符合 GMP 要求 */
    APPROVED,
    /** 已归档 — 长期保存，不可修改 */
    ARCHIVED;

    @Override
    public Set<BatchRecordStatus> allowedTransitions() {
        return switch (this) {
            case IN_PROGRESS -> Set.of(REVIEW);
            case REVIEW      -> Set.of(APPROVED, IN_PROGRESS);
            case APPROVED    -> Set.of(ARCHIVED);
            case ARCHIVED    -> Set.of();
        };
    }

}
