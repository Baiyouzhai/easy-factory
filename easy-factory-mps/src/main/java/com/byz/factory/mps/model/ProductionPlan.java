package com.byz.factory.mps.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductionPlan extends DataExpand {
    private String planNo;
    private String version;
    private String periodType;       // WEEKLY/MONTHLY/QUARTERLY
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String status;           // DRAFT/APPROVED/RELEASED
    private String approvedBy;
    private List<PlanItem> items;

    public record PlanItem(String productCode, String productName, BigDecimal quantity,
                            LocalDate dueDate, int priority, String factoryCode) {}
}
