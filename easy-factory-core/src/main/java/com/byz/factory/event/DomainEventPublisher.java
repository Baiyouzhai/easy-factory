package com.byz.factory.event;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 事件发布器 — 管理订阅者注册与事件分发。
 * <p>
 * 各模块可通过 subscribe() 注册特定事件类型的处理逻辑。
 *
 * @author 苏政
 */
public final class DomainEventPublisher {

    private static final Logger LOG = System.getLogger(DomainEventPublisher.class.getName());

    private static final Map<String, List<Consumer<IDomainEvent>>> subscribers = new ConcurrentHashMap<>();

    private DomainEventPublisher() {
    }

    /**
     * 订阅某类事件
     */
    public static void subscribe(String eventType, Consumer<IDomainEvent> handler) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    /**
     * 发布事件 — 异步通知所有订阅者（当前同步实现，后续改异步）
     */
    public static void publish(IDomainEvent event) {
        List<Consumer<IDomainEvent>> handlers = subscribers.get(event.getEventType());
        if (handlers == null) return;
        for (Consumer<IDomainEvent> h : handlers) {
            try {
                h.accept(event);
            } catch (Exception e) {
                LOG.log(Level.ERROR, "事件处理失败: " + event.getEventType() + " → " + e.getMessage(), e);
            }
        }
    }

    /**
     * 订阅通配符 — 匹配所有以给定前缀开头的事件
     */
    public static void subscribePrefix(String typePrefix, Consumer<IDomainEvent> handler) {
        subscribe(typePrefix, handler);
    }

}
