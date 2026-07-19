package com.byz.factory.aps.model;

/**
 * 排程任务状态 — ScheduledTask 的执行生命周期。
 * <p>
 * 任务状态由排程方案的状态变更驱动：
 * Schedule.OPTIMIZED → 所有任务 SCHEDULED；
 * Schedule.DISPATCHED → 所有任务 DISPATCHED；
 * MES 反馈工序开始/完成时更新单个任务状态。
 *
 * @author 苏政
 */
public enum TaskStatus {

    /** 已排程（待下发） */
    SCHEDULED,

    /** 已下发到 MES */
    DISPATCHED,

    /** 执行中（MES 已开工） */
    IN_PROGRESS,

    /** 已完成 */
    COMPLETED,

    /** 已取消 */
    CANCELLED;

}
