package com.byz.factory.web.controller.eam;

import com.byz.factory.eam.CalibrationResult;
import com.byz.factory.eam.CalibrationType;
import com.byz.factory.eam.MaintenancePriority;
import com.byz.factory.eam.MaintenanceType;
import com.byz.factory.eam.model.Asset;
import com.byz.factory.eam.model.CalibrationRecord;
import com.byz.factory.eam.model.MaintenanceOrder;
import com.byz.factory.eam.service.EamService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 资产管理 REST 控制器。
 * <p>
 * 提供资产注册、维护工单和校准记录管理 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/eam")
@Tag(name = "EAM — 资产管理", description = "资产、维护工单、校准")
public class EamController {

    @Autowired(required = false)
    private EamService eamService;

    // ==================== 资产生命周期 ====================

    @PostMapping("/assets")
    @Operation(summary = "注册资产")
    public Result<Asset> register(@RequestBody Asset asset) {
        return Result.ok(eamService.register(asset));
    }

    @PutMapping("/assets/{assetCode}/scrap")
    @Operation(summary = "报废资产")
    public Result<Asset> scrap(@PathVariable String assetCode) {
        return Result.ok(eamService.scrap(assetCode));
    }

    @PutMapping("/assets/{assetCode}/location")
    @Operation(summary = "更新资产存放位置")
    public Result<Asset> updateLocation(@PathVariable String assetCode,
                                         @RequestParam String location) {
        return Result.ok(eamService.updateLocation(assetCode, location));
    }

    // ==================== 维护工单生命周期 ====================

    @PostMapping("/maintenance-orders")
    @Operation(summary = "创建维护工单")
    public Result<MaintenanceOrder> createMaintenanceOrder(@RequestParam String assetCode,
                                                             @RequestParam MaintenanceType type,
                                                             @RequestParam MaintenancePriority priority,
                                                             @RequestParam String description) {
        return Result.ok(eamService.createMaintenanceOrder(assetCode, type, priority, description));
    }

    @PutMapping("/maintenance-orders/{orderCode}/start")
    @Operation(summary = "开始维修")
    public Result<MaintenanceOrder> startMaintenance(@PathVariable String orderCode) {
        return Result.ok(eamService.startMaintenance(orderCode));
    }

    @PutMapping("/maintenance-orders/{orderCode}/complete")
    @Operation(summary = "完成维修")
    public Result<MaintenanceOrder> completeMaintenance(@PathVariable String orderCode,
                                                          @RequestParam double downtime,
                                                          @RequestParam double cost,
                                                          @RequestParam String technician) {
        return Result.ok(eamService.completeMaintenance(orderCode, downtime, cost, technician));
    }

    @PutMapping("/maintenance-orders/{orderCode}/verify")
    @Operation(summary = "验证维修结果")
    public Result<MaintenanceOrder> verifyMaintenance(@PathVariable String orderCode) {
        return Result.ok(eamService.verifyMaintenance(orderCode));
    }

    @PutMapping("/maintenance-orders/{orderCode}/cancel")
    @Operation(summary = "取消维护工单")
    public Result<MaintenanceOrder> cancelMaintenanceOrder(@PathVariable String orderCode) {
        return Result.ok(eamService.cancelMaintenanceOrder(orderCode));
    }

    // ==================== 校准生命周期 ====================

    @PostMapping("/calibrations")
    @Operation(summary = "记录校准结果")
    public Result<CalibrationRecord> recordCalibration(@RequestParam String assetCode,
                                                         @RequestParam CalibrationType type,
                                                         @RequestParam String standard,
                                                         @RequestParam CalibrationResult result,
                                                         @RequestParam String calibratedBy,
                                                         @RequestParam LocalDate nextDue) {
        return Result.ok(eamService.recordCalibration(assetCode, type, standard, result, calibratedBy, nextDue));
    }

    @PostMapping("/calibrations/with-certificate")
    @Operation(summary = "记录校准结果（含证书）")
    public Result<CalibrationRecord> recordCalibrationWithCertificate(
            @RequestParam String assetCode,
            @RequestParam CalibrationType type,
            @RequestParam String standard,
            @RequestParam CalibrationResult result,
            @RequestParam String calibratedBy,
            @RequestParam LocalDate nextDue,
            @RequestParam String certificate) {
        return Result.ok(eamService.recordCalibrationWithCertificate(assetCode, type, standard, result, calibratedBy, nextDue, certificate));
    }

}
