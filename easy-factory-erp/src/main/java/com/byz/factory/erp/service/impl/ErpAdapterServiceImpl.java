package com.byz.factory.erp.service.impl;

import com.byz.factory.erp.TransactionStatus;
import com.byz.factory.erp.TransactionType;
import com.byz.factory.erp.model.InventorySnapshot;
import com.byz.factory.erp.model.MaterialCache;
import com.byz.factory.erp.model.Transaction;
import com.byz.factory.erp.repository.InventorySnapshotRepository;
import com.byz.factory.erp.repository.MaterialCacheRepository;
import com.byz.factory.erp.repository.TransactionRepository;
import com.byz.factory.erp.service.ErpAdapterService;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.ErpEventTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ERP 适配服务实现 — 基于本地 Repository 的 ERP 适配层。
 * <p>
 * 物料主数据/库存查询/事务队列在本地数据库操作。
 * 外部 ERP 对接（物料同步、事务回传发送）当前通过领域事件发布，
 * 后续由具体厂商适配器订阅事件完成对接。
 * <p>
 * 约定：
 * <ul>
 *   <li>写操作 @Transactional + publish 领域事件</li>
 *   <li>状态变更后 publish 对应事件常量（ErpEventTypes）</li>
 *   <li>读操作 @Transactional(readOnly = true)</li>
 * </ul>
 *
 * @author easy-factory
 */
@Service
public class ErpAdapterServiceImpl implements ErpAdapterService {

    private final MaterialCacheRepository materialCacheRepo;
    private final InventorySnapshotRepository inventorySnapshotRepo;
    private final TransactionRepository transactionRepo;

    public ErpAdapterServiceImpl(MaterialCacheRepository materialCacheRepo,
                                 InventorySnapshotRepository inventorySnapshotRepo,
                                 TransactionRepository transactionRepo) {
        this.materialCacheRepo = materialCacheRepo;
        this.inventorySnapshotRepo = inventorySnapshotRepo;
        this.transactionRepo = transactionRepo;
    }

    // ==================== 物料主数据 ====================

    @Override
    @Transactional
    public void syncMaterials() {
        // TODO: 实际需通过外部 ERP 适配器全量拉取物料主数据
        // 当前阶段：发布同步事件，由监听器处理
        DomainEventPublisher.publish(IDomainEvent.of(
                ErpEventTypes.MATERIAL_SYNCED, "erp",
                Map.of("syncType", "full", "timestamp", Instant.now().toString())));
    }

