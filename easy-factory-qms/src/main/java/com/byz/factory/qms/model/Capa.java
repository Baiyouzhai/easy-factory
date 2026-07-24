package com.byz.factory.qms.model;

import com.byz.factory.batch.CapaStatus;
import com.byz.factory.batch.ICapa;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.QmsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.Map;

/**
 * CAPA（纠正与预防措施）— QMS 核心实体，持续改进的根本解决方案。
 * <p>
 * 继承 BaseLifecycleEntity 获得状态机（OPEN→ROOT_CAUSE→IN_PROGRESS→VERIFIED→CLOSED），
 * 实现 ICapa 供 DMS/Andon/MES 等模块编译期引用。
 * 状态变更时自动通过 {@link DomainEventPublisher} 发布领域事件。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   qms.capa.problemDescription  — 问题描述
 *   qms.capa.verificationResult  — 效果验证结果
 *   qms.capa.approvedBy          — 批准人
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Capa extends BaseLifecycleEntity<CapaStatus> implements ICapa {

    /** 关联偏差编号 */
    private String deviationCode;

    /** 问题描述 */
    private String problemDescription;

    /** 根因分析结果 */
    private String rootCause;

    /** 纠正措施 */
    private String correctiveAction;

    /** 预防措施 */
    private String preventiveAction;

    /** 效果验证结果 */
    private String verification;

    /** 负责人 */
    private String assignTo;

    /** 批准人 */
    private String approvedBy;

    /** 计划完成日期 */
    private Instant dueDate;

    /** 关闭时间 */
    private Instant closedAt;

    /**
     * @param code              CAPA 编号
     * @param name              CAPA 名称
     * @param deviationCode     关联偏差编号
     * @param problemDescription 问题描述
     * @param assignTo           负责人
     */
    public Capa(String code, String name, String deviationCode,
                String problemDescription, String assignTo) {
        super(code, name, CapaStatus.OPEN);
        this.deviationCode = deviationCode;
        this.problemDescription = problemDescription;
        this.assignTo = assignTo;
    }

    // ==================== 业务便捷方法（含事件发布） ====================

    /**
     * 完成根因分析（OPEN → ROOT_CAUSE）。
     *
     * @param rootCause 根因分析结果（5Why / 鱼骨图等）
     */
    public void analyzeRootCause(String rootCause) {
        transition(CapaStatus.ROOT_CAUSE);
        this.rootCause = rootCause;
        markUpdated();
        publishEvent(QmsEventTypes.CAPA_ROOT_CAUSE_DONE, Map.of(
                "capaCode", getCode(),
                "rootCause", rootCause));
    }

    /**
     * 开始执行纠正和预防措施（ROOT_CAUSE → IN_PROGRESS）。
     *
     * @param correctiveAction 纠正措施
     * @param preventiveAction 预防措施
     */
    public void executeActions(String correctiveAction, String preventiveAction) {
        transition(CapaStatus.IN_PROGRESS);
        this.correctiveAction = correctiveAction;
        this.preventiveAction = preventiveAction;
        markUpdated();
        publishEvent(QmsEventTypes.CAPA_IN_PROGRESS, Map.of(
                "capaCode", getCode(),
                "correctiveAction", correctiveAction,
                "preventiveAction", preventiveAction));
    }

    /**
     * 完成效果验证（IN_PROGRESS → VERIFIED）。
     *
     * @param verification 验证结果
     * @param approvedBy   批准人
     */
    public void verify(String verification, String approvedBy) {
        transition(CapaStatus.VERIFIED);
        this.verification = verification;
        this.approvedBy = approvedBy;
        markUpdated();
        publishEvent(QmsEventTypes.CAPA_VERIFIED, Map.of(
                "capaCode", getCode(),
                "verification", verification,
                "approvedBy", approvedBy));
    }

    /**
     * 关闭 CAPA（VERIFIED → CLOSED）。
     */
    public void close() {
        transition(CapaStatus.CLOSED);
        this.closedAt = Instant.now();
        markUpdated();
        publishEvent(QmsEventTypes.CAPA_CLOSED, Map.of(
                "capaCode", getCode(),
                "closedAt", closedAt.toString()));
    }

    /**
     * 取消 CAPA（任意非终态 → CANCELLED）。
     *
     * @param reason 取消原因
     */
    public void cancel(String reason) {
        transition(CapaStatus.CANCELLED);
        markUpdated();
        setExpandProperty("qms.capa.cancelReason", reason);
    }

    // ==================== 查询方法 ====================

    /**
     * 判断 CAPA 是否逾期。
     */
    public boolean isOverdue() {
        if (dueDate == null) return false;
        CapaStatus s = getStatus();
        if (s == CapaStatus.CLOSED || s == CapaStatus.CANCELLED) return false;
        return Instant.now().isAfter(dueDate);
    }

    /**
     * 判断 CAPA 是否可编辑（仅 OPEN 和 ROOT_CAUSE 状态可编辑）。
     */
    public boolean isEditable() {
        CapaStatus s = getStatus();
        return s == CapaStatus.OPEN || s == CapaStatus.ROOT_CAUSE;
    }

    // ==================== 内部 ====================

    private void publishEvent(String eventType, Object payload) {
        IDomainEvent event = IDomainEvent.of(eventType, "qms", payload);
        DomainEventPublisher.publish(event);
    }

}
