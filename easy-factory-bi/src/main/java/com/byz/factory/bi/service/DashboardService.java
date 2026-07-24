package com.byz.factory.bi.service;

import com.byz.factory.bi.model.*;

import java.util.List;
import java.util.Map;

/**
 * 看板与报表服务接口 — BI 模块核心。
 * <p>
 * BI 是只读数据消费层——聚合所有业务模块（MES/QMS/Equip/LIMS/WMS/MPS/APS/Andon/EAM）
 * 的数据，生成制造运营的可视化看板和 KPI 报表。BI 不产生业务数据，只读取和聚合。
 * <p>
 * 刷新频率策略（design-decisions.md §4.10）：
 * <ul>
 *   <li>操作层（生产/OEE 看板）— 5s</li>
 *   <li>战术层（质量/仓储看板）— 30s</li>
 *   <li>战略层（KPI 报表）— 1h</li>
 * </ul>
 * <p>
 * <b>跨模块协作方向：</b>
 * <ul>
 *   <li>订阅 {@code mes.workorder.created/completed/closed} — 更新生产看板</li>
 *   <li>订阅 {@code mes.process.started/completed} — 更新工序进度</li>
 *   <li>订阅 {@code qms.inspection.completed/passed/failed} — 更新质量看板</li>
 *   <li>订阅 {@code qms.deviation.created/resolved} — 更新偏差统计</li>
 *   <li>订阅 {@code qms.capa.closed} — 更新 CAPA 关闭率</li>
 *   <li>订阅 {@code equip.oee.calculated} — 更新 OEE 看板</li>
 *   <li>订阅 {@code mps.plan.started/completed/closed} — 更新计划执行统计</li>
 *   <li>订阅 {@code aps.schedule.created} / {@code aps.task.delayed} — 更新排程看板</li>
 *   <li>订阅 {@code andon.call.created/resolved/closed} — 更新安灯看板</li>
 *   <li>订阅 {@code lims.batch_record.approved} — 更新批次收率</li>
 *   <li>订阅 {@code wms.inventory.changed} — 更新仓储看板</li>
 * </ul>
 *
 * @author 苏政
 */
public interface DashboardService {

    // ── 生产看板 ──

    /**
     * 获取生产看板数据（实时）。
     *
     * @param factoryCode 工厂编码
     * @return 生产看板数据
     */
    ProductionDashboard getProductionDashboard(String factoryCode);

    /**
     * 获取工单进度明细列表。
     *
     * @param factoryCode 工厂编码
     * @return 工单进度列表
     */
    List<ProductionDashboard.WorkOrderProgress> getWorkOrderProgressList(String factoryCode);

    /**
     * 获取工单进度（单个工单的完成百分比和当前工序）。
     *
     * @param workOrderNo 工单号
     * @return 进度信息 Map（progressPercent, currentProcess, status）
     */
    Map<String, Object> getWorkOrderProgress(String workOrderNo);

    // ── 质量看板 ──

    /**
     * 获取质量看板数据。
     *
     * @param factoryCode 工厂编码
     * @return 质量看板数据
     */
    QualityDashboard getQualityDashboard(String factoryCode);

    /**
     * 获取偏差统计数据。
     *
     * @param factoryCode 工厂编码
     * @return 偏差统计（openCount, totalCount, bySeverity, closureRate）
     */
    Map<String, Object> getDeviationStats(String factoryCode);

    /**
     * 获取最近偏差列表。
     *
     * @param factoryCode 工厂编码
     * @param limit       返回条数上限
     * @return 最近偏差摘要列表
     */
    List<QualityDashboard.DeviationSummary> getRecentDeviations(String factoryCode, int limit);

    // ── OEE 看板 ──

    /**
     * 获取 OEE 看板数据。
     *
     * @param factoryCode 工厂编码
     * @return OEE 看板数据
     */
    OeeDashboard getOeeDashboard(String factoryCode);

    /**
     * 获取单台设备 OEE 明细。
     *
     * @param equipmentCode 设备编码
     * @return 设备 OEE 明细，不存在返回 null
     */
    OeeDashboard.EquipmentOee getEquipmentOee(String equipmentCode);

    /**
     * 获取 OEE 低于阈值的设备列表。
     *
     * @param factoryCode 工厂编码
     * @param threshold   OEE 阈值（如 60）
     * @return 低 OEE 设备列表
     */
    List<OeeDashboard.EquipmentOee> getLowOeeEquipments(String factoryCode, java.math.BigDecimal threshold);

    // ── 仓储看板 ──

    /**
     * 获取仓储看板数据。
     *
     * @param factoryCode 工厂编码
     * @return 仓储看板数据
     */
    InventoryDashboard getInventoryDashboard(String factoryCode);

    /**
     * 获取呆滞物料列表。
     *
     * @param factoryCode    工厂编码
     * @param daysThreshold  呆滞天数阈值（如 90 天）
     * @return 呆滞物料列表
     */
    List<InventoryDashboard.SlowMovingItem> getSlowMovingItems(String factoryCode, int daysThreshold);

    /**
     * 获取近效期物料列表。
     *
     * @param factoryCode   工厂编码
     * @param daysThreshold 距效期天数阈值（如 30 天）
     * @return 近效期物料列表
     */
    List<InventoryDashboard.NearExpiryItem> getNearExpiryItems(String factoryCode, int daysThreshold);

    // ── KPI 管理 ──

    /**
     * 计算并保存月度/周期 KPI 快照。
     *
     * @param factoryCode 工厂编码
     * @param period      统计周期（如 "2026-07"）
     * @param computedBy  计算人
     * @return KPI 快照
     */
    KpiSnapshot computeKpi(String factoryCode, String period, String computedBy);

    /**
     * 获取指定周期的 KPI 快照。
     *
     * @param factoryCode 工厂编码
     * @param period      统计周期
     * @return KPI 快照，不存在返回 null
     */
    KpiSnapshot getKpiSnapshot(String factoryCode, String period);

    /**
     * 获取 KPI 历史趋势数据。
     *
     * @param factoryCode 工厂编码
     * @param kpiType     KPI 类型
     * @param period      统计周期粒度
     * @param count       返回最近 N 个周期的数据
     * @return KPI 历史列表（按周期升序）
     */
    List<KpiSnapshot> getKpiHistory(String factoryCode, KpiType kpiType, DashboardPeriod period, int count);

    /**
     * 获取最新 KPI 快照。
     *
     * @param factoryCode 工厂编码
     * @return 最新 KPI 快照，不存在返回 null
     */
    KpiSnapshot getLatestKpi(String factoryCode);

    // ── 批次追溯报告 ──

    /**
     * 生成批次追溯报告。
     * <p>
     * 聚合 MES 工序记录 + LIMS 称量记录 + QMS 检验记录 + 收率计算，
     * 生成 GMP 合规的批次追溯报告数据。
     *
     * @param batchNo 批次号
     * @return 批次报告数据（含工序链、称量明细、检验结果、收率）
     */
    Map<String, Object> getBatchReport(String batchNo);

}
