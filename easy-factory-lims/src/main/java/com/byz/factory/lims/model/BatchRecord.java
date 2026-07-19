package com.byz.factory.lims.model;

import com.byz.factory.lims.BatchRecordStatus;
import com.byz.factory.lims.IBatchRecord;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.LimsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 批记录 — GMP 合规核心文档，记录一批产品从投料到产出的完整制造过程。
 * <p>
 * 继承 BaseLifecycleEntity 获得状态机（IN_PROGRESS→REVIEW→APPROVED→ARCHIVED），
 * 实现 IBatchRecord 供 MES/QMS/DMS 等模块编译期引用。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   lims.batch.batchNo          — 批号
 *   lims.batch.workOrderId      — 工单号
 *   lims.batch.formulaVersion   — 配方版本
 *   lims.batch.yield            — 实际收率
 *   lims.batch.reviewedBy       — 审核人
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BatchRecord extends BaseLifecycleEntity<BatchRecordStatus> implements IBatchRecord {

    /** 批号 */
    private String batchNo;

    /** 关联工单号 */
    private String workOrderId;

    /** 配方编码 */
    private String formulaCode;

    /** 配方版本（冻结——批记录关联的配方版本不可变） */
    private String formulaVersion;

    /** 产品编码 */
    private String productCode;

    /** 实际批量 */
    private BigDecimal batchSize;

    /** 实际收率 */
    private BigDecimal yield;

    /** 工序执行记录（引用键列表） */
    private List<String> processRecords;

    /** 关联称量任务号列表 */
    private List<String> weighingTasks;

    /** 关联检验记录列表 */
    private List<String> inspectionRecords;

    /** 偏差记录列表 */
    private List<String> deviations;

    /** 审核人 */
    private String reviewedBy;

    /** 审核时间 */
    private Instant reviewedAt;

    /**
     * @param batchNo        批号
     * @param workOrderId    工单号
     * @param formulaCode    配方编码
     * @param formulaVersion 配方版本
     * @param productCode    产品编码
     */
    public BatchRecord(String batchNo, String workOrderId, String formulaCode,
                       String formulaVersion, String productCode) {
        super(batchNo, "Batch-" + batchNo, BatchRecordStatus.IN_PROGRESS);
        this.batchNo = batchNo;
        this.workOrderId = workOrderId;
        this.formulaCode = formulaCode;
        this.formulaVersion = formulaVersion;
        this.productCode = productCode;
        this.processRecords = new ArrayList<>();
        this.weighingTasks = new ArrayList<>();
        this.inspectionRecords = new ArrayList<>();
        this.deviations = new ArrayList<>();
    }

    // ==================== 业务便捷方法（含事件发布） ====================

    /**
     * 提交审核（IN_PROGRESS → REVIEW）。
     */
    public void submitForReview() {
        transition(BatchRecordStatus.REVIEW);
        markUpdated();
        publishEvent(LimsEventTypes.BATCH_RECORD_CREATED, Map.of(
                "batchNo", batchNo,
                "workOrderId", workOrderId,
                "formulaCode", formulaCode,
                "formulaVersion", formulaVersion));
    }

    /**
     * 批准批记录（REVIEW → APPROVED）。
     *
     * @param reviewedBy 审核人
     */
    public void approve(String reviewedBy) {
        this.reviewedBy = reviewedBy;
        this.reviewedAt = Instant.now();
        transition(BatchRecordStatus.APPROVED);
        markUpdated();
        publishEvent(LimsEventTypes.BATCH_RECORD_APPROVED, Map.of(
                "batchNo", batchNo,
                "reviewedBy", reviewedBy,
                "reviewedAt", reviewedAt.toString()));
    }

    /**
     * 驳回重审（REVIEW → IN_PROGRESS）。
     *
     * @param reason 驳回原因
     */
    public void reject(String reason) {
        transition(BatchRecordStatus.IN_PROGRESS);
        markUpdated();
        setExpandProperty("lims.batch.rejectionReason", reason);
    }

    /**
     * 归档批记录（APPROVED → ARCHIVED）。
     */
    public void archive() {
        transition(BatchRecordStatus.ARCHIVED);
        markUpdated();
        publishEvent(LimsEventTypes.BATCH_RECORD_ARCHIVED, Map.of(
                "batchNo", batchNo));
    }

    // ==================== 记录添加方法 ====================

    /**
     * 添加工序执行记录。
     *
     * @param recordRef 工序记录引用
     */
    public void addProcessRecord(String recordRef) {
        if (this.processRecords == null) {
            this.processRecords = new ArrayList<>();
        }
        this.processRecords.add(recordRef);
        markUpdated();
    }

    /**
     * 添加关联称量任务。
     *
     * @param taskCode 称量任务号
     */
    public void addWeighingTask(String taskCode) {
        if (this.weighingTasks == null) {
            this.weighingTasks = new ArrayList<>();
        }
        this.weighingTasks.add(taskCode);
        markUpdated();
    }

    /**
     * 添加偏差记录。
     *
     * @param deviation 偏差描述/引用
     */
    public void addDeviation(String deviation) {
        if (this.deviations == null) {
            this.deviations = new ArrayList<>();
        }
        this.deviations.add(deviation);
        markUpdated();
    }

    // ==================== 查询方法 ====================

    /**
     * 判断批记录是否可编辑（仅在 IN_PROGRESS 状态可编辑）。
     */
    public boolean isEditable() {
        return getStatus() == BatchRecordStatus.IN_PROGRESS;
    }

    /**
     * 判断批记录是否已归档（终态）。
     */
    public boolean isArchived() {
        return getStatus() == BatchRecordStatus.ARCHIVED;
    }

    // ==================== 内部 ====================

    private void publishEvent(String eventType, Object payload) {
        IDomainEvent event = IDomainEvent.of(eventType, "lims", payload);
        DomainEventPublisher.publish(event);
    }

}
