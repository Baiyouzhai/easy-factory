package com.byz.factory.event.types;

/**
 * APS 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>MES</b> — 订阅 {@link #SCHEDULE_RELEASED}，获取排程结果并下发工单；
 *       订阅 {@link #SCHEDULE_DISPATCHED}（等同于 RELEASED 的别名）</li>
 *   <li><b>Equip</b> — 订阅 {@link #SCHEDULE_RELEASED}，锁定设备占用时段</li>
 *   <li><b>MPS</b> — 订阅 {@link #PLAN_RECEIVED} 的回执确认</li>
 *   <li><b>BI</b> — 订阅 {@link #TASK_DELAYED}，交期延误统计</li>
 * </ul>
 *
 * @author 苏政
 */
public final class ApsEventTypes {

    private ApsEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "aps";

    // ── 计划接收 ──

    /** MPS 计划已接收 — 载荷: planNo + factoryCode */
    public static final String PLAN_RECEIVED = "aps.plan.received";

    // ── 排程生命周期 ──

    /** 排程已创建 — 载荷: scheduleCode + planNo */
    public static final String SCHEDULE_CREATED = "aps.schedule.created";

    /** 排程已下发 — 载荷: scheduleCode + taskCount */
    public static final String SCHEDULE_RELEASED = "aps.schedule.released";

    // ── 任务异常 ──

    /** 任务延误 — 载荷: scheduleCode + workOrderNo + delayMinutes */
    public static final String TASK_DELAYED = "aps.task.delayed";

    // ── 重排程 ──

    /** 重排程已触发 — 载荷: scheduleCode + triggerEvent + triggeredAt */
    public static final String RESCHEDULE_TRIGGERED = "aps.reschedule.triggered";

}
