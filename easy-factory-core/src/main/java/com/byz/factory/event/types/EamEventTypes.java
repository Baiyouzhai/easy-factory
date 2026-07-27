package com.byz.factory.event.types;

/**
 * EAM 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>Andon</b> — 订阅 {@link #MAINTENANCE_STARTED} / {@link #MAINTENANCE_COMPLETED}，
 *       跟踪设备维修进度，更新安灯面板</li>
 *   <li><b>DMS</b> — 订阅 {@link #CALIBRATION_RECORDED}，
 *       归档校准证书到文档系统</li>
 *   <li><b>MES</b> — 订阅 {@link #ASSET_SCRAPPED} / {@link #MAINTENANCE_STARTED}，
 *       设备不可用时调整工单排程</li>
 *   <li><b>ERP</b> — 订阅 {@link #ASSET_SCRAPPED}，
 *       同步资产报废到财务系统</li>
 * </ul>
 *
 * @author 苏政
 */
public final class EamEventTypes {

    private EamEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "eam";

    // ── 资产 ──

    /** 资产已报废 — 载荷: 资产编码 + 报废原因（MES 订阅调整排程，ERP 订阅同步财务） */
    public static final String ASSET_SCRAPPED = "eam.asset.scrapped";

    // ── 维护工单 ──

    /** 维护已开始 — 载荷: 工单号 + 资产编码 + 维护类型（Andon 订阅更新面板，MES 订阅调整排程） */
    public static final String MAINTENANCE_STARTED = "eam.maintenance.started";

    /** 维护已完成 — 载荷: 工单号 + 资产编码 + 停机时长 + 成本（Andon 订阅恢复面板） */
    public static final String MAINTENANCE_COMPLETED = "eam.maintenance.completed";

    /** 维护已验证 — 载荷: 工单号 + 资产编码 */
    public static final String MAINTENANCE_VERIFIED = "eam.maintenance.verified";

    /** 维护已取消 — 载荷: 工单号 + 资产编码 */
    public static final String MAINTENANCE_CANCELLED = "eam.maintenance.cancelled";

    // ── 校准 ──

    /** 校准已记录 — 载荷: 校准单号 + 资产编码 + 校准结果（DMS 订阅归档证书） */
    public static final String CALIBRATION_RECORDED = "eam.calibration.recorded";

}
