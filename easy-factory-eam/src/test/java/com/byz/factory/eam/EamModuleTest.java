package com.byz.factory.eam;

import com.byz.factory.batch.*;
import com.byz.factory.eam.model.Asset;
import com.byz.factory.eam.model.CalibrationRecord;
import com.byz.factory.eam.model.MaintenanceOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EAM 模块模型构造测试")
class EamModuleTest {

    // ==================== Asset ====================

    @Test
    @DisplayName("Asset 创建 — 初始状态为 IDLE")
    void asset_creation_shouldSetInitialStatus() {
        // Given
        // When
        Asset asset = new Asset("AST-001", "冲压机", "生产设备");

        // Then
        assertEquals("AST-001", asset.getAssetCode());
        assertEquals("冲压机", asset.getName());
        assertEquals("生产设备", asset.getCategory());
        assertEquals(AssetStatus.IDLE, asset.getStatus());
    }

    @Test
    @DisplayName("Asset 状态转换 — IDLE → IN_USE → UNDER_MAINTENANCE → IDLE")
    void asset_lifecycle_shouldTransitionCorrectly() {
        // Given
        Asset asset = new Asset("AST-002", "CNC 铣床", "精密加工");

        // When & Then: IDLE → IN_USE
        asset.transition(AssetStatus.IN_USE);
        assertEquals(AssetStatus.IN_USE, asset.getStatus());

        // When & Then: IN_USE → UNDER_MAINTENANCE
        asset.transition(AssetStatus.UNDER_MAINTENANCE);
        assertEquals(AssetStatus.UNDER_MAINTENANCE, asset.getStatus());

        // When & Then: UNDER_MAINTENANCE → IDLE
        asset.transition(AssetStatus.IDLE);
        assertEquals(AssetStatus.IDLE, asset.getStatus());
    }

    @Test
    @DisplayName("Asset 非法状态转换 — 应抛出 IllegalStateException")
    void asset_illegalTransition_shouldThrow() {
        // Given
        Asset asset = new Asset("AST-003", "叉车", "物流设备");

        // When & Then: IDLE → IN_USE 后再尝试 IN_USE → IDLE 是合法的
        asset.transition(AssetStatus.IN_USE);
        asset.transition(AssetStatus.IDLE); // 这个合法

        // 先转到 SCRAPPED
        asset.transition(AssetStatus.SCRAPPED);

        // SCRAPPED 是终端状态，任何转换都应抛出
        assertThrows(IllegalStateException.class, () -> asset.transition(AssetStatus.IDLE));
        assertThrows(IllegalStateException.class, () -> asset.transition(AssetStatus.IN_USE));
    }

    // ==================== MaintenanceOrder ====================

    @Test
    @DisplayName("MaintenanceOrder 创建 — 初始状态为 OPEN，默认优先级 MEDIUM")
    void maintenanceOrder_creation_shouldSetOpenStatus() {
        // Given
        // When
        MaintenanceOrder order = new MaintenanceOrder("MO-001", "AST-001",
                MaintenanceType.CORRECTIVE);

        // Then
        assertEquals("MO-001", order.getCode());
        assertEquals("AST-001", order.getAssetCode());
        assertEquals(MaintenanceType.CORRECTIVE, order.getType());
        assertEquals(MaintenancePriority.MEDIUM, order.getPriority());
        assertEquals(MaintenanceOrderStatus.OPEN, order.getStatus());
        assertNotNull(order.getSpareParts());
        assertTrue(order.getSpareParts().isEmpty());
    }

    @Test
    @DisplayName("MaintenanceOrder 状态转换 — OPEN → IN_PROGRESS → COMPLETED → VERIFIED")
    void maintenanceOrder_lifecycle_shouldTransitionCorrectly() {
        // Given
        MaintenanceOrder order = new MaintenanceOrder("MO-002", "AST-002",
                MaintenanceType.PREVENTIVE);

        // OPEN → IN_PROGRESS
        order.transition(MaintenanceOrderStatus.IN_PROGRESS);
        assertEquals(MaintenanceOrderStatus.IN_PROGRESS, order.getStatus());

        // IN_PROGRESS → COMPLETED
        order.transition(MaintenanceOrderStatus.COMPLETED);
        assertEquals(MaintenanceOrderStatus.COMPLETED, order.getStatus());

        // COMPLETED → VERIFIED
        order.transition(MaintenanceOrderStatus.VERIFIED);
        assertEquals(MaintenanceOrderStatus.VERIFIED, order.getStatus());
    }

    // ==================== CalibrationRecord ====================

    @Test
    @DisplayName("CalibrationRecord 创建 — 自动设置 calibratedAt")
    void calibrationRecord_creation_shouldSetCalibratedAt() {
        // Given
        // When
        CalibrationRecord record = new CalibrationRecord("CAL-001", "AST-001",
                CalibrationType.EXTERNAL);

        // Then
        assertEquals("CAL-001", record.getCode());
        assertEquals("AST-001", record.getAssetCode());
        assertEquals(CalibrationType.EXTERNAL, record.getCalibrationType());
        assertNotNull(record.getCalibratedAt(), "校准日期应在创建时自动设置");
        assertNull(record.getResult(), "校准结果在创建时应为 null");
    }

    @Test
    @DisplayName("CalibrationRecord — 可设置校准结果并记录证书")
    void calibrationRecord_shouldRecordResultAndCertificate() {
        // Given
        CalibrationRecord record = new CalibrationRecord("CAL-002", "AST-003",
                CalibrationType.INTERNAL);

        // When
        record.setResult(CalibrationResult.PASS);
        record.setCertificate("CERT-2026-001");
        record.setStandard("ISO 9001");
        record.setDeviation("0.01mm");

        // Then
        assertEquals(CalibrationResult.PASS, record.getResult());
        assertEquals("CERT-2026-001", record.getCertificate());
        assertEquals("ISO 9001", record.getStandard());
        assertEquals("0.01mm", record.getDeviation());
    }
}
