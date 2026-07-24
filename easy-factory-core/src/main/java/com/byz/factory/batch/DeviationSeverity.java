package com.byz.factory.batch;

/**
 * 偏差严重程度 — QMS 模块。
 * <p>
 * 用于评估质量偏差对产品、工艺和 GMP 合规的影响程度。
 *
 * @author 苏政
 */
public enum DeviationSeverity {

    /** 轻微偏差 — 对产品质量无实质影响 */
    MINOR,

    /** 重大偏差 — 可能影响产品质量，需评估放行条件 */
    MAJOR,

    /** 严重偏差 — 严重影响产品质量或 GMP 合规，需立即处置 */
    CRITICAL

}
