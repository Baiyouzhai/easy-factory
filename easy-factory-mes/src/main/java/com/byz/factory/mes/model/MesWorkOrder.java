package com.byz.factory.mes.model;

import com.byz.factory.batch.IWorkOrder;
import com.byz.factory.batch.WorkOrderStatus;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MES 工单实现 — 继承 BaseLifecycleEntity 获得状态机 + 审计能力。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWorkOrder extends BaseLifecycleEntity<WorkOrderStatus> implements IWorkOrder {

    private String workOrderNo;
    private String productCode;
    private IBlueprint blueprint;
    private BigDecimal quantity;
    private String batchNo;
    private String factoryCode;
    private Instant plannedStart;
    private Instant plannedEnd;
    private Instant actualStart;
    private Instant actualEnd;

    public MesWorkOrder(String workOrderNo, String productCode, BigDecimal quantity) {
        super(workOrderNo, productCode, WorkOrderStatus.CREATED);
        this.workOrderNo = workOrderNo;
        this.productCode = productCode;
        this.quantity = quantity;
    }

}
