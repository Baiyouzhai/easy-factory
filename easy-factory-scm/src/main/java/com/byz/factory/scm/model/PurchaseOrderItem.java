package com.byz.factory.scm.model;

import com.byz.factory.scm.IPurchaseOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购订单明细 — 单行物料采购信息。
 *
 * @author 苏政
 */
@Data
public class PurchaseOrderItem implements IPurchaseOrder.IPurchaseOrderItem {

    /** 物料编码 */
    private String materialCode;

    /** 采购数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 预计到货日期 */
    private LocalDate expectedDate;

    /** 已收数量 */
    private BigDecimal receivedQty;

    /**
     * @param materialCode 物料编码
     * @param quantity     采购数量
     */
    public PurchaseOrderItem(String materialCode, BigDecimal quantity) {
        this.materialCode = materialCode;
        this.quantity = quantity;
        this.receivedQty = BigDecimal.ZERO;
    }

    /**
     * 记录收货，累加已收数量。
     *
     * @param qty 本次收货数量
     */
    public void receive(BigDecimal qty) {
        this.receivedQty = this.receivedQty.add(qty);
    }

    /**
     * 剩余待收数量。
     */
    public BigDecimal remaining() {
        return quantity.subtract(receivedQty);
    }

    /**
     * 是否已完全收货。
     */
    public boolean isFullyReceived() {
        return remaining().compareTo(BigDecimal.ZERO) <= 0;
    }

}
