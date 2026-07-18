package com.byz.factory.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

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
}
