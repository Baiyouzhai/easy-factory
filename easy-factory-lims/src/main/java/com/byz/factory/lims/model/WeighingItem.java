package com.byz.factory.lims.model;

import com.byz.factory.batch.IWeighingTask;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 称量项目 — 单个物料的称量明细。
 * <p>
 * 实现 IWeighingTask.IWeighingItem 供跨模块引用。
 * 参照 PurchaseOrderItem 模式为独立类（非内部类）。
 *
 * @author 苏政
 */
@Data
public class WeighingItem implements IWeighingTask.IWeighingItem {

    /** 物料编码 */
    private String materialCode;

    /** 配方量 */
    private BigDecimal formulaQty;

    /** 实际称量量（称量前为 null） */
    private BigDecimal actualQty;

    /** 允差（百分比绝对值，如 1.0 表示 ±1%） */
    private BigDecimal tolerance;

    /** 使用天平编号 */
    private String balance;

    /** 称量人 */
    private String operator;

    /** 复核人 */
    private String verifier;

    /** 称量时间 */
    private Instant weighedAt;

    /**
     * @param materialCode 物料编码
     * @param formulaQty   配方量
     * @param tolerance    允差（百分比绝对值）
     */
    public WeighingItem(String materialCode, BigDecimal formulaQty, BigDecimal tolerance) {
        this.materialCode = materialCode;
        this.formulaQty = formulaQty;
        this.tolerance = tolerance;
    }

    /**
     * 记录称量结果。
     *
     * @param actualQty 实际称量量
     * @param operator  称量人
     * @param verifier  复核人
     * @param balance   使用天平编号
     */
    public void recordWeighing(BigDecimal actualQty, String operator, String verifier, String balance) {
        this.actualQty = actualQty;
        this.operator = operator;
        this.verifier = verifier;
        this.balance = balance;
        this.weighedAt = Instant.now();
    }

    /**
     * 计算偏差百分比。
     *
     * @return 偏差百分比（绝对值），若配方量为 0 或未称量则返回 null
     */
    public BigDecimal getDeviationPercent() {
        if (actualQty == null || formulaQty == null || formulaQty.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return actualQty.subtract(formulaQty).abs()
                .divide(formulaQty, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 判断是否超差。
     */
    public boolean isOutOfTolerance() {
        BigDecimal dev = getDeviationPercent();
        return dev != null && tolerance != null && dev.compareTo(tolerance) > 0;
    }

}
