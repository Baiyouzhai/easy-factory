package com.byz.factory.scm.service;

import com.byz.factory.scm.model.PurchaseOrder;
import com.byz.factory.scm.model.Supplier;

import java.math.BigDecimal;
import java.util.List;

/**
 * SCM 供应链管理服务 — 管理供应商主数据和采购订单全生命周期。
 * <p>
 * 职责：
 * <ul>
 *   <li>供应商注册、资质审核、状态管理</li>
 *   <li>采购订单创建、审批、发送、收货跟踪、完成</li>
 *   <li>供应商绩效评分</li>
 * </ul>
 * <p>
 * 跨模块协作：
 * <ul>
 *   <li>SCM → WMS：来料计划触发收货准备</li>
 *   <li>SCM ← WMS：收货结果更新 PO 进度</li>
 *   <li>SCM → ERP：采购成本回传财务应付</li>
 *   <li>SCM → QMS：供应商批次关联来料检验</li>
 * </ul>
 *
 * @author 苏政
 */
public interface ScmService {

    // ==================== 供应商管理 ====================

    /** 注册新供应商 */
    Supplier registerSupplier(String code, String name, String category);

    /** 查询供应商 */
    Supplier findSupplier(String code);

    /** 获取所有合格供应商 */
    List<Supplier> getQualifiedSuppliers();

    /** 通过资质审核 */
    void qualifySupplier(String code);

    /** 取消资质 */
    void disqualifySupplier(String code);

    /** 暂停合作 */
    void deactivateSupplier(String code);

    /** 恢复合作 */
    void reactivateSupplier(String code);

    // ==================== 采购订单管理 ====================

    /** 创建采购订单（初始状态 DRAFT） */
    PurchaseOrder createPurchaseOrder(String poNo, String supplierCode);

    /** 审批采购订单（DRAFT → APPROVED） */
    void approvePurchaseOrder(String poNo, String approvedBy);

    /** 发送采购订单给供应商（APPROVED → SENT） */
    void sendPurchaseOrder(String poNo);

    /** 记录收货——更新 PO 行已收数量，自动推进状态 */
    void receivePurchaseOrder(String poNo, String materialCode, BigDecimal receivedQty);

    /** 取消采购订单（DRAFT/APPROVED → CANCELLED） */
    void cancelPurchaseOrder(String poNo);

    /** 查询采购订单 */
    PurchaseOrder findPurchaseOrder(String poNo);

    /** 获取供应商的所有采购订单 */
    List<PurchaseOrder> getPurchaseOrdersBySupplier(String supplierCode);

}
