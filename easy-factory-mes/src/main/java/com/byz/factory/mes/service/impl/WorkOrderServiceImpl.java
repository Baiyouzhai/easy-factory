package com.byz.factory.mes.service.impl;

import com.byz.factory.batch.ProcessStatus;
import com.byz.factory.batch.WorkOrderStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.MesEventTypes;
import com.byz.factory.exception.ActionException;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.mes.model.ActionRecord;
import com.byz.factory.mes.model.MesWorkOrder;
import com.byz.factory.mes.model.ProcessRecord;
import com.byz.factory.mes.service.BlueprintProvider;
import com.byz.factory.mes.service.WorkOrderService;
import com.byz.factory.process.IAction;
import com.byz.factory.process.IProcess;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.IResourcePack;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工单服务实现 — 内存存储，事件驱动。
 * <p>
 * 实现 design-decisions.md 四项 MES 裁定：
 * <ul>
 *   <li>§2.1 蓝图版本冻结 — release() 时记录 blueprintVersion</li>
 *   <li>§2.2 动作级报工 — reportAction() 生成 ActionRecord</li>
 *   <li>§2.3 并行工序 — executeProcess() 检查 ExecutionMode</li>
 *   <li>§2.4 QMS 中断 — interruptProcess() + resumeProcess()</li>
 * </ul>
 *
 * <h3>事件发布（design-decisions.md §1.1）</h3>
 * 领域事件由 Service 实现层统一发布，Model 层不调用 publishEvent()。
 *
 * <h3>线程安全</h3>
 * 使用 ConcurrentHashMap 保证基本线程安全。生产环境应替换为数据库。
 *
 * @author 苏政
 */
public class WorkOrderServiceImpl implements WorkOrderService {

    // ==================== 内存存储 ====================

    private final Map<String, MesWorkOrder> workOrderStore = new ConcurrentHashMap<>();
    private final Map<String, ProcessRecord> processRecordStore = new ConcurrentHashMap<>();
    private final Map<String, List<String>> workOrderProcessIndex = new ConcurrentHashMap<>(); // woNo → [prId,...]

    private final BlueprintProvider blueprintProvider;
    private final AtomicInteger woSequence = new AtomicInteger(1);

    /**
     * @param blueprintProvider 蓝图供应者（PLM 适配器或测试桩）
     */
    public WorkOrderServiceImpl(BlueprintProvider blueprintProvider) {
        this.blueprintProvider = blueprintProvider;
    }

    // ==================== 工单生命周期 ====================

    @Override
    public MesWorkOrder create(String blueprintCode, String productCode, BigDecimal quantity) {
        String workOrderNo = generateWorkOrderNo();
        MesWorkOrder wo = new MesWorkOrder(workOrderNo, productCode, quantity);
        wo.setExpandProperty("mes.blueprintCode", blueprintCode);
        workOrderStore.put(workOrderNo, wo);

        publishEvent(MesEventTypes.WORKORDER_CREATED, Map.of(
                "workOrderNo", workOrderNo,
                "productCode", productCode,
                "blueprintCode", blueprintCode,
                "quantity", quantity));
        return wo;
    }

    @Override
    public MesWorkOrder release(String workOrderNo, String operator,
                                Instant plannedStart, Instant plannedEnd, String factoryCode) {
        MesWorkOrder wo = requireWorkOrder(workOrderNo);
        if (wo.getStatus() != WorkOrderStatus.CREATED) {
            throw new IllegalStateException(
                    "工单 " + workOrderNo + " 状态为 " + wo.getStatus() + "，无法下达（需 CREATED）");
        }

        // 获取蓝图并冻结版本号（design-decisions.md §2.1）
        String blueprintCode = (String) wo.getExpandProperty("mes.blueprintCode");
        IBlueprint blueprint = blueprintProvider.resolve(blueprintCode, null);
        if (blueprint == null) {
            throw new ActionException("蓝图 " + blueprintCode + " 不存在，无法下达工单 " + workOrderNo);
        }

        wo.release(blueprint, operator, plannedStart, plannedEnd, factoryCode);

        publishEvent(MesEventTypes.WORKORDER_RELEASED, Map.of(
                "workOrderNo", workOrderNo,
                "blueprintCode", blueprint.getCode(),
                "blueprintVersion", wo.getBlueprintVersion(),
                "operator", operator,
                "factoryCode", factoryCode,
                "plannedStart", plannedStart.toString(),
                "plannedEnd", plannedEnd.toString()));
        return wo;
    }

