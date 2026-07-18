package com.byz.factory.dms.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/**
 * 审批步骤 — 审批流中的单个审批节点。
 * <p>
 * 记录审批角色、审批人、决定和意见。
 * 每个步骤按 stepNo 顺序依次执行。
 *
 * @author 苏政
 */
@Data
@AllArgsConstructor
public class ApprovalStep {

    /** 步骤序号 */
    private int stepNo;

    /** 审批角色 */
    private String approverRole;

    /** 审批人 */
    private String approver;

    /** 审批决定 */
    private ApprovalDecision decision;

    /** 审批意见 */
    private String comment;

    /** 审批时间 */
    private Instant timestamp;

    /**
     * 创建待审批步骤。
     *
     * @param stepNo       步骤序号
     * @param approverRole 审批角色
     */
    public ApprovalStep(int stepNo, String approverRole) {
        this.stepNo = stepNo;
        this.approverRole = approverRole;
        this.decision = ApprovalDecision.PENDING;
    }

    /**
     * 执行此步骤的审批。
     *
     * @param approver  审批人
     * @param decision  审批决定
     * @param comment   审批意见
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
        /** 待审批 */
        PENDING,
        /** 已批准 */
        APPROVED,
        /** 已拒绝 */
        REJECTED,
        /** 需要修订 */
        NEEDS_REVISION
    }

}
