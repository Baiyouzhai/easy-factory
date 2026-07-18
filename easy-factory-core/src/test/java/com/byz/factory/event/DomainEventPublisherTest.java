package com.byz.factory.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DomainEventPublisher 事件发布测试")
class DomainEventPublisherTest {

    @Test
    @DisplayName("publish/subscribe — 基本发布订阅")
    void publishSubscribe_basic() {
        AtomicInteger counter = new AtomicInteger(0);
        DomainEventPublisher.subscribe("test.event.fired", event -> counter.incrementAndGet());

        IDomainEvent event = IDomainEvent.of("test.event.fired", "test", "payload");
        DomainEventPublisher.publish(event);

        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("publish — 无订阅者不报错")
    void publish_noSubscribers_silent() {
        IDomainEvent event = IDomainEvent.of("no.subscriber.event", "test", "payload");
        assertDoesNotThrow(() -> DomainEventPublisher.publish(event));
    }

    @Test
    @DisplayName("多订阅者 — 全部收到事件")
    void publish_multipleSubscribers() {
        AtomicInteger c1 = new AtomicInteger(0);
        AtomicInteger c2 = new AtomicInteger(0);

        DomainEventPublisher.subscribe("test.multi", event -> c1.incrementAndGet());
        DomainEventPublisher.subscribe("test.multi", event -> c2.incrementAndGet());

        DomainEventPublisher.publish(IDomainEvent.of("test.multi", "test", "data"));

        assertEquals(1, c1.get());
        assertEquals(1, c2.get());
    }

    @Test
    @DisplayName("单订阅者异常不影响其他")
    void publish_oneFailsOthersStillRun() {
        AtomicInteger c2 = new AtomicInteger(0);

        DomainEventPublisher.subscribe("test.resilient", event -> {
            throw new RuntimeException("故意失败");
        });
        DomainEventPublisher.subscribe("test.resilient", event -> c2.incrementAndGet());

        assertDoesNotThrow(() ->
            DomainEventPublisher.publish(IDomainEvent.of("test.resilient", "test", "data"))
        );
        assertEquals(1, c2.get());
    }

    @Test
    @DisplayName("IDomainEvent.of 工厂方法")
    void of_createsEventWithCorrectFields() {
        IDomainEvent event = IDomainEvent.of("mes.process.started", "mes", "data");

        assertEquals("mes.process.started", event.getEventType());
        assertEquals("mes", event.getSource());
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("subscribePrefix — 前缀匹配到多个事件")
    void subscribePrefix_matchesMultipleEvents() {
        DomainEventPublisher.clearAll();
        AtomicInteger counter = new AtomicInteger(0);
        DomainEventPublisher.subscribePrefix("mes.process", event -> counter.incrementAndGet());

        DomainEventPublisher.publish(IDomainEvent.of("mes.process.started", "mes", "data"));
        DomainEventPublisher.publish(IDomainEvent.of("mes.process.completed", "mes", "data"));
        DomainEventPublisher.publish(IDomainEvent.of("mes.workorder.created", "mes", "data"));

        assertEquals(2, counter.get());
    }

    @Test
    @DisplayName("subscribePrefix — 前缀不匹配时不触发")
    void subscribePrefix_noMatch() {
        DomainEventPublisher.clearAll();
        AtomicInteger counter = new AtomicInteger(0);
        DomainEventPublisher.subscribePrefix("qms.inspection", event -> counter.incrementAndGet());

        DomainEventPublisher.publish(IDomainEvent.of("mes.process.started", "mes", "data"));

        assertEquals(0, counter.get());
    }

    @Test
    @DisplayName("subscribePrefix + subscribe — 精确和前缀同时触发")
    void subscribePrefix_andExact_bothTriggered() {
        DomainEventPublisher.clearAll();
        AtomicInteger exact = new AtomicInteger(0);
        AtomicInteger prefix = new AtomicInteger(0);

        DomainEventPublisher.subscribe("mes.process.started", event -> exact.incrementAndGet());
        DomainEventPublisher.subscribePrefix("mes.process", event -> prefix.incrementAndGet());

        DomainEventPublisher.publish(IDomainEvent.of("mes.process.started", "mes", "data"));

        assertEquals(1, exact.get());
        assertEquals(1, prefix.get());
    }

    @Test
    @DisplayName("unsubscribe — 取消精确订阅后不再触发")
    void unsubscribe_exact_noLongerTriggered() {
        DomainEventPublisher.clearAll();
        AtomicInteger counter = new AtomicInteger(0);
        Consumer<IDomainEvent> handler = event -> counter.incrementAndGet();

        DomainEventPublisher.subscribe("test.event", handler);
        DomainEventPublisher.unsubscribe("test.event", handler);
        DomainEventPublisher.publish(IDomainEvent.of("test.event", "test", "data"));

        assertEquals(0, counter.get());
    }

    @Test
    @DisplayName("unsubscribe — 取消前缀订阅后不再触发")
    void unsubscribe_prefix_noLongerTriggered() {
        DomainEventPublisher.clearAll();
        AtomicInteger counter = new AtomicInteger(0);
        Consumer<IDomainEvent> handler = event -> counter.incrementAndGet();

        DomainEventPublisher.subscribePrefix("test.prefix", handler);
        DomainEventPublisher.unsubscribe("test.prefix", handler);
        DomainEventPublisher.publish(IDomainEvent.of("test.prefix.event", "test", "data"));

        assertEquals(0, counter.get());
    }

    @Test
    @DisplayName("unsubscribe — 取消不存在的订阅不报错")
    void unsubscribe_nonexistent_silent() {
        DomainEventPublisher.clearAll();
        assertDoesNotThrow(() ->
            DomainEventPublisher.unsubscribe("no.such.event", event -> {}));
    }

    @Test
    @DisplayName("clearAll — 清空所有订阅")
    void clearAll_removesAll() {
        DomainEventPublisher.clearAll();
        AtomicInteger counter = new AtomicInteger(0);
        DomainEventPublisher.subscribe("test.event", event -> counter.incrementAndGet());
        DomainEventPublisher.subscribePrefix("test", event -> counter.incrementAndGet());

        DomainEventPublisher.clearAll();
        DomainEventPublisher.publish(IDomainEvent.of("test.event", "test", "data"));

        assertEquals(0, counter.get());
    }

}