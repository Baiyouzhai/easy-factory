package com.byz.factory.eam;

import com.byz.factory.eam.model.Asset;
import com.byz.factory.eam.model.CalibrationRecord;
import com.byz.factory.eam.model.MaintenanceOrder;
import com.byz.factory.event.types.EamEventTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EAM 模块测试")
class EamModuleTest {

    // ==================== Asset 构造 ====================

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
    @DisplayName("Asset 完整字段 — 可设置台账属性")
    void asset_fullFields_shouldSetCorrectly() {
        // Given
        Asset asset = new Asset("AST-002", "CNC铣床", "精密加工");

        // When
        asset.setEquipmentCode("EQ-001");
        asset.setPurchaseDate(LocalDate.of(2025, 6, 1));
        asset.setPurchaseCost(new BigDecimal("850000.00"));
        asset.setWarrantyExpiry(LocalDate.of(2028, 6, 1));
        asset.setDepreciationYears(10);
        asset.setResidualValue(new BigDecimal("85000.00"));
        asset.setSupplier("德国DMG");
        asset.setLocation("车间A-1号工位");

        // Then
        assertEquals("EQ-001", asset.getEquipmentCode());
        assertEquals(LocalDate.of(2025, 6, 1), asset.getPurchaseDate());
        assertEquals(new BigDecimal("850000.00"), asset.getPurchaseCost());
        assertEquals(LocalDate.of(2028, 6, 1), asset.getWarrantyExpiry());
        assertEquals(10, asset.getDepreciationYears());
        assertEquals(new BigDecimal("85000.00"), asset.getResidualValue());
        assertEquals("德国DMG", asset.getSupplier());
        assertEquals("车间A-1号工位", asset.getLocation());
    }

    // ==================== Asset 底层状态转换 ====================

