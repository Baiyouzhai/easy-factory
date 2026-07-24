package com.byz.factory.event.types;

/**
 * BI 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * BI 是只读数据消费层——订阅所有模块的领域事件，聚合生成 KPI 和看板数据。
 * BI 自身发布的事件主要为看板刷新和 KPI 更新通知，供 Web 模块 SSE 推送使用。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>Web</b> — 订阅 {@link #KPI_UPDATED} / {@link #DASHBOARD_REFRESHED}，
 *       通过 SSE 推送到前端看板</li>
 * </ul>
 * <p>
 * <b>BI 订阅的外部事件（在 Service 实现层订阅）：</b>
 * <ul>
 *   <li><b>MES</b> — mes.workorder.created/completed/closed, mes.process.started/completed, mes.action.completed</li>
 *   <li><b>QMS</b> — qms.inspection.completed/passed/failed, qms.deviation.created/resolved, qms.capa.closed</li>
 *   <li><b>Equip</b> — equip.oee.calculated, equip.status.changed</li>
 *   <li><b>MPS</b> — mps.plan.started/completed/closed</li>
 *   <li><b>APS</b> — aps.schedule.created, aps.task.delayed</li>
 *   <li><b>Andon</b> — andon.call.created/resolved/closed</li>
 *   <li><b>LIMS</b> — lims.batch_record.approved, lims.weighing.completed, lims.batch_record.archived</li>
 *   <li><b>WMS</b> — wms.inventory.changed</li>
 *   <li><b>EAM</b> — 维护工单完成/关闭事件</li>
 * </ul>
 *
 * @author 苏政
 */
public final class BiEventTypes {

    private BiEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "bi";

    // ── KPI 管理 ──

    /** KPI 快照已更新 — 载荷: period + kpiType + value + factoryCode */
    public static final String KPI_UPDATED = "bi.kpi.updated";

    /** KPI 快照已计算 — 载荷: period + factoryCode + snapshotCode */
    public static final String KPI_COMPUTED = "bi.kpi.computed";

    // ── 看板刷新 ──

    /** 生产看板已刷新 — 载荷: factoryCode + refreshedAt */
    public static final String PRODUCTION_DASHBOARD_REFRESHED = "bi.dashboard.production_refreshed";

    /** 质量看板已刷新 — 载荷: factoryCode + refreshedAt */
    public static final String QUALITY_DASHBOARD_REFRESHED = "bi.dashboard.quality_refreshed";

    /** OEE 看板已刷新 — 载荷: factoryCode + refreshedAt */
    public static final String OEE_DASHBOARD_REFRESHED = "bi.dashboard.oee_refreshed";

    /** 仓储看板已刷新 — 载荷: factoryCode + refreshedAt */
    public static final String INVENTORY_DASHBOARD_REFRESHED = "bi.dashboard.inventory_refreshed";

}
