package com.byz.factory.factory.physical;

import java.util.Map;
import java.util.Set;

/**
 * 设备-动作绑定 — 声明一台设备能执行哪些动作，以及执行时的参数。
 * <p>
 * 这是物理层和工艺层的桥梁：蓝图定义"需要做什么动作"，
 * 设备绑定声明"这台设备能做哪些动作"。
 *
 * @author 苏政
 */
public interface IEquipmentBinding {

    /**
     * 设备编码（对应 equip 模块的设备台账）。
     */
    String getEquipmentCode();

    /**
     * 该设备能执行的动作编码集合。
     * 空集 = 设备已安装但未配置可执行的动作。
     */
    Set<String> getSupportedActionCodes();

    /**
     * 是否为主要设备（一个动作可能有多台备选设备，primary 为首选）。
     */
    default boolean isPrimary() {
        return true;
    }

    /**
     * 设备执行参数（工艺参数，如转速/温度/压力）。
     * key 格式: equip.paramName，如 "equip.speed", "equip.temp"
     */
    Map<String, Object> getParameters();

    /**
     * 检查该设备是否支持指定动作。
     */
    default boolean supports(String actionCode) {
        return getSupportedActionCodes().contains(actionCode);
    }
}
