package com.byz.factory.bi.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * OEE 看板数据 — 设备综合效率实时概览的不可变值对象。
 * <p>
 * 聚合 Equip 设备的可用率、性能率、质量率，计算 OEE = A × P × Q。
 * 遵循 ISO 22400 设备 KPI 标准。
 * 刷新频率：操作层 5s（design-decisions.md §4.10）。
 * <p>
 * 数据来源：
 * <ul>
 *   <li>Equip — 设备状态、OEE 指标</li>
 *   <li>IoT — 设备运行数据采集</li>
 * </ul>
 *
 * @param factoryCode      工厂编码
 * @param equipmentMetrics 设备 OEE 明细列表
 * @param avgAvailability  平均可用率（%）
 * @param avgPerformance   平均性能率（%）
 * @param avgQuality       平均质量率（%）
 * @param avgOee           平均 OEE（%）
 * @param worstEquipment   最差设备编码（OEE 最低）
 * @param bestEquipment    最佳设备编码（OEE 最高）
 * @param refreshedAt      数据刷新时间
 *
 * @author 苏政
 */
public record OeeDashboard(
        String factoryCode,
        List<EquipmentOee> equipmentMetrics,
        BigDecimal avgAvailability,
        BigDecimal avgPerformance,
        BigDecimal avgQuality,
        BigDecimal avgOee,
        String worstEquipment,
        String bestEquipment,
        Instant refreshedAt
) {

    /**
     * 设备 OEE 明细 — 单台设备的 OEE 指标快照。
     *
     * @param equipmentCode 设备编码
     * @param equipmentName 设备名称
     * @param availability  可用率（%, 0-100）
     * @param performance   性能率（%, 0-100）
     * @param quality       质量率（%, 0-100）
     * @param oee           OEE 综合效率（%, 0-100）
     * @param status        设备当前状态
     * @param trend         趋势方向（"up"提升 / "stable"稳定 / "down"下降）
     */
    public record EquipmentOee(
            String equipmentCode,
            String equipmentName,
            BigDecimal availability,
            BigDecimal performance,
            BigDecimal quality,
            BigDecimal oee,
            String status,
            String trend
    ) {}

    /**
     * 创建空的看板数据。
     */
    public static OeeDashboard empty(String factoryCode) {
        return new OeeDashboard(
                factoryCode, List.of(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, Instant.now());
    }

    /**
     * 判断是否有设备需要关注（OEE 低于阈值）。
     *
     * @param threshold OEE 阈值（如 60）
     */
    public boolean hasLowOeeEquipment(BigDecimal threshold) {
        return equipmentMetrics.stream()
                .anyMatch(e -> e.oee.compareTo(threshold) < 0);
    }

    /**
     * 获取设备数量。
     */
    public int getEquipmentCount() {
        return equipmentMetrics.size();
    }

}
