package com.byz.factory.wms.service.impl;

import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.wms.MaterialStatus;
import com.byz.factory.wms.model.InventorySnapshot;
import com.byz.factory.wms.model.PickingTask;
import com.byz.factory.wms.model.Receipt;
import com.byz.factory.wms.repository.InventorySnapshotRepository;
import com.byz.factory.wms.repository.PickingTaskRepository;
import com.byz.factory.wms.repository.ReceiptRepository;
import com.byz.factory.wms.service.WmsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * WMS 仓储管理服务实现 — 管理收货、拣料、库存和盘点全流程。
 * <p>
 * 约定：
 * <ul>
 *   <li>写操作 @Transactional + publish 领域事件</li>
 *   <li>状态变更后 publish 对应事件（遵循 {module}.{entity}.{past_tense} 命名）</li>
 *   <li>读操作 @Transactional(readOnly = true)</li>
 * </ul>
 *
 * @author easy-factory
 */
@Service
public class WmsServiceImpl implements WmsService {

    private final ReceiptRepository receiptRepo;
    private final PickingTaskRepository pickingTaskRepo;
    private final InventorySnapshotRepository inventoryRepo;

    public WmsServiceImpl(ReceiptRepository receiptRepo,
                          PickingTaskRepository pickingTaskRepo,
                          InventorySnapshotRepository inventoryRepo) {
        this.receiptRepo = receiptRepo;
        this.pickingTaskRepo = pickingTaskRepo;
        this.inventoryRepo = inventoryRepo;
    }

    // ==================== 收货管理 ====================

    @Override
    @Transactional
    public Receipt createReceipt(String referenceNo, String supplierCode) {
        String receiptNo = "RCP-" + System.currentTimeMillis();
        Receipt receipt = new Receipt(receiptNo, referenceNo, supplierCode);
        Receipt saved = receiptRepo.save(receipt);
        DomainEventPublisher.publish(IDomainEvent.of(
                "wms.receipt.created", "wms",
                Map.of("receiptNo", receiptNo, "referenceNo", referenceNo, "supplierCode", supplierCode)));
        return saved;
    }

    @Override
    @Transactional
    public void acceptAndPutaway(String receiptNo, String locationCode) {
        Receipt receipt = requireReceipt(receiptNo);
        receipt.acceptItem(getFirstQuarantineMaterial(receipt), locationCode);
        if (receipt.isFullyProcessed()) {
            receipt.complete();
        }
        receiptRepo.save(receipt);
        DomainEventPublisher.publish(IDomainEvent.of(
                "wms.item.accepted", "wms",
                Map.of("receiptNo", receiptNo, "locationCode", locationCode)));
    }

    @Override
    @Transactional
    public void completeReceipt(String receiptNo) {
        Receipt receipt = requireReceipt(receiptNo);
        receipt.complete();
        receiptRepo.save(receipt);
        DomainEventPublisher.publish(IDomainEvent.of(
                "wms.receipt.completed", "wms",
                Map.of("receiptNo", receiptNo)));
    }

    @Override
    @Transactional
    public void closeReceipt(String receiptNo) {
        Receipt receipt = requireReceipt(receiptNo);
        receipt.close();
        receiptRepo.save(receipt);
        DomainEventPublisher.publish(IDomainEvent.of(
                "wms.receipt.closed", "wms",
                Map.of("receiptNo", receiptNo)));
    }

    @Override
    @Transactional(readOnly = true)
    public Receipt findReceipt(String receiptNo) {
        return receiptRepo.findByCode(receiptNo);
    }

    // ==================== 拣料管理 ====================

    @Override
    @Transactional
    public PickingTask createPickingTask(String workOrderNo, String batchNo) {
        String taskCode = "PK-" + System.currentTimeMillis();
        PickingTask task = new PickingTask(taskCode, workOrderNo, batchNo);
        PickingTask saved = pickingTaskRepo.save(task);
        DomainEventPublisher.publish(IDomainEvent.of(
                "wms.picking.created", "wms",
                Map.of("taskCode", taskCode, "workOrderNo", workOrderNo, "batchNo", batchNo)));
        return saved;
    }

    @Override
    @Transactional
    public void startPicking(String taskCode) {
        PickingTask task = requirePickingTask(taskCode);
        task.start();
        pickingTaskRepo.save(task);
    }

    @Override
    @Transactional
    public void completePicking(String taskCode) {
        PickingTask task = requirePickingTask(taskCode);
        task.completePicking();
        pickingTaskRepo.save(task);
    }

