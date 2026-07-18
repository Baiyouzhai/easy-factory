package com.byz.factory.qms.model;

import com.byz.factory.batch.IInspectionOrder;
import com.byz.factory.batch.InspectionStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * QMS 检验指令实现 — 继承 BaseLifecycleEntity 获得状态机 + 审计能力。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionOrder extends BaseLifecycleEntity<InspectionStatus> implements IInspectionOrder {

    private String inspectionNo;
    private String workOrderNo;
    private String batchNo;
    private String processCode;
    private String inspectionType;
    private String planCode;
    private int totalItems;
    private int completedItems;
    private int passedItems;

    public InspectionOrder(String inspectionNo, String batchNo, String processCode) {
        super(inspectionNo, batchNo, InspectionStatus.PENDING);
        this.inspectionNo = inspectionNo;
        this.batchNo = batchNo;
        this.processCode = processCode;
    }

}
