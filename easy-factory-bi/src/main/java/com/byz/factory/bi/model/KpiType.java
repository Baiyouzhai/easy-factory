package com.byz.factory.bi.model;

/**
 * KPI 指标类型 — 制造运营的关键绩效指标枚举。
 * <p>
 * 每个 KPI 类型定义了其计算公式、数据来源和目标值，
 * 供 DashboardService 在计算和查询时使用。
 * <p>
 * 指标定义依据 ISO 22400 制造 KPI 标准：
 * <ul>
 *   <li>{@link #PLAN_COMPLETION_RATE} — 计划完成率</li>
 *   <li>{@link #FIRST_PASS_RATE} — 一次合格率</li>
 *   <li>{@link #OEE} — 设备综合效率</li>
 *   <li>{@link #MTTR} — 平均维修时间</li>
 *   <li>{@link #MTBF} — 平均故障间隔</li>
 *   <li>{@link #BATCH_YIELD} — 批次收率</li>
 *   <li>{@link #INVENTORY_TURNOVER} — 库存周转率</li>
 *   <li>{@link #DEVIATION_CLOSURE_RATE} — 偏差关闭率</li>
 *   <li>{@link #CAPA_CLOSURE_RATE} — CAPA 关闭率</li>
 *   <li>{@link #ANDON_RESPONSE_TIME} — 安灯平均响应时间</li>
 * </ul>
 *
 * @author 苏政
 */
public enum KpiType {

    /** 计划完成率 = 实际产出 / 计划产出 × 100%，数据来源: MES + MPS，目标 ≥ 95% */
    PLAN_COMPLETION_RATE("计划完成率", "MES + MPS", "≥ 95%"),

    /** 一次合格率 = 一次合格数 / 总产出 × 100%，数据来源: QMS，目标 ≥ 98% */
    FIRST_PASS_RATE("一次合格率", "QMS", "≥ 98%"),

    /** OEE = 可用率 × 性能率 × 质量率，数据来源: Equip + IoT，目标 ≥ 85% */
    OEE("设备综合效率", "Equip + IoT", "≥ 85%"),

    /** 平均维修时间 = 总维修时间 / 维修次数，数据来源: EAM，目标 < 2h */
    MTTR("平均维修时间", "EAM", "< 2h"),

    /** 平均故障间隔 = 总运行时间 / 故障次数，数据来源: IoT + EAM，目标 > 500h */
    MTBF("平均故障间隔", "IoT + EAM", "> 500h"),

    /** 批次收率 = 实际产量 / 批量 × 100%，数据来源: LIMS，目标 ≥ 97% */
    BATCH_YIELD("批次收率", "LIMS", "≥ 97%"),

    /** 库存周转率 = 月出库量 / 平均库存，数据来源: WMS，目标 ≥ 10 */
    INVENTORY_TURNOVER("库存周转率", "WMS", "≥ 10"),

    /** 偏差关闭率 = 已关闭偏差 / 总偏差 × 100%，数据来源: QMS，目标 ≥ 90% */
    DEVIATION_CLOSURE_RATE("偏差关闭率", "QMS", "≥ 90%"),

    /** CAPA 关闭率 = 已验证CAPA / 总CAPA × 100%，数据来源: QMS，目标 ≥ 95% */
    CAPA_CLOSURE_RATE("CAPA关闭率", "QMS", "≥ 95%"),

    /** 安灯平均响应时间 = 总响应时间 / 呼叫次数，数据来源: Andon，目标 < 5min */
    ANDON_RESPONSE_TIME("安灯平均响应时间", "Andon", "< 5min");

    private final String displayName;
    private final String dataSource;
    private final String target;

    KpiType(String displayName, String dataSource, String target) {
        this.displayName = displayName;
        this.dataSource = dataSource;
        this.target = target;
    }

    public String getDisplayName() { return displayName; }
    public String getDataSource() { return dataSource; }
    public String getTarget() { return target; }
}
