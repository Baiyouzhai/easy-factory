package com.byz.factory.lims;

import java.math.BigDecimal;
import java.util.List;

public interface IFormula {
    String getCode();
    String getName();
    String getProductCode();
    String getVersion();
    BigDecimal getBatchSize();
    FormulaStatus getStatus();
    String getApprovedBy();
    String getYield();
    List<? extends IFormulaPhase> getPhases();

    interface IFormulaPhase {
        int getPhaseNo();
        String getPhaseName();
    }
}
