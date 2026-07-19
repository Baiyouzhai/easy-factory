package com.byz.factory.mes.service;

import com.byz.factory.mes.model.ActionRecord;
import com.byz.factory.mes.model.MesWorkOrder;
import com.byz.factory.mes.model.ProcessRecord;
import com.byz.factory.resource.IResourcePack;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 工单服务接口 — MES 模块的核心业务接口。
 * <p>
 * 工单生命周期：创建 → 下达(冻结蓝图版本) → 开工 → 工序流转 → 报工 → 完工 → 关闭。
 * <p>
 * 事件发布由 Service 实现类统一负责（design-decisions.md §1.1），
 * 模型层仅负责状态转换和审计。
 *
 * <h3>QMS 中断流程（design-decisions.md §2.4）</h3>
 * <pre>
 * MES 执行到检验动作 → Control.Interrupt → 暂停工序
 *   → publish("mes.process.interrupted")
 *   → QMS 订阅 → 创建 InspectionOrder → 执行检验
 *   → QMS 判定: PASS → publish("qms.inspection.passed") → MES 订阅 → resumeProcess()
 *              FAIL → publish("qms.deviation.created") → CAPA → resolve → resumeProcess()
 * </pre>
 *
 * @author 苏政
 */
public interface WorkOrderService {

    // ==================== 工单生命周期 ====================

    /**
     * 创建工单 — 从蓝图创建新工单。
     *
     * @param blueprintCode 蓝图编码
     * @param productCode   产品编码
     * @param quantity      计划数量
     * @return 新创建的工单（状态: CREATED）
     */
    MesWorkOrder create(String blueprintCode, String productCode, BigDecimal quantity);

    /**
     * 下达工单 — 冻结蓝图版本号（design-decisions.md §2.1）。
     * <p>
     * 工单发布时记录当前 Blueprint 的 version，运行时按 blueprintCode + blueprintVersion
     * 从 PLM 获取 Blueprint。后续蓝图变更不影响已发布工单。
     *
     * @param workOrderNo  工单号
     * @param operator     操作人
     * @param plannedStart 计划开始时间
     * @param plannedEnd   计划结束时间
     * @param factoryCode  执行工厂
     * @return 已下达的工单（状态: RELEASED）
     */
    MesWorkOrder release(String workOrderNo, String operator,
                         Instant plannedStart, Instant plannedEnd, String factoryCode);

    /**
     * 开工 — 记录实际开始时间。
     *
     * @param workOrderNo 工单号
     * @return 已开工的工单（状态: IN_PROGRESS）
     */
    MesWorkOrder start(String workOrderNo);

    /**
     * 完工 — 所有工序执行完毕。
     *
     * @param workOrderNo 工单号
     * @return 已完工的工单（状态: COMPLETED）
     */
    MesWorkOrder complete(String workOrderNo);

    /**
     * 关闭工单 — 终态。
     *
     * @param workOrderNo 工单号
     * @return 已关闭的工单（状态: CLOSED）
     */
    MesWorkOrder close(String workOrderNo);

    // ==================== 工序流转 ====================

    /**
     * 执行指定工序 — 遍历工序的动作链，按 ExecutionMode 执行。
     * <p>
     * SEQUENTIAL: 串行执行所有动作<br>
     * PARALLEL: 并发提交（每个分支必须使用不同工位/设备）
     *
     * @param workOrderNo 工单号
     * @param processCode 工序编码
     * @param input       输入资源包（上一工序的输出）
     * @return 本工序的输出资源包
     */
    IResourcePack executeProcess(String workOrderNo, String processCode, IResourcePack input);

    /**
     * 报工 — 对指定动作进行报工，生成 ActionRecord（design-decisions.md §2.2）。
     * <p>
     * 报工粒度是动作级，与 ITraceable 的 before/after 快照粒度一致。
     *
     * @param workOrderNo 工单号
     * @param processCode 工序编码
     * @param actionCode  动作编码
     * @param result      执行结果
     * @param remark      备注
     * @return 生成的 ActionRecord
     */
    ActionRecord reportAction(String workOrderNo, String processCode, String actionCode,
                              String result, String remark);

    // ==================== QMS 中断恢复 ====================

    /**
     * 恢复工序 — 从中断状态恢复执行（design-decisions.md §2.4）。
     * <p>
     * 由 QMS/Equip 的外部订阅方在问题解决后调用。
     * 超时未恢复时自动升级为 Andon 异常（预留）。
     *
     * @param workOrderNo 工单号
     * @param processCode 工序编码
     * @return 恢复后的 ProcessRecord（状态: IN_PROGRESS）
     */
    ProcessRecord resumeProcess(String workOrderNo, String processCode);

    // ==================== 查询 ====================

    /**
     * 根据工单号查询工单。
     *
     * @param workOrderNo 工单号
     * @return 工单，不存在返回 null
     */
    MesWorkOrder findByWorkOrderNo(String workOrderNo);

    /**
     * 查询工单的所有工序记录。
     *
     * @param workOrderNo 工单号
     * @return 工序记录列表
     */
    List<ProcessRecord> findProcessRecords(String workOrderNo);

    /**
     * 查询工序下的所有动作记录。
     *
     * @param processRecordId 工序记录ID
     * @return 动作记录列表
     */
    List<ActionRecord> findActionRecords(String processRecordId);

}
