package com.byz.factory.aps.service;

import com.byz.factory.aps.model.Schedule.ScheduledTask;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * 排程规则引擎 — 实现 EDD/SPT/CR 三种经典排程优先级规则。
 * <p>
 * 每种规则返回一个 Comparator，用于对 ScheduledTask 列表排序。
 * 规则可组合（先按优先级分组 → 组内按 EDD 排序）。
 * <p>
 * 设计裁定（design-decisions.md §2.5）：
 * Phase 3 为规则式，预留 IOptimizationEngine 接口供后续接入 OR-Tools / OptaPlanner。
 *
 * @author 苏政
 */
public final class SchedulingRule {

    private SchedulingRule() { /* 工具类 */ }

    /**
     * EDD (Earliest Due Date) — 交期优先。
     * 按 endTime 升序排列，最早交期的任务排最前面。
     */
    public static final Comparator<ScheduledTask> EDD = Comparator.comparing(
            ScheduledTask::getEndTime,
            Comparator.nullsLast(Comparator.naturalOrder()));

    /**
     * SPT (Shortest Processing Time) — 最小工时优先。
     * 按总耗时（setupTimeMin + processTimeMin）升序排列，工时最短的任务排最前面。
     */
    public static final Comparator<ScheduledTask> SPT = Comparator.comparingInt(
            ScheduledTask::getTotalDurationMin);

    /**
     * CR (Critical Ratio) — 关键比率优先。
     * CR = (endTime - now) / totalDurationMin，CR 越小越紧迫，排最前面。
     * <p>
     * CR &lt; 1 表示任务已经延误（剩余时间不足以完成加工）。
     * CR = 1 表示刚好完成。
     * CR &gt; 1 表示有缓冲。
     *
     * @param now 当前时刻（排程计算的参考点）
     */
    public static Comparator<ScheduledTask> criticalRatio(Instant now) {
        return Comparator.comparingDouble((ScheduledTask t) -> {
            if (t.getEndTime() == null || t.getTotalDurationMin() == 0) {
                return Double.MAX_VALUE; // 无截止日或无工时的任务排最后
            }
            double remainingMinutes = (double) (t.getEndTime().toEpochMilli() - now.toEpochMilli()) / 60_000.0;
            return remainingMinutes / t.getTotalDurationMin();
        });
    }

    /**
     * 按指定策略名称获取对应的 Comparator。
     *
     * @param strategy 策略名称（EDD/SPT/CR）
     * @param now      当前时刻（仅 CR 策略需要）
     * @return 对应的 Comparator
     * @throws IllegalArgumentException 如果策略名称未知
     */
    public static Comparator<ScheduledTask> of(String strategy, Instant now) {
        return switch (strategy.toUpperCase()) {
            case "EDD" -> EDD;
            case "SPT" -> SPT;
            case "CR" -> criticalRatio(now);
            default -> throw new IllegalArgumentException("未知排程策略: " + strategy);
        };
    }

    /**
     * 按指定策略对任务列表排序（就地排序）。
     *
     * @param tasks    任务列表
     * @param strategy 策略名称（EDD/SPT/CR）
     * @param now      当前时刻（仅 CR 策略需要）
     */
    public static void sort(List<ScheduledTask> tasks, String strategy, Instant now) {
        if (tasks == null || tasks.isEmpty()) return;
        tasks.sort(of(strategy, now));
    }

}