    @Override
    public MesWorkOrder start(String workOrderNo) {
        MesWorkOrder wo = requireWorkOrder(workOrderNo);
        if (wo.getStatus() != WorkOrderStatus.RELEASED) {
            throw new IllegalStateException(
                    "工单 " + workOrderNo + " 状态为 " + wo.getStatus() + "，无法开工（需 RELEASED）");
        }

        wo.start();

        publishEvent(MesEventTypes.WORKORDER_STARTED, Map.of(
                "workOrderNo", workOrderNo,
                "startTime", wo.getActualStart().toString()));
        return wo;
    }

    @Override
    public MesWorkOrder complete(String workOrderNo) {
        MesWorkOrder wo = requireWorkOrder(workOrderNo);
        if (wo.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "工单 " + workOrderNo + " 状态为 " + wo.getStatus() + "，无法完工（需 IN_PROGRESS）");
        }

        // 检查所有工序是否完成
        List<ProcessRecord> allPrs = findProcessRecords(workOrderNo);
        if (allPrs.isEmpty()) {
            throw new IllegalStateException(
                    "工单 " + workOrderNo + " 无工序记录，无法完工（请先执行工序）");
        }
        boolean allFinished = allPrs.stream().allMatch(ProcessRecord::isFinished);
        if (!allFinished) {
            throw new IllegalStateException(
                    "工单 " + workOrderNo + " 仍有未完成的工序，无法完工");
        }

        wo.complete();

