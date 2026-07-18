package com.byz.factory.batch;

import java.math.BigDecimal;
import java.util.List;

/**
 * 设备配方抽象 — 适用于特定设备+产品的工艺参数集合，按阶段组织。
 * <p>
 * 配方由 PLM 工艺设计产生，下发到 Equip 管理，MES 执行时调用。
 * <p>
 * <b>谁来引用？</b>
 * <ul>
 *   <li><b>EngineeringBOM.EquipmentAssignment</b> — 标注时通过 recipeCode + phaseName 引用 → 后续迭代</li>
 *   <li><b>IEquipmentAction</b> — 执行时读取配方阶段参数下发到设备</li>
 *   <li><b>PLM</b> — 工艺参数转化为配方时通过此接口写入</li>
 *   <li><b>IoT</b> — 配方下发时通过此接口读取阶段参数</li>
 * </ul>
 *
 * <h3>与 EngineeringBOM 的关系</h3>
 * 当前 {@code EngineeringBOM.EquipmentAssignment} 用 {@code Map<String,Object> parameters}
 * 存储参数。引入本接口后，可改为引用 {@code recipeCode + phaseName}，
 * EngineeringBOM 不再持有参数副本，而是引用 Equip 管理的配方权威数据。
 *
 * @author 苏政
 * @see IRecipePhase
 */
public interface IEquipmentRecipe {

    /** 配方编码 */
    String getCode();

    /** 配方名称 */
    String getName();

    /** 适用设备编码 */
    String getEquipmentCode();

    /** 适用产品编码 */
    String getProductCode();

    /** 配方版本 */
    String getVersion();

    /** 配方阶段列表（按执行顺序排列） */
    List<? extends IRecipePhase> getPhases();

    /**
     * 配方阶段 — 单个参数在特定阶段的设定值。
     * <p>
     * 示例：反应釜配方中，"温度"参数在"升温段"设定 120°C，
     * 持续 600 秒，升温速率 2°C/min；在"恒温段"设定 120°C，持续 1800 秒。
     */
    interface IRecipePhase {

        /** 参数编码 */
        String getParamCode();

        /** 阶段名称（如：升温段、恒温段、降温段） */
        String getPhase();

        /** 设定值 */
        BigDecimal getSetValue();

        /** 持续时间（秒） */
        int getDuration();

        /** 变化速率（单位/秒），恒温段为 0 */
        BigDecimal getRampRate();

    }

}
