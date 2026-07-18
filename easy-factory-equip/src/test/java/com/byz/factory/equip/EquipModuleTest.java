package com.byz.factory.equip;

import com.byz.factory.batch.MachineStatus;
import com.byz.factory.equip.model.Equipment;
import com.byz.factory.shared.Dict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Equip 模块模型构造测试")
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
        eq.setPurchaseDate(java.time.LocalDate.of(2026, 1, 15));

        // Then
        assertEquals("分离设备", eq.getCategory());
        assertEquals("车间A-3号工位", eq.getLocation());
        assertEquals("FIX-2026-0001", eq.getAssetCode());
        assertEquals("转速: 0-5000rpm, 容量: 100L", eq.getSpecifications());
        assertEquals("上海离心机厂", eq.getSupplier());
        assertEquals(java.time.LocalDate.of(2026, 1, 15), eq.getPurchaseDate());
    }

    // ==================== 状态转换 ====================

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

    // ==================== 资源操作（继承自 AbstractResourceItem） ====================

    @Test
    @DisplayName("Equipment 作为资源 — number 初始为 1，支持扩展属性")
    void equipment_asResource_shouldSupportQuantityAndExpand() {
        // Given
        Equipment eq = new Equipment("EQ-010", "灭菌柜", "ST-1000");

        // Then: 设备作为单件资源，初始数量为 1
        assertEquals(java.math.BigDecimal.ONE, eq.getNumber());
        assertFalse(eq.isEmpty());

        // IExpand 动态属性
        eq.setProperty("equip.oee", "0.92");
        eq.setProperty("equip.lastMaintenanceDate", "2026-06-15");
        assertEquals("0.92", eq.getProperty("equip.oee"));
        assertEquals("2026-06-15", eq.getProperty("equip.lastMaintenanceDate"));
    }
}
