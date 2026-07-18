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
    private static final Map<String, List<Consumer<IDomainEvent>>> prefixSubscribers = new ConcurrentHashMap<>();

    private DomainEventPublisher() {
    }

    /**
     * 订阅某类事件
     */
    public static void subscribe(String eventType, Consumer<IDomainEvent> handler) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    /**
     * 发布事件 — 通知精确匹配订阅者 + 前缀匹配订阅者。
     */
    public static void publish(IDomainEvent event) {
        String eventType = event.getEventType();

        // 精确匹配
        List<Consumer<IDomainEvent>> exactHandlers = subscribers.get(eventType);
        if (exactHandlers != null) {
            for (Consumer<IDomainEvent> h : exactHandlers) {
                invokeHandler(event, h);
            }
        }

        // 前缀匹配
        for (Map.Entry<String, List<Consumer<IDomainEvent>>> entry : prefixSubscribers.entrySet()) {
            if (eventType.startsWith(entry.getKey())) {
                for (Consumer<IDomainEvent> h : entry.getValue()) {
                    invokeHandler(event, h);
                }
            }
        }
    }

    private static void invokeHandler(IDomainEvent event, Consumer<IDomainEvent> handler) {
        try {
            handler.accept(event);
        } catch (Exception e) {
            LOG.log(Level.ERROR, "事件处理失败: " + event.getEventType() + " → " + e.getMessage(), e);
        }
    }

    /**
     * 订阅前缀 — 匹配所有以给定前缀开头的事件。
     */
    public static void subscribePrefix(String typePrefix, Consumer<IDomainEvent> handler) {
        prefixSubscribers.computeIfAbsent(typePrefix, k -> new ArrayList<>()).add(handler);
    }

    /**
     * 取消订阅 — 从精确和前缀订阅中移除指定 handler。
     */
    public static void unsubscribe(String eventType, Consumer<IDomainEvent> handler) {
        List<Consumer<IDomainEvent>> exact = subscribers.get(eventType);
        if (exact != null) {
            exact.remove(handler);
            if (exact.isEmpty()) subscribers.remove(eventType);
        }
        List<Consumer<IDomainEvent>> prefix = prefixSubscribers.get(eventType);
        if (prefix != null) {
            prefix.remove(handler);
            if (prefix.isEmpty()) prefixSubscribers.remove(eventType);
        }
    }

    /**
     * 清空所有订阅（仅供测试使用）
     */
    public static void clearAll() {
        subscribers.clear();
        prefixSubscribers.clear();
    }

}
