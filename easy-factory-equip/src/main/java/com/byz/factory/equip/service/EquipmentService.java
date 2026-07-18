package com.byz.factory.equip.service;

import com.byz.factory.batch.MachineStatus;
import com.byz.factory.equip.model.Equipment;
import com.byz.factory.equip.model.EquipmentParameter;
import com.byz.factory.equip.model.EquipmentRecipe;
import com.byz.factory.equip.model.OEMetrics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 设备服务 — Equip 模块核心。
 * <p>
 * 职责：
 * <ul>
 *   <li>设备台账管理（注册、查询）</li>
 *   <li>设备状态管理（生产运行、换型、故障、维护）</li>
 *   <li>设备配方管理（创建、查询、下发）</li>
 *   <li>设备参数管理（设定值更新、实际值回传）</li>
 *   <li>OEE 计算与查询</li>
 * </ul>
 * <p>
 * 跨模块协作：
 * <ul>
 *   <li>Equip ← PLM：接收工艺参数 → 转化为设备配方</li>
 *   <li>Equip → MES：提供设备状态 → 工单开工前检查</li>
 *   <li>Equip → IoT：下发设定值 → 设备执行</li>
 *   <li>Equip ← IoT：接收实际值 → 参数监控、报警</li>
 *   <li>Equip ← MES：接收生产工单 → 关联到设备运行记录</li>
 *   <li>Equip → QMS：设备参数数据 → SPC 分析</li>
 *   <li>Equip → EAM：设备故障/维护事件 → 维护工单联动</li>
 * </ul>
 *
 * @author 苏政
 */
public interface EquipmentService {

    // ==================== 设备台账 ====================

    /** 注册新设备 */
    Equipment registerEquipment(String code, String name, String model);

    /** 查询设备 */
    Equipment findEquipment(String code);

    /** 获取所有设备 */
    List<Equipment> getAllEquipments();

    // ==================== 设备状态管理 ====================

    /** 查询设备状态 */
    MachineStatus getStatus(String equipmentCode);

    /** 开始生产（IDLE 或 SETUP → RUNNING） */
    void startProduction(String equipmentCode);

    /** 停止生产（RUNNING → IDLE） */
    void stopProduction(String equipmentCode);

    /** 报告故障（RUNNING → FAULT），记录故障原因 */
    void reportFault(String equipmentCode, String reason);

    /** 开始维护（IDLE 或 FAULT → MAINTENANCE） */
    void startMaintenance(String equipmentCode);

    /** 完成维护（MAINTENANCE → IDLE） */
    void completeMaintenance(String equipmentCode);

    /**
     * @deprecated 使用 {@link #startProduction(String)} 替代
     */
    @Deprecated
    void occupy(String equipmentCode, String workOrderNo);

    /**
     * @deprecated 使用 {@link #stopProduction(String)} 替代
     */
    @Deprecated
    void release(String equipmentCode);

    // ==================== 配方管理 ====================

    /** 创建配方 */
    EquipmentRecipe createRecipe(String code, String name, String equipmentCode, String productCode);

    /** 查询配方 */
    EquipmentRecipe findRecipe(String code);

    /** 获取设备的所有配方 */
    List<EquipmentRecipe> getRecipesByEquipment(String equipmentCode);

    /** 下发配方到设备（触发 IoT 参数下发） */
    void dispatchRecipe(String recipeCode);

    // ==================== 参数管理 ====================

    /** 更新参数设定值 */
    void updateParameter(String equipmentCode, String paramCode, BigDecimal setValue);

    /** 查询参数 */
    EquipmentParameter getParameter(String equipmentCode, String paramCode);

    /** 获取设备的所有参数 */
    List<EquipmentParameter> getParametersByEquipment(String equipmentCode);

    /** 回传参数实际值（从 IoT 接收） */
    void reportActualValue(String equipmentCode, String paramCode, BigDecimal actualValue);

    // ==================== OEE ====================

    /** 计算 OEE */
    OEMetrics calculateOee(String equipmentCode, LocalDate periodStart, LocalDate periodEnd,
                           BigDecimal availability, BigDecimal performance, BigDecimal quality);

    /** 查询最近一次 OEE */
    OEMetrics getLatestOee(String equipmentCode);

    /** 查询设备 OEE 历史 */
    List<OEMetrics> getOeeHistory(String equipmentCode, LocalDate start, LocalDate end);

}
