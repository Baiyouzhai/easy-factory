package com.byz.factory.event.types;

/**
 * MPS 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>MES</b> — 订阅 {@link #PLAN_RELEASED}，自动生成工单</li>
 *   <li><b>APS</b> — 订阅 {@link #PLAN_CREATED} / {@link #PLAN_APPROVED} / {@link #PLAN_RELEASED}，更新排程输入</li>
 *   <li><b>ERP</b> — 订阅 {@link #DEMAND_REGISTERED}，同步需求到采购计划</li>
 *   <li><b>BI</b> — 订阅 {@link #PLAN_STARTED} / {@link #PLAN_COMPLETED} / {@link #PLAN_CLOSED}，生产计划执行看板</li>
 * </ul>
 *
 * @author 苏政
 */
public final class MpsEventTypes {

    private MpsEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "mps";

    // ── 计划生命周期 ──

    /** 生产计划已创建 — 载荷: 计划编号 + 周期类型 + 明细数量 */
    public static final String PLAN_CREATED = "mps.plan.created";

    /** 生产计划已审批 — 载荷: 计划编号 + 审批人 */
    public static final String PLAN_APPROVED = "mps.plan.approved";

    /** 生产计划已发布 — 载荷: 计划编号（MES 订阅此事件生成工单） */
    public static final String PLAN_RELEASED = "mps.plan.released";

    /** 生产计划已开始执行 — 载荷: 计划编号 */
    public static final String PLAN_STARTED = "mps.plan.started";

    /** 生产计划已完成 — 载荷: 计划编号 */
    public static final String PLAN_COMPLETED = "mps.plan.completed";

    /** 生产计划已关闭 — 载荷: 计划编号 */
    public static final String PLAN_CLOSED = "mps.plan.closed";

    // ── 需求管理 ──

    /** 需求已登记 — 载荷: 来源类型 + 产品编码 + 数量 */
    public static final String DEMAND_REGISTERED = "mps.demand.registered";

    // ── 产能 ──

    /** 粗产能检查完成 — 载荷: 计划编号 + 工厂编码 + 结果(PASS/WARNING/FAIL) */
    public static final String CAPACITY_CHECKED = "mps.capacity.checked";

}
