package com.byz.factory.plm.model;

import com.byz.factory.process.IProcessParameter;
import com.byz.factory.shared.BaseEntity;
import com.byz.factory.shared.UOM;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 工艺参数 — 工序的目标参数定义（设定值 + 控制限）。
 * <p>
 * 继承 BaseEntity 获得 code/name/时间戳，实现 IProcessParameter 供跨模块引用。
 * 用于设定值与实际测量值的对比，支撑 SPC/Cpk 分析。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessParameter extends BaseEntity implements IProcessParameter {

    /** 数据类型 */
    public enum DataType { NUMERIC, BOOLEAN, ENUM, TEXT }

    /** 控制方式 */
    public enum ControlMethod { MANUAL, AUTO, SEMI_AUTO }

    /** 重要性 */
    public enum Importance { CRITICAL, IMPORTANT, NORMAL }

    /** 计量单位 */
    private UOM uom;

    /** 目标值（工艺设定值） */
    private BigDecimal targetValue;

    /** 控制下限 */
    private BigDecimal lowerLimit;

    /** 控制上限 */
    private BigDecimal upperLimit;

    /** 数据类型 */
    private DataType dataType;

    /** 控制方式 */
    private ControlMethod controlMethod;

    /** 重要性 */
    private Importance importance;

    /** 默认值 */
    private String defaultValue;

    /**
     * @param code        参数编码
     * @param name        参数名称（如"干燥温度"、"转速"）
     * @param uom         计量单位
     * @param targetValue 目标值
     * @param lowerLimit  控制下限
     * @param upperLimit  控制上限
     */
    public ProcessParameter(String code, String name, UOM uom,
                            BigDecimal targetValue, BigDecimal lowerLimit,
                            BigDecimal upperLimit) {
        super(code, name);
        this.uom = uom;
        this.targetValue = targetValue;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.dataType = DataType.NUMERIC;
        this.controlMethod = ControlMethod.AUTO;
        this.importance = Importance.IMPORTANT;
    }

}
