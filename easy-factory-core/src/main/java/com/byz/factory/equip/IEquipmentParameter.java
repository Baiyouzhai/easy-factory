package com.byz.factory.equip;

import java.math.BigDecimal;

/**
 * 设备工艺参数抽象 — 供跨模块引用参数设定值/实际值。
 * <p>
 * 参数由 PLM 工艺设计产生，经 Equip 配方下发到 IoT/PLC 执行，
 * IoT 实时回传实际值供 SPC 监控。
 * <p>
 * <b>谁来引用？</b>
 * <ul>
 *   <li><b>IEquipmentBinding</b> — 设备-动作绑定的参数模板 → 后续迭代从 {@code Map<String,Object>} 迁移</li>
 *   <li><b>IEquipmentAction</b> — 运行时下发的参数值 → 后续迭代从 {@code Map<String,Object>} 迁移</li>
 *   <li><b>EngineeringBOM.EquipmentAssignment</b> — 工程标注的参数设定 → 后续迭代从 {@code Map<String,Object>} 迁移</li>
 *   <li><b>IoT</b> — 回传实际值时通过此接口写入</li>
 *   <li><b>QMS</b> — SPC 分析时通过此接口读取参数数据</li>
 * </ul>
 *
 * <h3>与现有 Map 的关系</h3>
 * 当前 core 中 {@code getParameters()} 返回 {@code Map<String, Object>}。
 * 本接口定义了类型安全的参数模型。后续各调用方可逐步迁移，
 * 本次迭代不做破坏性修改。
 *
 * @author 苏政
 */
public interface IEquipmentParameter {

    /** 关联设备编码 */
    String getEquipmentCode();

    /** 参数编码 */
    String getParamCode();

    /** 参数名称（如：转速、温度、压力、流量） */
    String getParamName();

    /** 设定值 */
    BigDecimal getSetValue();

    /** 实际值（从 IoT 采集，可能为 null） */
    BigDecimal getActualValue();

    /** 控制上限（可能为 null，表示未设置） */
    BigDecimal getUpperLimit();

    /** 控制下限（可能为 null，表示未设置） */
    BigDecimal getLowerLimit();

    /** 单位 */
    String getUnit();

    /** 控制方式（PLC自动 / 人工调节） */
    String getControlMethod();

    /**
     * 判断当前实际值是否在控制范围内。
     * 若未设置控制限或实际值为 null，默认为受控。
     */
    default boolean isInControl() {
        BigDecimal actual = getActualValue();
        BigDecimal upper = getUpperLimit();
        BigDecimal lower = getLowerLimit();
        if (actual == null || upper == null || lower == null) {
            return true;
        }
        return actual.compareTo(lower) >= 0
                && actual.compareTo(upper) <= 0;
    }

}
