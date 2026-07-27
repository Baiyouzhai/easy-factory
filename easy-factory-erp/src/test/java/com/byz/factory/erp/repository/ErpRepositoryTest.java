package com.byz.factory.erp.repository;

import com.byz.factory.erp.TransactionStatus;
import com.byz.factory.erp.TransactionType;
import com.byz.factory.erp.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("ERP Repository 集成测试")
class ErpRepositoryTest {

    @Autowired
    private MaterialCacheRepository materialCacheRepo;

    @Autowired
    private InventorySnapshotRepository inventorySnapshotRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    // ==================== MaterialCacheRepository ====================

    @Test
    @DisplayName("MaterialCache — 保存并查回")
    void materialCache_saveAndFind() {
        MaterialCache cache = new MaterialCache("MAT-001", "阿莫西林原料药", "KG");
        cache.setMaterialType(ErpMaterialType.ROH);
        cache.setSourceSystem("SAP");
        MaterialCache saved = materialCacheRepo.save(cache);

        assertNotNull(saved.getId(), "save 后应自动生成 id");

        Optional<MaterialCache> found = materialCacheRepo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("MAT-001", found.get().getCode());
        assertEquals(ErpMaterialType.ROH, found.get().getMaterialType());
    }

    @Test
    @DisplayName("MaterialCache — findByCode 按物料编码查询")
    void materialCache_findByCode() {
        materialCacheRepo.save(new MaterialCache("MAT-002", "乳糖", "KG"));

        Optional<MaterialCache> found = materialCacheRepo.findByCode("MAT-002");
        assertTrue(found.isPresent());
        assertEquals("乳糖", found.get().getName());
    }

    @Test
    @DisplayName("MaterialCache — 枚举字段持久化为字符串")
    void materialCache_enumPersistence() {
        MaterialCache cache = new MaterialCache("MAT-003", "成品药片", "BOX");
        cache.setMaterialType(ErpMaterialType.FERT);
        materialCacheRepo.saveAndFlush(cache);

        MaterialCache found = materialCacheRepo.findByCode("MAT-003").orElseThrow();
        assertEquals(ErpMaterialType.FERT, found.getMaterialType(),
                "枚举应持久化为字符串并正确还原");
    }

    @Test
    @DisplayName("MaterialCache — createdAt/updatedAt 自动填充")
    void materialCache_auditTimestamps() {
        MaterialCache cache = new MaterialCache("MAT-004", "辅料", "KG");
        MaterialCache saved = materialCacheRepo.save(cache);

        assertNotNull(saved.getCreatedAt(), "createdAt 应自动填充");
        assertNotNull(saved.getUpdatedAt(), "updatedAt 应自动填充");
    }

    // ==================== InventorySnapshotRepository ====================

    @Test
    @DisplayName("InventorySnapshot — 保存并查回")
    void inventorySnapshot_saveAndFind() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");
        snap.setUnrestrictedQty(new BigDecimal("100.00"));
        snap.setInspectionQty(new BigDecimal("20.00"));
        snap.setBlockedQty(new BigDecimal("5.00"));
        InventorySnapshot saved = inventorySnapshotRepo.save(snap);

        assertNotNull(saved.getId());

