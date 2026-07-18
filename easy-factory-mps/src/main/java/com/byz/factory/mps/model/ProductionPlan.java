package com.byz.factory.mps.model;

import com.byz.factory.batch.ProductionPlanStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 生产计划 — 继承 BaseLifecycleEntity 获得状态机（DRAFT→APPROVED→RELEASED→IN_PROGRESS→COMPLETED）。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductionPlan extends BaseLifecycleEntity<ProductionPlanStatus> {

    private String periodType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String approvedBy;
    private List<PlanItem> items;

    public ProductionPlan(String planNo, String periodType, LocalDate periodStart, LocalDate periodEnd) {
        super(planNo, "计划-" + planNo, ProductionPlanStatus.DRAFT);
        this.periodType = periodType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public record PlanItem(String productCode, String productName, BigDecimal quantity,
                            LocalDate dueDate, int priority, String factoryCode) {}
}
