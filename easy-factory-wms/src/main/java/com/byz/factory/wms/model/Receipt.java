package com.byz.factory.wms.model;

import com.byz.factory.batch.ReceiptStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 收货单 — 继承 BaseLifecycleEntity 获得状态机（PENDING→PARTIAL→COMPLETED→CLOSED）。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Receipt extends BaseLifecycleEntity<ReceiptStatus> {

    private String sourceType;
    private String referenceNo;
    private String supplierCode;
    private String receivedBy;
    private Instant receivedAt;
    private List<ReceiptItem> items;

    public Receipt(String receiptNo, String referenceNo, String supplierCode) {
        super(receiptNo, "收货-" + receiptNo, ReceiptStatus.PENDING);
        this.referenceNo = referenceNo;
        this.supplierCode = supplierCode;
    }

    public record ReceiptItem(String materialCode, String batchNo, BigDecimal receivedQty,
                               BigDecimal orderedQty, String locationCode, String status) {}
}
