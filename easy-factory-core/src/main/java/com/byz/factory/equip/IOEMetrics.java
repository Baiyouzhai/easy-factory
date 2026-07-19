package com.byz.factory.equip;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OEE 指标抽象 — 设备综合效率（Overall Equipment Effectiveness）。
 * <p>
 * OEE = 可用率(Availability) × 性能率(Performance) × 质量率(Quality)。
 * 三大指标均以百分比表示（0~1），OEE 为三者乘积。
 * <p>
 * <b>谁来引用？</b>
 * <ul>
 *   <li><b>BI</b> — OEE 看板通过此接口读取数据</li>
 *   <li><b>APS</b> — 排程时通过此接口获取实际 OEE 修正产能因子</li>
 *   <li><b>FactoryCapacityProfile</b> — {@code getEffectiveMachineTime()} 后续迭代可接收此接口做精确计算</li>
 * </ul>
 *
 * <h3>与 FactoryCapacityProfile 的关系</h3>
 * 当前 {@code FactoryCapacityProfile} 有静态 OEE 因子（{@code machineOeeFactors}），
 * 用于排程时预估。{@code IOEMetrics} 是实际统计结果。两者互补：
 * 规划用因子，实际用 OEE 指标。后续 {@code getEffectiveMachineTime()} 可接收
 * {@code IOEMetrics} 做精确计算。
 *
 * @author 苏政
 */
public interface IOEMetrics {

    /** 关联设备编码 */
    String getEquipmentCode();

    /** 统计周期起始 */
    LocalDate getPeriodStart();

    /** 统计周期结束 */
    LocalDate getPeriodEnd();

    /** 可用率（0~1）：实际运行时间 ÷ 计划运行时间 */
    BigDecimal getAvailability();

    /** 性能率（0~1）：实际产出 ÷ 理论产出 */
    BigDecimal getPerformance();

    /** 质量率（0~1）：合格品数量 ÷ 总产出数量 */
    BigDecimal getQuality();

    /** OEE 综合效率 = A × P × Q */
    BigDecimal getOee();

}
