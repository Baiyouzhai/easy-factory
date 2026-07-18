package com.byz.factory.operation.time;

/**
 * 时间分类 — 用于精益/价值流分析，区分时间的使用性质。
 *
 * @author 苏政
 */
public enum TimeCategory {

    /** 增值时间 — 实际改变产品形态的加工时间 */
    VALUE_ADDED,

    /** 辅助增值 — 设置/换型/调试等必要但不直接增值的时间 */
    SETUP,

    /** 检验时间 — 质量检测/取样/判定 */
    INSPECTION,

    /** 转运时间 — 工序间物料移动 */
    TRANSFER,

    /** 等待时间 — 排队/待料/待检 */
    WAIT,

    /** 浪费 — 返工/停机/异常处理 */
    WASTE
}
