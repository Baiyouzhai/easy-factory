package com.byz.factory.equip.model;

import com.byz.factory.equip.IEquipmentParameter;
import com.byz.factory.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 设备工艺参数 — 关联设备与工艺参数的设定值和实际值。
 * 实现 {@link IEquipmentParameter} 供 IoT/QMS 跨模块引用。
 * <p>
 * 参数由 PLM 工艺设计产生，经 Equip 配方下发到 IoT/PLC 执行，
 * IoT 实时回传实际值供 SPC 监控。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   equip.param.{paramCode}.alarmThreshold  — 报警阈值
 *   equip.param.{paramCode}.lastActual      — 最近一次实际值
 *   equip.param.{paramCode}.trend           — 趋势方向(UP/DOWN/STABLE)
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "equip_parameter")
public class EquipmentParameter extends BaseEntity implements IEquipmentParameter {

    /** 关联设备编码 */
    @Column(nullable = false, length = 100)
    private String equipmentCode;

    /** 参数编码 */
    @Column(nullable = false, length = 50)
    private String paramCode;

    /** 参数名称（如：转速、温度、压力、流量） */
    @Column(nullable = false, length = 100)
    private String paramName;

    /** 设定值 */
    @Column(precision = 20, scale = 6)
    private BigDecimal setValue;

    /** 实际值（从 IoT 采集） */
    @Column(precision = 20, scale = 6)
    private BigDecimal actualValue;

    /** 控制上限 */
    @Column(precision = 20, scale = 6)
    private BigDecimal upperLimit;

    /** 控制下限 */
    @Column(precision = 20, scale = 6)
    private BigDecimal lowerLimit;

    /** 单位 */
    @Column(length = 20)
    private String unit;

    /** 控制方式（PLC自动 / 人工调节） */
    @Column(length = 20)
    private String controlMethod;

    public EquipmentParameter() {
        super();
    }

    /**
     * @param equipmentCode 关联设备编码
     * @param paramCode     参数编码
     * @param paramName     参数名称
     * @param setValue      设定值
     * @param unit          单位
     */
    public EquipmentParameter(String equipmentCode, String paramCode, String paramName,
                              BigDecimal setValue, String unit) {
        super(paramCode, paramName); // code=参数编码, name=参数名称
        this.equipmentCode = equipmentCode;
        this.paramCode = paramCode;
        this.paramName = paramName;
        this.setValue = setValue;
        this.unit = unit;
        this.controlMethod = "PLC自动";
    }

    /** 更新从 IoT 回传的实际值 */
    public void updateActual(BigDecimal actualValue) {
        this.actualValue = actualValue;
        markUpdated();
    }

    // isInControl() 由 IEquipmentParameter 接口提供 default 实现，此處不重複

}
