package com.byz.factory.crm.model;

import com.byz.factory.batch.WorkOrderStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 销售订单 — 协作层实体，客户需求→MPS 生产计划的桥梁。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SalesOrder extends BaseLifecycleEntity<WorkOrderStatus> {

    private String customerCode;
    private String productCode;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private Instant requiredDate;
    private Instant committedDate;
    private String priority;        // NORMAL/RUSH/EXPRESS
    private String gxpRequirements;
    private String specialInstructions;

    public SalesOrder(String orderNo, String customerCode, String productCode, BigDecimal quantity) {
        super(orderNo, "订单-" + orderNo, WorkOrderStatus.CREATED);
        this.customerCode = customerCode;
        this.productCode = productCode;
        this.quantity = quantity;
        this.priority = "NORMAL";
    }

}
