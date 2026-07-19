package com.byz.factory.aps.service;

import com.byz.factory.aps.model.Schedule;

/**
 * APS 排程服务 — 从 MPS 计划创建排程、优化、下发到 MES。
 * <p>
 * 排程流程：
 * <ol>
 *   <li>{@link #createSchedule(String, String)} — 从 MPS 计划创建排程草稿</li>
 *   <li>{@link #optimize(String, String)} — 按策略（EDD/SPT/CR）优化排程</li>
 *   <li>{@link #dispatch(String)} — 下发排程到 MES</li>
 *   <li>{@link #reschedule(String, String)} — 事件驱动增量重排程</li>
 * </ol>
 * <p>
 * 排程规则（design-decisions.md §2.5）：
 * <ul>
 *   <li>EDD (Earliest Due Date) — 交期优先</li>
 *   <li>SPT (Shortest Processing Time) — 最小工时优先</li>
 *   <li>CR (Critical Ratio) — (交期-今天)/剩余加工时间，最小者优先</li>
 * </ul>
 * <p>
 * 重排程触发条件（design-decisions.md §2.6）：
 * <ul>
 *   <li>{@code mes.workorder.released} — 新工单插入</li>
 *   <li>{@code equip.fault.reported} — 设备不可用</li>
 *   <li>{@code scm.receipt.delayed} — 物料延迟</li>
 * </ul>
 * <p>
 * 约束（design-decisions.md §4.11）：
 * <ul>
 *   <li>设备容量超限 → 硬约束（不可违反）</li>
 *   <li>人员资质不匹配 → 硬约束</li>
 *   <li>交期延误 → 软约束（可违反，有惩罚分）</li>
 * </ul>
 *
 * @author 苏政
 */
public interface ApsService {

    /**
     * 从 MPS 计划创建排程草稿。
     *
     * @param planNo      MPS 计划编号
     * @param factoryCode 工厂编码
     * @return 新创建的排程方案（状态：DRAFT）
     */
    Schedule createSchedule(String planNo, String factoryCode);

    /**
     * 按指定策略优化排程。
     * <p>
     * 支持的策略：
     * <ul>
     *   <li>{@code EDD} — 最早交期优先</li>
     *   <li>{@code SPT} — 最短加工时间优先</li>
     *   <li>{@code CR} — 关键比率优先</li>
     * </ul>
     *
     * @param scheduleCode 排程编号
     * @param strategy     排程策略（EDD/SPT/CR）
     * @return 优化后的排程方案（状态：OPTIMIZED）
     */
    Schedule optimize(String scheduleCode, String strategy);

    /**
     * 下发排程到 MES。
     * <p>
     * 下发后发布 {@code aps.schedule.released} 事件，
     * MES 订阅后根据 ScheduledTask 列表生成工单。
     *
     * @param scheduleCode 排程编号
     */
    void dispatch(String scheduleCode);

    /**
     * 事件驱动增量重排程。
     * <p>
     * 触发条件：新工单插入、设备故障、物料延迟等。
     * 仅影响受波及的任务，不做全量重新排程。
     *
     * @param scheduleCode 排程编号
     * @param triggerEvent 触发事件类型（如 "mes.workorder.released"）
     * @return 重排程后的排程方案
     */
    Schedule reschedule(String scheduleCode, String triggerEvent);

    /**
     * 查询排程方案。
     *
     * @param scheduleCode 排程编号
     * @return 排程方案（不存在时返回 null）
     */
    Schedule getSchedule(String scheduleCode);

}
