package com.byz.factory.dms.model;

import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 审批工作流 — 文档的多步骤审批流程。
 * <p>
 * 继承 BaseEntity 管理审批流的创建和时间追踪。
 * 每个 Document 可关联一个审批流，审批流包含多个有序的 {@link ApprovalStep}。
 * <p>
 * <h3>IExpand 约定</h3>
 * <pre>
 *   dms.wf.documentCode  — 关联文档编码
 *   dms.wf.initiator     — 发起人
 *   dms.wf.priority      — 审批优先级
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovalWorkflow extends BaseEntity {

    /** 关联文档编码 */
    private String documentCode;

    /** 审批步骤列表（按 stepNo 排序） */
    private List<ApprovalStep> steps;

    /** 发起人 */
    private String initiator;

    /** 完成时间 */
    private Instant completedAt;

    /**
     * @param documentCode 关联文档编码
     * @param initiator    发起人
     */
    public ApprovalWorkflow(String documentCode, String initiator) {
        super("WF-" + documentCode, "审批流-" + documentCode);
        this.documentCode = documentCode;
        this.initiator = initiator;
        this.steps = new ArrayList<>();
    }

    // ==================== 步骤管理 ====================

    /**
     * 添加审批步骤。
     *
     * @param approverRole 审批角色
     * @return 创建的步骤
     */
    public ApprovalStep addStep(String approverRole) {
        int stepNo = steps.size() + 1;
        ApprovalStep step = new ApprovalStep(stepNo, approverRole);
        this.steps.add(step);
        markUpdated();
        return step;
    }

    /**
     * 执行当前待审批步骤。
     *
     * @param approver  审批人
     * @param decision  审批决定
     * @param comment   审批意见
     * @return 被执行的步骤，若无非待审批步骤则返回空
     */
    public Optional<ApprovalStep> executeCurrentStep(String approver,
                                                      ApprovalStep.ApprovalDecision decision,
                                                      String comment) {
        return findPendingStep().map(step -> {
            step.execute(approver, decision, comment);
            if (isCompleted()) {
                this.completedAt = Instant.now();
            }
            markUpdated();
            return step;
        });
    }

    /**
     * 查找第一个待审批步骤。
     */
    public Optional<ApprovalStep> findPendingStep() {
        return steps.stream()
                .sorted(Comparator.comparingInt(ApprovalStep::getStepNo))
                .filter(s -> s.getDecision() == ApprovalStep.ApprovalDecision.PENDING)
                .findFirst();
    }

    // ==================== 查询方法 ====================

    /**
     * 判断审批流是否已完成（全部步骤通过，或任一步骤拒绝/需修订即终止流程）。
     */
    public boolean isCompleted() {
        if (steps.isEmpty()) return false;
        // 任一步骤拒绝或需修订 → 流程终止
        if (isRejected()) return true;
        // 全部步骤非 PENDING → 全部通过
        return steps.stream().noneMatch(s -> s.getDecision() == ApprovalStep.ApprovalDecision.PENDING);
    }

    /**
     * 判断审批流是否全部批准通过。
     */
    public boolean isApproved() {
        if (steps.isEmpty()) return false;
        return steps.stream().allMatch(s -> s.getDecision() == ApprovalStep.ApprovalDecision.APPROVED);
    }

    /**
     * 判断审批流是否被拒绝（任一步骤拒绝或需修订）。
     */
    public boolean isRejected() {
        return steps.stream().anyMatch(s ->
                s.getDecision() == ApprovalStep.ApprovalDecision.REJECTED
                        || s.getDecision() == ApprovalStep.ApprovalDecision.NEEDS_REVISION);
    }

    /**
     * 获取当前步骤序号（第一个待审批步骤编号）。
     */
    public int getCurrentStepNo() {
        return findPendingStep().map(ApprovalStep::getStepNo).orElse(-1);
    }

    /**
     * 获取总步骤数。
     */
    public int getTotalSteps() {
        return steps.size();
    }

}
