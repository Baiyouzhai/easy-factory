package com.byz.factory.bi.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 仓储看板数据 — 库存运营实时概览的不可变值对象。
 * <p>
 * 聚合 WMS 库存数据，提供库存健康度、周转效率和风险预警。
 * 刷新频率：战术层 30s（design-decisions.md §4.10）。
 * <p>
 * 数据来源：
 * <ul>
 *   <li>WMS — 库存快照、出入库记录</li>
 *   <li>ERP — 库存金额（如有对接）</li>
 * </ul>
 *
 * @param factoryCode             工厂编码
 * @param turnoverRate            库存周转率（次/月）
 * @param slowMovingCount         呆滞物料数（> 90 天未动）
 * @param pendingInspectionCount  待检库存批次数
 * @param nearExpiryCount         近效期物料数（< 30 天过效期）
 * @param onHandTotal             在手库存总量
 * @param allocatedTotal          已分配库存总量
 * @param availableTotal          可用库存总量
 * @param quarantineTotal         待检/隔离库存总量
 * @param totalStockValue         总库存金额（元）
 * @param slowMovingDetails       呆滞物料明细
 * @param nearExpiryDetails       近效期物料明细
 * @param refreshedAt             数据刷新时间
 *
 * @author 苏政
 */
public record InventoryDashboard(
        String factoryCode,
        BigDecimal turnoverRate,
        int slowMovingCount,
        int pendingInspectionCount,
        int nearExpiryCount,
        BigDecimal onHandTotal,
        BigDecimal allocatedTotal,
        BigDecimal availableTotal,
        BigDecimal quarantineTotal,
        BigDecimal totalStockValue,
        List<SlowMovingItem> slowMovingDetails,
        List<NearExpiryItem> nearExpiryDetails,
        Instant refreshedAt
) {

    /**
     * 呆滞物料明细。
     *
     * @param materialCode    物料编码
     * @param materialName    物料名称
     * @param daysSinceLastMove 距上次移动天数
     * @param onHandQty       在手数量
     * @param locationCode    库位编码
     */
    public record SlowMovingItem(
            String materialCode,
            String materialName,
            int daysSinceLastMove,
            BigDecimal onHandQty,
            String locationCode
    ) {}

    /**
     * 近效期物料明细。
     *
     * @param materialCode  物料编码
     * @param materialName  物料名称
     * @param batchNo       批次号
     * @param expiryDate    效期日期
     * @param daysToExpiry  距效期天数
     * @param onHandQty     在手数量
     */
    public record NearExpiryItem(
            String materialCode,
            String materialName,
            String batchNo,
            String expiryDate,
            int daysToExpiry,
            BigDecimal onHandQty
    ) {}

    /**
     * 创建空的看板数据。
     */
    public static InventoryDashboard empty(String factoryCode) {
        return new InventoryDashboard(
                factoryCode, BigDecimal.ZERO, 0, 0, 0,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, List.of(), List.of(), Instant.now());
    }

    /**
     * 判断是否有呆滞物料需要关注。
     */
    public boolean hasSlowMovingItems() {
        return slowMovingCount > 0;
    }

    /**
     * 判断是否有近效期物料需要关注。
     */
    public boolean hasNearExpiryItems() {
        return nearExpiryCount > 0;
    }

    /**
     * 计算可用率（可用库存 / 在手库存，无库存时返回 0）。
     */
    public BigDecimal getAvailabilityRate() {
        if (onHandTotal.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return availableTotal.divide(onHandTotal, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

}
