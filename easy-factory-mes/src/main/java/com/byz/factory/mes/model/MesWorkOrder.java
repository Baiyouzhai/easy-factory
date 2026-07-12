package com.byz.factory.mes.model;

import com.byz.data.DataExpand;
import com.byz.factory.design.IBlueprint;
import com.byz.factory.model.IWorkOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MES 工单实现
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWorkOrder extends DataExpand implements IWorkOrder {

    private String workOrderNo;
    private String productCode;
    private IBlueprint blueprint;
    private BigDecimal quantity;
    private String batchNo;
    private String factoryCode;
    private String status;
    private Instant plannedStart;
    private Instant plannedEnd;
    private Instant actualStart;
    private Instant actualEnd;
    private Instant createdAt;

    public MesWorkOrder(String workOrderNo, String productCode, BigDecimal quantity) {
        this.workOrderNo = workOrderNo;
        this.productCode = productCode;
        this.quantity = quantity;
        this.status = "CREATED";
        this.createdAt = Instant.now();
    }

}
