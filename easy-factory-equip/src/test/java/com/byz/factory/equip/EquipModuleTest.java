package com.byz.factory.equip;

import com.byz.factory.batch.MachineStatus;
import com.byz.factory.event.types.EquipEventTypes;
import com.byz.factory.equip.model.*;
import com.byz.factory.shared.Dict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Equip 模块测试")
class EquipModuleTest {

    // ==================== Equipment 构造 ====================

    @Test
    @DisplayName("Equipment 创建 — 初始状态为 IDLE，group 固定为 Machine")
    void equipment_creation_shouldSetInitialStatusAndGroup() {
        // Given
        // When
        Equipment eq = new Equipment("EQ-001", "反应釜", "RF-2000");

        // Then
        assertEquals("EQ-001", eq.getCode());
        assertEquals("反应釜", eq.getName());
        assertEquals("RF-2000", eq.getModel());
        assertEquals(MachineStatus.IDLE, eq.getStatus());
        assertEquals(Dict.SourceGroup.Machine, eq.getGroup());
        assertEquals(Dict.SourceType.Machine, eq.getType());
    }

    @Test
    @DisplayName("Equipment 完整字段 — 可设置台账属性")
    void equipment_fullFields_shouldSetCorrectly() {
        // Given
        Equipment eq = new Equipment("EQ-002", "离心机", "CF-500");

        // When
        eq.setCategory("分离设备");
        eq.setLocation("车间A-3号工位");
        eq.setAssetCode("FIX-2026-0001");
        eq.setSpecifications("转速: 0-5000rpm, 容量: 100L");
        eq.setSupplier("上海离心机厂");
        eq.setPurchaseDate(LocalDate.of(2026, 1, 15));

        // Then
        assertEquals("分离设备", eq.getCategory());
        assertEquals("车间A-3号工位", eq.getLocation());
        assertEquals("FIX-2026-0001", eq.getAssetCode());
        assertEquals("转速: 0-5000rpm, 容量: 100L", eq.getSpecifications());
        assertEquals("上海离心机厂", eq.getSupplier());
        assertEquals(LocalDate.of(2026, 1, 15), eq.getPurchaseDate());
    }

    // ==================== 状态转换（底层 transition） ====================

    @Test
    @DisplayName("Equipment 状态转换 — IDLE → RUNNING → IDLE → MAINTENANCE → IDLE")
    void equipment_lifecycle_normalPath_shouldTransitionCorrectly() {
        // Given
        Equipment eq = new Equipment("EQ-003", "包装机", "PK-100");

        // When & Then: IDLE → RUNNING
        eq.transition(MachineStatus.RUNNING);
        assertEquals(MachineStatus.RUNNING, eq.getStatus());

        // When & Then: RUNNING → IDLE
        eq.transition(MachineStatus.IDLE);
        assertEquals(MachineStatus.IDLE, eq.getStatus());

        // When & Then: IDLE → MAINTENANCE
        eq.transition(MachineStatus.MAINTENANCE);
        assertEquals(MachineStatus.MAINTENANCE, eq.getStatus());

        // When & Then: MAINTENANCE → IDLE
        eq.transition(MachineStatus.IDLE);
        assertEquals(MachineStatus.IDLE, eq.getStatus());
    }

    @Test
    @DisplayName("Equipment 状态转换 — RUNNING → FAULT → MAINTENANCE → IDLE（故障恢复路径）")
    void equipment_lifecycle_faultRecoveryPath_shouldTransitionCorrectly() {
        // Given
        Equipment eq = new Equipment("EQ-004", "CNC铣床", "CNC-500");
        eq.transition(MachineStatus.RUNNING);

        // When & Then: RUNNING → FAULT（设备故障）
        eq.transition(MachineStatus.FAULT);
        assertEquals(MachineStatus.FAULT, eq.getStatus());

        // When & Then: FAULT → MAINTENANCE（送修）
        eq.transition(MachineStatus.MAINTENANCE);
        assertEquals(MachineStatus.MAINTENANCE, eq.getStatus());

        // When & Then: MAINTENANCE → IDLE（修复完成，恢复空闲）
        eq.transition(MachineStatus.IDLE);
        assertEquals(MachineStatus.IDLE, eq.getStatus());
    }

