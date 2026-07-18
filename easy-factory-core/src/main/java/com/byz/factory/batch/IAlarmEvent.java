package com.byz.factory.batch;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 设备报警事件抽象 — 供 QMS/Andon/MES/EAM 跨模块引用设备报警。
 * <p>
 * IoT 将采集值与阈值对比，触发报警事件。报警通过 {@link com.byz.factory.event.DomainEventPublisher}
 * 发布，各业务模块异步订阅处理。
 * <p>
 * <b>谁来引用？</b>
 * <ul>
 *   <li><b>QMS</b> — 设备参数异常 → 自动发起 SPC 偏差调查</li>
 *   <li><b>Andon</b> — 触发安灯呼叫（CRITICAL/EMERGENCY 级别）</li>
 *   <li><b>MES</b> — 设备报警 → 暂停当前工序执行</li>
 *   <li><b>EAM</b> — EMERGENCY 级别 → 自动生成紧急维护工单</li>
 * </ul>
 *
 * @author 苏政
 */
public interface IAlarmEvent {

    /** 报警严重程度 — 参照 EEMUA 191 / ISA-18.2 */
    enum AlarmSeverity {
        /** 信息 — 无需立即处理 */
        INFO,
        /** 警告 — 需关注，可能恶化 */
        WARNING,
        /** 严重 — 需立即处理 */
        CRITICAL,
        /** 紧急 — 安全/质量事故风险 */
        EMERGENCY
    }

    /** 设备编码 */
    String getEquipmentCode();

    /** 报警编码（如 TEMP_HIGH、PRESS_LOW） */
    String getAlarmCode();

    /** 严重程度 */
    AlarmSeverity getSeverity();

    /** 报警消息 */
    String getMessage();

    /** 触发值 */
    BigDecimal getTriggerValue();

    /** 阈值 */
    BigDecimal getThreshold();

    /** 触发时间 */
    Instant getTriggeredAt();

    /** 确认人（null=未确认） */
    String getAcknowledgedBy();

    /** 确认时间（null=未确认） */
    Instant getAcknowledgedAt();

    /** 恢复时间（null=未恢复） */
    Instant getResolvedAt();

    /** 联动动作列表（如 "STOP_MACHINE", "NOTIFY_SUPERVISOR", "CREATE_QMS_DEVIATION"） */
    List<String> getActions();

}
