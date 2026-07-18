package com.byz.factory.erp;

import com.byz.factory.batch.TransactionStatus;
import com.byz.factory.batch.TransactionType;
import com.byz.factory.erp.model.*;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.shared.Dict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ERP 模块模型构造测试")
class ErpModuleTest {

    // ==================== MaterialCache ====================

    @Test
    @DisplayName("MaterialCache 创建 — 字段正确初始化")
    void materialCache_creation_shouldSetFields() {
        MaterialCache cache = new MaterialCache("MAT-001", "阿莫西林原料药", "KG");

        assertEquals("MAT-001", cache.getCode());
        assertEquals("阿莫西林原料药", cache.getName());
        assertEquals("KG", cache.getUnit());
        assertEquals(ErpMaterialType.ROH, cache.getMaterialType());
        assertFalse(cache.isBatchManaged());
        assertEquals(0, cache.getShelfLifeDays());
        assertNotNull(cache.getCreatedAt());
    }

    @Test
    @DisplayName("MaterialCache.toResourceItem — 映射为 core IResourceItem")
    void materialCache_toResourceItem_shouldMapToResourceItem() {
        MaterialCache cache = new MaterialCache("MAT-001", "阿莫西林原料药", "KG");
        cache.setMaterialType(ErpMaterialType.ROH);

        IResourceItem item = cache.toResourceItem();

        assertEquals("MAT-001", item.getName());
        assertEquals(Dict.SourceGroup.Material, item.getGroup());
        assertEquals(Dict.SourceType.RawMaterial, item.getType());
    }

    @Test
    @DisplayName("MaterialCache.mapUom — ERP 单位映射到 core UOM")
    void materialCache_mapUom_shouldMapCorrectly() {
        MaterialCache cacheKg = new MaterialCache("M1", "物料1", "KG");
        MaterialCache cachePcs = new MaterialCache("M2", "物料2", "PCS");
        MaterialCache cacheUnknown = new MaterialCache("M3", "物料3", "UNKNOWN");

        assertEquals(com.byz.factory.shared.UOM.KG, cacheKg.mapUom());
        assertEquals(com.byz.factory.shared.UOM.PCS, cachePcs.mapUom());
        assertEquals(com.byz.factory.shared.UOM.NONE, cacheUnknown.mapUom());
    }

    // ==================== InventorySnapshot ====================

    @Test
    @DisplayName("InventorySnapshot 创建 — 初始库存为零")
    void inventorySnapshot_creation_shouldSetZeroQty() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");

        assertEquals("MAT-001", snap.getMaterialCode());
        assertEquals("PLANT-01", snap.getPlantCode());
        assertEquals("LOC-A01", snap.getStorageLocation());
        assertEquals(BigDecimal.ZERO, snap.getUnrestrictedQty());
        assertEquals(BigDecimal.ZERO, snap.getInspectionQty());
        assertEquals(BigDecimal.ZERO, snap.getBlockedQty());
        assertNotNull(snap.getSnapshotTime());
    }

    // ==================== Transaction ====================

    @Test
    @DisplayName("Transaction 创建 — 初始状态 PENDING")
    void transaction_creation_shouldSetPendingStatus() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));

        assertEquals("TX-001", tx.getCode());
        assertEquals(TransactionType.GOODS_ISSUE, tx.getTransactionType());
        assertEquals(TransactionStatus.PENDING, tx.getStatus());
        assertEquals(0, tx.getRetryCount());
    }

}
