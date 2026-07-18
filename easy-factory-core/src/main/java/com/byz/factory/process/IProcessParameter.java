package com.byz.factory.process;

import com.byz.factory.shared.UOM;

import java.math.BigDecimal;

/**
 * 工艺参数 — 工序的目标参数定义（设定值 + 控制限）。
 * <p>
 * 用于设定值与实际测量值的对比，支撑 SPC/Cpk 分析。
 *
 * @author 苏政
 */
public interface IProcessParameter {

    /** 参数编码 */
    String getCode();

    /** 参数名称（如"干燥温度"、"转速"） */
    String getName();

    /** 计量单位 */
    UOM getUom();

    /** 目标值（工艺设定值） */
    BigDecimal getTargetValue();

    /** 控制下限 */
    BigDecimal getLowerLimit();

    /** 控制上限 */
    BigDecimal getUpperLimit();
}
