package com.byz.factory.wms;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 收货单抽象 — WMS 收货单实体的跨模块契约。
 * <p>
 * SCM 通过此接口跟踪采购订单收货进度；QMS 通过此接口获取来料检验任务。
 * 典型调用链：SCM 创建 PO → 供应商送货 → WMS 创建 Receipt → QMS 来料检 → 上架入库。
 *
 * @author 苏政
 * @see com.byz.factory.wms.model.Receipt
 */
public interface IReceipt {

    /** 收货单号 */
    String getReceiptNo();

    /** 来源类型（PURCHASE_ORDER / RETURN / TRANSFER） */
    String getSourceType();

    /** 来源单号（采购订单号/退货单号/转储单号） */
    String getReferenceNo();

    /** 供应商编码 */
    String getSupplierCode();

    /** 收货人 */
    String getReceivedBy();

    /** 收货时间 */
    Instant getReceivedAt();

    /** 收货状态 */
    ReceiptStatus getStatus();

    /** 收货明细 */
    List<? extends IReceiptItem> getItems();

    /**
     * 收货明细项 — 单行物料收货信息。
     */
    interface IReceiptItem {

        /** 物料编码 */
        String getMaterialCode();

        /** 供应商批号 */
        String getBatchNo();

        /** 实收数量 */
        BigDecimal getReceivedQty();

        /** 订单数量 */
        BigDecimal getOrderedQty();

        /** 上架库位 */
        String getLocationCode();

        /** 物料质量状态 */
        MaterialStatus getStatus();

    }

}