    @Test
    @DisplayName("Equipment 状态转换 — IDLE → SETUP → RUNNING → IDLE（换型路径）")
    void equipment_lifecycle_setupPath_shouldTransitionCorrectly() {
        // Given
        Equipment eq = new Equipment("EQ-005", "注塑机", "IM-300");

        // When & Then: IDLE → SETUP（换模/调参）
        eq.transition(MachineStatus.SETUP);
        assertEquals(MachineStatus.SETUP, eq.getStatus());

        // When & Then: SETUP → RUNNING
        eq.transition(MachineStatus.RUNNING);
        assertEquals(MachineStatus.RUNNING, eq.getStatus());

        // When & Then: RUNNING → IDLE
        eq.transition(MachineStatus.IDLE);
        assertEquals(MachineStatus.IDLE, eq.getStatus());
    }

    @Test
    @DisplayName("Equipment 同状态转换 — 不抛异常（幂等）")
    void equipment_transition_sameState_shouldNotThrow() {
        // Given
        Equipment eq = new Equipment("EQ-006", "干燥机", "DR-100");

        // When & Then: IDLE → IDLE 不抛异常
        assertDoesNotThrow(() -> eq.transition(MachineStatus.IDLE));
        assertEquals(MachineStatus.IDLE, eq.getStatus());
    }

    // ==================== 非法状态转换 ====================

    @Test
    @DisplayName("Equipment 非法状态转换 — FAULT 只能转 MAINTENANCE")
    void equipment_illegalTransition_fromFault_shouldThrow() {
        // Given
        Equipment eq = new Equipment("EQ-007", "搅拌机", "MX-200");
        eq.transition(MachineStatus.RUNNING);
        eq.transition(MachineStatus.FAULT);
        assertEquals(MachineStatus.FAULT, eq.getStatus());

        // When & Then: FAULT → IDLE 非法
        assertThrows(IllegalStateException.class, () -> eq.transition(MachineStatus.IDLE));
        // When & Then: FAULT → RUNNING 非法
        assertThrows(IllegalStateException.class, () -> eq.transition(MachineStatus.RUNNING));
        // When & Then: FAULT → SETUP 非法
        assertThrows(IllegalStateException.class, () -> eq.transition(MachineStatus.SETUP));
    }

    @Test
    @DisplayName("Equipment 非法状态转换 — RUNNING 不能直接转 MAINTENANCE（需先 FAULT）")
    void equipment_illegalTransition_runningToMaintenance_shouldThrow() {
        // Given
        Equipment eq = new Equipment("EQ-008", "粉碎机", "CR-50");
        eq.transition(MachineStatus.RUNNING);

        // When & Then: RUNNING → MAINTENANCE 非法（必须先 FAULT）
        assertThrows(IllegalStateException.class,
                () -> eq.transition(MachineStatus.MAINTENANCE));
    }

    @Test
    @DisplayName("Equipment canTransition — 检查允许的目标状态")
    void equipment_canTransition_shouldCheckAllowedTargets() {
        // Given
        Equipment eq = new Equipment("EQ-009", "压片机", "TB-80");

        // Then: IDLE 可以转 RUNNING, SETUP, MAINTENANCE
        assertTrue(eq.canTransition(MachineStatus.RUNNING));
        assertTrue(eq.canTransition(MachineStatus.SETUP));
        assertTrue(eq.canTransition(MachineStatus.MAINTENANCE));
        assertFalse(eq.canTransition(MachineStatus.FAULT));
    }

    // ==================== 业务便捷方法（正常路径） ====================

    @Test
    @DisplayName("startProduction — IDLE → RUNNING")
    void equipment_startProduction_fromIdle_shouldTransitionToRunning() {
        // Given
        Equipment eq = new Equipment("EQ-010", "反应釜", "RF-2000");

        // When
        eq.startProduction();

        // Then
        assertEquals(MachineStatus.RUNNING, eq.getStatus());
        assertNotNull(eq.getUpdatedAt()); // markUpdated() 被调用
    }

    @Test
    @DisplayName("startProduction — SETUP → RUNNING（换型完成后开始生产）")
    void equipment_startProduction_fromSetup_shouldTransitionToRunning() {
        // Given
        Equipment eq = new Equipment("EQ-011", "注塑机", "IM-300");
        eq.startSetup();

        // When
        eq.startProduction();

        // Then
        assertEquals(MachineStatus.RUNNING, eq.getStatus());
    }

    @Test
    @DisplayName("stopProduction — RUNNING → IDLE")
    void equipment_stopProduction_shouldTransitionToIdle() {
        // Given
        Equipment eq = new Equipment("EQ-012", "包装机", "PK-100");
        eq.startProduction();

        // When
        eq.stopProduction();

        // Then
        assertEquals(MachineStatus.IDLE, eq.getStatus());
    }

