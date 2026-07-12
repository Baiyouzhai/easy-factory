package com.byz.factory.wms.service;

import com.byz.factory.wms.model.Receipt;
import com.byz.factory.wms.model.Storage;
import java.math.BigDecimal;

public interface WmsService {
    Receipt createReceipt(String referenceNo, String supplierCode);
    void acceptAndPutaway(String receiptNo, String locationCode);
    void createPickingTask(String workOrderNo, String batchNo);
    BigDecimal queryAvailableStock(String materialCode, String batchNo);
}
