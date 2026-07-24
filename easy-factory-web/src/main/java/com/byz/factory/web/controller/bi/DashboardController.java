package com.byz.factory.web.controller.bi;

import com.byz.factory.bi.model.*;
import com.byz.factory.bi.service.DashboardService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 看板与报表 REST 控制器。
 * <p>
 * BI 是只读数据消费层——聚合所有业务模块的数据，生成制造运营的可视化看板和 KPI 报表。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/bi")
@Tag(name = "BI — 看板报表", description = "生产/质量/OEE/仓储看板、KPI 管理、批次追溯")
public class DashboardController {

    @Autowired(required = false)
    private DashboardService dashboardService;

    // ── 生产看板 ──

    @GetMapping("/production-dashboard")
    @Operation(summary = "获取生产看板数据（实时）")
    public Result<ProductionDashboard> getProductionDashboard(@RequestParam String factoryCode) {
        return Result.ok(dashboardService.getProductionDashboard(factoryCode));
    }

    @GetMapping("/work-order-progress")
    @Operation(summary = "获取工单进度明细列表")
    public Result<List<ProductionDashboard.WorkOrderProgress>> getWorkOrderProgressList(
            @RequestParam String factoryCode) {
        return Result.ok(dashboardService.getWorkOrderProgressList(factoryCode));
    }

    @GetMapping("/work-order-progress/{workOrderNo}")
    @Operation(summary = "获取单工单进度")
    public Result<Map<String, Object>> getWorkOrderProgress(@PathVariable String workOrderNo) {
        return Result.ok(dashboardService.getWorkOrderProgress(workOrderNo));
    }

    // ── 质量看板 ──

    @GetMapping("/quality-dashboard")
    @Operation(summary = "获取质量看板数据")
    public Result<QualityDashboard> getQualityDashboard(@RequestParam String factoryCode) {
        return Result.ok(dashboardService.getQualityDashboard(factoryCode));
    }

    @GetMapping("/deviation-stats")
    @Operation(summary = "获取偏差统计数据")
    public Result<Map<String, Object>> getDeviationStats(@RequestParam String factoryCode) {
        return Result.ok(dashboardService.getDeviationStats(factoryCode));
    }

    @GetMapping("/recent-deviations")
    @Operation(summary = "获取最近偏差列表")
    public Result<List<QualityDashboard.DeviationSummary>> getRecentDeviations(
            @RequestParam String factoryCode,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(dashboardService.getRecentDeviations(factoryCode, limit));
    }

    // ── OEE 看板 ──

    @GetMapping("/oee-dashboard")
    @Operation(summary = "获取 OEE 看板数据")
    public Result<OeeDashboard> getOeeDashboard(@RequestParam String factoryCode) {
        return Result.ok(dashboardService.getOeeDashboard(factoryCode));
    }

    @GetMapping("/equipment-oee/{equipmentCode}")
    @Operation(summary = "获取单台设备 OEE 明细")
    public Result<OeeDashboard.EquipmentOee> getEquipmentOee(@PathVariable String equipmentCode) {
        return Result.ok(dashboardService.getEquipmentOee(equipmentCode));
    }

    @GetMapping("/low-oee-equipments")
    @Operation(summary = "获取 OEE 低于阈值的设备列表")
    public Result<List<OeeDashboard.EquipmentOee>> getLowOeeEquipments(
            @RequestParam String factoryCode,
            @RequestParam BigDecimal threshold) {
        return Result.ok(dashboardService.getLowOeeEquipments(factoryCode, threshold));
    }

    // ── 仓储看板 ──

    @GetMapping("/inventory-dashboard")
    @Operation(summary = "获取仓储看板数据")
    public Result<InventoryDashboard> getInventoryDashboard(@RequestParam String factoryCode) {
        return Result.ok(dashboardService.getInventoryDashboard(factoryCode));
    }

    @GetMapping("/slow-moving-items")
    @Operation(summary = "获取呆滞物料列表")
    public Result<List<InventoryDashboard.SlowMovingItem>> getSlowMovingItems(
            @RequestParam String factoryCode,
            @RequestParam(defaultValue = "90") int daysThreshold) {
        return Result.ok(dashboardService.getSlowMovingItems(factoryCode, daysThreshold));
    }

    @GetMapping("/near-expiry-items")
    @Operation(summary = "获取近效期物料列表")
    public Result<List<InventoryDashboard.NearExpiryItem>> getNearExpiryItems(
            @RequestParam String factoryCode,
            @RequestParam(defaultValue = "30") int daysThreshold) {
        return Result.ok(dashboardService.getNearExpiryItems(factoryCode, daysThreshold));
    }

    // ── KPI 管理 ──

    @PostMapping("/kpi/compute")
    @Operation(summary = "计算并保存月度/周期 KPI 快照")
    public Result<KpiSnapshot> computeKpi(@RequestParam String factoryCode,
                                           @RequestParam String period,
                                           @RequestParam String computedBy) {
        return Result.ok(dashboardService.computeKpi(factoryCode, period, computedBy));
    }

    @GetMapping("/kpi/snapshot")
    @Operation(summary = "获取指定周期的 KPI 快照")
    public Result<KpiSnapshot> getKpiSnapshot(@RequestParam String factoryCode,
                                               @RequestParam String period) {
        return Result.ok(dashboardService.getKpiSnapshot(factoryCode, period));
    }

    @GetMapping("/kpi/history")
    @Operation(summary = "获取 KPI 历史趋势数据")
    public Result<List<KpiSnapshot>> getKpiHistory(@RequestParam String factoryCode,
                                                     @RequestParam KpiType kpiType,
                                                     @RequestParam DashboardPeriod period,
                                                     @RequestParam(defaultValue = "12") int count) {
        return Result.ok(dashboardService.getKpiHistory(factoryCode, kpiType, period, count));
    }

    @GetMapping("/kpi/latest")
    @Operation(summary = "获取最新 KPI 快照")
    public Result<KpiSnapshot> getLatestKpi(@RequestParam String factoryCode) {
        return Result.ok(dashboardService.getLatestKpi(factoryCode));
    }

    // ── 批次追溯报告 ──

    @GetMapping("/batch-report/{batchNo}")
    @Operation(summary = "生成批次追溯报告", description = "聚合 MES 工序 + LIMS 称量 + QMS 检验 + 收率")
    public Result<Map<String, Object>> getBatchReport(@PathVariable String batchNo) {
        return Result.ok(dashboardService.getBatchReport(batchNo));
    }

}
