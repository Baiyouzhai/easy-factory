package com.byz.factory.wms.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Receipt extends DataExpand {
    private String receiptNo;
    private String sourceType;       // PURCHASE_ORDER/RETURN/TRANSFER
    private String referenceNo;
    private String supplierCode;
    private String status;           // RECEIVED/QUARANTINE/ACCEPTED/REJECTED
    private String receivedBy;
    private Instant receivedAt;
    private List<ReceiptItem> items;

    public record ReceiptItem(String materialCode, String batchNo, BigDecimal receivedQty,
                               BigDecimal orderedQty, String locationCode, String status) {}
}
