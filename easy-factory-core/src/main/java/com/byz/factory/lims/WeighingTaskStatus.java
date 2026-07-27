package com.byz.factory.lims;

import com.byz.factory.lifecycle.ILifecycle;
import java.util.Set;

public enum WeighingTaskStatus implements ILifecycle.StatusEnum {
    PENDING, WEIGHING, VERIFIED, COMPLETE;

    @Override
    public Set<WeighingTaskStatus> allowedTransitions() {
        return switch (this) {
            case PENDING   -> Set.of(WEIGHING);
            case WEIGHING  -> Set.of(VERIFIED);
            case VERIFIED  -> Set.of(COMPLETE);
            case COMPLETE  -> Set.of();
        };
    }
}
