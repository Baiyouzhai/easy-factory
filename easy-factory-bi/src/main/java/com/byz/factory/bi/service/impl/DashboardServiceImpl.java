package com.byz.factory.bi.service.impl;

import com.byz.factory.bi.model.*;
import com.byz.factory.bi.repository.KpiSnapshotRepository;
import com.byz.factory.bi.service.DashboardService;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.BiEventTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * DashboardService 实现 — BI 模块核心业务逻辑。
 * <p>
 * BI 是只读数据消费层——KPI 快照是唯一的持久化实体，
 * 看板数据（Production/Quality/OEE/Inventory Dashboard）为实时计算的值对象。
 * <p>
 * 当前阶段（Phase 4）实现 KPI 快照的 CRUD 管理；
 * 看板聚合查询和跨模块事件订阅在 Phase 5 实现。
 *
 * @author 苏政
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final KpiSnapshotRepository kpiRepo;

    public DashboardServiceImpl(KpiSnapshotRepository kpiRepo) {
        this.kpiRepo = kpiRepo;
    }

    // ==================== 生产看板 ====================

    @Override
    @Transactional(readOnly = true)
    public ProductionDashboard getProductionDashboard(String factoryCode) {
        // Phase 5: 聚合 MES 工单数据 + MPS 计划数据
        return ProductionDashboard.empty(factoryCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionDashboard.WorkOrderProgress> getWorkOrderProgressList(String factoryCode) {
        // Phase 5: 查询 MES 工单进度
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getWorkOrderProgress(String workOrderNo) {
        // Phase 5: 查询单个工单进度
        return Map.of();
    }

    // ==================== 质量看板 ====================

    @Override
    @Transactional(readOnly = true)
    public QualityDashboard getQualityDashboard(String factoryCode) {
        // Phase 5: 聚合 QMS 检验数据 + 偏差统计
        return QualityDashboard.empty(factoryCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDeviationStats(String factoryCode) {
        // Phase 5: 查询 QMS 偏差统计
        return Map.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityDashboard.DeviationSummary> getRecentDeviations(String factoryCode, int limit) {
        // Phase 5: 查询 QMS 最近偏差
        return List.of();
    }

    // ==================== OEE 看板 ====================

    @Override
    @Transactional(readOnly = true)
    public OeeDashboard getOeeDashboard(String factoryCode) {
        // Phase 5: 聚合 Equip OEE 数据
        return OeeDashboard.empty(factoryCode);
    }

    @Override
    @Transactional(readOnly = true)
    public OeeDashboard.EquipmentOee getEquipmentOee(String equipmentCode) {
        // Phase 5: 查询单台设备 OEE
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OeeDashboard.EquipmentOee> getLowOeeEquipments(String factoryCode, BigDecimal threshold) {
        // Phase 5: 筛选低 OEE 设备
        return List.of();
    }

    // ==================== 仓储看板 ====================

    @Override
    @Transactional(readOnly = true)
    public InventoryDashboard getInventoryDashboard(String factoryCode) {
        // Phase 5: 聚合 WMS 库存数据
        return InventoryDashboard.empty(factoryCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDashboard.SlowMovingItem> getSlowMovingItems(String factoryCode, int daysThreshold) {
        // Phase 5: 查询 WMS 呆滞物料
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDashboard.NearExpiryItem> getNearExpiryItems(String factoryCode, int daysThreshold) {
        // Phase 5: 查询 WMS 近效期物料
        return List.of();
    }

    // ==================== KPI 管理 ====================

    @Override
    @Transactional
    public KpiSnapshot computeKpi(String factoryCode, String period, String computedBy) {
        // 如果已存在同工厂同周期的快照，先删除再重建（覆盖重新计算）
        KpiSnapshot existing = kpiRepo.findByFactoryCodeAndPeriod(factoryCode, period);
        if (existing != null) {
            kpiRepo.delete(existing);
            kpiRepo.flush(); // 确保删除已提交，避免 code 唯一约束冲突
        }

        String code = "KPI-" + period + "-" + factoryCode;
        KpiSnapshot snapshot = new KpiSnapshot(code, period, factoryCode);

        // Phase 5: 从各模块聚合实时 KPI 数据
        // 当前阶段创建空快照，后续通过 setKpiValue() 填充
        snapshot.markComputed(computedBy);
        KpiSnapshot saved = kpiRepo.save(snapshot);

        publish(BiEventTypes.KPI_COMPUTED, Map.of(
                "snapshotCode", saved.getCode(),
                "factoryCode", factoryCode,
                "period", period,
                "computedBy", computedBy,
                "computedAt", saved.getComputedAt()));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public KpiSnapshot getKpiSnapshot(String factoryCode, String period) {
        return kpiRepo.findByFactoryCodeAndPeriod(factoryCode, period);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiSnapshot> getKpiHistory(String factoryCode, KpiType kpiType,
                                            DashboardPeriod period, int count) {
        List<KpiSnapshot> snapshots = kpiRepo.findLatestByFactoryCode(factoryCode, count);
        // 返回所有快照，由调用方按 KpiType 过滤对应的值
        // periods 按升序返回（数据库查询已按 cycle DESC，这里反转）
        List<KpiSnapshot> ascending = new ArrayList<>(snapshots);
        java.util.Collections.reverse(ascending);
        return ascending;
    }

    @Override
    @Transactional(readOnly = true)
    public KpiSnapshot getLatestKpi(String factoryCode) {
        return kpiRepo.findTopByFactoryCodeOrderByPeriodDesc(factoryCode).orElse(null);
    }

    // ==================== 批次追溯报告 ====================

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBatchReport(String batchNo) {
        // Phase 5: 聚合 MES 工序记录 + LIMS 称量记录 + QMS 检验记录 + 收率计算
        return Map.of("batchNo", batchNo, "status", "NOT_IMPLEMENTED",
                "message", "批次追溯报告将在 Phase 5 跨模块集成时实现");
    }

    // ==================== 内部辅助 ====================

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, BiEventTypes.PREFIX, payload));
    }

}
