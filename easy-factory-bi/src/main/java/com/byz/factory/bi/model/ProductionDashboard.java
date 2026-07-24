package com.byz.factory.bi.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 生产看板数据 — 制造执行实时概览的不可变值对象。
 * <p>
 * 聚合 MES 工单进度 + MPS 计划完成率，提供生产现场的宏观指标。
 * 刷新频率：操作层 5s（design-decisions.md §4.10）。
 * <p>
 * 数据来源：
 * <ul>
 *   <li>MES — 工单状态、工序进度、当日产出</li>
 *   <li>MPS — 计划数量（完成率分母）</li>
 * </ul>
 *
 * @param factoryCode           工厂编码
 * @param planCompletionRate    计划完成率（%）
 * @param wipCount              在制品批次数
 * @param dailyOutput           当日产出数量
 * @param totalWorkOrders       总工单数
 * @param completedWorkOrders   已完成工单数
 * @param inProgressWorkOrders  进行中工单数
 * @param interruptedWorkOrders 中断工单数
 * @param workOrderProgress     工单进度明细列表
 * @param refreshedAt           数据刷新时间
 *
 * @author 苏政
 */
public record ProductionDashboard(
        String factoryCode,
        BigDecimal planCompletionRate,
        int wipCount,
        BigDecimal dailyOutput,
        int totalWorkOrders,
        int completedWorkOrders,
        int inProgressWorkOrders,
        int interruptedWorkOrders,
        List<WorkOrderProgress> workOrderProgress,
        Instant refreshedAt
) {

    /**
     * 工单进度明细 — 单个工单的工序进度快照。
     *
     * @param workOrderNo      工单号
     * @param productName      产品名称
     * @param progressPercent  进度百分比（0-100）
     * @param currentProcess   当前工序名称
     * @param status           工单状态
     */
    public record WorkOrderProgress(
            String workOrderNo,
            String productName,
            int progressPercent,
            String currentProcess,
            String status
    ) {}

    /**
     * 创建空的看板数据。
     */
    public static ProductionDashboard empty(String factoryCode) {
        return new ProductionDashboard(
                factoryCode, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                0, 0, 0, 0, List.of(), Instant.now());
    }

    /**
     * 判断是否有中断工单需要关注。
     */
    public boolean hasInterruptedOrders() {
        return interruptedWorkOrders > 0;
    }

    /**
     * 获取工单完成率（0-1 之间）。
     */
    public double getCompletionRatio() {
        if (totalWorkOrders == 0) return 0.0;
        return (double) completedWorkOrders / totalWorkOrders;
    }

}
