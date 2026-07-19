package com.byz.factory.factory.physical;

import java.util.Collections;
import java.util.List;
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

    /**
     * 换型/设置时间（分钟）— APS 排程时消费。
     * <p>
     * 从上一产品切换到当前产品需要对设备进行的调整时间。
     * 默认返回 0（无换型时间）。
     */
    default int getSetupMinutes() {
        return 0;
    }

    /**
     * 清场/清理时间（分钟）— APS 排程时消费。
     * <p>
     * 生产完成后对设备进行清洁、消毒、清场的时间。
     * 默认返回 0（无需清场）。
     */
    default int getCleanupMinutes() {
        return 0;
    }

    /**
     * 设备支持的动作序列列表 — 表达动作间的顺序约束。
     * <p>
     * 每个 List&lt;String&gt; 是一个可执行的动作序列（如 ["A001","A002","A003"] 表示
     * A001→A002→A003 依次执行）。多个序列表示设备支持多种工艺路径。
     * <p>
     * 默认返回空（不限制顺序，向后兼容：仅以 getSupportedActionCodes() 判定）。
     */
    default List<List<String>> getSupportedSequences() {
        return Collections.emptyList();
    }

    /**
     * 匹配蓝图动作序列与设备支持的序列。
     * <p>
     * 判定规则：
     * <ul>
     *   <li>设备未声明序列（getSupportedSequences() 为空）→ 跳过检查，返回 EXACT</li>
     *   <li>蓝图序列与设备声明的某个序列完全一致（同顺序）→ EXACT</li>
     *   <li>蓝图所有动作都在设备支持集合中，但顺序不匹配任何声明序列 → FLEXIBLE</li>
     *   <li>蓝图某个动作不在设备的 supportedActionCodes 中 → REJECTED</li>
     * </ul>
     *
     * @param actionCodes 蓝图要求的动作编码序列（按执行顺序）
     * @return 匹配结果
     */
    default SequenceMatch matchSequence(List<String> actionCodes) {
        List<List<String>> sequences = getSupportedSequences();

        // 未声明序列 → 降级到集合匹配
        if (sequences.isEmpty()) {
            for (String code : actionCodes) {
                if (!getSupportedActionCodes().contains(code)) {
                    return SequenceMatch.REJECTED;
                }
            }
            return SequenceMatch.EXACT;
        }

        // 逐序列比对
        for (List<String> seq : sequences) {
            if (seq.equals(actionCodes)) {
                return SequenceMatch.EXACT;
            }
        }

        // 无序列完全匹配 → 检查动作集合是否全部支持
        for (String code : actionCodes) {
            if (!getSupportedActionCodes().contains(code)) {
                return SequenceMatch.REJECTED;
            }
        }
        return SequenceMatch.FLEXIBLE;
    }
}
