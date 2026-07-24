package com.byz.factory.andon.model;

import java.util.Map;

/**
 * 安灯看板数据 — 活跃呼叫统计的不可变值对象。
 * <p>
 * 提供按严重程度分组的活跃呼叫数量，供 BI 看板展示。
 *
 * @param totalCalls     活跃呼叫总数
 * @param bySeverity     按严重程度分组的数量（key=AndonSeverity.name()）
 * @param byTriggerType  按触发类型分组的数量（key=TriggerType.name()）
 * @param urgentCount    紧急呼叫数量（CRITICAL + EMERGENCY）
 * @param avgResponseMinutes 平均响应时间（分钟）
 *
 * @author 苏政
 */
public record AndonDashboard(
        int totalCalls,
        Map<String, Integer> bySeverity,
        Map<String, Integer> byTriggerType,
        int urgentCount,
        double avgResponseMinutes
) {

    /**
     * 创建空的看板数据。
     */
    public static AndonDashboard empty() {
        return new AndonDashboard(0, Map.of(), Map.of(), 0, 0.0);
    }

    /**
     * 判断是否有紧急呼叫需要关注。
     */
    public boolean hasUrgentCalls() {
        return urgentCount > 0;
    }

}
