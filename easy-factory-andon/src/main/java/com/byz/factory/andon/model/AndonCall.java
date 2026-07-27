package com.byz.factory.andon.model;

import com.byz.factory.batch.AndonStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.AndonEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.Map;

/**
 * 安灯呼叫 — 产线异常实时呼叫与逐级上报的核心实体。
 * <p>
 * 继承 BaseLifecycleEntity 获得状态机（OPEN→ACKNOWLEDGED→RESOLVED/ESCALATED→CLOSED）。
 * 当设备故障、质量异常、物料短缺、安全事件或工序超时发生时，操作工或系统自动触发 Andon 呼叫。
 * 呼叫确认后超时未响应则逐级上报，严重时自动暂停产线。
 * <p>
 * 与其它模块联动（design-decisions.md §3.4）：
 * <ul>
 *   <li>EQUIPMENT_FAULT → 通知 EAM 创建维护工单</li>
 *   <li>QUALITY_ISSUE → 通知 QMS 创建偏差</li>
 *   <li>EMERGENCY → 通知 MES 暂停工单/产线</li>
 * </ul>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   andon.call.escalationReason   — 上报原因
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "andon_call")
public class AndonCall extends BaseLifecycleEntity<AndonStatus> {

    /** 呼叫来源（人工触发/系统自动） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AndonSource source;

    /** 触发类型 */
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 30)
    private TriggerType triggerType;

    /** 关联工单号 */
    @Column(name = "work_order_no", length = 100)
    private String workOrderNo;

    /** 关联工序编码 */
    @Column(name = "process_code", length = 100)
    private String processCode;

    /** 关联设备编码 */
    @Column(name = "equipment_code", length = 100)
    private String equipmentCode;

    /** 严重程度 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AndonSeverity severity;

    /** 呼叫描述 */
    @Column(length = 500)
    private String description;

    /** 触发人 */
    @Column(name = "triggered_by", nullable = false, length = 100)
    private String triggeredBy;

    /** 触发时间 */
    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    /** 当前上报级别（0=未上报，1/2/3/4 逐级递增） */
    @Column(name = "escalation_level")
    private int escalationLevel;

    /** 确认人 */
    @Column(name = "acknowledged_by", length = 100)
    private String acknowledgedBy;

    /** 确认时间 */
    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    /** 解决方案 */
    @Column(length = 1000)
    private String resolution;

    /** 解决时间 */
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /** 关闭时间 */
    @Column(name = "closed_at")
    private Instant closedAt;

    /** JPA 要求无参构造 */
    public AndonCall() {
        super();
    }

    /**
     * @param code         呼叫编号
     * @param triggerType  触发类型
     * @param severity     严重程度
     * @param source       呼叫来源
     * @param triggeredBy  触发人
     * @param description  呼叫描述
     */
    public AndonCall(String code, TriggerType triggerType, AndonSeverity severity,
                     AndonSource source, String triggeredBy, String description) {
        super(code, "Andon-" + code, AndonStatus.OPEN);
        this.triggerType = triggerType;
        this.severity = severity;
        this.source = source;
        this.triggeredBy = triggeredBy;
        this.description = description;
        this.triggeredAt = Instant.now();
        this.escalationLevel = 0;
    }

    // ==================== 业务便捷方法（含事件发布） ====================

    /**
     * 确认呼叫（OPEN → ACKNOWLEDGED）。
     *
     * @param acknowledgedBy 确认人
     */
    public void acknowledge(String acknowledgedBy) {
        transition(AndonStatus.ACKNOWLEDGED);
        this.acknowledgedBy = acknowledgedBy;
        this.acknowledgedAt = Instant.now();
        markUpdated();
        publishEvent(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, Map.of(
                "callCode", getCode(),
                "acknowledgedBy", acknowledgedBy,
                "acknowledgedAt", acknowledgedAt.toString()));
    }

    /**
     * 逐级上报（OPEN/ACKNOWLEDGED → ESCALATED 或已在 ESCALATED 状态下继续升级）。
     *
     * @param reason 上报原因（如：超时未响应）
     */
    public void escalate(String reason) {
        AndonStatus current = getStatus();
        if (current == AndonStatus.CLOSED || current == AndonStatus.RESOLVED) {
            throw new IllegalStateException(
                    "无法从 " + current + " 状态执行上报操作");
        }
        if (current == AndonStatus.OPEN) {
            transition(AndonStatus.ESCALATED);
        } else if (current == AndonStatus.ACKNOWLEDGED) {
            transition(AndonStatus.ESCALATED);
        }
        this.escalationLevel++;
        markUpdated();
        setExpandProperty("andon.call.escalationReason", reason);
        publishEvent(AndonEventTypes.ANDON_CALL_ESCALATED, Map.of(
                "callCode", getCode(),
                "escalationLevel", escalationLevel,
                "escalatedAt", Instant.now().toString(),
                "reason", reason));
    }

    /**
     * 标记已解决（ACKNOWLEDGED/ESCALATED → RESOLVED）。
     *
     * @param resolution 解决方案描述
     */
    public void resolve(String resolution) {
        transition(AndonStatus.RESOLVED);
        this.resolution = resolution;
        this.resolvedAt = Instant.now();
        markUpdated();
        publishEvent(AndonEventTypes.ANDON_CALL_RESOLVED, Map.of(
                "callCode", getCode(),
                "resolution", resolution,
                "resolvedAt", resolvedAt.toString()));
    }

    /**
     * 关闭呼叫（RESOLVED → CLOSED 或 OPEN → CLOSED 直接关闭）。
     */
    public void close() {
        transition(AndonStatus.CLOSED);
        this.closedAt = Instant.now();
        markUpdated();
        publishEvent(AndonEventTypes.ANDON_CALL_CLOSED, Map.of(
                "callCode", getCode(),
                "closedAt", closedAt.toString()));
    }

    // ==================== 查询方法 ====================

    /** 判断呼叫是否处于活跃状态（未关闭）。 */
    public boolean isActive() {
        return getStatus() != AndonStatus.CLOSED;
    }

    /** 判断呼叫是否需要立即响应（CRITICAL 或 EMERGENCY）。 */
    public boolean isUrgent() {
        return severity == AndonSeverity.CRITICAL || severity == AndonSeverity.EMERGENCY;
    }

    /** 判断呼叫是否由系统自动触发。 */
    public boolean isAutoTriggered() {
        return source == AndonSource.AUTO;
    }

    /** 获取呼叫持续时长（秒）。 */
    public long getDurationSeconds() {
        if (triggeredAt == null) return 0;
        Instant end = (resolvedAt != null) ? resolvedAt : Instant.now();
        return end.getEpochSecond() - triggeredAt.getEpochSecond();
    }

    // ==================== 字段设置 ====================

    /** 关联工单。 */
    public void linkWorkOrder(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    /** 关联工序。 */
    public void linkProcess(String processCode) {
        this.processCode = processCode;
    }

    /** 关联设备。 */
    public void linkEquipment(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    // ==================== 内部 ====================

    private void publishEvent(String eventType, Object payload) {
        IDomainEvent event = IDomainEvent.of(eventType, AndonEventTypes.PREFIX, payload);
        DomainEventPublisher.publish(event);
    }

}
