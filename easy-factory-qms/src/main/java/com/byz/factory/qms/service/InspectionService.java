package com.byz.factory.qms.service;

import com.byz.factory.qms.model.*;

import java.util.List;

/**
 * 检验服务接口 — QMS 模块核心。
 * <p>
 * 提供检验全流程管理：方案管理 → 指令创建 → 检验执行 → 结果判定。
 * <b>跨模块协作方向：</b>
 * <ul>
 *   <li>订阅 {@code mes.process.started} — 自动创建检验指令</li>
 *   <li>订阅 {@code mes.action.completed} — 关联检验动作的检验记录</li>
 *   <li>发布 {@code qms.inspection.passed} — 通知 MES 恢复工序</li>
 *   <li>发布 {@code qms.inspection.failed} — 通知 MES 暂停工序 + QMS 创建偏差</li>
 * </ul>
 *
 * @author 苏政
 */
public interface InspectionService {

    // ── 检验方案管理 ──

    /** 创建检验方案 */
    InspectionPlan createPlan(String code, String name, String productCode,
                              String processCode, String inspectionType);

    /** 按产品编码查询检验方案 */
    List<InspectionPlan> findPlansByProduct(String productCode);

    /** 按产品和工序查询检验方案 */
    InspectionPlan findPlanByProductAndProcess(String productCode, String processCode);

    /** 获取检验方案 */
    InspectionPlan getPlan(String planCode);

    // ── 检验指令管理 ──

    /** 创建检验指令 */
    InspectionOrder createOrder(String workOrderNo, String batchNo,
                                String processCode, String planCode);

    /** 开始检验 */
    InspectionOrder startInspection(String inspectionNo, String inspector);

    /** 提交单条检验记录 */
    InspectionOrder submitResult(String inspectionNo, InspectionRecord record);

    /** 完成检验，自动判定 PASS/FAIL */
    InspectionOrder completeInspection(String inspectionNo);

    /** 关闭检验指令 */
    InspectionOrder closeOrder(String inspectionNo);

    /** 按检验编号查询 */
    InspectionOrder findOrder(String inspectionNo);

    /** 按工单号查询检验列表 */
    List<InspectionOrder> findOrdersByWorkOrder(String workOrderNo);

    // ── 检验记录查询 ──

    /** 获取检验指令的全部记录 */
    List<InspectionRecord> getRecords(String inspectionNo);

}
