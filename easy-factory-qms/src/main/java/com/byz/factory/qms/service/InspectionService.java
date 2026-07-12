package com.byz.factory.qms.service;

import com.byz.factory.qms.model.InspectionOrder;

/**
 * 检验服务接口 — QMS 模块核心。
 * <p>
 * TODO 待实现：检验方案管理、检验记录、判定逻辑、偏差发起
 *
 * @author 苏政
 */
public interface InspectionService {

    /** 创建检验指令 */
    InspectionOrder createOrder(String workOrderNo, String batchNo, String processCode, String planCode);

    /** 提交检验值 */
    InspectionOrder submitResult(String inspectionNo, String itemCode, double measuredValue);

    /** 判定（PASS/CONCESSION/FAIL） */
    String judge(String inspectionNo);

}