    @Test
    @DisplayName("startSetup — IDLE → SETUP")
    void equipment_startSetup_shouldTransitionToSetup() {
        // Given
        Equipment eq = new Equipment("EQ-013", "CNC铣床", "CNC-500");

        // When
        eq.startSetup();

        // Then
        assertEquals(MachineStatus.SETUP, eq.getStatus());
    }

    @Test
    @DisplayName("completeSetup — SETUP → RUNNING")
    void equipment_completeSetup_shouldTransitionToRunning() {
        // Given
        Equipment eq = new Equipment("EQ-014", "注塑机", "IM-300");
        eq.startSetup();

        // When
        eq.completeSetup();

        // Then
        assertEquals(MachineStatus.RUNNING, eq.getStatus());
    }

    @Test
    @DisplayName("reportFault — RUNNING → FAULT")
    void equipment_reportFault_shouldTransitionToFault() {
        // Given
        Equipment eq = new Equipment("EQ-015", "搅拌机", "MX-200");
        eq.startProduction();

        // When
        eq.reportFault();

        // Then
        assertEquals(MachineStatus.FAULT, eq.getStatus());
    }

    @Test
    @DisplayName("startMaintenance — IDLE → MAINTENANCE（计划维护）")
    void equipment_startMaintenance_fromIdle_shouldTransition() {
        // Given
        Equipment eq = new Equipment("EQ-016", "干燥机", "DR-100");

        // When
        eq.startMaintenance();

        // Then
        assertEquals(MachineStatus.MAINTENANCE, eq.getStatus());
    }

    @Test
    @DisplayName("startMaintenance — FAULT → MAINTENANCE（故障后送修）")
    void equipment_startMaintenance_fromFault_shouldTransition() {
        // Given
        Equipment eq = new Equipment("EQ-017", "粉碎机", "CR-50");
        eq.startProduction();
        eq.reportFault();

        // When
        eq.startMaintenance();

        // Then
        assertEquals(MachineStatus.MAINTENANCE, eq.getStatus());
    }

    @Test
    @DisplayName("completeMaintenance — MAINTENANCE → IDLE（修复完成）")
    void equipment_completeMaintenance_shouldTransitionToIdle() {
        // Given
        Equipment eq = new Equipment("EQ-018", "压片机", "TB-80");
        eq.startMaintenance();

        // When
        eq.completeMaintenance();

        // Then
        assertEquals(MachineStatus.IDLE, eq.getStatus());
    }

    // ==================== 业务便捷方法（非法操作） ====================

    @Test
    @DisplayName("FAULT 状态下不能 startProduction（需先维修）")
    void equipment_startProduction_whenFault_shouldThrow() {
        // Given
        Equipment eq = new Equipment("EQ-019", "离心机", "CF-500");
        eq.startProduction();
        eq.reportFault();

        // When & Then
        assertThrows(IllegalStateException.class, eq::startProduction);
    }

    @Test
    @DisplayName("RUNNING 状态下不能 startSetup（需先停机）")
    void equipment_startSetup_whenRunning_shouldThrow() {
        // Given
        Equipment eq = new Equipment("EQ-020", "反应釜", "RF-2000");
        eq.startProduction();

        // When & Then
        assertThrows(IllegalStateException.class, eq::startSetup);
    }

    @Test
    @DisplayName("IDLE 状态下不能 reportFault（设备未运行）")
    void equipment_reportFault_whenIdle_shouldThrow() {
        // Given
        Equipment eq = new Equipment("EQ-021", "灭菌柜", "ST-1000");

        // When & Then
        assertThrows(IllegalStateException.class, eq::reportFault);
    }

    @Test
    @DisplayName("MAINTENANCE 状态下不能 completeSetup")
    void equipment_completeSetup_whenMaintenance_shouldThrow() {
        // Given
        Equipment eq = new Equipment("EQ-022", "包装机", "PK-200");
        eq.startMaintenance();

        // When & Then
        assertThrows(IllegalStateException.class, eq::completeSetup);
    }

    // ==================== 资源操作（继承自 AbstractResourceItem） ====================

