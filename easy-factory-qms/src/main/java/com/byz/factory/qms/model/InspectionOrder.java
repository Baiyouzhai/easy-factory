package com.byz.factory.qms.model;

import com.byz.factory.batch.IInspectionOrder;
import com.byz.factory.batch.InspectionStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.QmsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QMS 检验指令 — 继承 BaseLifecycleEntity 获得状态机（PENDING→IN_PROGRESS→PASSED/FAILED→CLOSED）。
 * <p>
 * 实现 IInspectionOrder 供 MES/Andon/DMS/LIMS 等模块编译期引用。
 * 状态变更时自动通过 {@link DomainEventPublisher} 发布领域事件。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   qms.inspectionNo          — 检验编号
 *   qms.inspectionType        — IQC | IPQC | FQC | OQC
 *   qms.workOrderNo           — 工单号
 *   qms.batchNo               — 批号
 *   qms.processCode           — 工序编码
 *   qms.planCode              — 检验方案编码
 *   qms.inspector             — 检验员
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionOrder extends BaseLifecycleEntity<InspectionStatus> implements IInspectionOrder {

    /** 检验编号 */
    private String inspectionNo;

    /** 工单号 */
    private String workOrderNo;

    /** 批号 */
    private String batchNo;

    /** 工序编码（检验发生的工序） */
    private String processCode;

    /** 检验类型: IQC | IPQC | FQC | OQC */
    private String inspectionType;

    /** 检验方案编码 */
    private String planCode;

    /** 检验员 */
    private String inspector;

    /** 检验项目总数 */
    private int totalItems;

    /** 已完成项目数 */
    private int completedItems;

    /** 合格项目数 */
    private int passedItems;

    /** 检验明细记录列表 */
    private List<InspectionRecord> records;

    /** 开始检验时间 */
    private Instant startedAt;

    /** 检验完成时间 */
    private Instant completedAt;

    /**
     * @param inspectionNo 检验编号
     * @param batchNo      批号
     * @param processCode  工序编码
     */
    public InspectionOrder(String inspectionNo, String batchNo, String processCode) {
        super(inspectionNo, "检验指令-" + inspectionNo, InspectionStatus.PENDING);
        this.inspectionNo = inspectionNo;
        this.batchNo = batchNo;
        this.processCode = processCode;
        this.records = new ArrayList<>();
    }

    // ==================== 业务便捷方法（含事件发布） ====================

    /**
     * 开始检验（PENDING → IN_PROGRESS）。
     *
     * @param inspector 检验员
     */
    public void startInspection(String inspector) {
        transition(InspectionStatus.IN_PROGRESS);
        this.inspector = inspector;
        this.startedAt = Instant.now();
        markUpdated();
        publishEvent(QmsEventTypes.INSPECTION_STARTED,
                "inspectionNo", inspectionNo,
                "inspector", inspector,
                "startedAt", startedAt.toString());
    }

    /**
     * 提交检验结果 — 记录单条检验明细并更新进度。
     *
     * @param record 检验记录
     */
    public void submitResult(InspectionRecord record) {
        if (this.records == null) {
            this.records = new ArrayList<>();
        }
        record.setInspectionNo(this.inspectionNo);
        this.records.add(record);
        this.completedItems++;
        if ("PASS".equals(record.getJudgement())) {
            this.passedItems++;
        }
        markUpdated();
    }

    /**
     * 完成检验，自动判定结果（IN_PROGRESS → PASSED/FAILED）。
     * <p>
     * 所有项目通过 → PASSED；任一项目失败 → FAILED。
     */
    public void completeInspection() {
        if (passedItems == totalItems && completedItems == totalItems) {
            transition(InspectionStatus.PASSED);
            this.completedAt = Instant.now();
            markUpdated();
            publishEvent(QmsEventTypes.INSPECTION_PASSED,
                    "inspectionNo", inspectionNo,
                    "processCode", processCode,
                    "workOrderNo", workOrderNo,
                    "totalItems", totalItems,
                    "passedItems", passedItems);
        } else {
            transition(InspectionStatus.FAILED);
            this.completedAt = Instant.now();
            markUpdated();
            publishEvent(QmsEventTypes.INSPECTION_FAILED,
                    "inspectionNo", inspectionNo,
                    "processCode", processCode,
                    "workOrderNo", workOrderNo,
                    "totalItems", totalItems,
                    "passedItems", passedItems,
                    "failedItems", totalItems - passedItems);
        }
        publishEvent(QmsEventTypes.INSPECTION_COMPLETED,
                "inspectionNo", inspectionNo,
                "status", getStatus().name(),
                "totalItems", totalItems,
                "passedItems", passedItems);
    }

    /**
     * 关闭检验指令（PASSED/FAILED → CLOSED）。
     */
    public void close() {
        transition(InspectionStatus.CLOSED);
        markUpdated();
    }

    // ==================== 查询方法 ====================

    /**
     * 判断检验是否已完成（含 PASSED / FAILED / CLOSED）。
     */
    public boolean isFinished() {
        InspectionStatus s = getStatus();
        return s == InspectionStatus.PASSED || s == InspectionStatus.FAILED || s == InspectionStatus.CLOSED;
    }

    /**
     * 判定是否合格。
     */
    public boolean isPassed() {
        return getStatus() == InspectionStatus.PASSED || getStatus() == InspectionStatus.CLOSED;
    }

    /**
     * 获取不合格项目数量。
     */
    public int getFailedItems() {
        return totalItems - passedItems;
    }

    // ==================== 内部 ====================

    private void publishEvent(String eventType, Object... kvPairs) {
        Map<String, Object> payload = new HashMap<>();
        for (int i = 0; i < kvPairs.length; i += 2) {
            payload.put((String) kvPairs[i], kvPairs[i + 1]);
        }
        IDomainEvent event = IDomainEvent.of(eventType, "qms", payload);
        DomainEventPublisher.publish(event);
    }

}
