package com.byz.factory.plm.model;

import com.byz.factory.process.IProcessParameter;
import com.byz.factory.shared.BaseEntity;
import com.byz.factory.shared.UOM;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 工艺参数 — 工序的目标参数定义（设定值 + 控制限）。
 * <p>
 * 继承 BaseEntity 获得 id/code/name/时间戳，实现 IProcessParameter 供跨模块引用。
 * 用于设定值与实际测量值的对比，支撑 SPC/Cpk 分析。
 *
 * @author 苏政
 */
@Entity
@Table(name = "plm_process_parameter")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ProcessParameter extends BaseEntity implements IProcessParameter {

    /** 数据类型 */
    public enum DataType { NUMERIC, BOOLEAN, ENUM, TEXT }

    /** 控制方式 */
    public enum ControlMethod { MANUAL, AUTO, SEMI_AUTO }

    /** 重要性 */
    public enum Importance { CRITICAL, IMPORTANT, NORMAL }

    /** 计量单位 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UOM uom;

    /** 目标值（工艺设定值） */
    @Column(name = "target_value", precision = 20, scale = 6)
    private BigDecimal targetValue;

    /** 控制下限 */
    @Column(name = "lower_limit", precision = 20, scale = 6)
    private BigDecimal lowerLimit;

    /** 控制上限 */
    @Column(name = "upper_limit", precision = 20, scale = 6)
    private BigDecimal upperLimit;

    /** 数据类型 */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 20, nullable = false)
    private DataType dataType;

    /** 控制方式 */
    @Enumerated(EnumType.STRING)
    @Column(name = "control_method", length = 20, nullable = false)
    private ControlMethod controlMethod;

    /** 重要性 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Importance importance;

    /** 默认值 */
    @Column(name = "default_value", length = 100)
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
