package com.byz.factory.equip.service.impl;

import com.byz.factory.equip.MachineStatus;
import com.byz.factory.equip.model.Equipment;
import com.byz.factory.equip.model.EquipmentParameter;
import com.byz.factory.equip.model.EquipmentRecipe;
import com.byz.factory.equip.model.OEMetrics;
import com.byz.factory.equip.repository.EquipmentParameterRepository;
import com.byz.factory.equip.repository.EquipmentRecipeRepository;
import com.byz.factory.equip.repository.EquipmentRepository;
import com.byz.factory.equip.repository.OEMetricsRepository;
import com.byz.factory.equip.service.EquipmentService;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.EquipEventTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 设备服务实现 — 基于本地 Repository 的设备管理。
 * <p>
 * 约定：
 * <ul>
 *   <li>写操作 @Transactional + publish 领域事件</li>
 *   <li>状态变更后 publish 对应事件常量（EquipEventTypes）</li>
 *   <li>读操作 @Transactional(readOnly = true)</li>
 * </ul>
 *
 * @author easy-factory
 */
@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepo;
    private final EquipmentParameterRepository parameterRepo;
    private final EquipmentRecipeRepository recipeRepo;
    private final OEMetricsRepository oeeRepo;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepo,
                                 EquipmentParameterRepository parameterRepo,
                                 EquipmentRecipeRepository recipeRepo,
                                 OEMetricsRepository oeeRepo) {
        this.equipmentRepo = equipmentRepo;
        this.parameterRepo = parameterRepo;
        this.recipeRepo = recipeRepo;
        this.oeeRepo = oeeRepo;
    }

    // ==================== 设备台账 ====================

    @Override
    @Transactional
    public Equipment registerEquipment(String code, String name, String model) {
        Equipment eq = new Equipment(code, name, model);
        Equipment saved = equipmentRepo.save(eq);
        DomainEventPublisher.publish(IDomainEvent.of(
                EquipEventTypes.STATUS_CHANGED, "equip",
                Map.of("equipmentCode", code, "status", MachineStatus.IDLE.name())));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Equipment findEquipment(String code) {
        return equipmentRepo.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> getAllEquipments() {
        return equipmentRepo.findAll();
    }

    // ==================== 设备状态管理 ====================

    @Override
    @Transactional(readOnly = true)
    public MachineStatus getStatus(String equipmentCode) {
        Equipment eq = requireEquipment(equipmentCode);
        return eq.getStatus();
    }

    @Override
    @Transactional
    public void startProduction(String equipmentCode) {
        Equipment eq = requireEquipment(equipmentCode);
        MachineStatus before = eq.getStatus();
        eq.startProduction();
        equipmentRepo.save(eq);
        DomainEventPublisher.publish(IDomainEvent.of(
                EquipEventTypes.PRODUCTION_STARTED, "equip",
                Map.of("equipmentCode", equipmentCode, "statusBefore", before.name())));
    }

    @Override
    @Transactional
    public void stopProduction(String equipmentCode) {
        Equipment eq = requireEquipment(equipmentCode);
        eq.stopProduction();
        equipmentRepo.save(eq);
        DomainEventPublisher.publish(IDomainEvent.of(
                EquipEventTypes.PRODUCTION_STOPPED, "equip",
                Map.of("equipmentCode", equipmentCode)));
    }

    @Override
    @Transactional
    public void reportFault(String equipmentCode, String reason) {
        Equipment eq = requireEquipment(equipmentCode);
        eq.reportFault();
        equipmentRepo.save(eq);
        DomainEventPublisher.publish(IDomainEvent.of(
                EquipEventTypes.FAULT_REPORTED, "equip",
                Map.of("equipmentCode", equipmentCode, "reason", reason)));
    }

    @Override
    @Transactional
    public void startMaintenance(String equipmentCode) {
        Equipment eq = requireEquipment(equipmentCode);
        MachineStatus before = eq.getStatus();
        eq.startMaintenance();
        equipmentRepo.save(eq);
        DomainEventPublisher.publish(IDomainEvent.of(
                EquipEventTypes.MAINTENANCE_STARTED, "equip",
                Map.of("equipmentCode", equipmentCode, "statusBefore", before.name())));
    }

    @Override
    @Transactional
    public void completeMaintenance(String equipmentCode) {
        Equipment eq = requireEquipment(equipmentCode);
        eq.completeMaintenance();
        equipmentRepo.save(eq);
        DomainEventPublisher.publish(IDomainEvent.of(
                EquipEventTypes.MAINTENANCE_COMPLETED, "equip",
                Map.of("equipmentCode", equipmentCode)));
    }

    @Override
    @Transactional
    @Deprecated
    public void occupy(String equipmentCode, String workOrderNo) {
        startProduction(equipmentCode);
    }

    @Override
    @Transactional
    @Deprecated
    public void release(String equipmentCode) {
        stopProduction(equipmentCode);
    }

    // ==================== 配方管理 ====================

    @Override
    @Transactional
    public EquipmentRecipe createRecipe(String code, String name, String equipmentCode, String productCode) {
        EquipmentRecipe recipe = new EquipmentRecipe(code, name, equipmentCode, productCode);
        EquipmentRecipe saved = recipeRepo.save(recipe);
        DomainEventPublisher.publish(IDomainEvent.of(
                EquipEventTypes.RECIPE_DISPATCHED, "equip",
                Map.of("recipeCode", code, "equipmentCode", equipmentCode)));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentRecipe findRecipe(String code) {
        return recipeRepo.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentRecipe> getRecipesByEquipment(String equipmentCode) {
        return recipeRepo.findByEquipmentCode(equipmentCode);
    }

    @Override
    @Transactional
    public void dispatchRecipe(String recipeCode) {
        EquipmentRecipe recipe = recipeRepo.findByCode(recipeCode);
        if (recipe == null) {
            throw new IllegalArgumentException("配方不存在: " + recipeCode);
        }
        DomainEventPublisher.publish(IDomainEvent.of(
                EquipEventTypes.RECIPE_DISPATCHED, "equip",
                Map.of("recipeCode", recipeCode, "equipmentCode", recipe.getEquipmentCode(),
                       "version", recipe.getVersion())));
    }

    // ==================== 参数管理 ====================

    @Override
    @Transactional
    public void updateParameter(String equipmentCode, String paramCode, BigDecimal setValue) {
        EquipmentParameter param = parameterRepo.findByEquipmentCodeAndParamCode(equipmentCode, paramCode);
        if (param == null) {
            param = new EquipmentParameter(equipmentCode, paramCode, paramCode, setValue, "");
        }
        param.setSetValue(setValue);
        param.markUpdated();
        parameterRepo.save(param);
        DomainEventPublisher.publish(IDomainEvent.of(
                EquipEventTypes.PARAMETER_UPDATED, "equip",
                Map.of("equipmentCode", equipmentCode, "paramCode", paramCode,
                       "setValue", setValue.toString())));
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentParameter getParameter(String equipmentCode, String paramCode) {
        return parameterRepo.findByEquipmentCodeAndParamCode(equipmentCode, paramCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentParameter> getParametersByEquipment(String equipmentCode) {
        return parameterRepo.findByEquipmentCode(equipmentCode);
    }

    @Override
    @Transactional
    public void reportActualValue(String equipmentCode, String paramCode, BigDecimal actualValue) {
        EquipmentParameter param = parameterRepo.findByEquipmentCodeAndParamCode(equipmentCode, paramCode);
        if (param != null) {
            param.updateActual(actualValue);
            parameterRepo.save(param);
        }
    }

    // ==================== OEE ====================

    @Override
    @Transactional
    public OEMetrics calculateOee(String equipmentCode, LocalDate periodStart, LocalDate periodEnd,
                                   BigDecimal availability, BigDecimal performance, BigDecimal quality) {
        OEMetrics metrics = OEMetrics.of(equipmentCode, periodStart, periodEnd,
                availability, performance, quality);
        OEMetrics saved = oeeRepo.save(metrics);
        DomainEventPublisher.publish(IDomainEvent.of(
                EquipEventTypes.OEE_CALCULATED, "equip",
                Map.of("equipmentCode", equipmentCode, "oee", metrics.toPercentString(),
                       "periodStart", periodStart.toString(), "periodEnd", periodEnd.toString())));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public OEMetrics getLatestOee(String equipmentCode) {
        return oeeRepo.findFirstByEquipmentCodeOrderByPeriodEndDesc(equipmentCode).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OEMetrics> getOeeHistory(String equipmentCode, LocalDate start, LocalDate end) {
        return oeeRepo.findByEquipmentCodeAndPeriodStartBetweenOrderByPeriodStartDesc(
                equipmentCode, start, end);
    }

    // ==================== private ====================

    private Equipment requireEquipment(String equipmentCode) {
        Equipment eq = equipmentRepo.findByCode(equipmentCode);
        if (eq == null) {
            throw new IllegalArgumentException("设备不存在: " + equipmentCode);
        }
        return eq;
    }

}
