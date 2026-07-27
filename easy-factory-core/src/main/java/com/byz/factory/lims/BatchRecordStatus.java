package com.byz.factory.lims;

import com.byz.factory.lifecycle.ILifecycle;
import java.util.Set;

public enum BatchRecordStatus implements ILifecycle.StatusEnum {
    IN_PROGRESS, REVIEW, APPROVED, ARCHIVED;

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
