package com.byz.factory.dms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 审批步骤 — 审批流中的单个审批节点（JPA 实体）。
 * <p>
 * 每个步骤按 stepNo 顺序依次执行，记录审批角色、审批人、决定和意见。
 *
 * @author 苏政
 */
@Entity
@Table(name = "dms_approval_step")
@Data
@NoArgsConstructor
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联审批流 ID */
    @Column(name = "workflow_id", nullable = false)
    private Long workflowId;

    /** 步骤序号 */
    @Column(name = "step_no", nullable = false)
    private int stepNo;

    /** 审批角色 */
    @Column(name = "approver_role", nullable = false, length = 100)
    private String approverRole;

    /** 审批人 */
    @Column(length = 100)
    private String approver;

    /** 审批决定 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalDecision decision;

    /** 审批意见 */
    @Column(length = 500)
    private String comment;

    /** 审批时间 */
    private Instant timestamp;

    /**
     * 创建待审批步骤。
     */
    public ApprovalStep(int stepNo, String approverRole) {
        this.stepNo = stepNo;
        this.approverRole = approverRole;
        this.decision = ApprovalDecision.PENDING;
    }

    /**
     * 执行此步骤的审批。
     */
    public void execute(String approver, ApprovalDecision decision, String comment) {
        this.approver = approver;
        this.decision = decision;
        this.comment = comment;
        this.timestamp = Instant.now();
    }

    /**
     * 审批决定枚举。
     */
    public enum ApprovalDecision {
        PENDING,
        APPROVED,
        REJECTED,
        NEEDS_REVISION
    }

}
