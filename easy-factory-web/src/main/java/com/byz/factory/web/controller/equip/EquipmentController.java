package com.byz.factory.web.controller.equip;

import com.byz.factory.equip.MachineStatus;
import com.byz.factory.equip.model.Equipment;
import com.byz.factory.equip.model.EquipmentParameter;
import com.byz.factory.equip.model.EquipmentRecipe;
import com.byz.factory.equip.model.OEMetrics;
import com.byz.factory.equip.service.EquipmentService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 设备管理 REST 控制器。
 * <p>
 * 提供设备台账、状态管理、设备配方、工艺参数和 OEE 效率指标的 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/equip/equipments")
@Tag(name = "Equip — 设备管理", description = "设备台账、状态、配方、参数、OEE")
public class EquipmentController {

    @Autowired(required = false)
    private EquipmentService equipmentService;

    // ==================== 设备台账 ====================

    @PostMapping
    @Operation(summary = "注册新设备")
    public Result<Equipment> registerEquipment(@RequestParam String code,
                                                @RequestParam String name,
                                                @RequestParam String model) {
        return Result.ok(equipmentService.registerEquipment(code, name, model));
    }

    @GetMapping("/{code}")
    @Operation(summary = "查询设备")
    public Result<Equipment> findEquipment(@PathVariable String code) {
        return Result.ok(equipmentService.findEquipment(code));
    }

    @GetMapping
    @Operation(summary = "获取所有设备")
    public Result<List<Equipment>> getAllEquipments() {
        return Result.ok(equipmentService.getAllEquipments());
    }

    // ==================== 设备状态管理 ====================

    @GetMapping("/{equipmentCode}/status")
    @Operation(summary = "查询设备状态")
    public Result<MachineStatus> getStatus(@PathVariable String equipmentCode) {
        return Result.ok(equipmentService.getStatus(equipmentCode));
    }

    @PutMapping("/{equipmentCode}/start-production")
    @Operation(summary = "开始生产", description = "IDLE/SETUP → RUNNING")
    public Result<Void> startProduction(@PathVariable String equipmentCode) {
        equipmentService.startProduction(equipmentCode);
        return Result.ok();
    }

    @PutMapping("/{equipmentCode}/stop-production")
    @Operation(summary = "停止生产", description = "RUNNING → IDLE")
    public Result<Void> stopProduction(@PathVariable String equipmentCode) {
        equipmentService.stopProduction(equipmentCode);
        return Result.ok();
    }

    @PutMapping("/{equipmentCode}/report-fault")
    @Operation(summary = "报告故障", description = "RUNNING → FAULT")
    public Result<Void> reportFault(@PathVariable String equipmentCode,
                                     @RequestParam String reason) {
        equipmentService.reportFault(equipmentCode, reason);
        return Result.ok();
    }

    @PutMapping("/{equipmentCode}/start-maintenance")
    @Operation(summary = "开始维护", description = "IDLE/FAULT → MAINTENANCE")
    public Result<Void> startMaintenance(@PathVariable String equipmentCode) {
        equipmentService.startMaintenance(equipmentCode);
        return Result.ok();
    }

    @PutMapping("/{equipmentCode}/complete-maintenance")
    @Operation(summary = "完成维护", description = "MAINTENANCE → IDLE")
    public Result<Void> completeMaintenance(@PathVariable String equipmentCode) {
        equipmentService.completeMaintenance(equipmentCode);
        return Result.ok();
    }

    // ==================== 配方管理 ====================

    @PostMapping("/recipes")
    @Operation(summary = "创建设备配方")
    public Result<EquipmentRecipe> createRecipe(@RequestParam String code,
                                                  @RequestParam String name,
                                                  @RequestParam String equipmentCode,
                                                  @RequestParam String productCode) {
        return Result.ok(equipmentService.createRecipe(code, name, equipmentCode, productCode));
    }

    @GetMapping("/recipes/{code}")
    @Operation(summary = "查询配方")
    public Result<EquipmentRecipe> findRecipe(@PathVariable String code) {
        return Result.ok(equipmentService.findRecipe(code));
    }

    @GetMapping("/{equipmentCode}/recipes")
    @Operation(summary = "获取设备的所有配方")
    public Result<List<EquipmentRecipe>> getRecipesByEquipment(@PathVariable String equipmentCode) {
        return Result.ok(equipmentService.getRecipesByEquipment(equipmentCode));
    }

    @PutMapping("/recipes/{recipeCode}/dispatch")
    @Operation(summary = "下发配方到设备", description = "触发 IoT 参数下发")
    public Result<Void> dispatchRecipe(@PathVariable String recipeCode) {
        equipmentService.dispatchRecipe(recipeCode);
        return Result.ok();
    }

    // ==================== 参数管理 ====================

    @PutMapping("/{equipmentCode}/parameters/{paramCode}")
    @Operation(summary = "更新参数设定值")
    public Result<Void> updateParameter(@PathVariable String equipmentCode,
                                         @PathVariable String paramCode,
                                         @RequestParam BigDecimal setValue) {
        equipmentService.updateParameter(equipmentCode, paramCode, setValue);
        return Result.ok();
    }

    @GetMapping("/{equipmentCode}/parameters/{paramCode}")
    @Operation(summary = "查询参数")
    public Result<EquipmentParameter> getParameter(@PathVariable String equipmentCode,
                                                      @PathVariable String paramCode) {
        return Result.ok(equipmentService.getParameter(equipmentCode, paramCode));
    }

    @GetMapping("/{equipmentCode}/parameters")
    @Operation(summary = "获取设备的所有参数")
    public Result<List<EquipmentParameter>> getParametersByEquipment(@PathVariable String equipmentCode) {
        return Result.ok(equipmentService.getParametersByEquipment(equipmentCode));
    }

    @PutMapping("/{equipmentCode}/parameters/{paramCode}/actual")
    @Operation(summary = "回传参数实际值", description = "从 IoT 接收实际值")
    public Result<Void> reportActualValue(@PathVariable String equipmentCode,
                                           @PathVariable String paramCode,
                                           @RequestParam BigDecimal actualValue) {
        equipmentService.reportActualValue(equipmentCode, paramCode, actualValue);
        return Result.ok();
    }

    // ==================== OEE ====================

    @PostMapping("/{equipmentCode}/oee/calculate")
    @Operation(summary = "计算 OEE")
    public Result<OEMetrics> calculateOee(@PathVariable String equipmentCode,
                                           @RequestParam LocalDate periodStart,
                                           @RequestParam LocalDate periodEnd,
                                           @RequestParam BigDecimal availability,
                                           @RequestParam BigDecimal performance,
                                           @RequestParam BigDecimal quality) {
        return Result.ok(equipmentService.calculateOee(equipmentCode, periodStart, periodEnd,
                availability, performance, quality));
    }

    @GetMapping("/{equipmentCode}/oee/latest")
    @Operation(summary = "查询最近一次 OEE")
    public Result<OEMetrics> getLatestOee(@PathVariable String equipmentCode) {
        return Result.ok(equipmentService.getLatestOee(equipmentCode));
    }

    @GetMapping("/{equipmentCode}/oee/history")
    @Operation(summary = "查询设备 OEE 历史")
    public Result<List<OEMetrics>> getOeeHistory(@PathVariable String equipmentCode,
                                                   @RequestParam LocalDate start,
                                                   @RequestParam LocalDate end) {
        return Result.ok(equipmentService.getOeeHistory(equipmentCode, start, end));
    }

}
