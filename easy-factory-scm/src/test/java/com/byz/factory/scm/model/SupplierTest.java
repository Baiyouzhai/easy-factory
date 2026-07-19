package com.byz.factory.scm.model;

import com.byz.factory.scm.SupplierStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Supplier 供应商测试")
class SupplierTest {

    // ==================== 构造 ====================

    @Test
    @DisplayName("构造 — code/name/category 正确赋值，默认状态 ACTIVE + UNDER_REVIEW")
    void constructor_shouldSetRequiredFields() {
        Supplier supplier = new Supplier("SUP-001", "华东原料供应", "RAW_MATERIAL");

        assertEquals("SUP-001", supplier.getCode());
        assertEquals("华东原料供应", supplier.getName());
        assertEquals("RAW_MATERIAL", supplier.getCategory());
        assertEquals("UNDER_REVIEW", supplier.getQualification());
        assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
    }

    @Test
    @DisplayName("构造 — 评分和周期默认为零")
    void constructor_shouldSetZeroDefaults() {
        Supplier supplier = new Supplier("SUP-002", "供应商", "SERVICE");

        assertEquals(0, supplier.getLeadTimeDays());
        assertEquals(0.0, supplier.getOnTimeRate());
        assertEquals(0.0, supplier.getQualityRate());
    }

    // ==================== 资质管理 ====================

    @Test
    @DisplayName("资质 — UNDER_REVIEW → QUALIFIED")
    void qualification_shouldQualify() {
        Supplier supplier = new Supplier("SUP-003", "合格供应商", "RAW_MATERIAL");
        supplier.qualify();

        assertEquals("QUALIFIED", supplier.getQualification());
    }

    @Test
    @DisplayName("资质 — QUALIFIED → DISQUALIFIED")
    void qualification_shouldDisqualify() {
        Supplier supplier = new Supplier("SUP-004", "降级供应商", "PACKAGING");
        supplier.qualify();
        supplier.disqualify();

        assertEquals("DISQUALIFIED", supplier.getQualification());
    }

    // ==================== 运营状态 ====================

    @Test
    @DisplayName("状态 — ACTIVE → INACTIVE")
    void status_shouldDeactivate() {
        Supplier supplier = new Supplier("SUP-005", "暂停供应商", "EQUIPMENT");
        supplier.deactivate();

        assertEquals(SupplierStatus.INACTIVE, supplier.getStatus());
    }

    @Test
    @DisplayName("状态 — INACTIVE → ACTIVE（恢复）")
    void status_shouldReactivate() {
        Supplier supplier = new Supplier("SUP-006", "恢复供应商", "RAW_MATERIAL");
        supplier.deactivate();
        supplier.reactivate();

        assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
    }

    @Test
    @DisplayName("状态 — ACTIVE → BLACKLISTED（拉黑）")
    void status_shouldBlacklist() {
        Supplier supplier = new Supplier("SUP-007", "拉黑供应商", "RAW_MATERIAL");
        supplier.blacklist();

        assertEquals(SupplierStatus.BLACKLISTED, supplier.getStatus());
    }

    @Test
    @DisplayName("状态 — 可直接从 INACTIVE 拉黑")
    void status_shouldBlacklistFromInactive() {
        Supplier supplier = new Supplier("SUP-008", "从暂停到拉黑", "SERVICE");
        supplier.deactivate();
        supplier.blacklist();

        assertEquals(SupplierStatus.BLACKLISTED, supplier.getStatus());
    }

    // ==================== 扩展字段 ====================

    @Test
    @DisplayName("扩展字段 — leadTime/onTimeRate/qualityRate 可设可读")
    void optionalFields_shouldBeReadWrite() {
        Supplier supplier = new Supplier("SUP-009", "优级供应商", "RAW_MATERIAL");
        supplier.setLeadTimeDays(14);
        supplier.setOnTimeRate(98.5);
        supplier.setQualityRate(99.2);

        assertEquals(14, supplier.getLeadTimeDays());
        assertEquals(98.5, supplier.getOnTimeRate());
        assertEquals(99.2, supplier.getQualityRate());
    }

    // ==================== SupplierStatus 枚举 ====================

    @Test
    @DisplayName("SupplierStatus — 三类状态值")
    void supplierStatus_shouldHaveThreeValues() {
        assertEquals(3, SupplierStatus.values().length);
        assertNotNull(SupplierStatus.valueOf("ACTIVE"));
        assertNotNull(SupplierStatus.valueOf("INACTIVE"));
        assertNotNull(SupplierStatus.valueOf("BLACKLISTED"));
    }

}
