package com.byz.factory.eam.service;

import com.byz.factory.eam.CalibrationResult;
import com.byz.factory.eam.CalibrationType;
import com.byz.factory.eam.MaintenancePriority;
import com.byz.factory.eam.MaintenanceType;
import com.byz.factory.eam.model.Asset;
import com.byz.factory.eam.model.CalibrationRecord;
import com.byz.factory.eam.model.MaintenanceOrder;

import java.time.LocalDate;

/**
 * EAM 服务接口 — 资产全生命周期管理。
 * <p>
 * TODO 待实现：资产注册→运行→维护→校准→报废
 *
 * @author 苏政
 */
public interface EamService {

    // ==================== 资产生命周期 ====================

    /** 注册资产 */
    Asset register(Asset asset);

    /** 报废资产 */
    Asset scrap(String assetCode);

    /** 更新资产存放位置 */
    Asset updateLocation(String assetCode, String location);

    // ==================== 维护工单生命周期 ====================

    /** 创建维护工单 */
    MaintenanceOrder createMaintenanceOrder(String assetCode, MaintenanceType type,
                                            MaintenancePriority priority, String description);

    /** 开始维修 */
    MaintenanceOrder startMaintenance(String orderCode);

    /** 完成维修 */
    MaintenanceOrder completeMaintenance(String orderCode, double downtime, double cost,
                                         String technician);

    /** 验证维修结果 */
    MaintenanceOrder verifyMaintenance(String orderCode);

    /** 取消维护工单 */
    MaintenanceOrder cancelMaintenanceOrder(String orderCode);

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
