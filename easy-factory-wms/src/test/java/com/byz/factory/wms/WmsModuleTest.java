package com.byz.factory.wms;

import com.byz.factory.batch.*;
import com.byz.factory.wms.model.InventorySnapshot;
import com.byz.factory.wms.model.PickingTask;
import com.byz.factory.wms.model.Receipt;
import com.byz.factory.wms.model.Storage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WMS 模块模型测试")
class WmsModuleTest {

    // ==================== Storage ====================

    @Test
    @DisplayName("Storage 创建 — 默认存储类型为 AMBIENT，容量为 0")
    void storage_creation_shouldSetDefaults() {
        // Given
        // When
        Storage storage = new Storage("WH-A-Z01", "原料仓", "原料区");

        // Then
        assertEquals("WH-A-Z01", storage.getLocationCode());
        assertEquals("原料仓", storage.getWarehouse());
        assertEquals("原料区", storage.getZone());
        assertEquals(StorageType.AMBIENT, storage.getStorageType());
        assertEquals(BigDecimal.ZERO, storage.getCapacity());
        assertNotNull(storage.getCreatedAt());
    }

    @Test
    @DisplayName("Storage — 可设置层次结构和存储类型")
    void storage_shouldSupportHierarchy() {
        // Given
        Storage storage = new Storage("WH-B-Z02-R01-L03-P05", "包材仓", "包材区");

        // When
        storage.setRack("R01");
        storage.setLevel("L03");
        storage.setPosition("P05");
        storage.setStorageType(StorageType.COLD);
        storage.setCapacity(new BigDecimal("500.00"));

        // Then
        assertEquals("R01", storage.getRack());
        assertEquals("L03", storage.getLevel());
        assertEquals("P05", storage.getPosition());
        assertEquals(StorageType.COLD, storage.getStorageType());
        assertEquals(new BigDecimal("500.00"), storage.getCapacity());
    }

    @Test
    @DisplayName("Storage — 实现 IStorage 接口")
    void storage_shouldImplementIStorage() {
        // Given
        Storage storage = new Storage("WH-C-Z03", "成品仓", "成品区");
        storage.setStorageType(StorageType.AMBIENT);

        // When & Then
        IStorage iStorage = storage;
        assertEquals("WH-C-Z03", iStorage.getLocationCode());
        assertEquals("成品仓", iStorage.getWarehouse());
        assertEquals("成品区", iStorage.getZone());
        assertEquals(StorageType.AMBIENT, iStorage.getStorageType());
    }

    // ==================== Receipt ====================

    @Test
    @DisplayName("Receipt 创建 — 初始状态为 PENDING")
    void receipt_creation_shouldSetPendingStatus() {
        // Given
        // When
        Receipt receipt = new Receipt("RCP-001", "PO-2026-001", "SUP-001");

        // Then
        assertEquals("RCP-001", receipt.getReceiptNo());
        assertEquals("PO-2026-001", receipt.getReferenceNo());
        assertEquals("SUP-001", receipt.getSupplierCode());
        assertEquals(ReceiptStatus.PENDING, receipt.getStatus());
        assertNotNull(receipt.getItems());
        assertTrue(receipt.getItems().isEmpty());
    }

    @Test
    @DisplayName("Receipt 状态转换 — PENDING → PARTIAL → COMPLETED → CLOSED")
    void receipt_lifecycle_shouldTransitionCorrectly() {
        // Given
        Receipt receipt = new Receipt("RCP-002", "PO-2026-002", "SUP-002");

        // When & Then: PENDING → PARTIAL
        receipt.receive("张三");
        assertEquals(ReceiptStatus.PARTIAL, receipt.getStatus());
        assertEquals("张三", receipt.getReceivedBy());
        assertNotNull(receipt.getReceivedAt());

        // PARTIAL → COMPLETED
        receipt.complete();
        assertEquals(ReceiptStatus.COMPLETED, receipt.getStatus());

        // COMPLETED → CLOSED
        receipt.close();
        assertEquals(ReceiptStatus.CLOSED, receipt.getStatus());
    }

    @Test
    @DisplayName("Receipt 非法状态转换 — CLOSED 后任何转换都应抛出")
    void receipt_illegalTransition_shouldThrow() {
        // Given
        Receipt receipt = new Receipt("RCP-003", "PO-2026-003", "SUP-003");
        receipt.receive("李四");
        receipt.complete();
        receipt.close();

        // When & Then: CLOSED 是终端状态
        assertThrows(IllegalStateException.class, () -> receipt.receive("王五"));
        assertThrows(IllegalStateException.class, () -> receipt.complete());
    }

