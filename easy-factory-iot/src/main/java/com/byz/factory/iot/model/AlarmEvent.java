package com.byz.factory.iot.model;

import com.byz.factory.iot.IAlarmEvent;
import com.byz.factory.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 设备报警事件 — 继承 BaseEntity 获得 code/name/audit，实现 IAlarmEvent。
 * <p>
 * IoT 将采集值与阈值对比，当实际值超出控制限时触发报警。
 * 报警通过 {@link com.byz.factory.event.DomainEventPublisher} 发布，
 * QMS/Andon/MES/EAM 异步订阅处理。
 * <p>
 * <h3>IExpand 约定</h3>
 * <pre>
 *   iot.alarm.equipmentCode   — 设备编码
 *   iot.alarm.triggerValue    — 触发值
 *   iot.alarm.threshold       — 阈值
 *   iot.alarm.actionTaken     — 已执行的联动动作
 * </pre>
 *
 * @author 苏政
 */
@Entity
@Table(name = "iot_alarm_event")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AlarmEvent extends BaseEntity implements IAlarmEvent {

    @Column(name = "equipment_code", nullable = false, length = 100)
    private String equipmentCode;

    @Column(name = "alarm_code", nullable = false, length = 100)
    private String alarmCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlarmSeverity severity;

    @Column(length = 500)
    private String message;

    @Column(name = "trigger_value", precision = 20, scale = 6)
    private BigDecimal triggerValue;

    @Column(precision = 20, scale = 6)
    private BigDecimal threshold;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    @Column(name = "acknowledged_by", length = 100)
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Convert(converter = JsonListConverter.class)
    @Column(columnDefinition = "text")
    private List<String> actions;

    /**
     * @param alarmCode     报警编码
     * @param equipmentCode 设备编码
     * @param severity      严重程度
     * @param triggerValue  触发值
     * @param threshold     阈值
     */
    public AlarmEvent(String alarmCode, String equipmentCode, AlarmSeverity severity,
                      BigDecimal triggerValue, BigDecimal threshold) {
        super(alarmCode, "报警-" + alarmCode);
        this.alarmCode = alarmCode;
        this.equipmentCode = equipmentCode;
        this.severity = severity;
        this.triggerValue = triggerValue;
        this.threshold = threshold;
        this.triggeredAt = Instant.now();
        this.actions = new ArrayList<>();
    }

    /** 创建工厂方法 — 同时设置报警消息 */
    public static AlarmEvent of(String alarmCode, String equipmentCode,
                                 AlarmSeverity severity, String message,
                                 BigDecimal triggerValue, BigDecimal threshold) {
        AlarmEvent alarm = new AlarmEvent(alarmCode, equipmentCode, severity, triggerValue, threshold);
        alarm.setMessage(message);
        return alarm;
    }

    // ==================== 业务便捷方法 ====================

    /** 确认报警 */
    public void acknowledge(String by) {
        this.acknowledgedBy = by;
        this.acknowledgedAt = Instant.now();
        markUpdated();
    }

    /** 恢复报警 */
    public void resolve() {
        this.resolvedAt = Instant.now();
        markUpdated();
    }

    /** 添加联动动作 */
    public void addAction(String action) {
        if (this.actions == null) {
            this.actions = new ArrayList<>();
        }
        this.actions.add(action);
        markUpdated();
    }

    /** 获取联动动作列表（不可修改） */
    public List<String> getActions() {
        if (actions == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(actions);
    }

    /** 是否已确认 */
    public boolean isAcknowledged() {
        return acknowledgedAt != null;
    }

    /** 是否已恢复 */
    public boolean isResolved() {
        return resolvedAt != null;
    }

}
