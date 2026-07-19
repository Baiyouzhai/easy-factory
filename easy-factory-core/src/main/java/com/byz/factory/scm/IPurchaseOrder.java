package com.byz.factory.scm;

import java.time.LocalDate;
import java.util.List;

/**
 * 采购订单抽象 — SCM 采购订单实体的跨模块契约。
 * <p>
 * WMS 通过此接口获取来料计划；ERP 通过此接口获取应付数据。
 * 典型调用链：SCM 创建 PO → 发送供应商 → WMS 订阅收货 → 更新 PO 进度。
 *
 * @author 苏政
 * @see com.byz.factory.scm.model.PurchaseOrder
 */
public interface IPurchaseOrder {

    /** 采购单号 */
    String getPoNo();

    /** 供应商编码 */
    String getSupplierCode();

    /** 订单状态 */
    PurchaseOrderStatus getStatus();

    /** 采购明细 */
    List<? extends IPurchaseOrderItem> getItems();

    /** 审批人 */
    String getApprovedBy();

    /** 预计交付日期 */
    LocalDate getExpectedDelivery();

    /**
     * 采购订单明细项 — 单行物料采购信息。
     */
    interface IPurchaseOrderItem {

        /** 物料编码 */
        String getMaterialCode();

        /** 采购数量 */
        java.math.BigDecimal getQuantity();

        /** 单价 */
        java.math.BigDecimal getUnitPrice();

        /** 预计到货日期 */
        LocalDate getExpectedDate();

        /** 已收数量 */
        java.math.BigDecimal getReceivedQty();

    }

}