    @Test
    @DisplayName("Receipt — 可添加收货明细，默认进入待检状态")
    void receipt_shouldAddItemsWithQuarantineStatus() {
        // Given
        Receipt receipt = new Receipt("RCP-004", "PO-2026-004", "SUP-004");

        // When
        receipt.addItem("MAT-001", "BATCH-001", new BigDecimal("100"), new BigDecimal("100"));
        receipt.addItem("MAT-002", "BATCH-002", new BigDecimal("50"), new BigDecimal("50"));

        // Then
        assertEquals(2, receipt.getItems().size());
        Receipt.ReceiptItem item = receipt.getItems().get(0);
        assertEquals("MAT-001", item.getMaterialCode());
        assertEquals("BATCH-001", item.getBatchNo());
        assertEquals(new BigDecimal("100"), item.getReceivedQty());
        assertEquals(MaterialStatus.QUARANTINE, item.getStatus());
    }

    @Test
    @DisplayName("Receipt — 验收物料：QUARANTINE → RELEASED，记录上架库位")
    void receipt_shouldAcceptItemAndSetLocation() {
        // Given
        Receipt receipt = new Receipt("RCP-005", "PO-2026-005", "SUP-005");
        receipt.addItem("MAT-001", "BATCH-001", new BigDecimal("100"), new BigDecimal("100"));

        // When
        receipt.acceptItem("MAT-001", "WH-A-Z01-R01");

        // Then
        Receipt.ReceiptItem item = receipt.getItems().get(0);
        assertEquals(MaterialStatus.RELEASED, item.getStatus());
        assertEquals("WH-A-Z01-R01", item.getLocationCode());
    }

    @Test
    @DisplayName("Receipt — 拒收物料：QUARANTINE → REJECTED")
    void receipt_shouldRejectItem() {
        // Given
        Receipt receipt = new Receipt("RCP-006", "PO-2026-006", "SUP-006");
        receipt.addItem("MAT-001", "BATCH-001", new BigDecimal("100"), new BigDecimal("100"));

        // When
        receipt.rejectItem("MAT-001");

        // Then
        Receipt.ReceiptItem item = receipt.getItems().get(0);
        assertEquals(MaterialStatus.REJECTED, item.getStatus());
    }

    @Test
    @DisplayName("Receipt — isFullyProcessed 判定所有物料是否验收完毕")
    void receipt_isFullyProcessed_shouldCheckAllItems() {
        // Given
        Receipt receipt = new Receipt("RCP-007", "PO-2026-007", "SUP-007");
        receipt.addItem("MAT-001", "BATCH-001", new BigDecimal("100"), new BigDecimal("100"));
        receipt.addItem("MAT-002", "BATCH-002", new BigDecimal("50"), new BigDecimal("50"));

        // When & Then: 还有待检物料
        assertFalse(receipt.isFullyProcessed());

        // 全部验收
        receipt.acceptItem("MAT-001", "WH-A-Z01-R01");
        receipt.rejectItem("MAT-002");
        assertTrue(receipt.isFullyProcessed());
    }

    // ==================== PickingTask ====================

    @Test
    @DisplayName("PickingTask 创建 — 初始状态为 PENDING，默认拣料类型 FULL")
    void pickingTask_creation_shouldSetPendingStatus() {
        // Given
        // When
        PickingTask task = new PickingTask("PK-001", "WO-2026-001", "BATCH-001");

        // Then
        assertEquals("PK-001", task.getCode());
        assertEquals("WO-2026-001", task.getWorkOrderNo());
        assertEquals("BATCH-001", task.getBatchNo());
        assertEquals(PickingType.FULL, task.getPickingType());
        assertEquals(PickingTaskStatus.PENDING, task.getStatus());
        assertNotNull(task.getItems());
        assertTrue(task.getItems().isEmpty());
    }

    @Test
    @DisplayName("PickingTask 状态转换 — PENDING → IN_PROGRESS → PICKED → DELIVERED")
    void pickingTask_lifecycle_shouldTransitionCorrectly() {
        // Given
        PickingTask task = new PickingTask("PK-002", "WO-2026-002", "BATCH-002");

        // PENDING → IN_PROGRESS
        task.start();
        assertEquals(PickingTaskStatus.IN_PROGRESS, task.getStatus());

        // IN_PROGRESS → PICKED
        task.completePicking();
        assertEquals(PickingTaskStatus.PICKED, task.getStatus());

        // PICKED → DELIVERED
        task.deliver();
        assertEquals(PickingTaskStatus.DELIVERED, task.getStatus());
        assertNotNull(task.getDeliveredAt());
    }

