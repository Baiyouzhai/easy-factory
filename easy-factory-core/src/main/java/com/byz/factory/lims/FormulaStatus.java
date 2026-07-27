package com.byz.factory.lims;

import com.byz.factory.lifecycle.ILifecycle;
import java.util.Set;

public enum FormulaStatus implements ILifecycle.StatusEnum {
    DRAFT, APPROVED, ACTIVE, RETIRED;

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
