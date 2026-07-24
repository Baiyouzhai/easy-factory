package com.byz.factory.bi.model;

/**
 * 看板/KPI 统计周期枚举。
 * <p>
 * 不同周期的刷新频率策略（design-decisions.md §4.10）：
 * <ul>
 *   <li>{@link #REALTIME} — 操作层 5s 刷新</li>
 *   <li>{@link #HOURLY} — 班次内实时</li>
 *   <li>{@link #DAILY} — 战术层 30s 刷新</li>
 *   <li>{@link #WEEKLY} — 战术层汇总</li>
 *   <li>{@link #MONTHLY} — 战略层 1h 刷新</li>
 *   <li>{@link #QUARTERLY} — 战略层季度汇总</li>
 *   <li>{@link #YEARLY} — 年度汇总</li>
 * </ul>
 *
 * @author 苏政
 */
public enum DashboardPeriod {

    /** 实时（5s 刷新） */
    REALTIME("实时", 5),

    /** 小时 */
    HOURLY("小时", 60),

    /** 每日（30s 刷新） */
    DAILY("日", 30),

    /** 每周 */
    WEEKLY("周", 300),

    /** 每月（1h 刷新） */
    MONTHLY("月", 3600),

    /** 每季度 */
    QUARTERLY("季度", 3600),

    /** 每年 */
    YEARLY("年", 86400);

    private final String displayName;
    private final int refreshIntervalSeconds;

    DashboardPeriod(String displayName, int refreshIntervalSeconds) {
        this.displayName = displayName;
        this.refreshIntervalSeconds = refreshIntervalSeconds;
    }

    public String getDisplayName() { return displayName; }
    public int getRefreshIntervalSeconds() { return refreshIntervalSeconds; }
}