    @Test
    @DisplayName("PickingTask 取消 — PENDING → CANCELLED / IN_PROGRESS → CANCELLED")
    void pickingTask_cancel_shouldWorkFromPendingOrInProgress() {
        // Given: PENDING 取消
        PickingTask task1 = new PickingTask("PK-003", "WO-2026-003", "BATCH-003");
        task1.cancel();
        assertEquals(PickingTaskStatus.CANCELLED, task1.getStatus());

        // Given: IN_PROGRESS 取消
        PickingTask task2 = new PickingTask("PK-004", "WO-2026-004", "BATCH-004");
        task2.start();
        task2.cancel();
        assertEquals(PickingTaskStatus.CANCELLED, task2.getStatus());
    }

    @Test
    @DisplayName("PickingTask 非法状态转换 — PICKED 后不可取消")
    void pickingTask_illegalTransition_shouldThrow() {
        // Given
        PickingTask task = new PickingTask("PK-005", "WO-2026-005", "BATCH-005");
        task.start();
        task.completePicking(); // PICKED

        // When & Then: PICKED → CANCELLED 非法
        assertThrows(IllegalStateException.class, task::cancel);

        // PICKED → DELIVERED 合法
        task.deliver();

        // DELIVERED 终态
        assertThrows(IllegalStateException.class, task::start);
        assertThrows(IllegalStateException.class, task::cancel);
    }

    @Test
    @DisplayName("PickingTask — 可添加和拣料明细行")
    void pickingTask_shouldAddAndPickItems() {
        // Given
        PickingTask task = new PickingTask("PK-006", "WO-2026-006", "BATCH-006");
        task.addItem("MAT-001", new BigDecimal("100"));
        task.addItem("MAT-002", new BigDecimal("50"));

        // When: 拣第一行
        task.pickItem("MAT-001", new BigDecimal("100"), "BATCH-L001", "WH-A-Z01-R01");

        // Then
        assertEquals(2, task.getItems().size());
        PickingTask.PickingTaskItem item1 = task.getItems().get(0);
        assertEquals("MAT-001", item1.getMaterialCode());
        assertEquals(new BigDecimal("100"), item1.getPickedQty());
        assertEquals("BATCH-L001", item1.getBatchNo());
        assertEquals("WH-A-Z01-R01", item1.getLocationCode());

        // 第二行还未拣
        PickingTask.PickingTaskItem item2 = task.getItems().get(1);
        assertEquals(BigDecimal.ZERO, item2.getPickedQty());
    }

    @Test
    @DisplayName("PickingTask — isFullyPicked 判定所有物料是否拣齐")
    void pickingTask_isFullyPicked_shouldCheckAllItems() {
        // Given
        PickingTask task = new PickingTask("PK-007", "WO-2026-007", "BATCH-007");
        task.addItem("MAT-001", new BigDecimal("100"));
        task.addItem("MAT-002", new BigDecimal("50"));

        // When & Then: 未拣齐
        assertFalse(task.isFullyPicked());

        // 全部拣齐
        task.pickItem("MAT-001", new BigDecimal("100"), "BATCH-L001", "WH-A-Z01-R01");
        task.pickItem("MAT-002", new BigDecimal("50"), "BATCH-L002", "WH-A-Z01-R02");
        assertTrue(task.isFullyPicked());
    }

    @Test
    @DisplayName("PickingTask — 实现 IPickingTask 接口")
    void pickingTask_shouldImplementIPickingTask() {
        // Given
        PickingTask task = new PickingTask("PK-008", "WO-2026-008", "BATCH-008");
        task.addItem("MAT-001", new BigDecimal("100"));

        // When & Then
        IPickingTask iTask = task;
        assertEquals("PK-008", iTask.getCode());
        assertEquals("WO-2026-008", iTask.getWorkOrderNo());
        assertEquals(PickingType.FULL, iTask.getPickingType());
        assertEquals(PickingTaskStatus.PENDING, iTask.getStatus());
        assertEquals(1, iTask.getItems().size());
    }

    // ==================== InventorySnapshot ====================

