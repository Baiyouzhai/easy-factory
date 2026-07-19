package com.byz.factory.wms.service;

import com.byz.factory.wms.model.InventorySnapshot;
import com.byz.factory.wms.model.PickingTask;
import com.byz.factory.wms.model.Receipt;

import java.math.BigDecimal;
import java.util.List;

/**
 * WMS 仓储管理服务 — 管理收货、拣料、库存和盘点全流程。
 * <p>
 * 职责：
 * <ul>
 *   <li>收货管理：创建收货单、验收上架、完成、关闭</li>
 *   <li>拣料管理：创建拣料任务、开始拣料、完成拣料、送达线边仓、取消</li>
 *   <li>库存管理：库存查询、库存快照、分配/释放库存</li>
 *   <li>盘点管理：创建盘点任务</li>
 * </ul>
 * <p>
 * 跨模块协作：
 * <ul>
 *   <li>WMS ← SCM/ERP：采购订单驱动收货</li>
 *   <li>WMS ← MES：工单驱动拣料任务</li>
 *   <li>WMS → MES：拣料完成确认投料</li>
 *   <li>WMS → LIMS：物料批次信息供称量</li>
 *   <li>WMS → QMS：来料待检通知</li>
 *   <li>WMS ← QMS：来料检结果→放行/退货</li>
 *   <li>WMS → ERP：库存变更触发财务过账</li>
 * </ul>
 *
 * @author 苏政
 */
public interface WmsService {

    // ==================== 收货管理 ====================

    /** 创建收货单（初始状态 PENDING） */
    Receipt createReceipt(String referenceNo, String supplierCode);

    /** 验收上架——物料验收合格后上架到指定库位 */
    void acceptAndPutaway(String receiptNo, String locationCode);

    /** 完成收货（PARTIAL → COMPLETED） */
    void completeReceipt(String receiptNo);

    /** 关闭收货单（COMPLETED → CLOSED） */
    void closeReceipt(String receiptNo);

    /** 查询收货单 */
    Receipt findReceipt(String receiptNo);

    // ==================== 拣料管理 ====================

    /** 创建拣料任务（初始状态 PENDING） */
    PickingTask createPickingTask(String workOrderNo, String batchNo);

    /** 开始拣料（PENDING → IN_PROGRESS） */
    void startPicking(String taskCode);

    /** 拣料完成（IN_PROGRESS → PICKED） */
    void completePicking(String taskCode);

    /** 送达线边仓（PICKED → DELIVERED） */
    void deliverPicking(String taskCode);

    /** 取消拣料任务（PENDING 或 IN_PROGRESS → CANCELLED） */
    void cancelPicking(String taskCode);

    /** 查询拣料任务 */
    PickingTask findPickingTask(String taskCode);

    // ==================== 库存管理 ====================

    /** 查询物料可用库存（汇总所有库位） */
    BigDecimal queryAvailableStock(String materialCode, String batchNo);

    /** 获取指定库位的库存快照 */
    InventorySnapshot getInventorySnapshot(String materialCode, String batchNo, String locationCode);

    /** 获取物料在所有库位的库存快照列表 */
    List<InventorySnapshot> getInventorySnapshots(String materialCode, String batchNo);

    /** 分配库存（工单预留） */
    void allocateStock(String materialCode, String batchNo, String locationCode, BigDecimal qty);

    /** 释放已分配库存（取消预留） */
    void deallocateStock(String materialCode, String batchNo, String locationCode, BigDecimal qty);

    // ==================== 盘点管理 ====================

    /** 创建盘点任务（按库位范围） */
    void createCountTask(String locationCode);

}
