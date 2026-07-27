package com.byz.factory.erp.service.impl;

import com.byz.factory.erp.TransactionStatus;
import com.byz.factory.erp.TransactionType;
import com.byz.factory.erp.model.*;
import com.byz.factory.erp.repository.InventorySnapshotRepository;
import com.byz.factory.erp.repository.MaterialCacheRepository;
import com.byz.factory.erp.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ErpAdapterService 单元测试")
class ErpAdapterServiceImplTest {

    @Mock private MaterialCacheRepository materialCacheRepo;
    @Mock private InventorySnapshotRepository inventorySnapshotRepo;
    @Mock private TransactionRepository transactionRepo;

    private ErpAdapterServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ErpAdapterServiceImpl(materialCacheRepo, inventorySnapshotRepo, transactionRepo);
    }

    // ==================== 物料主数据 ====================

    @Test
    @DisplayName("findByCode — 存在时返回 MaterialCache")
    void findByCode_found_shouldReturnCache() {
        MaterialCache cache = new MaterialCache("MAT-001", "阿莫西林", "KG");
        when(materialCacheRepo.findByCode("MAT-001")).thenReturn(Optional.of(cache));

        MaterialCache result = service.findByCode("MAT-001");

        assertNotNull(result);
        assertEquals("MAT-001", result.getCode());
    }

    @Test
    @DisplayName("findByCode — 不存在时返回 null")
    void findByCode_notFound_shouldReturnNull() {
        when(materialCacheRepo.findByCode("NOT-FOUND")).thenReturn(Optional.empty());

        MaterialCache result = service.findByCode("NOT-FOUND");

        assertNull(result);
    }

    @Test
    @DisplayName("getAllMaterialCodes — 返回所有物料编码")
    void getAllMaterialCodes_shouldReturnAllCodes() {
        MaterialCache cache1 = new MaterialCache("MAT-001", "物料1", "KG");
        MaterialCache cache2 = new MaterialCache("MAT-002", "物料2", "PCS");
        when(materialCacheRepo.findAll()).thenReturn(List.of(cache1, cache2));

        List<String> codes = service.getAllMaterialCodes();

        assertEquals(2, codes.size());
        assertTrue(codes.contains("MAT-001"));
        assertTrue(codes.contains("MAT-002"));
    }

    // ==================== 库存查询 ====================

    @Test
    @DisplayName("queryStock — 按工厂汇总库存")
    void queryStock_byPlant_shouldSumUnrestricted() {
        InventorySnapshot snap1 = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");
        snap1.setUnrestrictedQty(new BigDecimal("100.00"));
        InventorySnapshot snap2 = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A02");
        snap2.setUnrestrictedQty(new BigDecimal("50.00"));
        when(inventorySnapshotRepo.findByMaterialCodeAndPlantCode("MAT-001", "PLANT-01"))
                .thenReturn(List.of(snap1, snap2));

        BigDecimal total = service.queryStock("MAT-001", "PLANT-01");

        assertEquals(0, new BigDecimal("150.00").compareTo(total));
    }

    @Test
    @DisplayName("queryStock — 无库存时返回零")
    void queryStock_noStock_shouldReturnZero() {
        when(inventorySnapshotRepo.findByMaterialCodeAndPlantCode("MAT-001", "PLANT-01"))
                .thenReturn(Collections.emptyList());

        BigDecimal total = service.queryStock("MAT-001", "PLANT-01");

        assertEquals(0, BigDecimal.ZERO.compareTo(total));
    }

    @Test
    @DisplayName("queryStock — 按工厂+库位精确查询")
    void queryStock_byPlantAndLoc_shouldReturnSnapshot() {
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "PLANT-01", "LOC-A01");
        when(inventorySnapshotRepo.findByMaterialCodeAndPlantCodeAndStorageLocation(
                "MAT-001", "PLANT-01", "LOC-A01")).thenReturn(Optional.of(snap));

        InventorySnapshot result = service.queryStock("MAT-001", "PLANT-01", "LOC-A01");

        assertNotNull(result);
        assertEquals("LOC-A01", result.getStorageLocation());
    }

    // ==================== 事务创建 ====================

    @Test
    @DisplayName("createTransaction — 创建并保存，初始状态 PENDING")
    void createTransaction_shouldSaveWithPendingStatus() {
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction tx = service.createTransaction(TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"), "BATCH-001", "WO-001");

        assertNotNull(tx);
        assertEquals(TransactionType.GOODS_ISSUE, tx.getTransactionType());
        assertEquals("MAT-001", tx.getMaterialCode());
        assertEquals("BATCH-001", tx.getBatchNo());
        assertEquals("WO-001", tx.getReferenceDoc());
        assertEquals(TransactionStatus.PENDING, tx.getStatus());
        verify(transactionRepo).save(any(Transaction.class));
    }

    @Test
    @DisplayName("postGoodsIssue — 创建 GOODS_ISSUE 事务并返回编号")
    void postGoodsIssue_shouldCreateTransaction() {
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        String txCode = service.postGoodsIssue("MAT-001", new BigDecimal("100.00"),
                "BATCH-001", "WO-001");

        assertNotNull(txCode);
        assertTrue(txCode.startsWith("TX-GI-"));
    }

    @Test
    @DisplayName("postGoodsReceipt — 创建 GOODS_RECEIPT 事务并返回编号")
    void postGoodsReceipt_shouldCreateTransaction() {
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        String txCode = service.postGoodsReceipt("MAT-002", new BigDecimal("50.00"),
                "BATCH-002", "WO-002");

        assertNotNull(txCode);
        assertTrue(txCode.startsWith("TX-GR-"));
    }

    @Test
    @DisplayName("postTransfer — 创建 TRANSFER 事务并返回编号")
    void postTransfer_shouldCreateTransaction() {
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        String txCode = service.postTransfer("MAT-003", new BigDecimal("30.00"),
                "BATCH-003", "LOC-A01", "LOC-B01", "WO-003");

        assertNotNull(txCode);
        assertTrue(txCode.startsWith("TX-TR-"));
    }

    // ==================== 事务队列管理 ====================

    @Test
    @DisplayName("getPendingTransactions — 查找 PENDING 并标记为 SENT")
    void getPendingTransactions_shouldMarkAsSent() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        when(transactionRepo.findByStatus(TransactionStatus.PENDING)).thenReturn(List.of(tx));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Transaction> result = service.getPendingTransactions();

        assertEquals(1, result.size());
        assertEquals(TransactionStatus.SENT, result.get(0).getStatus());
    }

    @Test
    @DisplayName("retryFailedTransactions — FAILED → PENDING 然后保存")
    void retryFailedTransactions_shouldTransitionToPending() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        tx.markSent();
        tx.markFailed("ERP timeout");
        when(transactionRepo.findByStatus(TransactionStatus.FAILED)).thenReturn(List.of(tx));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Transaction> result = service.retryFailedTransactions();

        assertEquals(1, result.size());
        assertEquals(TransactionStatus.PENDING, result.get(0).getStatus());
        assertEquals(1, result.get(0).getRetryCount());
    }

    @Test
    @DisplayName("retryTransaction — 单个事务重试")
    void retryTransaction_shouldRetrySingleTx() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        tx.markSent();
        tx.markFailed("ERP timeout");
        when(transactionRepo.findByCode("TX-001")).thenReturn(tx);
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = service.retryTransaction("TX-001");

        assertNotNull(result);
        assertEquals(TransactionStatus.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("cancelTransaction — PENDING → CANCELLED")
    void cancelTransaction_shouldCancel() {
        Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                "MAT-001", new BigDecimal("100.00"));
        when(transactionRepo.findByCode("TX-001")).thenReturn(tx);
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = service.cancelTransaction("TX-001");

        assertNotNull(result);
        assertEquals(TransactionStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("cancelTransaction — 不存在时返回 null")
    void cancelTransaction_notFound_shouldReturnNull() {
        when(transactionRepo.findByCode("NOT-FOUND")).thenReturn(null);

        Transaction result = service.cancelTransaction("NOT-FOUND");

        assertNull(result);
    }

    // ==================== 同步 ====================

    @Test
    @DisplayName("syncMaterials — 调用成功不抛异常")
    void syncMaterials_shouldNotThrow() {
        assertDoesNotThrow(() -> service.syncMaterials());
    }

    @Test
    @DisplayName("syncMaterialsIncremental — 调用成功不抛异常")
    void syncMaterialsIncremental_shouldNotThrow() {
        assertDoesNotThrow(() -> service.syncMaterialsIncremental());
    }

}
