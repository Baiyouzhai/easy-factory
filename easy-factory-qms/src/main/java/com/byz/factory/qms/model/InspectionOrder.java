package com.byz.factory.qms.model;

import com.byz.data.DataExpand;
import com.byz.factory.model.IInspectionOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * QMS 检验指令实现
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionOrder extends DataExpand implements IInspectionOrder {

    private String inspectionNo;
    private String workOrderNo;
    private String batchNo;
    private String processCode;
    private String inspectionType;
    private String planCode;
    private String status;
    private int totalItems;
    private int completedItems;
    private int passedItems;

    public InspectionOrder(String inspectionNo, String batchNo, String processCode) {
        this.inspectionNo = inspectionNo;
        this.batchNo = batchNo;
        this.processCode = processCode;
        this.status = "PENDING";
    }

}
