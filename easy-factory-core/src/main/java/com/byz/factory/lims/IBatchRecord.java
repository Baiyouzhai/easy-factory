package com.byz.factory.lims;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface IBatchRecord {
    String getBatchNo();
    String getWorkOrderId();
    String getFormulaCode();
    String getFormulaVersion();
    String getProductCode();
    BigDecimal getBatchSize();
    BigDecimal getYield();
    BatchRecordStatus getStatus();
    String getReviewedBy();
    Instant getReviewedAt();
    List<String> getProcessRecords();
    List<String> getWeighingTasks();
    List<String> getInspectionRecords();
    List<String> getDeviations();
}
