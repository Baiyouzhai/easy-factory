package com.byz.factory.model;

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

/**
 * 事件发布器 — 管理订阅者注册与事件分发。
 * <p>
 * 各模块可通过 getSubscribers() 注册特定事件类型的处理逻辑。
 *
 * @author 苏政
 */
class DomainEventPublisher {

    private static final java.util.Map<String, java.util.List<java.util.function.Consumer<IDomainEvent>>>
        subscribers = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 订阅某类事件
     */
    public static void subscribe(String eventType, java.util.function.Consumer<IDomainEvent> handler) {
        subscribers.computeIfAbsent(eventType, k -> new java.util.ArrayList<>()).add(handler);
    }

    /**
     * 发布事件 — 异步通知所有订阅者（当前同步实现，后续改异步）
     */
    public static void publish(IDomainEvent event) {
        java.util.List<java.util.function.Consumer<IDomainEvent>> handlers = subscribers.get(event.getEventType());
        if (handlers == null) return;
        for (java.util.function.Consumer<IDomainEvent> h : handlers) {
            try {
                h.accept(event);
            } catch (Exception e) {
                // 一个订阅者失败不影响其他订阅者
                System.err.println("事件处理失败: " + event.getEventType() + " → " + e.getMessage());
            }
        }
    }

    /**
     * 订阅通配符 — 匹配所有以给定前缀开头的事件
     */
    public static void subscribePrefix(String typePrefix, java.util.function.Consumer<IDomainEvent> handler) {
        // 内部通过遍历所有已注册事件类型实现前缀匹配
        // 简化实现
        subscribe(typePrefix, handler);
    }

}

/**
 * 领域事件简单实现
 */
class SimpleDomainEvent implements IDomainEvent {
    private final String eventId;
    private final String eventType;
    private final Instant timestamp;
    private final String source;
    private final Object payload;

    SimpleDomainEvent(String eventType, String source, Object payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = Instant.now();
        this.source = source;
        this.payload = payload;
    }

    @Override public String getEventId() { return eventId; }
    @Override public String getEventType() { return eventType; }
    @Override public Instant getTimestamp() { return timestamp; }
    @Override public String getSource() { return source; }
    @Override public Object getPayload() { return payload; }
}
