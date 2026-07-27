package com.byz.factory.lims.model;

import com.byz.factory.lims.IWeighingTask;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "lims_weighing_item")
public class WeighingItem implements IWeighingTask.IWeighingItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_code", nullable = false, length = 100)
    private String materialCode;

    @Column(name = "formula_qty", precision = 20, scale = 6)
    private BigDecimal formulaQty;

    @Column(name = "actual_qty", precision = 20, scale = 6)
    private BigDecimal actualQty;

    @Column(precision = 10, scale = 4)
    private BigDecimal tolerance;

    @Column(length = 50)
    private String balance;

    @Column(length = 100)
    private String operator;

    @Column(length = 100)
    private String verifier;

    @Column(name = "weighed_at")
    private Instant weighedAt;

    public WeighingItem() {}

    public WeighingItem(String materialCode, BigDecimal formulaQty, BigDecimal tolerance) {
        this.materialCode = materialCode;
        this.formulaQty = formulaQty;
        this.tolerance = tolerance;
    }

    @Override public String getMaterialCode() { return materialCode; }
    @Override public BigDecimal getFormulaQty() { return formulaQty; }
    @Override public BigDecimal getActualQty() { return actualQty; }
    @Override public BigDecimal getTolerance() { return tolerance; }
    @Override public String getBalance() { return balance; }
    @Override public String getOperator() { return operator; }
    @Override public String getVerifier() { return verifier; }
    @Override public Instant getWeighedAt() { return weighedAt; }

    public void recordWeighing(BigDecimal actualQty, String operator, String verifier, String balance) {
        this.actualQty = actualQty; this.operator = operator; this.verifier = verifier; this.balance = balance;
        this.weighedAt = Instant.now();
    }

    public BigDecimal getDeviationPercent() {
        if (actualQty == null || formulaQty == null || formulaQty.compareTo(BigDecimal.ZERO) == 0) return null;
        return actualQty.subtract(formulaQty).abs().divide(formulaQty, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public boolean isOutOfTolerance() {
        BigDecimal dev = getDeviationPercent();
        return dev != null && tolerance != null && dev.compareTo(tolerance) > 0;
    }
}
