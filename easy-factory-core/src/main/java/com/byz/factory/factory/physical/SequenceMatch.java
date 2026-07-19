package com.byz.factory.factory.physical;

/**
 * 动作序列匹配结果 — 用于设备绑定验证蓝图动作顺序是否匹配设备能力。
 * <p>
 * 三档判定：
 * <pre>
 * EXACT     设备声明的动作序列与蓝图完全一致（同顺序），直接通过
 * FLEXIBLE  设备支持这些动作，但执行顺序不同 → 允许，但排程时加 setup 时间惩罚
 * REJECTED  设备不支持其中某个动作 → 门禁拒绝，不可行
 * </pre>
 *
 * @author 苏政
 */
public enum SequenceMatch {

    /** 序列完全一致 — 直接通过 */
    EXACT,

    /** 动作集合匹配但顺序不同 — 允许，但标记换型惩罚 */
    FLEXIBLE,

    /** 设备不支持某动作 — 拒绝 */
    REJECTED
}
