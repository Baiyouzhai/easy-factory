package com.byz.factory.plm.model;

import com.byz.factory.lifecycle.ILifecycle;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 变更请求 — 工程变更请求/指令 (ECR/ECO)。
 * <p>
 * 管理蓝图变更的全生命周期：发起→评审→批准→实施→关闭。
 * 变更实施后，蓝图创建新版本（通过 {@link Blueprint#createNewVersion}）。
 *
 * <h3>状态机</h3>
 * <pre>
 *   DRAFT → SUBMITTED → APPROVED → IMPLEMENTED → CLOSED
 *                  ↓          ↓
 *              REJECTED   REJECTED
 * </pre>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   plm.cr.blueprintCode   — 关联的蓝图编码
 *   plm.cr.fromVersion     — 变更前的版本号
 *   plm.cr.toVersion       — 变更后的版本号（实施后填写）
 *   plm.cr.approvedBy      — 批准人（DMS 审批流回填）
 *   plm.cr.implementedAt   — 实施时间
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChangeRequest extends BaseLifecycleEntity<ChangeRequest.ChangeRequestStatus> {

    /** 变更请求状态枚举 */
    public enum ChangeRequestStatus implements ILifecycle.StatusEnum {
        /** 草稿 */
        DRAFT,
        /** 已提交评审 */
        SUBMITTED,
        /** 已批准 */
        APPROVED,
        /** 已实施 */
        IMPLEMENTED,
        /** 已关闭 */
        CLOSED,
        /** 已驳回 */
        REJECTED;

        @Override
        public Set<ChangeRequestStatus> allowedTransitions() {
            return switch (this) {
                case DRAFT       -> Set.of(SUBMITTED);
                case SUBMITTED   -> Set.of(APPROVED, REJECTED);
                case APPROVED    -> Set.of(IMPLEMENTED, REJECTED);
                case IMPLEMENTED -> Set.of(CLOSED);
                case CLOSED      -> Set.of();
                case REJECTED    -> Set.of(DRAFT);  // 驳回后可修改后重新提交
            };
        }
    }

    /** 关联的蓝图编码 */
    private String blueprintCode;

    /** 变更前的版本号 */
    private String fromVersion;

    /** 变更后的版本号（实施后设置） */
    private String toVersion;

    /** 变更原因（如：客户要求/工艺优化/法规更新/质量改进） */
    private String changeReason;

    /** 变更描述（详细说明改了什么） */
    private String description;

    /** 影响的维度（PROCESS / PARAMETER / RESOURCE，逗号分隔） */
    private String affectedDimensions;

    /** 批准人 */
    private String approvedBy;

    /** 申请人 */
    private String requestedBy;

    /**
     * @param code          变更请求编码（如 CR-2026-001）
     * @param blueprintCode 关联蓝图编码
     * @param fromVersion   变更前版本
     * @param changeReason  变更原因
     */
    public ChangeRequest(String code, String blueprintCode, String fromVersion, String changeReason) {
        super(code, "蓝图变更-" + blueprintCode, ChangeRequestStatus.DRAFT);
        this.blueprintCode = blueprintCode;
        this.fromVersion = fromVersion;
        this.changeReason = changeReason;
    }

    // ==================== 业务便捷方法 ====================

    /** 提交评审（DRAFT → SUBMITTED） */
    public void submit() {
        transition(ChangeRequestStatus.SUBMITTED);
        markUpdated();
    }

    /** 批准变更（SUBMITTED → APPROVED） */
    public void approve(String approvedBy) {
        this.approvedBy = approvedBy;
        transition(ChangeRequestStatus.APPROVED);
        markUpdated();
    }

    /** 驳回变更（SUBMITTED/APPROVED → REJECTED） */
    public void reject() {
        transition(ChangeRequestStatus.REJECTED);
        markUpdated();
    }

    /** 重新提交（REJECTED → DRAFT → SUBMITTED 的快捷方式） */
    public void resubmit() {
        transition(ChangeRequestStatus.DRAFT);
        transition(ChangeRequestStatus.SUBMITTED);
        markUpdated();
    }

    /** 标记已实施（APPROVED → IMPLEMENTED） */
    public void implement(String toVersion) {
        this.toVersion = toVersion;
        transition(ChangeRequestStatus.IMPLEMENTED);
        markUpdated();
    }

    /** 关闭变更请求（IMPLEMENTED → CLOSED） */
    public void close() {
        transition(ChangeRequestStatus.CLOSED);
        markUpdated();
    }

    /** 是否为终端状态 */
    public boolean isTerminal() {
        return getStatus() == ChangeRequestStatus.CLOSED
                || getStatus() == ChangeRequestStatus.REJECTED;
    }

}
