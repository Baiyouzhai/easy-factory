package com.byz.factory.mes.model;

import com.byz.factory.batch.ProcessStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 工序记录 — 工序在工单中的一次执行记录，汇总其下所有 ActionRecord。
 * <p>
 * 继承 BaseLifecycleEntity 获得 ProcessStatus 状态机（PENDING→IN_PROGRESS→COMPLETED），
 * 支持 QMS 中断（IN_PROGRESS→INTERRUPTED→IN_PROGRESS 恢复）。
 *
 * <h3>设计裁定（design-decisions.md §2.2）</h3>
 * <ul>
 *   <li>报工粒度是动作级——每次 Action 执行生成一条 ActionRecord</li>
 *   <li>ProcessRecord 是工序级聚合视图（汇总其下所有 ActionRecord）</li>
 * </ul>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   mes.processRecord.workOrderNo    — 所属工单号
 *   mes.processRecord.processCode    — 工序编码
 *   mes.processRecord.operator       — 操作人
 *   mes.processRecord.interruptedBy  — 中断触发方（QMS/Equip/Manual）
 *   mes.processRecord.interruptReason— 中断原因
 * </pre>
 *
 * <h3>注意</h3>
 * 模型层不发布领域事件——事件发布由 Service 实现层统一负责（design-decisions.md §1.1）。
 *
 * @author 苏政
 * @see ActionRecord
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessRecord extends BaseLifecycleEntity<ProcessStatus> {

    /** 所属工单号 */
    private String workOrderNo;

    /** 工序编码 */
    private String processCode;

    /** 实际开始时间 */
    private Instant actualStart;

    /** 实际结束时间 */
    private Instant actualEnd;

    /** 操作人 */
    private String operator;

    /** 中断触发方（QMS / Equip / Manual） */
    private String interruptedBy;

    /** 中断原因 */
    private String interruptReason;

    /** 动作执行记录列表（设计裁定 §2.2：动作级报工） */
    private List<ActionRecord> actions;

    /**
     * @param workOrderNo 所属工单号
     * @param processCode 工序编码
     */
    public ProcessRecord(String workOrderNo, String processCode) {
        super(workOrderNo + "-" + processCode, processCode, ProcessStatus.PENDING);
        this.workOrderNo = workOrderNo;
        this.processCode = processCode;
        this.actions = new ArrayList<>();
    }

    // ==================== 业务便捷方法（不发布事件） ====================

    /**
     * 开始执行工序。
     * <p>
     * PENDING → IN_PROGRESS。
     *
     * @param operator 操作人
     */
    public void start(String operator) {
        this.operator = operator;
        this.actualStart = Instant.now();
        transition(ProcessStatus.IN_PROGRESS);
        markUpdated();
    }

    /**
     * 中断工序 — QMS 检验 / 设备故障 / 人工暂停。
     * <p>
     * IN_PROGRESS → INTERRUPTED。
     *
     * @param interruptedBy   中断触发方（QMS / Equip / Manual）
     * @param interruptReason 中断原因
     */
    public void interrupt(String interruptedBy, String interruptReason) {
        this.interruptedBy = interruptedBy;
        this.interruptReason = interruptReason;
        transition(ProcessStatus.INTERRUPTED);
        markUpdated();
    }

    /**
     * 恢复工序 — 从中断状态恢复执行。
     * <p>
     * INTERRUPTED → IN_PROGRESS。
     */
    public void resume() {
        transition(ProcessStatus.IN_PROGRESS);
        markUpdated();
    }

    /**
     * 完成工序。
     * <p>
     * IN_PROGRESS → COMPLETED。
     */
    public void complete() {
        this.actualEnd = Instant.now();
        transition(ProcessStatus.COMPLETED);
        markUpdated();
    }

    /**
     * 跳过工序。
     * <p>
     * PENDING → SKIPPED。
     */
    public void skip() {
        transition(ProcessStatus.SKIPPED);
        markUpdated();
    }

    /**
     * 取消工序。
     */
    public void cancel() {
        transition(ProcessStatus.CANCELLED);
        markUpdated();
    }

    // ==================== 动作管理 ====================

    /**
     * 添加动作执行记录。
     *
     * @param actionRecord 动作记录
     */
    public void addAction(ActionRecord actionRecord) {
        if (this.actions == null) {
            this.actions = new ArrayList<>();
        }
        this.actions.add(actionRecord);
        markUpdated();
    }

    // ==================== 查询方法 ====================

    /**
     * 是否在运行（IN_PROGRESS 或 INTERRUPTED）。
     */
    public boolean isRunning() {
        ProcessStatus s = getStatus();
        return s == ProcessStatus.IN_PROGRESS || s == ProcessStatus.INTERRUPTED;
    }

    /**
     * 是否已中断。
     */
    public boolean isInterrupted() {
        return getStatus() == ProcessStatus.INTERRUPTED;
    }

    /**
     * 是否已完成（终态）。
     */
    public boolean isFinished() {
        ProcessStatus s = getStatus();
        return s == ProcessStatus.COMPLETED || s == ProcessStatus.SKIPPED || s == ProcessStatus.CANCELLED;
    }

}
