package com.byz.factory.qms.model;

import com.byz.factory.batch.*;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.QmsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.Map;

/**
 * 偏差 — QMS 核心实体，记录生产过程中的质量偏差。
 * <p>
 * 继承 BaseLifecycleEntity 获得状态机（OPEN→INVESTIGATING→DISPOSITIONED→RESOLVED→CLOSED），
 * 实现 IDeviation 供 MES/Andon/DMS/EAM 等模块编译期引用。
 * 状态变更时自动通过 {@link DomainEventPublisher} 发布领域事件。
 * <p>
 * 偏差处理流程（design-decisions.md §3.3）：
 * MES 自动暂停 → QMS 自动创建 Deviation → <b>人工判定</b>（QA 决定处置方式）→ CAPA（如需要）→ 通知 MES 恢复。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   qms.deviation.source       — 偏差来源
 *   qms.deviation.inspectionNo — 关联检验单号
 *   qms.deviation.capaCode     — 关联 CAPA 编号
 *   qms.deviation.rootCause    — 根因
 *   qms.deviation.resolvedAt   — 解决时间
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Deviation extends BaseLifecycleEntity<DeviationStatus> implements IDeviation {

    /** 偏差来源（如：检验不合格、设备异常、人为差错） */
    private String source;

    /** 关联的检验单编号 */
    private String inspectionNo;

    /** 关联的工单号 */
    private String workOrderNo;

    /** 关联的批号 */
    private String batchNo;

    /** 关联的工序编码 */
    private String processCode;

    /** 偏差严重程度 */
    private DeviationSeverity severity;

    /** 产品影响评估 */
    private String productImpact;

    /** 处置方式 */
    private DeviationDisposition disposition;

    /** 处置说明 */
    private String dispositionNote;

    /** 关联的 CAPA 编号 */
    private String capaCode;

    /** 调查人 */
    private String investigator;

    /** 处置人（QA） */
    private String disposedBy;

    /** 根因分析 */
    private String rootCause;

    /** 关闭时间 */
    private Instant closedAt;

    /**
     * @param code          偏差编号
     * @param name          偏差名称
     * @param source        偏差来源
     * @param inspectionNo  关联检验单号
     * @param severity      严重程度
     */
    public Deviation(String code, String name, String source,
                     String inspectionNo, DeviationSeverity severity) {
        super(code, name, DeviationStatus.OPEN);
        this.source = source;
        this.inspectionNo = inspectionNo;
        this.severity = severity;
    }

    // ==================== 业务便捷方法（含事件发布） ====================

    /**
     * 开始调查（OPEN → INVESTIGATING）。
     *
     * @param investigator 调查人
     */
    public void startInvestigation(String investigator) {
        transition(DeviationStatus.INVESTIGATING);
        this.investigator = investigator;
        markUpdated();
        publishEvent(QmsEventTypes.DEVIATION_CREATED, Map.of(
                "deviationCode", getCode(),
                "inspectionNo", inspectionNo,
                "severity", severity.name(),
                "source", source));
    }

    /**
     * 完成调查（INVESTIGATING → INVESTIGATING，记录根因和影响评估）。
     *
     * @param rootCause      根因分析结果
     * @param productImpact  产品影响评估
     */
    public void completeInvestigation(String rootCause, String productImpact) {
        this.rootCause = rootCause;
        this.productImpact = productImpact;
        markUpdated();
        publishEvent(QmsEventTypes.DEVIATION_INVESTIGATED, Map.of(
                "deviationCode", getCode(),
                "rootCause", rootCause,
                "productImpact", productImpact));
    }

    /**
     * QA 处置判定（INVESTIGATING → DISPOSITIONED）。
     * <p>
     * 由 QA 人工判定处置方式（design-decisions.md §3.3）。
     *
     * @param disposition     处置方式（返工/让步/拒收）
     * @param dispositionNote 处置说明
     * @param disposedBy      处置人
     */
    public void dispose(DeviationDisposition disposition, String dispositionNote, String disposedBy) {
        transition(DeviationStatus.DISPOSITIONED);
        this.disposition = disposition;
        this.dispositionNote = dispositionNote;
        this.disposedBy = disposedBy;
        markUpdated();
        publishEvent(QmsEventTypes.DEVIATION_DISPOSITIONED, Map.of(
                "deviationCode", getCode(),
                "disposition", disposition.name(),
                "disposedBy", disposedBy));
    }

    /**
     * 关联 CAPA（DISPOSITIONED → RESOLVED）。
     * <p>
     * 当处置需要根本原因改进时，创建 CAPA 并关联到此偏差。
     *
     * @param capaCode CAPA 编号
     */
    public void linkCapa(String capaCode) {
        this.capaCode = capaCode;
        markUpdated();
    }

    /**
     * 标记已解决（DISPOSITIONED → RESOLVED）。
     */
    public void resolve() {
        transition(DeviationStatus.RESOLVED);
        markUpdated();
        publishEvent(QmsEventTypes.DEVIATION_RESOLVED, Map.of(
                "deviationCode", getCode(),
                "resolvedAt", Instant.now().toString()));
    }

    /**
     * 关闭偏差（RESOLVED → CLOSED）。
     */
    public void close() {
        transition(DeviationStatus.CLOSED);
        this.closedAt = Instant.now();
        markUpdated();
        publishEvent(QmsEventTypes.DEVIATION_CLOSED, Map.of(
                "deviationCode", getCode(),
                "closedAt", closedAt.toString()));
    }

    /**
     * 取消偏差（任意非终态 → CANCELLED）。
     *
     * @param reason 取消原因
     */
    public void cancel(String reason) {
        transition(DeviationStatus.CANCELLED);
        markUpdated();
        setExpandProperty("qms.deviation.cancelReason", reason);
    }

    // ==================== 查询方法 ====================

    /**
     * 判断是否需要创建 CAPA。
     * <p>
     * CRITICAL 严重偏差和 MAJOR 重大偏差（非让步）需要 CAPA。
     */
    public boolean requiresCapa() {
        if (severity == DeviationSeverity.CRITICAL) return true;
        if (severity == DeviationSeverity.MAJOR
                && disposition != DeviationDisposition.CONCESSION) return true;
        return false;
    }

    /**
     * 判断偏差是否可编辑（仅 OPEN 状态可编辑）。
     */
    public boolean isEditable() {
        return getStatus() == DeviationStatus.OPEN;
    }

    // ==================== 内部 ====================

    private void publishEvent(String eventType, Object payload) {
        IDomainEvent event = IDomainEvent.of(eventType, "qms", payload);
        DomainEventPublisher.publish(event);
    }

}
