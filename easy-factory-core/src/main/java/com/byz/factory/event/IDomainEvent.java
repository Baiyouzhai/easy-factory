package com.byz.factory.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件 — 模块间异步通信的基本单元。
 * <p>
 * 事件发布后，订阅者异步处理。典型事件：ProcessStarted, ActionExecuted,
 * ProcessInterrupted, DeviationResolved, BatchCompleted, EquipmentAlarm,
 * BlueprintReleased。
 *
 * @author 苏政
 * @see DomainEventPublisher
 */
public interface IDomainEvent {

    /** 事件唯一ID */
    String getEventId();

    /** 事件类型，格式: {module}.{action}.{past_tense} 如 "mes.process.started" */
    String getEventType();

    /** 事件发生时间戳 */
    Instant getTimestamp();

    /** 事件来源（模块名/实例ID） */
    String getSource();

    /** 事件负载（业务数据） */
    Object getPayload();

    /**
     * 创建新事件的工厂方法
     */
    static IDomainEvent of(String eventType, String source, Object payload) {
        return new SimpleDomainEvent(eventType, source, payload);
    }

}
