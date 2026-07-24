package com.byz.factory.bi.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 质量看板数据 — 质量管理实时概览的不可变值对象。
 * <p>
 * 聚合 QMS 检验数据 + 偏差/CAPA 统计，提供质量态势感知。
 * 刷新频率：战术层 30s（design-decisions.md §4.10）。
 * <p>
 * 数据来源：
 * <ul>
 *   <li>QMS — 检验结果、偏差状态、CAPA 状态</li>
 * </ul>
 *
 * @param factoryCode         工厂编码
 * @param firstPassRate       一次合格率（%）
 * @param openDeviationCount  未关闭偏差数
 * @param capaClosureRate     CAPA 关闭率（%）
 * @param totalInspections    总检验批次
 * @param passedInspections   通过批次
 * @param failedInspections   失败批次
 * @param deviationClosureRate 偏差关闭率（%）
 * @param recentDeviations    最近偏差列表（最多 5 条）
 * @param bySeverity          按严重程度分组的偏差数
 * @param refreshedAt         数据刷新时间
 *
 * @author 苏政
 */
public record QualityDashboard(
        String factoryCode,
        BigDecimal firstPassRate,
        int openDeviationCount,
        BigDecimal capaClosureRate,
        int totalInspections,
        int passedInspections,
        int failedInspections,
        BigDecimal deviationClosureRate,
        List<DeviationSummary> recentDeviations,
        Map<String, Integer> bySeverity,
        Instant refreshedAt
) {

    /**
     * 偏差摘要 — 最近偏差的简要视图。
     *
     * @param deviationCode 偏差编号
     * @param description   偏差描述
     * @param severity      严重程度
     * @param status        当前状态
     * @param createdAt     创建时间
     */
    public record DeviationSummary(
            String deviationCode,
            String description,
            String severity,
            String status,
            String createdAt
    ) {}

    /**
     * 创建空的看板数据。
     */
    public static QualityDashboard empty(String factoryCode) {
        return new QualityDashboard(
                factoryCode, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                0, 0, 0, BigDecimal.ZERO,
                List.of(), Map.of(), Instant.now());
    }

    /**
     * 判断是否有未关闭的偏差需要关注。
     */
    public boolean hasOpenDeviations() {
        return openDeviationCount > 0;
    }

    /**
     * 获取检验通过率（0-1 之间）。
     */
    public double getPassRatio() {
        if (totalInspections == 0) return 0.0;
        return (double) passedInspections / totalInspections;
    }

}