    @Test
    @DisplayName("Equipment 作为资源 — number 初始为 1，支持扩展属性")
    void equipment_asResource_shouldSupportQuantityAndExpand() {
        // Given
        Equipment eq = new Equipment("EQ-023", "灭菌柜", "ST-1000");

        // Then: 设备作为单件资源，初始数量为 1
        assertEquals(java.math.BigDecimal.ONE, eq.getNumber());
        assertFalse(eq.isEmpty());

        // IExpand 动态属性
        eq.setProperty("equip.oee", "0.92");
        eq.setProperty("equip.lastMaintenanceDate", "2026-06-15");
        assertEquals("0.92", eq.getProperty("equip.oee"));
        assertEquals("2026-06-15", eq.getProperty("equip.lastMaintenanceDate"));
    }

    // ==================== EquipmentParameter ====================

    @Test
    @DisplayName("EquipmentParameter 创建 — 完整字段设置正确")
    void equipmentParameter_creation_shouldSetFields() {
        // Given
        // When
        EquipmentParameter param = new EquipmentParameter(
                "EQ-001", "TEMP", "温度", new BigDecimal("120.5"), "°C");
        param.setUpperLimit(new BigDecimal("130.0"));
        param.setLowerLimit(new BigDecimal("110.0"));

        // Then
        assertEquals("EQ-001", param.getEquipmentCode());
        assertEquals("TEMP", param.getParamCode());
        assertEquals("温度", param.getParamName());
        assertEquals(new BigDecimal("120.5"), param.getSetValue());
        assertEquals("°C", param.getUnit());
        assertEquals(new BigDecimal("130.0"), param.getUpperLimit());
        assertEquals(new BigDecimal("110.0"), param.getLowerLimit());
        assertEquals("PLC自动", param.getControlMethod()); // 默认值
    }

    @Test
    @DisplayName("EquipmentParameter 默认值 — controlMethod 默认为 PLC自动")
    void equipmentParameter_defaultControlMethod_shouldBePLC() {
        // Given
        EquipmentParameter param = new EquipmentParameter(
                "EQ-002", "SPEED", "转速", new BigDecimal("3000"), "rpm");

        // Then
        assertEquals("PLC自动", param.getControlMethod());
        assertNull(param.getActualValue()); // 初始无实际值
    }

    @Test
    @DisplayName("EquipmentParameter isInControl — 实际值在控制范围内")
    void equipmentParameter_isInControl_withinBounds_shouldReturnTrue() {
        // Given
        EquipmentParameter param = new EquipmentParameter(
                "EQ-003", "PRESS", "压力", new BigDecimal("5.0"), "MPa");
        param.setUpperLimit(new BigDecimal("6.0"));
        param.setLowerLimit(new BigDecimal("4.0"));
        param.setActualValue(new BigDecimal("5.2"));

        // When & Then
        assertTrue(param.isInControl());
    }

    @Test
    @DisplayName("EquipmentParameter isInControl — 实际值超出上限")
    void equipmentParameter_isInControl_aboveUpperLimit_shouldReturnFalse() {
        // Given
        EquipmentParameter param = new EquipmentParameter(
                "EQ-004", "TEMP", "温度", new BigDecimal("120.0"), "°C");
        param.setUpperLimit(new BigDecimal("130.0"));
        param.setLowerLimit(new BigDecimal("110.0"));
        param.setActualValue(new BigDecimal("135.0"));

        // When & Then
        assertFalse(param.isInControl());
    }

    @Test
    @DisplayName("EquipmentParameter isInControl — 未设置控制限时默认受控")
    void equipmentParameter_isInControl_noLimits_shouldReturnTrue() {
        // Given
        EquipmentParameter param = new EquipmentParameter(
                "EQ-005", "FLOW", "流量", new BigDecimal("50.0"), "L/min");
        param.setActualValue(new BigDecimal("55.0"));

        // When & Then
        assertTrue(param.isInControl()); // 无控制限 → 不报警
    }

    @Test
    @DisplayName("EquipmentParameter updateActual — 更新实际值并更新时间戳")
    void equipmentParameter_updateActual_shouldSetValueAndMarkUpdated() {
        // Given
        EquipmentParameter param = new EquipmentParameter(
                "EQ-006", "TEMP", "温度", new BigDecimal("120.0"), "°C");

        // When
        param.updateActual(new BigDecimal("118.5"));

        // Then
        assertEquals(new BigDecimal("118.5"), param.getActualValue());
        assertNotNull(param.getUpdatedAt());
    }

    // ==================== EquipmentRecipe ====================