    @Override
    @Transactional
    public void syncMaterialsIncremental() {
        // TODO: 实际需通过外部 ERP 适配器增量拉取
        DomainEventPublisher.publish(IDomainEvent.of(
                ErpEventTypes.MATERIAL_SYNCED, "erp",
                Map.of("syncType", "incremental", "timestamp", Instant.now().toString())));
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialCache findByCode(String materialCode) {
        return materialCacheRepo.findByCode(materialCode).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllMaterialCodes() {
        return materialCacheRepo.findAll().stream()
                .map(MaterialCache::getCode)
                .toList();
    }

    // ==================== 库存查询 ====================

    @Override
    @Transactional(readOnly = true)
    public InventorySnapshot queryStock(String materialCode, String plantCode, String storageLocation) {
        return inventorySnapshotRepo
                .findByMaterialCodeAndPlantCodeAndStorageLocation(materialCode, plantCode, storageLocation)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal queryStock(String materialCode, String plantCode) {
        List<InventorySnapshot> snaps = inventorySnapshotRepo
                .findByMaterialCodeAndPlantCode(materialCode, plantCode);
        return snaps.stream()
                .map(InventorySnapshot::getUnrestrictedQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventorySnapshot> queryStockBatch(List<String> materialCodes, String plantCode) {
        List<InventorySnapshot> result = new ArrayList<>();
        for (String materialCode : materialCodes) {
            result.addAll(inventorySnapshotRepo.findByMaterialCodeAndPlantCode(materialCode, plantCode));
        }
        return result;
    }

    // ==================== 事务回传 ====================

    @Override
    @Transactional
    public String postGoodsIssue(String materialCode, BigDecimal quantity,
                                  String batchNo, String referenceDoc) {
        Transaction tx = createTransaction(TransactionType.GOODS_ISSUE,
                materialCode, quantity, batchNo, referenceDoc);
        return tx.getCode();
    }

    @Override
    @Transactional
    public String postGoodsReceipt(String materialCode, BigDecimal quantity,
                                    String batchNo, String referenceDoc) {
        Transaction tx = createTransaction(TransactionType.GOODS_RECEIPT,
                materialCode, quantity, batchNo, referenceDoc);
        return tx.getCode();
    }

    @Override
    @Transactional
    public String postTransfer(String materialCode, BigDecimal quantity, String batchNo,
                                String fromLocation, String toLocation, String referenceDoc) {
        Transaction tx = createTransaction(TransactionType.TRANSFER,
                materialCode, quantity, batchNo, referenceDoc);
        return tx.getCode();
    }

    // ==================== 事务队列管理 ====================

    @Override
    @Transactional
    public Transaction createTransaction(TransactionType type, String materialCode,
                                          BigDecimal quantity, String batchNo, String referenceDoc) {
        String code = generateTransactionCode(type);
        Transaction tx = new Transaction(code, type, materialCode, quantity);
        tx.setBatchNo(batchNo);
        tx.setReferenceDoc(referenceDoc);
        Transaction saved = transactionRepo.save(tx);

        DomainEventPublisher.publish(IDomainEvent.of(
                ErpEventTypes.TRANSACTION_CREATED, "erp",
                Map.of("transactionCode", code, "transactionType", type.name(),
                       "materialCode", materialCode, "quantity", quantity)));

        return saved;
    }

    @Override
    @Transactional
    public List<Transaction> getPendingTransactions() {
        List<Transaction> pending = transactionRepo.findByStatus(TransactionStatus.PENDING);
        // 自动将 PENDING 事务标记为 SENT
        for (Transaction tx : pending) {
            tx.markSent();
            transactionRepo.save(tx);
            DomainEventPublisher.publish(IDomainEvent.of(
                    ErpEventTypes.TRANSACTION_SENT, "erp",
                    Map.of("transactionCode", tx.getCode(), "sentTime", Instant.now().toString())));
        }
        return pending;
    }

    @Override
    @Transactional
    public List<Transaction> retryFailedTransactions() {
        List<Transaction> failed = transactionRepo.findByStatus(TransactionStatus.FAILED);
        List<Transaction> retried = new ArrayList<>();
        for (Transaction tx : failed) {
            tx.retry();
            Transaction saved = transactionRepo.save(tx);
            retried.add(saved);
            DomainEventPublisher.publish(IDomainEvent.of(
                    ErpEventTypes.TRANSACTION_CREATED, "erp",
                    Map.of("transactionCode", tx.getCode(), "retryCount", tx.getRetryCount())));
        }
        return retried;
    }

    @Override
    @Transactional
    public Transaction retryTransaction(String transactionCode) {
        Transaction tx = transactionRepo.findByCode(transactionCode);
        if (tx == null) return null;
        tx.retry();
        Transaction saved = transactionRepo.save(tx);
        DomainEventPublisher.publish(IDomainEvent.of(
                ErpEventTypes.TRANSACTION_CREATED, "erp",
                Map.of("transactionCode", transactionCode, "retryCount", tx.getRetryCount())));
        return saved;
    }

    @Override
    @Transactional
    public Transaction cancelTransaction(String transactionCode) {
        Transaction tx = transactionRepo.findByCode(transactionCode);
        if (tx == null) return null;
        tx.cancel();
        Transaction saved = transactionRepo.save(tx);
        DomainEventPublisher.publish(IDomainEvent.of(
                ErpEventTypes.TRANSACTION_CANCELLED, "erp",
                Map.of("transactionCode", transactionCode)));
        return saved;
    }

    // ==================== private ====================

    /** 生成事务编号: TX-{TYPE缩写}-{UUID前8位} */
    private String generateTransactionCode(TransactionType type) {
        String abbr = switch (type) {
            case GOODS_ISSUE   -> "GI";
            case GOODS_RECEIPT -> "GR";
            case TRANSFER      -> "TR";
        };
        return "TX-" + abbr + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