        InventorySnapshot found = inventorySnapshotRepo.findById(saved.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("100.00").compareTo(found.getUnrestrictedQty()));
        assertEquals(0, new BigDecimal("125.00").compareTo(found.getTotalQty()));
    }

    @Test
    @DisplayName("InventorySnapshot — findByMaterialCode 按物料查库存")
    void inventorySnapshot_findByMaterialCode() {
        inventorySnapshotRepo.save(new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01"));
        inventorySnapshotRepo.save(new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A02"));
        inventorySnapshotRepo.save(new InventorySnapshot("MAT-002", "PLANT-01", "LOC-A01"));

        List<InventorySnapshot> results = inventorySnapshotRepo.findByMaterialCode("MAT-001");
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("InventorySnapshot — findByMaterialCodeAndPlantCodeAndStorageLocation")
    void inventorySnapshot_findByMatAndPlantAndLoc() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");
        inventorySnapshotRepo.save(snap);

        Optional<InventorySnapshot> found = inventorySnapshotRepo
                .findByMaterialCodeAndPlantCodeAndStorageLocation("MAT-001", "PLANT-01", "LOC-A01");
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("InventorySnapshot — BigDecimal 精度保留 (20,4)")
    void inventorySnapshot_bigDecimalPrecision() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");
        snap.setUnrestrictedQty(new BigDecimal("9999999999999999.9999"));
        InventorySnapshot saved = inventorySnapshotRepo.saveAndFlush(snap);

        InventorySnapshot found = inventorySnapshotRepo.findById(saved.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("9999999999999999.9999").compareTo(found.getUnrestrictedQty()),
                "BigDecimal 精度 (20,4) 应正确保留");
    }

    // ==================== TransactionRepository ====================

    @Test
    @DisplayName("Transaction — 保存并查回，初始状态 PENDING")
    void transaction_saveAndFind() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        Transaction saved = transactionRepo.save(tx);

        assertNotNull(saved.getId());
        assertEquals(TransactionStatus.PENDING, saved.getStatus());
    }

    @Test
    @DisplayName("Transaction — 状态枚举持久化 CONFIRMED")
    void transaction_statusEnumPersistence() {
        Transaction tx = new Transaction("TX-002", TransactionType.GOODS_RECEIPT,
                "MAT-002", new BigDecimal("50.00"));
        tx.markSent();
        tx.markConfirmed();
        Transaction saved = transactionRepo.saveAndFlush(tx);

        Transaction found = transactionRepo.findById(saved.getId()).orElseThrow();
        assertEquals(TransactionStatus.CONFIRMED, found.getStatus(),
                "CONFIRMED 状态应正确持久化");
        assertEquals(TransactionType.GOODS_RECEIPT, found.getTransactionType());
    }

    @Test
    @DisplayName("Transaction — findByStatus 按状态查询")
    void transaction_findByStatus() {
        Transaction tx1 = new Transaction("TX-003", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("10.00"));
        transactionRepo.save(tx1);

        Transaction tx2 = new Transaction("TX-004", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("20.00"));
        tx2.markSent();
        transactionRepo.save(tx2);

        List<Transaction> pending = transactionRepo.findByStatus(TransactionStatus.PENDING);
        assertEquals(1, pending.size());
        assertEquals("TX-003", pending.get(0).getCode());

        List<Transaction> sent = transactionRepo.findByStatus(TransactionStatus.SENT);
        assertEquals(1, sent.size());
        assertEquals("TX-004", sent.get(0).getCode());
    }

    @Test
    @DisplayName("Transaction — retryCount 和 errorMessage 持久化")
    void transaction_retryAndError() {
        Transaction tx = new Transaction("TX-005", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        tx.markSent();
        tx.markFailed("ERP 连接超时");
        Transaction saved = transactionRepo.saveAndFlush(tx);

        Transaction found = transactionRepo.findById(saved.getId()).orElseThrow();
        assertEquals(1, found.getRetryCount());
        assertEquals("ERP 连接超时", found.getErrorMessage());
    }

    @Test
    @DisplayName("Transaction — 长错误消息 TEXT 列")
    void transaction_longErrorMessage() {
        Transaction tx = new Transaction("TX-006", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("10.00"));
        tx.markSent();
        String longError = "Connection refused: no further information. "
                + "Retry after 30s. Error code: SAP-ERR-5001.";
        tx.markFailed(longError);
        Transaction saved = transactionRepo.saveAndFlush(tx);

        Transaction found = transactionRepo.findById(saved.getId()).orElseThrow();
        assertEquals(longError, found.getErrorMessage());
    }

    @Test
    @DisplayName("Transaction — findByReferenceDoc 按参考单据查询")
    void transaction_findByReferenceDoc() {
        Transaction tx = new Transaction("TX-007", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("10.00"));
        tx.setReferenceDoc("WO-2026-001");
        transactionRepo.save(tx);

        List<Transaction> results = transactionRepo.findByReferenceDoc("WO-2026-001");
        assertEquals(1, results.size());
        assertEquals("TX-007", results.get(0).getCode());
    }

    @Test
    @DisplayName("Transaction — 同物料多个事务")
    void transaction_multipleForSameMaterial() {
        transactionRepo.save(new Transaction("TX-010", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("10.00")));
        transactionRepo.save(new Transaction("TX-011", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("20.00")));
        transactionRepo.save(new Transaction("TX-012", TransactionType.GOODS_ISSUE,
                "MAT-002", new BigDecimal("30.00")));

        List<Transaction> forMat001 = transactionRepo.findByMaterialCode("MAT-001");
        assertEquals(2, forMat001.size());
    }

}