    @Test
    @DisplayName("EquipmentRecipe 创建 — 完整字段设置正确，初始版本 1.0.0")
    void equipmentRecipe_creation_shouldSetFields() {
        // Given
        // When
        EquipmentRecipe recipe = new EquipmentRecipe(
                "REC-001", "阿莫西林反应釜配方", "EQ-001", "PROD-AMX");

        // Then
        assertEquals("REC-001", recipe.getCode());
        assertEquals("阿莫西林反应釜配方", recipe.getName());
        assertEquals("EQ-001", recipe.getEquipmentCode());
        assertEquals("PROD-AMX", recipe.getProductCode());
        assertEquals("1.0.0", recipe.getVersion());
        assertTrue(recipe.getPhases().isEmpty());
    }

    @Test
    @DisplayName("EquipmentRecipe addPhase — 添加配方阶段")
    void equipmentRecipe_addPhase_shouldAppendPhase() {
        // Given
        EquipmentRecipe recipe = new EquipmentRecipe(
                "REC-002", "注塑配方", "EQ-005", "PROD-ABS");

        // When
        recipe.addPhase(new EquipmentRecipe.RecipePhase(
                "TEMP", "升温段", new BigDecimal("220.0"), 300, new BigDecimal("1.5")));
        recipe.addPhase(new EquipmentRecipe.RecipePhase(
                "TEMP", "恒温段", new BigDecimal("220.0"), 600));

        // Then
        assertEquals(2, recipe.getPhases().size());
        assertEquals("升温段", recipe.getPhases().get(0).getPhase());
        assertEquals("恒温段", recipe.getPhases().get(1).getPhase());
        assertEquals(new BigDecimal("1.5"), recipe.getPhases().get(0).getRampRate());
        assertEquals(BigDecimal.ZERO, recipe.getPhases().get(1).getRampRate()); // 默认 0
    }

    @Test
    @DisplayName("EquipmentRecipe bumpVersion — MINOR 版本递增")
    void equipmentRecipe_bumpVersion_shouldIncrementMinor() {
        // Given
        EquipmentRecipe recipe = new EquipmentRecipe(
                "REC-003", "干燥配方", "EQ-006", "PROD-GRN");

        // When
        recipe.bumpVersion();

        // Then
        assertEquals("1.1.0", recipe.getVersion());
    }

    @Test
    @DisplayName("EquipmentRecipe bumpVersion — 多次递增")
    void equipmentRecipe_bumpVersion_twice_shouldIncrementCorrectly() {
        // Given
        EquipmentRecipe recipe = new EquipmentRecipe(
                "REC-004", "混合配方", "EQ-007", "PROD-MIX");

        // When
        recipe.bumpVersion();
        recipe.bumpVersion();

        // Then
        assertEquals("1.2.0", recipe.getVersion());
    }

    @Test
    @DisplayName("EquipmentRecipe getPhases — 返回不可修改列表")
    void equipmentRecipe_getPhases_shouldReturnUnmodifiable() {
        // Given
        EquipmentRecipe recipe = new EquipmentRecipe(
                "REC-005", "压片配方", "EQ-009", "PROD-TAB");
        recipe.addPhase(new EquipmentRecipe.RecipePhase(
                "PRESS", "压片段", new BigDecimal("50.0"), 120));

        // When & Then
        assertThrows(UnsupportedOperationException.class,
                () -> recipe.getPhases().add(new EquipmentRecipe.RecipePhase()));
    }

    // ==================== OEMetrics ====================

    @Test
    @DisplayName("OEMetrics of — 标准计算 OEE = A × P × Q")
    void oeMetrics_of_shouldCalculateOeeCorrectly() {
        // Given
        BigDecimal availability = new BigDecimal("0.90");  // 90%
        BigDecimal performance = new BigDecimal("0.95");   // 95%
        BigDecimal quality = new BigDecimal("0.98");       // 98%

        // When
        OEMetrics metrics = OEMetrics.of("EQ-001",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 7),
                availability, performance, quality);