    @Test
    @DisplayName("InventorySnapshot 创建 — 初始库存全部为 0，状态 QUARANTINE")
    void inventorySnapshot_creation_shouldSetZeroStock() {
        // Given
        // When
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");

        // Then
        assertEquals("MAT-001", snap.getMaterialCode());
        assertEquals("BATCH-001", snap.getBatchNo());
        assertEquals("WH-A-Z01-R01", snap.getLocationCode());
        assertEquals(BigDecimal.ZERO, snap.getOnHandQty());
        assertEquals(BigDecimal.ZERO, snap.getAllocatedQty());
        assertEquals(BigDecimal.ZERO, snap.getAvailableQty());
        assertEquals(BigDecimal.ZERO, snap.getQuarantineQty());
        assertEquals(BigDecimal.ZERO, snap.getRejectedQty());
        assertEquals(MaterialStatus.QUARANTINE, snap.getStatus());
    }

    @Test
    @DisplayName("InventorySnapshot — 入库增加在手库存")
    void inventorySnapshot_addStock_shouldIncreaseOnHand() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");

        // When
        snap.addStock(new BigDecimal("100"));

        // Then
        assertEquals(new BigDecimal("100"), snap.getOnHandQty());
        assertEquals(new BigDecimal("100"), snap.getAvailableQty());
    }

    @Test
    @DisplayName("InventorySnapshot — 出库减少在手库存")
    void inventorySnapshot_removeStock_shouldDecreaseOnHand() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.addStock(new BigDecimal("100"));

        // When
        snap.removeStock(new BigDecimal("30"));

        // Then
        assertEquals(new BigDecimal("70"), snap.getOnHandQty());
        assertEquals(new BigDecimal("70"), snap.getAvailableQty());
    }

    @Test
    @DisplayName("InventorySnapshot — 出库超过可用库存应抛出异常")
    void inventorySnapshot_removeStock_exceedsAvailable_shouldThrow() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.addStock(new BigDecimal("100"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> snap.removeStock(new BigDecimal("200")));
    }

    @Test
    @DisplayName("InventorySnapshot — 分配库存减少可用量")
    void inventorySnapshot_allocate_shouldReduceAvailable() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.addStock(new BigDecimal("100"));

        // When
        snap.allocate(new BigDecimal("40"));

        // Then
        assertEquals(new BigDecimal("100"), snap.getOnHandQty());
        assertEquals(new BigDecimal("40"), snap.getAllocatedQty());
        assertEquals(new BigDecimal("60"), snap.getAvailableQty());
    }

    @Test
    @DisplayName("InventorySnapshot — 释放已分配库存恢复可用量")
    void inventorySnapshot_deallocate_shouldRestoreAvailable() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.addStock(new BigDecimal("100"));
        snap.allocate(new BigDecimal("40"));

        // When
        snap.deallocate(new BigDecimal("20"));

        // Then
        assertEquals(new BigDecimal("20"), snap.getAllocatedQty());
        assertEquals(new BigDecimal("80"), snap.getAvailableQty());
    }

    @Test
    @DisplayName("InventorySnapshot — 分配超过可用库存应抛出异常")
    void inventorySnapshot_allocate_exceedsAvailable_shouldThrow() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.addStock(new BigDecimal("100"));
        snap.allocate(new BigDecimal("60")); // 可用=40

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> snap.allocate(new BigDecimal("50")));
    }

    @Test
    @DisplayName("InventorySnapshot — 待检→放行：quarantineQty 转 onHandQty")
    void inventorySnapshot_quarantine_to_release_shouldTransferStock() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.quarantine(new BigDecimal("100"));

        // When
        snap.release(new BigDecimal("100"));

        // Then
        assertEquals(BigDecimal.ZERO, snap.getQuarantineQty());
        assertEquals(new BigDecimal("100"), snap.getOnHandQty());
        assertEquals(MaterialStatus.RELEASED, snap.getStatus());
    }

    @Test
    @DisplayName("InventorySnapshot — 待检→拒收：quarantineQty 转 rejectedQty")
    void inventorySnapshot_quarantine_to_reject_shouldTransferStock() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.quarantine(new BigDecimal("100"));

        // When
        snap.reject(new BigDecimal("100"));

        // Then
        assertEquals(BigDecimal.ZERO, snap.getQuarantineQty());
        assertEquals(new BigDecimal("100"), snap.getRejectedQty());
        assertEquals(MaterialStatus.REJECTED, snap.getStatus());
    }

    @Test
    @DisplayName("InventorySnapshot — 放行数量超过待检数量应抛出异常")
    void inventorySnapshot_release_exceedsQuarantine_shouldThrow() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.quarantine(new BigDecimal("50"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> snap.release(new BigDecimal("100")));
    }

    @Test
    @DisplayName("InventorySnapshot — 盘点更新在手库存并记录盘点时间")
    void inventorySnapshot_count_shouldUpdateOnHandAndTimestamp() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.addStock(new BigDecimal("100"));
        Instant countedAt = Instant.now();

        // When
        snap.count(new BigDecimal("98"), countedAt);

        // Then
        assertEquals(new BigDecimal("98"), snap.getOnHandQty());
        assertEquals(countedAt, snap.getLastCounted());
    }

    @Test
    @DisplayName("InventorySnapshot — 负数防护：入库/出库/分配/待检/放行/拒收/盘点均拒绝负数")
    void inventorySnapshot_negativeQuantity_shouldThrow() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.addStock(new BigDecimal("100"));
        BigDecimal negative = new BigDecimal("-1");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> snap.addStock(negative));
        assertThrows(IllegalArgumentException.class, () -> snap.removeStock(negative));
        assertThrows(IllegalArgumentException.class, () -> snap.allocate(negative));
        assertThrows(IllegalArgumentException.class, () -> snap.deallocate(negative));
        assertThrows(IllegalArgumentException.class, () -> snap.quarantine(negative));
        assertThrows(IllegalArgumentException.class, () -> snap.release(negative));
        assertThrows(IllegalArgumentException.class, () -> snap.reject(negative));
        assertThrows(IllegalArgumentException.class, () -> snap.count(negative, Instant.now()));
    }

    @Test
    @DisplayName("InventorySnapshot — 实现 IInventorySnapshot 接口")
    void inventorySnapshot_shouldImplementIInventorySnapshot() {
        // Given
        InventorySnapshot snap = new InventorySnapshot("MAT-001", "BATCH-001", "WH-A-Z01-R01");
        snap.addStock(new BigDecimal("200"));
        snap.allocate(new BigDecimal("50"));
        snap.setExpiryDate(Instant.now());

        // When & Then
        IInventorySnapshot iSnap = snap;
        assertEquals("MAT-001", iSnap.getMaterialCode());
        assertEquals("BATCH-001", iSnap.getBatchNo());
        assertEquals(new BigDecimal("200"), iSnap.getOnHandQty());
        assertEquals(new BigDecimal("50"), iSnap.getAllocatedQty());
        assertEquals(new BigDecimal("150"), iSnap.getAvailableQty());
        assertNotNull(iSnap.getExpiryDate());
    }

    // ==================== 枚举验证 ====================

    @Test
    @DisplayName("PickingTaskStatus 状态机 — 合法转换路径")
    void pickingTaskStatus_allowedTransitions_shouldBeCorrect() {
        assertTrue(PickingTaskStatus.PENDING.allowedTransitions().contains(PickingTaskStatus.IN_PROGRESS));
        assertTrue(PickingTaskStatus.PENDING.allowedTransitions().contains(PickingTaskStatus.CANCELLED));
        assertFalse(PickingTaskStatus.PENDING.allowedTransitions().contains(PickingTaskStatus.PICKED));

        assertTrue(PickingTaskStatus.IN_PROGRESS.allowedTransitions().contains(PickingTaskStatus.PICKED));
        assertTrue(PickingTaskStatus.IN_PROGRESS.allowedTransitions().contains(PickingTaskStatus.CANCELLED));

        assertTrue(PickingTaskStatus.PICKED.allowedTransitions().contains(PickingTaskStatus.DELIVERED));
        assertFalse(PickingTaskStatus.PICKED.allowedTransitions().contains(PickingTaskStatus.CANCELLED));

        assertTrue(PickingTaskStatus.DELIVERED.allowedTransitions().isEmpty());
        assertTrue(PickingTaskStatus.CANCELLED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("ReceiptStatus 状态机 — 供 WMS 使用的值确认")
    void receiptStatus_allowedTransitions_shouldBeCorrect() {
        assertTrue(ReceiptStatus.PENDING.allowedTransitions().contains(ReceiptStatus.PARTIAL));
        assertTrue(ReceiptStatus.PENDING.allowedTransitions().contains(ReceiptStatus.COMPLETED));

        assertTrue(ReceiptStatus.PARTIAL.allowedTransitions().contains(ReceiptStatus.COMPLETED));

        assertTrue(ReceiptStatus.COMPLETED.allowedTransitions().contains(ReceiptStatus.CLOSED));

        assertTrue(ReceiptStatus.CLOSED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("枚举值确认 — StorageType / PickingType / MaterialStatus")
    void enum_values_shouldBeComplete() {
        assertEquals(4, StorageType.values().length);
        assertEquals(3, PickingType.values().length);
        assertEquals(3, MaterialStatus.values().length);
    }

}
