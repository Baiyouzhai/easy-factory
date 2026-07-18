package com.byz.factory.erp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventorySnapshot 库存快照测试")
class InventorySnapshotTest {

    @Test
    @DisplayName("构造 — materialCode/plantCode/storageLocation 正确赋值")
    void constructor_shouldSetRequiredFields() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");

        assertEquals("MAT-001", snap.getMaterialCode());
        assertEquals("PLANT-01", snap.getPlantCode());
        assertEquals("LOC-A01", snap.getStorageLocation());
    }

    @Test
    @DisplayName("构造 — code 自动生成为 materialCode-plantCode-storageLocation")
    void constructor_shouldGenerateCode() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");

        assertEquals("MAT-001-PLANT-01-LOC-A01", snap.getCode());
    }

    @Test
    @DisplayName("初始值 — unrestrictedQty/inspectionQty/blockedQty 均为零")
    void initialValues_shouldBeZero() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");

        assertEquals(BigDecimal.ZERO, snap.getUnrestrictedQty());
        assertEquals(BigDecimal.ZERO, snap.getInspectionQty());
        assertEquals(BigDecimal.ZERO, snap.getBlockedQty());
    }

    @Test
    @DisplayName("初始值 — snapshotTime 在创建时自动设置")
    void snapshotTime_shouldBeAutoSet() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");

        assertNotNull(snap.getSnapshotTime());
        // 快照时间应在最近 1 秒内
        Instant now = Instant.now();
        assertTrue(snap.getSnapshotTime().isBefore(now.plusSeconds(1)));
        assertTrue(snap.getSnapshotTime().isAfter(now.minusSeconds(5)));
    }

    @Test
    @DisplayName("getTotalQty — 三者合计")
    void getTotalQty_shouldSumAll() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");
        snap.setUnrestrictedQty(new BigDecimal("100.00"));
        snap.setInspectionQty(new BigDecimal("20.00"));
        snap.setBlockedQty(new BigDecimal("5.00"));

        assertEquals(new BigDecimal("125.00"), snap.getTotalQty());
    }

    @Test
    @DisplayName("getTotalQty — 空库存时为零")
    void getTotalQty_empty_shouldBeZero() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");

        assertEquals(BigDecimal.ZERO, snap.getTotalQty());
    }

    @Test
    @DisplayName("getAvailableQty — 仅返回非限制库存")
    void getAvailableQty_shouldReturnUnrestrictedOnly() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");
        snap.setUnrestrictedQty(new BigDecimal("100.00"));
        snap.setInspectionQty(new BigDecimal("20.00"));
        snap.setBlockedQty(new BigDecimal("5.00"));

        assertEquals(new BigDecimal("100.00"), snap.getAvailableQty(),
                "可用库存 = 非限制库存（不含待检和冻结）");
    }

    @Test
    @DisplayName("batchNo / sourceSystem — 可设可读")
    void optionalFields_shouldBeReadWrite() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");
        snap.setBatchNo("BATCH-2026-001");
        snap.setSourceSystem("SAP");

        assertEquals("BATCH-2026-001", snap.getBatchNo());
        assertEquals("SAP", snap.getSourceSystem());
    }

    @Test
    @DisplayName("自动审计 — createdAt/updatedAt 在构造时自动设置")
    void audit_shouldAutoSetTimestamps() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");

        assertNotNull(snap.getCreatedAt());
        assertNotNull(snap.getUpdatedAt());
    }

}