        // Then: OEE = 0.90 × 0.95 × 0.98 = 0.8379
        assertEquals(new BigDecimal("0.8379"), metrics.getOee());
        assertEquals("EQ-001", metrics.getEquipmentCode());
        assertEquals(LocalDate.of(2026, 7, 1), metrics.getPeriodStart());
        assertEquals(LocalDate.of(2026, 7, 7), metrics.getPeriodEnd());
    }

    @Test
    @DisplayName("OEMetrics of — 边界值：全部为 0")
    void oeMetrics_of_allZero_shouldCalculateZero() {
        // Given
        BigDecimal zero = BigDecimal.ZERO;

        // When
        OEMetrics metrics = OEMetrics.of("EQ-002",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1),
                zero, zero, zero);

        // Then
        assertEquals(new BigDecimal("0.0000"), metrics.getOee());
    }

    @Test
    @DisplayName("OEMetrics of — 边界值：全部为 100%")
    void oeMetrics_of_allOne_shouldCalculateOne() {
        // Given
        BigDecimal one = BigDecimal.ONE;

        // When
        OEMetrics metrics = OEMetrics.of("EQ-003",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1),
                one, one, one);

        // Then
        assertEquals(new BigDecimal("1.0000"), metrics.getOee());
    }

    @Test
    @DisplayName("OEMetrics toPercentString — 转为百分比字符串")
    void oeMetrics_toPercentString_shouldFormatCorrectly() {
        // Given
        OEMetrics metrics = OEMetrics.of("EQ-004",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 7),
                new BigDecimal("0.90"), new BigDecimal("0.95"), new BigDecimal("0.98"));

        // When
        String percent = metrics.toPercentString();

        // Then
        assertEquals("83.79%", percent);
    }

    @Test
    @DisplayName("OEMetrics toPercentString — oee 为 null 时返回 N/A")
    void oeMetrics_toPercentString_nullOee_shouldReturnNA() {
        // Given
        OEMetrics metrics = new OEMetrics();

        // When
        String percent = metrics.toPercentString();

        // Then
        assertEquals("N/A", percent);
    }

    // ==================== EquipEventTypes ====================

    @Test
    @DisplayName("EquipEventTypes — 事件类型遵循 {module}.{entity}.{past_tense} 命名约定")
    void equipEventTypes_shouldFollowNamingConvention() {
        // Then: 所有事件常量以 "equip." 开头
        assertTrue(EquipEventTypes.STATUS_CHANGED.startsWith("equip."));
        assertTrue(EquipEventTypes.FAULT_REPORTED.startsWith("equip."));
        assertTrue(EquipEventTypes.PRODUCTION_STARTED.startsWith("equip."));
        assertTrue(EquipEventTypes.PRODUCTION_STOPPED.startsWith("equip."));
        assertTrue(EquipEventTypes.MAINTENANCE_STARTED.startsWith("equip."));
        assertTrue(EquipEventTypes.MAINTENANCE_COMPLETED.startsWith("equip."));
        assertTrue(EquipEventTypes.RECIPE_DISPATCHED.startsWith("equip."));
        assertTrue(EquipEventTypes.PARAMETER_UPDATED.startsWith("equip."));
        assertTrue(EquipEventTypes.OEE_CALCULATED.startsWith("equip."));
    }

    @Test
    @DisplayName("EquipEventTypes — PREFIX 为 equip")
    void equipEventTypes_prefix_shouldBeEquip() {
        assertEquals("equip", EquipEventTypes.PREFIX);
    }

    // ==================== RecipePhase ====================

    @Test
    @DisplayName("RecipePhase 创建 — 三参数构造（无 rampRate，默认 0）")
    void recipePhase_threeArgConstructor_shouldDefaultRampRate() {
        // Given
        // When
        EquipmentRecipe.RecipePhase phase = new EquipmentRecipe.RecipePhase(
                "TEMP", "恒温段", new BigDecimal("220.0"), 600);

        // Then
        assertEquals("TEMP", phase.getParamCode());
        assertEquals("恒温段", phase.getPhase());
        assertEquals(new BigDecimal("220.0"), phase.getSetValue());
        assertEquals(600, phase.getDuration());
        assertEquals(BigDecimal.ZERO, phase.getRampRate());
    }

    @Test
    @DisplayName("RecipePhase 创建 — 五参数构造（含 rampRate）")
    void recipePhase_fiveArgConstructor_shouldSetAllFields() {
        // Given
        // When
        EquipmentRecipe.RecipePhase phase = new EquipmentRecipe.RecipePhase(
                "TEMP", "升温段", new BigDecimal("120.0"), 300, new BigDecimal("2.0"));

        // Then
        assertEquals("TEMP", phase.getParamCode());
        assertEquals("升温段", phase.getPhase());
        assertEquals(new BigDecimal("120.0"), phase.getSetValue());
        assertEquals(300, phase.getDuration());
        assertEquals(new BigDecimal("2.0"), phase.getRampRate());
    }

}