        publishEvent(MesEventTypes.WORKORDER_COMPLETED, Map.of(
                "workOrderNo", workOrderNo,
                "endTime", wo.getActualEnd().toString(),
                "actualQuantity", wo.getQuantity()));
        return wo;
    }

    @Override
    public MesWorkOrder close(String workOrderNo) {
        MesWorkOrder wo = requireWorkOrder(workOrderNo);
        if (wo.getStatus() != WorkOrderStatus.COMPLETED) {
            throw new IllegalStateException(
                    "工单 " + workOrderNo + " 状态为 " + wo.getStatus() + "，无法关闭（需 COMPLETED）");
        }

        wo.close();

        publishEvent(MesEventTypes.WORKORDER_CLOSED, Map.of("workOrderNo", workOrderNo));
        return wo;
    }

    // ==================== 工序流转 ====================

    @Override
    public IResourcePack executeProcess(String workOrderNo, String processCode, IResourcePack input) {
        MesWorkOrder wo = requireWorkOrder(workOrderNo);
        if (wo.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "工单 " + workOrderNo + " 状态为 " + wo.getStatus() + "，无法执行工序（需 IN_PROGRESS）");
        }

        // 通过蓝图获取工序定义（design-decisions.md §2.1：运行时按版本号解析）
        IBlueprint blueprint = resolveBlueprint(wo);
        IProcess process = findProcess(blueprint, processCode);

        // 创建或获取工序记录
        String prId = processRecordId(workOrderNo, processCode);
        ProcessRecord pr = processRecordStore.computeIfAbsent(prId,
                k -> new ProcessRecord(workOrderNo, processCode));
        workOrderProcessIndex
                .computeIfAbsent(workOrderNo, k -> new ArrayList<>())
                .add(prId);

        if (pr.getStatus() == ProcessStatus.PENDING) {
            pr.start(wo.getOperator());
            publishEvent(MesEventTypes.PROCESS_STARTED, Map.of(
                    "workOrderNo", workOrderNo, "processCode", processCode));
        }

        // 执行动作链（design-decisions.md §2.3：检查并行模式）
        List<IAction> actions = process.getActions();
        if (actions == null || actions.isEmpty()) {
            throw new ActionException("工序 " + processCode + " 动作清单为空");
        }

        IResourcePack output = input;
        String operator = wo.getOperator();
        String batchNo = wo.getBatchNo() != null ? wo.getBatchNo() : workOrderNo;

        for (IAction action : actions) {
            // 检查是否处于中断状态（外部可能已暂停）
            if (pr.isInterrupted()) {
                break;
            }

            String actionCode = action.getCode();
            ActionRecord ar = new ActionRecord(prId, actionCode, workOrderNo,
                    batchNo, processCode, operator, action.getCode());

            // 记录执行前快照
            String beforeSnapshot = snapshotResources(output);
            ar.recordSnapshot(beforeSnapshot, null);

            // 执行动作
            IResourceItem[] actionInputs = output != null ? output.getResources() : new IResourceItem[0];
            output = action.execute(process, actionInputs);

            // 记录执行后快照
            String afterSnapshot = snapshotResources(output);
            ar.complete("PASS", beforeSnapshot, afterSnapshot, null);
            pr.addAction(ar);

            // 发布动作报工事件
            publishEvent(MesEventTypes.ACTION_COMPLETED, Map.of(
                    "workOrderNo", workOrderNo,
                    "processCode", processCode,
                    "actionCode", actionCode,
                    "result", "PASS"));
        }

        // 如果未被中断，完成工序
        if (!pr.isInterrupted()) {
            pr.complete();
            publishEvent(MesEventTypes.PROCESS_COMPLETED, Map.of(
                    "workOrderNo", workOrderNo,
                    "processCode", processCode));
        }

        return output;
    }

    @Override
    public ActionRecord reportAction(String workOrderNo, String processCode, String actionCode,
                                     String result, String remark) {
        MesWorkOrder wo = requireWorkOrder(workOrderNo);
        String prId = processRecordId(workOrderNo, processCode);
        ProcessRecord pr = processRecordStore.get(prId);
        if (pr == null) {
            throw new ActionException("工序记录不存在: " + prId + "，请先执行 executeProcess");
        }

        String batchNo = wo.getBatchNo() != null ? wo.getBatchNo() : workOrderNo;
        String operator = wo.getOperator();
        String operationType = actionCode; // 动作编码即操作类型

        ActionRecord ar = new ActionRecord(prId, actionCode, workOrderNo,
                batchNo, processCode, operator, operationType);
        ar.complete(result, null, null, remark);
        pr.addAction(ar);

        publishEvent(MesEventTypes.ACTION_COMPLETED, Map.of(
                "workOrderNo", workOrderNo,
                "processCode", processCode,
                "actionCode", actionCode,
                "result", result,
                "remark", remark != null ? remark : ""));
        return ar;
    }

    // ==================== QMS 中断恢复（design-decisions.md §2.4） ====================

    /**
     * 中断工序 — 由外部（QMS/Equip/Andon）在检测到问题时调用。
     * <p>
     * QMS 流程：检验不合格 → Control.Interrupt → 调用此方法 → 发布 mes.process.interrupted
     * → QMS 订阅创建 InspectionOrder → 判定后回调 resumeProcess()
     *
     * @param workOrderNo     工单号
     * @param processCode     工序编码
     * @param interruptedBy   中断触发方（QMS / Equip / Manual）
     * @param interruptReason 中断原因
     * @return 中断后的工序记录
     */
    public ProcessRecord interruptProcess(String workOrderNo, String processCode,
                                          String interruptedBy, String interruptReason) {
        String prId = processRecordId(workOrderNo, processCode);
        ProcessRecord pr = processRecordStore.get(prId);
        if (pr == null) {
            throw new ActionException("工序记录不存在: " + prId);
        }
        if (pr.getStatus() != ProcessStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "工序 " + processCode + " 状态为 " + pr.getStatus() + "，无法中断（需 IN_PROGRESS）");
        }

        pr.interrupt(interruptedBy, interruptReason);

        publishEvent(MesEventTypes.PROCESS_INTERRUPTED, Map.of(
                "workOrderNo", workOrderNo,
                "processCode", processCode,
                "interruptedBy", interruptedBy,
                "interruptReason", interruptReason));
        return pr;
    }

    @Override
    public ProcessRecord resumeProcess(String workOrderNo, String processCode) {
        String prId = processRecordId(workOrderNo, processCode);
        ProcessRecord pr = processRecordStore.get(prId);
        if (pr == null) {
            throw new ActionException("工序记录不存在: " + prId);
        }
        if (pr.getStatus() != ProcessStatus.INTERRUPTED) {
            throw new IllegalStateException(
                    "工序 " + processCode + " 状态为 " + pr.getStatus() + "，无法恢复（需 INTERRUPTED）");
        }

        pr.resume();

        publishEvent(MesEventTypes.PROCESS_RESUMED, Map.of(
                "workOrderNo", workOrderNo,
                "processCode", processCode));
        return pr;
    }

    // ==================== 查询 ====================

    @Override
    public MesWorkOrder findByWorkOrderNo(String workOrderNo) {
        return workOrderStore.get(workOrderNo);
    }

    @Override
    public List<ProcessRecord> findProcessRecords(String workOrderNo) {
        List<String> prIds = workOrderProcessIndex.getOrDefault(workOrderNo, List.of());
        List<ProcessRecord> result = new ArrayList<>();
        for (String prId : prIds) {
            ProcessRecord pr = processRecordStore.get(prId);
            if (pr != null) {
                result.add(pr);
            }
        }
        return result;
    }

    @Override
    public List<ActionRecord> findActionRecords(String processRecordId) {
        ProcessRecord pr = processRecordStore.get(processRecordId);
        if (pr == null || pr.getActions() == null) {
            return List.of();
        }
        return List.copyOf(pr.getActions());
    }

    // ==================== 内部辅助 ====================

    /** 生成工单号 */
    private String generateWorkOrderNo() {
        return "WO-" + Instant.now().toString().replace(":", "").replace("-", "")
                .substring(0, 15) + "-" + String.format("%03d", woSequence.getAndIncrement());
    }

    /** 生成工序记录ID */
    static String processRecordId(String workOrderNo, String processCode) {
        return workOrderNo + "-" + processCode;
    }

    /** 获取工单，不存在抛异常 */
    private MesWorkOrder requireWorkOrder(String workOrderNo) {
        MesWorkOrder wo = workOrderStore.get(workOrderNo);
        if (wo == null) {
            throw new ActionException("工单不存在: " + workOrderNo);
        }
        return wo;
    }

    /** 按工单冻结版本解析蓝图 */
    private IBlueprint resolveBlueprint(MesWorkOrder wo) {
        String blueprintCode = (String) wo.getExpandProperty("mes.blueprintCode");
        IBlueprint blueprint = blueprintProvider.resolve(blueprintCode, wo.getBlueprintVersion());
        if (blueprint == null) {
            throw new ActionException("蓝图 " + blueprintCode + " v" + wo.getBlueprintVersion() + " 不存在");
        }
        return blueprint;
    }

    /** 从蓝图中查找工序 */
    private IProcess findProcess(IBlueprint blueprint, String processCode) {
        return blueprint.getProductionProcessList().stream()
                .filter(p -> p.getCode().equals(processCode))
                .findFirst()
                .orElseThrow(() -> new ActionException(
                        "蓝图 " + blueprint.getCode() + " 中不存在工序: " + processCode));
    }

    /** 资源包快照 */
    private String snapshotResources(IResourcePack pack) {
        if (pack == null || pack.isEmpty()) {
            return "{}";
        }
        IResourceItem[] items = pack.getResources();
        if (items == null || items.length == 0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(items[i].toJsonString());
        }
        sb.append("]");
        return sb.toString();
    }

    /** 发布领域事件 */
    private void publishEvent(String eventType, Object payload) {
        IDomainEvent event = IDomainEvent.of(eventType, "mes", payload);
        DomainEventPublisher.publish(event);
    }

    // ==================== 测试辅助（仅测试使用） ====================

    /**
     * 清空所有存储（仅供测试）。
     */
    public void clearAll() {
        workOrderStore.clear();
        processRecordStore.clear();
        workOrderProcessIndex.clear();
        woSequence.set(1);
    }

    /**
     * 获取当前存储的工单数量（仅供测试）。
     */
    public int workOrderCount() {
        return workOrderStore.size();
    }

    /**
     * 获取当前存储的工序记录数量（仅供测试）。
     */
    public int processRecordCount() {
        return processRecordStore.size();
    }

}