    @Override
    @Transactional
    public void deliverPicking(String taskCode) {
        PickingTask task = requirePickingTask(taskCode);
        task.deliver();
        pickingTaskRepo.save(task);
        DomainEventPublisher.publish(IDomainEvent.of(
                "wms.picking.delivered", "wms",
                Map.of("taskCode", taskCode, "workOrderNo", task.getWorkOrderNo(),
                       "batchNo", task.getBatchNo())));
    }

    @Override
    @Transactional
    public void cancelPicking(String taskCode) {
        PickingTask task = requirePickingTask(taskCode);
        task.cancel();
        pickingTaskRepo.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public PickingTask findPickingTask(String taskCode) {
        return pickingTaskRepo.findByCode(taskCode);
    }

    // ==================== 库存管理 ====================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal queryAvailableStock(String materialCode, String batchNo) {
        BigDecimal sum = inventoryRepo.sumAvailableStock(materialCode, batchNo);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public InventorySnapshot getInventorySnapshot(String materialCode, String batchNo, String locationCode) {
        return inventoryRepo.findByMaterialCodeAndBatchNoAndLocationCode(materialCode, batchNo, locationCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventorySnapshot> getInventorySnapshots(String materialCode, String batchNo) {
        return inventoryRepo.findByMaterialCodeAndBatchNo(materialCode, batchNo);
    }

    @Override
    @Transactional
    public void allocateStock(String materialCode, String batchNo, String locationCode, BigDecimal qty) {
        InventorySnapshot snapshot = requireSnapshot(materialCode, batchNo, locationCode);
        snapshot.allocate(qty);
        inventoryRepo.save(snapshot);
        DomainEventPublisher.publish(IDomainEvent.of(
                "wms.inventory.changed", "wms",
                Map.of("materialCode", materialCode, "batchNo", batchNo,
                       "locationCode", locationCode, "action", "allocated", "qty", qty.toString())));
    }

    @Override
    @Transactional
    public void deallocateStock(String materialCode, String batchNo, String locationCode, BigDecimal qty) {
        InventorySnapshot snapshot = requireSnapshot(materialCode, batchNo, locationCode);
        snapshot.deallocate(qty);
        inventoryRepo.save(snapshot);
        DomainEventPublisher.publish(IDomainEvent.of(
                "wms.inventory.changed", "wms",
                Map.of("materialCode", materialCode, "batchNo", batchNo,
                       "locationCode", locationCode, "action", "deallocated", "qty", qty.toString())));
    }

    // ==================== 盘点管理 ====================

    @Override
    @Transactional
    public void createCountTask(String locationCode) {
        // 获取指定库位的所有库存快照，标记待盘点（可后续扩展为创建盘点任务实体）
        List<InventorySnapshot> snapshots = inventoryRepo.findByLocationCode(locationCode);
        DomainEventPublisher.publish(IDomainEvent.of(
                "wms.count.created", "wms",
                Map.of("locationCode", locationCode, "snapshotCount", String.valueOf(snapshots.size()))));
    }

    // ==================== private helpers ====================

    private Receipt requireReceipt(String receiptNo) {
        Receipt receipt = receiptRepo.findByCode(receiptNo);
        if (receipt == null) {
            throw new IllegalArgumentException("收货单不存在: " + receiptNo);
        }
        return receipt;
    }

    private PickingTask requirePickingTask(String taskCode) {
        PickingTask task = pickingTaskRepo.findByCode(taskCode);
        if (task == null) {
            throw new IllegalArgumentException("拣料任务不存在: " + taskCode);
        }
        return task;
    }

    private InventorySnapshot requireSnapshot(String materialCode, String batchNo, String locationCode) {
        InventorySnapshot snapshot = inventoryRepo.findByMaterialCodeAndBatchNoAndLocationCode(
                materialCode, batchNo, locationCode);
        if (snapshot == null) {
            throw new IllegalArgumentException("库存快照不存在: " + materialCode + "/" + batchNo + "/" + locationCode);
        }
        return snapshot;
    }

    /**
     * 获取收货单中第一个待检物料编码（供 acceptAndPutaway 使用）。
     */
    private String getFirstQuarantineMaterial(Receipt receipt) {
        return receipt.getItems().stream()
                .filter(i -> i.getStatus() == MaterialStatus.QUARANTINE)
                .findFirst()
                .map(Receipt.ReceiptItem::getMaterialCode)
                .orElseThrow(() -> new IllegalStateException("收货单中没有待验收物料: " + receipt.getReceiptNo()));
    }

}
