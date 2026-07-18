package com.byz.factory.equip.model;

import com.byz.factory.batch.IOEMetrics;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * OEE 指标 — 设备综合效率（Overall Equipment Effectiveness）。
 * 实现 {@link IOEMetrics} 供 BI/APS/FactoryCapacityProfile 跨模块引用。
 * <p>
 * OEE = 可用率(Availability) × 性能率(Performance) × 质量率(Quality)。
 * 三大指标均以百分比表示（0~1 或 0%~100%），OEE 为三者乘积。
 *
 * <h3>计算公式</h3>
 * <ul>
 *   <li><b>可用率 A</b> = 实际运行时间 ÷ 计划运行时间</li>
 *   <li><b>性能率 P</b> = 实际产出 ÷ 理论产出（在运行时间内）</li>
 *   <li><b>质量率 Q</b> = 合格品数量 ÷ 总产出数量</li>
 *   <li><b>OEE</b> = A × P × Q</li>
 * </ul>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   equip.oee.{equipmentCode}.downtimeEvents — 停机事件列表(JSON)
 *   equip.oee.{equipmentCode}.lossAnalysis   — 六大损失分析(JSON)
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OEMetrics extends BaseEntity implements IOEMetrics {

    /** 关联设备编码 */
    private String equipmentCode;

    /** 统计周期起始 */
    private LocalDate periodStart;

    /** 统计周期结束 */
    private LocalDate periodEnd;

    /** 可用率（0~1） */
    private BigDecimal availability;

    /** 性能率（0~1） */
    private BigDecimal performance;

    /** 质量率（0~1） */
    private BigDecimal quality;

    /** OEE 综合效率 = A × P × Q */
    private BigDecimal oee;

    public OEMetrics() {
        super();
    }

    /**
     * 工厂方法 — 创建 OEE 指标并自动计算 OEE 值。
     *
     * @param equipmentCode 设备编码
     * @param periodStart   周期起始
     * @param periodEnd     周期结束
     * @param availability  可用率（0~1）
     * @param performance   性能率（0~1）
     * @param quality       质量率（0~1）
     * @return 计算好的 OEMetrics 实例
     */
    public static OEMetrics of(String equipmentCode, LocalDate periodStart, LocalDate periodEnd,
                               BigDecimal availability, BigDecimal performance, BigDecimal quality) {
        OEMetrics metrics = new OEMetrics();
        metrics.setCode("OEE-" + equipmentCode + "-" + periodStart);
        metrics.setName(equipmentCode + " OEE " + periodStart + "~" + periodEnd);
        metrics.setEquipmentCode(equipmentCode);
        metrics.setPeriodStart(periodStart);
        metrics.setPeriodEnd(periodEnd);
        metrics.setAvailability(availability);
        metrics.setPerformance(performance);
        metrics.setQuality(quality);
        metrics.setOee(availability.multiply(performance).multiply(quality)
                .setScale(4, RoundingMode.HALF_UP));
        return metrics;
    }

    /** OEE 转为百分比字符串（如 "85.23%"） */
    public String toPercentString() {
        if (oee == null) return "N/A";
        return oee.multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP) + "%";
    }

}
