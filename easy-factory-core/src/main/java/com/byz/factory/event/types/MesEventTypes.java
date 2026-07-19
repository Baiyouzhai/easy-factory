package com.byz.factory.event.types;

/**
 * MES 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>QMS</b> — 订阅 {@link #PROCESS_STARTED}，自动创建检验指令；
 *       订阅 {@link #PROCESS_INTERRUPTED}，发起偏差调查</li>
 *   <li><b>APS</b> — 订阅 {@link #WORKORDER_STARTED} / {@link #WORKORDER_COMPLETED}，
 *       更新排程在制品状态</li>
 *   <li><b>LIMS</b> — 订阅 {@link #WORKORDER_RELEASED}，创建称量任务；
 *       订阅 {@link #ACTION_COMPLETED}，记录物料消耗</li>
 *   <li><b>Equip</b> — 订阅 {@link #PROCESS_STARTED}，下发设备配方参数</li>
 *   <li><b>Andon</b> — 订阅 {@link #PROCESS_INTERRUPTED}，触发安灯异常呼叫</li>
 *   <li><b>BI</b> — 订阅 {@link #WORKORDER_COMPLETED}，更新完工看板</li>
 * </ul>
 *
 * @author 苏政
 */
public final class MesEventTypes {

    private MesEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "mes";

    // ── 工单生命周期 ──

    /** 工单已创建 — 载荷: workOrderNo + productCode */
    public static final String WORKORDER_CREATED = "mes.workorder.created";

    /** 工单已下达 — 载荷: workOrderNo + blueprintVersion */
    public static final String WORKORDER_RELEASED = "mes.workorder.released";

    /** 工单已开工 — 载荷: workOrderNo + startTime */
    public static final String WORKORDER_STARTED = "mes.workorder.started";

    /** 工单已完成 — 载荷: workOrderNo + endTime + actualQuantity */
    public static final String WORKORDER_COMPLETED = "mes.workorder.completed";

    /** 工单已关闭 — 载荷: workOrderNo */
    public static final String WORKORDER_CLOSED = "mes.workorder.closed";

    // ── 工序流转 ──

    /** 工序开始 — 载荷: workOrderNo + processCode */
    public static final String PROCESS_STARTED = "mes.process.started";

    /** 工序完成 — 载荷: workOrderNo + processCode + duration */
    public static final String PROCESS_COMPLETED = "mes.process.completed";

    /** 工序中断 — 载荷: workOrderNo + processCode + reason */
    public static final String PROCESS_INTERRUPTED = "mes.process.interrupted";

    /** 工序恢复 — 载荷: workOrderNo + processCode */
    public static final String PROCESS_RESUMED = "mes.process.resumed";

    // ── 动作执行 ──

    /** 动作执行完成 — 载荷: workOrderNo + processCode + actionCode */
    public static final String ACTION_COMPLETED = "mes.action.completed";

}
