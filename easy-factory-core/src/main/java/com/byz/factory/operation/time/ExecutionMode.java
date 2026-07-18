package com.byz.factory.operation.time;

/**
 * 执行模式 — 描述工序内动作的执行顺序。
 *
 * @author 苏政
 */
public enum ExecutionMode {

    /** 顺序执行 — 动作逐个执行，总时间 = Σ(各动作时间) */
    SEQUENTIAL,

    /** 并行执行 — 所有动作同时执行，总时间 = max(各动作时间) */
    PARALLEL,

    /** 混合执行 — 按 ActionGroup 分组，组间串行，组内并行 */
    GROUPED

}
