package com.byz.factory.erp.service;

import com.byz.factory.erp.TransactionType;
import com.byz.factory.erp.model.InventorySnapshot;
import com.byz.factory.erp.model.MaterialCache;
import com.byz.factory.erp.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * ERP 适配服务 — 对接外部 ERP 系统。
 * <p>
 * ERP 模块是外部系统的适配层。本接口定义适配器契约；具体实现对接特定 ERP
 * （SAP / Oracle / 用友 / 金蝶）。当前为接口骨架，所有方法待实现。
 * <p>
 * 职责：
 * <ul>
 *   <li>物料主数据同步 — 定时全量 + 增量更新</li>
 *   <li>库存查询 — 按需查询 + 预留</li>
 *   <li>事务回传 — 物料消耗、成品入库、转储，含失败重试队列</li>
 *   <li>本地缓存 — 物料主数据、库存快照本地缓存</li>
 * </ul>
 *
 * @author 苏政
 */
public interface ErpAdapterService {

    // ==================== 物料主数据 ====================

    /** 同步物料主数据（全量） */
    void syncMaterials();

    /** 增量同步物料主数据（基于 lastModified 时间戳） */
    void syncMaterialsIncremental();

    /** 根据物料编码查找本地缓存 */
    MaterialCache findByCode(String materialCode);

    /** 获取所有缓存的物料编码列表 */
    List<String> getAllMaterialCodes();

    // ==================== 库存查询 ====================

    /** 按工厂+库位查询物料库存快照 */
    InventorySnapshot queryStock(String materialCode, String plantCode, String storageLocation);

    /** 按工厂汇总查询物料库存（所有库位合计） */
    BigDecimal queryStock(String materialCode, String plantCode);

    /** 批量查询库存快照 */
    List<InventorySnapshot> queryStockBatch(List<String> materialCodes, String plantCode);

    // ==================== 事务回传 ====================

    /** 物料消耗回传 ERP */
    String postGoodsIssue(String materialCode, BigDecimal quantity, String batchNo, String referenceDoc);

    /** 成品入库回传 ERP */
    String postGoodsReceipt(String materialCode, BigDecimal quantity, String batchNo, String referenceDoc);

    /** 库存转储回传 ERP */
    String postTransfer(String materialCode, BigDecimal quantity, String batchNo,
                        String fromLocation, String toLocation, String referenceDoc);

    // ==================== 事务队列管理 ====================

    /** 创建事务回传记录（加入发送队列） */
    Transaction createTransaction(TransactionType type, String materialCode,
                                  BigDecimal quantity, String batchNo, String referenceDoc);

    /** 获取所有待发送事务 */
    List<Transaction> getPendingTransactions();

    /** 重试所有失败事务 */
    List<Transaction> retryFailedTransactions();

    /** 重试指定事务 */
    Transaction retryTransaction(String transactionCode);

    /** 取消指定事务 */
    Transaction cancelTransaction(String transactionCode);

}
