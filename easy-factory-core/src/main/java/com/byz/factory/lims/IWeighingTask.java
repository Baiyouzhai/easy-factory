package com.byz.factory.lims;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface IWeighingTask {
    String getCode();
    String getFormulaCode();
    String getWorkOrderId();
    String getBatchNo();
    List<? extends IWeighingItem> getItems();
    WeighingTaskStatus getStatus();

    interface IWeighingItem {
        String getMaterialCode();
        BigDecimal getFormulaQty();
        BigDecimal getActualQty();
        BigDecimal getTolerance();
        String getBalance();
        String getOperator();
        String getVerifier();
        Instant getWeighedAt();
    }
}
