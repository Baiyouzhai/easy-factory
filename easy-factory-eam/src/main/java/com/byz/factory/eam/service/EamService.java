package com.byz.factory.eam.service;

import com.byz.factory.eam.*;
import com.byz.factory.eam.model.Asset;
import com.byz.factory.eam.model.CalibrationRecord;
import com.byz.factory.eam.model.MaintenanceOrder;

import java.time.LocalDate;
import java.util.List;

/**
 * EAM 服务接口 — 资产全生命周期管理。
 * <p>
 * 职责：
 * <ul>
 *   <li>资产生命周期（注册→投产→停用→送修→修复→报废）</li>
 *   <li>维护工单生命周期（创建→派工→完工→验证→取消）</li>
 *   <li>校准管理（内部/外部校准记录）</li>
 *   <li>到期预警（质保到期、校准到期）</li>
 * </ul>
 * <p>
 * 跨模块协作：
 * <ul>
 *   <li>EAM ← Equip：设备运行状态 → 维护预警</li>
 *   <li>EAM → Equip：维护/校准 → 设备状态锁定</li>
 *   <li>EAM → MES：设备不可用 → 工单排程调整</li>
 *   <li>EAM → ERP：设备折旧 → 财务成本归集</li>
 *   <li>EAM → Andon：维护工单状态变更 → 安灯面板更新</li>
 *   <li>EAM → DMS：校准证书 → 文档归档</li>
 * </ul>
 *
 * @author 苏政
 */
public interface EamService {

    // ==================== 资产查询 ====================

    /** 按资产编码查询 */
    Asset findByAssetCode(String assetCode);

    /** 按类别查询 */
    List<Asset> findByCategory(String category);

    /** 查询质保即将到期的资产 */
    List<Asset> findAssetsWithExpiringWarranty(LocalDate before);

    // ==================== 资产生命周期 ====================

    /** 注册资产 */
    Asset register(Asset asset);

    /** 投产（IDLE → IN_USE） */
    Asset startUse(String assetCode);

    /** 停用（IN_USE → IDLE） */
    Asset stopUse(String assetCode);

    /** 送修（IDLE|IN_USE → UNDER_MAINTENANCE） */
    Asset sendToMaintenance(String assetCode);

    /** 修复完成（UNDER_MAINTENANCE → IDLE） */
    Asset completeMaintenance(String assetCode);

    /** 报废资产 */
    Asset scrap(String assetCode);

    /** 更新资产存放位置 */
    Asset updateLocation(String assetCode, String location);

    // ==================== 维护工单查询 ====================

    /** 按资产编码查询维护工单 */
    List<MaintenanceOrder> findMaintenanceOrdersByAsset(String assetCode);

    // ==================== 维护工单生命周期 ====================

    /** 创建维护工单 */
    MaintenanceOrder createMaintenanceOrder(String assetCode, MaintenanceType type,
                                            MaintenancePriority priority, String description);

    /** 开始维修（OPEN → IN_PROGRESS，自动记录实际开始时间） */
    MaintenanceOrder startMaintenance(String orderCode);

    /** 完成维修（IN_PROGRESS → COMPLETED，记录停机时长/成本/维修人） */
    MaintenanceOrder completeMaintenance(String orderCode, double downtime, double cost,
                                         String technician);

    /** 验证维修结果（COMPLETED → VERIFIED） */
    MaintenanceOrder verifyMaintenance(String orderCode);

    /** 取消维护工单（OPEN → CANCELLED） */
    MaintenanceOrder cancelMaintenanceOrder(String orderCode);

    // ==================== 校准查询 ====================

    /** 按资产编码查询校准记录 */
    List<CalibrationRecord> findCalibrationsByAsset(String assetCode);

    /** 查询即将到期的校准 */
    List<CalibrationRecord> findDueCalibrations(LocalDate before);

    // ==================== 校准生命周期 ====================

    /** 记录校准结果 */
    CalibrationRecord recordCalibration(String assetCode, CalibrationType type,
                                        String standard, CalibrationResult result,
                                        String calibratedBy, LocalDate nextDue);

    /** 记录校准结果（含证书） */
    CalibrationRecord recordCalibrationWithCertificate(String assetCode, CalibrationType type,
                                                       String standard, CalibrationResult result,
                                                       String calibratedBy, LocalDate nextDue,
                                                       String certificate);

}
