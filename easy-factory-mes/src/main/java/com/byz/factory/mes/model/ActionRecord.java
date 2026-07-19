package com.byz.factory.mes.model;

import com.byz.factory.batch.ITraceable;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * 动作记录 — 每个动作执行后生成一条记录。
 * <p>
 * 继承 BaseEntity（无独立状态机，生命周期由 ProcessRecord 驱动），
 * 实现 ITraceable 提供 before/after 资源快照，满足 GMP 追溯合规要求。
 *
 * <h3>设计裁定（design-decisions.md §2.2）</h3>
 * 报工粒度是 <b>动作级</b>——每次 Action 执行生成一条 ActionRecord，
 * 与 ITraceable 的 before/after 快照粒度一致。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   mes.actionRecord.processRecordId  — 所属工序记录ID
 *   mes.actionRecord.actionCode       — 动作编码
 *   mes.actionRecord.workOrderNo      — 所属工单号
 *   mes.actionRecord.batchNo          — 所属批号
 *   mes.actionRecord.operator         — 操作人
 *   mes.actionRecord.result           — 执行结果
 * </pre>
 *
 * @author 苏政
 * @see ProcessRecord
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActionRecord extends BaseEntity implements ITraceable {

    /** 所属工序记录ID（关联 ProcessRecord） */
    private String processRecordId;

    /** 动作编码 */
    private String actionCode;

    /** 所属工单号 */
    private String workOrderNo;

    /** 所属批号 */
    private String batchNo;

    /** 所属工序编码 */
    private String processCode;

    /** 操作人 */
    private String operator;

    /** 操作时间戳 */
    private Instant timestamp;

    /** 操作类型（ADD / USE / CONVERT / SPLIT...），对应 Dict.Execute */
    private String operationType;

    /** 操作前的资源快照（JSON） */
    private String beforeSnapshot;

    /** 操作后的资源快照（JSON） */
    private String afterSnapshot;

    /** 执行开始时间 */
    private Instant startTime;

    /** 执行结束时间 */
    private Instant endTime;

    /** 执行结果（PASS / FAIL / WARNING 或自定义） */
    private String result;

    /** 备注 */
    private String remark;

    /**
     * 无参构造（框架需要）。
     */
    public ActionRecord() {
        super();
    }

    /**
     * @param processRecordId 所属工序记录ID
     * @param actionCode      动作编码
     * @param workOrderNo     所属工单号
     * @param batchNo         所属批号
     * @param processCode     工序编码
     * @param operator        操作人
     * @param operationType   操作类型
     */
    public ActionRecord(String processRecordId, String actionCode, String workOrderNo,
                        String batchNo, String processCode, String operator, String operationType) {
        super(processRecordId + "-" + actionCode, actionCode);
        this.processRecordId = processRecordId;
        this.actionCode = actionCode;
        this.workOrderNo = workOrderNo;
        this.batchNo = batchNo;
        this.processCode = processCode;
        this.operator = operator;
        this.operationType = operationType;
        this.timestamp = Instant.now();
    }

    // ==================== 业务方法 ====================

    /**
     * 完成动作记录。
     *
     * @param result          执行结果
     * @param beforeSnapshot  操作前资源快照（JSON）
     * @param afterSnapshot   操作后资源快照（JSON）
     * @param remark          备注
     */
    public void complete(String result, String beforeSnapshot, String afterSnapshot, String remark) {
        this.endTime = Instant.now();
        this.result = result;
        this.beforeSnapshot = beforeSnapshot;
        this.afterSnapshot = afterSnapshot;
        this.remark = remark;
        markUpdated();
    }

    /**
     * 记录快照（用于中间步骤，不标记完成）。
     *
     * @param beforeSnapshot 操作前资源快照（JSON）
     * @param afterSnapshot  操作后资源快照（JSON）
     */
    public void recordSnapshot(String beforeSnapshot, String afterSnapshot) {
        this.beforeSnapshot = beforeSnapshot;
        this.afterSnapshot = afterSnapshot;
        markUpdated();
    }

    // ==================== ITraceable 实现 ====================

    /**
     * 获取操作时间戳。
     * <p>
     * 优先返回 endTime（动作完成时间），未完成时返回 timestamp（创建时间）。
     */
    @Override
    public Instant getTimestamp() {
        return endTime != null ? endTime : timestamp;
    }

    // ==================== 查询方法 ====================

    /**
     * 是否已完成（已设置结果）。
     */
    public boolean isCompleted() {
        return result != null && endTime != null;
    }

    /**
     * 是否有资源快照。
     */
    public boolean hasSnapshot() {
        return beforeSnapshot != null && afterSnapshot != null;
    }

}