    @Test
    @DisplayName("Asset 状态转换 — IDLE → IN_USE → UNDER_MAINTENANCE → IDLE")
    void asset_lifecycle_shouldTransitionCorrectly() {
        // Given
        Asset asset = new Asset("AST-003", "叉车", "物流设备");

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
    @DisplayName("Asset 非法状态转换 — SCRAPPED 是终态")
    void asset_illegalTransition_shouldThrow() {
        // Given
        Asset asset = new Asset("AST-004", "注塑机", "成型设备");
        asset.transition(AssetStatus.IN_USE);
        asset.transition(AssetStatus.IDLE);
        asset.transition(AssetStatus.SCRAPPED);

        // When & Then: SCRAPPED 是终端状态，任何转换都应抛出
        assertThrows(IllegalStateException.class, () -> asset.transition(AssetStatus.IDLE));
        assertThrows(IllegalStateException.class, () -> asset.transition(AssetStatus.IN_USE));
    }

    // ==================== Asset 业务便捷方法 ====================

    @Test
    @DisplayName("startUse — IDLE → IN_USE")
    void asset_startUse_shouldTransitionToInUse() {
        // Given
        Asset asset = new Asset("AST-010", "冲压机", "生产设备");

        // When
        asset.startUse();

        // Then
        assertEquals(AssetStatus.IN_USE, asset.getStatus());
        assertNotNull(asset.getUpdatedAt());
    }

    @Test
    @DisplayName("stopUse — IN_USE → IDLE")
    void asset_stopUse_shouldTransitionToIdle() {
        // Given
        Asset asset = new Asset("AST-011", "包装机", "包装设备");
        asset.startUse();

        // When
        asset.stopUse();

        // Then
        assertEquals(AssetStatus.IDLE, asset.getStatus());
    }

    @Test
    @DisplayName("startMaintenance — IDLE → UNDER_MAINTENANCE（闲置资产送修）")
    void asset_startMaintenance_fromIdle_shouldTransition() {
        // Given
        Asset asset = new Asset("AST-012", "干燥机", "烘干设备");

        // When
        asset.startMaintenance();

        // Then
        assertEquals(AssetStatus.UNDER_MAINTENANCE, asset.getStatus());
    }

    @Test
    @DisplayName("startMaintenance — IN_USE → UNDER_MAINTENANCE（运行中资产故障送修）")
    void asset_startMaintenance_fromInUse_shouldTransition() {
        // Given
        Asset asset = new Asset("AST-013", "搅拌机", "混合设备");
        asset.startUse();

        // When
        asset.startMaintenance();

        // Then
        assertEquals(AssetStatus.UNDER_MAINTENANCE, asset.getStatus());
    }

    @Test
    @DisplayName("completeMaintenance — UNDER_MAINTENANCE → IDLE（修复完成）")
    void asset_completeMaintenance_shouldTransitionToIdle() {
        // Given
        Asset asset = new Asset("AST-014", "磨床", "精加工");
        asset.startMaintenance();

        // When
        asset.completeMaintenance();

        // Then
        assertEquals(AssetStatus.IDLE, asset.getStatus());
    }

    @Test
    @DisplayName("scrap — IDLE → SCRAPPED")
    void asset_scrap_fromIdle_shouldTransition() {
        // Given
        Asset asset = new Asset("AST-015", "老旧车床", "机加工");

        // When
        asset.scrap();

        // Then
        assertEquals(AssetStatus.SCRAPPED, asset.getStatus());
    }

    @Test
    @DisplayName("scrap — SCRAPPED 终态的同状态操作是幂等的（不抛异常）")
    void asset_scrap_twice_shouldBeIdempotent() {
        // Given
        Asset asset = new Asset("AST-016", "报废铣床", "机加工");
        asset.scrap();

        // When & Then: 已报废资产再次 scrap 不抛异常（同状态幂等）
        assertDoesNotThrow(asset::scrap);
        assertEquals(AssetStatus.SCRAPPED, asset.getStatus());
    }

    @Test
    @DisplayName("Asset IExpand — 支持 eam.* 前缀约定")
    void asset_iexpand_shouldSupportPrefix() {
        // Given
        Asset asset = new Asset("AST-017", "反应釜", "化工设备");

        // When
        asset.setProperty("eam.maintenancePlan", "每季度");
        asset.setProperty("eam.calibrationDue", "2026-12-31");

        // Then
        assertEquals("每季度", asset.getProperty("eam.maintenancePlan"));
        assertEquals("2026-12-31", asset.getProperty("eam.calibrationDue"));
    }

    // ==================== MaintenanceOrder 构造 ====================

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

    // ==================== MaintenanceOrder 底层状态转换 ====================

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

    // ==================== MaintenanceOrder 业务便捷方法 ====================

    @Test
    @DisplayName("startWork — OPEN → IN_PROGRESS，自动记录 actualStart")
    void maintenanceOrder_startWork_shouldRecordActualStart() {
        // Given
        MaintenanceOrder order = new MaintenanceOrder("MO-010", "AST-001",
                MaintenanceType.CORRECTIVE);

        // When
        order.startWork();

        // Then
        assertEquals(MaintenanceOrderStatus.IN_PROGRESS, order.getStatus());
        assertNotNull(order.getActualStart(), "实际开始时间应在 startWork 时自动记录");
        assertNotNull(order.getUpdatedAt());
    }

    @Test
    @DisplayName("completeWork — IN_PROGRESS → COMPLETED，记录停机时长/成本/维修人")
    void maintenanceOrder_completeWork_shouldRecordMetrics() {
        // Given
        MaintenanceOrder order = new MaintenanceOrder("MO-011", "AST-002",
                MaintenanceType.CORRECTIVE);
        order.startWork();

        // When
        order.completeWork(120.0, 3500.0, "张师傅");

        // Then
        assertEquals(MaintenanceOrderStatus.COMPLETED, order.getStatus());
        assertEquals(120.0, order.getDowntime());
        assertEquals(3500.0, order.getCost());
        assertEquals("张师傅", order.getTechnician());
        assertNotNull(order.getActualEnd(), "实际结束时间应在 completeWork 时自动记录");
    }

    @Test
    @DisplayName("verifyWork — COMPLETED → VERIFIED")
    void maintenanceOrder_verifyWork_shouldTransitionToVerified() {
        // Given
        MaintenanceOrder order = new MaintenanceOrder("MO-012", "AST-003",
                MaintenanceType.PREVENTIVE);
        order.startWork();
        order.completeWork(60.0, 500.0, "李师傅");

        // When
        order.verifyWork();

        // Then
        assertEquals(MaintenanceOrderStatus.VERIFIED, order.getStatus());
    }

    @Test
    @DisplayName("cancel — OPEN → CANCELLED")
    void maintenanceOrder_cancel_shouldTransitionToCancelled() {
        // Given
        MaintenanceOrder order = new MaintenanceOrder("MO-013", "AST-004",
                MaintenanceType.PREVENTIVE);

        // When
        order.cancel();

        // Then
        assertEquals(MaintenanceOrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("cancel — IN_PROGRESS 状态下不能取消（只能取消 OPEN）")
    void maintenanceOrder_cancel_whenInProgress_shouldThrow() {
        // Given
        MaintenanceOrder order = new MaintenanceOrder("MO-014", "AST-005",
                MaintenanceType.CORRECTIVE);
        order.startWork();

        // When & Then
        assertThrows(IllegalStateException.class, order::cancel);
    }

    @Test
    @DisplayName("VERIFIED 终态 — 不可再转换")
    void maintenanceOrder_verified_shouldBeTerminal() {
        // Given
        MaintenanceOrder order = new MaintenanceOrder("MO-015", "AST-006",
                MaintenanceType.PREVENTIVE);
        order.startWork();
        order.completeWork(30.0, 200.0, "王师傅");
        order.verifyWork();

        // When & Then: VERIFIED 是终态
        assertThrows(IllegalStateException.class,
                () -> order.transition(MaintenanceOrderStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("MaintenanceOrder — 备件清单管理")
    void maintenanceOrder_spareParts_shouldManageList() {
        // Given
        MaintenanceOrder order = new MaintenanceOrder("MO-016", "AST-007",
                MaintenanceType.CORRECTIVE);

        // When
        order.getSpareParts().add("Bearing-6205");
        order.getSpareParts().add("Seal-Kit-001");

        // Then
        assertEquals(2, order.getSpareParts().size());
        assertTrue(order.getSpareParts().contains("Bearing-6205"));
        assertTrue(order.getSpareParts().contains("Seal-Kit-001"));
    }

    @Test
    @DisplayName("MaintenanceOrder IExpand — 支持 eam.mo.* 前缀约定")
    void maintenanceOrder_iexpand_shouldSupportPrefix() {
        // Given
        MaintenanceOrder order = new MaintenanceOrder("MO-017", "AST-008",
                MaintenanceType.PREDICTIVE);

        // When
        order.setProperty("eam.mo.faultCode", "F-001");
        order.setProperty("eam.mo.rootCause", "轴承磨损");

        // Then
        assertEquals("F-001", order.getProperty("eam.mo.faultCode"));
        assertEquals("轴承磨损", order.getProperty("eam.mo.rootCause"));
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
    @DisplayName("recordPass — 设置结果为 PASS，记录校准人/证书/下次校准日期")
    void calibrationRecord_recordPass_shouldSetFields() {
        // Given
        CalibrationRecord record = new CalibrationRecord("CAL-010", "AST-001",
                CalibrationType.EXTERNAL);

        // When
        record.recordPass("上海计量院",
                "CERT-2026-001",
                LocalDate.of(2027, 6, 15));

        // Then
        assertEquals(CalibrationResult.PASS, record.getResult());
        assertEquals("上海计量院", record.getCalibratedBy());
        assertEquals("CERT-2026-001", record.getCertificate());
        assertEquals(LocalDate.of(2027, 6, 15), record.getNextDue());
        assertNotNull(record.getUpdatedAt());
    }

    @Test
    @DisplayName("recordFail — 设置结果为 FAIL，记录偏差值")
    void calibrationRecord_recordFail_shouldSetDeviation() {
        // Given
        CalibrationRecord record = new CalibrationRecord("CAL-011", "AST-002",
                CalibrationType.INTERNAL);

        // When
        record.recordFail("张工", "偏差+0.05mm",
                LocalDate.of(2026, 12, 1));

        // Then
        assertEquals(CalibrationResult.FAIL, record.getResult());
        assertEquals("张工", record.getCalibratedBy());
        assertEquals("偏差+0.05mm", record.getDeviation());
        assertEquals(LocalDate.of(2026, 12, 1), record.getNextDue());
        assertNull(record.getCertificate(), "FAIL 不应有证书编号");
    }

    @Test
    @DisplayName("recordAdjusted — 设置结果为 ADJUSTED，记录偏差和证书")
    void calibrationRecord_recordAdjusted_shouldSetDeviationAndCertificate() {
        // Given
        CalibrationRecord record = new CalibrationRecord("CAL-012", "AST-003",
                CalibrationType.INTERNAL);

        // When
        record.recordAdjusted("李工", "调整后偏差+0.01mm",
                "CERT-2026-ADJ-001",
                LocalDate.of(2027, 3, 1));

        // Then
        assertEquals(CalibrationResult.ADJUSTED, record.getResult());
        assertEquals("李工", record.getCalibratedBy());
        assertEquals("调整后偏差+0.01mm", record.getDeviation());
        assertEquals("CERT-2026-ADJ-001", record.getCertificate());
        assertEquals(LocalDate.of(2027, 3, 1), record.getNextDue());
    }

    @Test
    @DisplayName("CalibrationRecord IExpand — 支持 eam.cal.* 前缀约定")
    void calibrationRecord_iexpand_shouldSupportPrefix() {
        // Given
        CalibrationRecord record = new CalibrationRecord("CAL-013", "AST-004",
                CalibrationType.EXTERNAL);

        // When
        record.setProperty("eam.cal.standardVersion", "ISO 9001:2026");
        record.setProperty("eam.cal.environment", "22±1°C, 50±5%RH");

        // Then
        assertEquals("ISO 9001:2026", record.getProperty("eam.cal.standardVersion"));
        assertEquals("22±1°C, 50±5%RH", record.getProperty("eam.cal.environment"));
    }

    // ==================== EamEventTypes ====================

    @Test
    @DisplayName("EamEventTypes — 事件类型遵循 {module}.{entity}.{past_tense} 命名约定")
    void eamEventTypes_shouldFollowNamingConvention() {
        assertTrue(EamEventTypes.ASSET_SCRAPPED.startsWith("eam."));
        assertTrue(EamEventTypes.MAINTENANCE_STARTED.startsWith("eam."));
        assertTrue(EamEventTypes.MAINTENANCE_COMPLETED.startsWith("eam."));
        assertTrue(EamEventTypes.MAINTENANCE_VERIFIED.startsWith("eam."));
        assertTrue(EamEventTypes.MAINTENANCE_CANCELLED.startsWith("eam."));
        assertTrue(EamEventTypes.CALIBRATION_RECORDED.startsWith("eam."));
    }

    @Test
    @DisplayName("EamEventTypes — PREFIX 为 eam")
    void eamEventTypes_prefix_shouldBeEam() {
        assertEquals("eam", EamEventTypes.PREFIX);
    }
}
