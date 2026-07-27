package com.byz.factory.aps;

import com.byz.factory.aps.model.Schedule;
import com.byz.factory.aps.model.Schedule.ScheduledTask;
import com.byz.factory.aps.model.TaskStatus;
import com.byz.factory.aps.service.impl.ApsServiceImpl;
import com.byz.factory.batch.ScheduleStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.ApsEventTypes;
import com.byz.factory.operation.common.IOptimizationEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * APS 排程服务实现测试 — 验证 ApsServiceImpl 的内存存储、事件发布、排程流程。
 *
 * @author 苏政
 */
@DisplayName("ApsServiceImpl 排程服务实现")
class ApsServiceImplTest {

    private ApsServiceImpl service;
    private List<IDomainEvent> capturedEvents;
    private final AtomicInteger eventCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new ApsServiceImpl();
        capturedEvents = new CopyOnWriteArrayList<>();
        eventCount.set(0);

        // 订阅所有 APS 事件用于断言
        DomainEventPublisher.subscribePrefix("aps.", event -> {
            capturedEvents.add(event);
            eventCount.incrementAndGet();
        });
    }

    @AfterEach
    void tearDown() {
        DomainEventPublisher.clearAll();
        service.clearAll();
    }

    // ==================== createSchedule ====================

    @Nested
    @DisplayName("createSchedule 创建排程")
    class CreateScheduleTests {

        @Test
        @DisplayName("创建排程 — 返回 DRAFT 状态且存入 store")
        void shouldCreateDraftSchedule() {
            // When
            Schedule schedule = service.createSchedule("PLAN-001", "FACTORY-01");

            // Then
            assertNotNull(schedule);
            assertEquals(ScheduleStatus.DRAFT, schedule.getStatus());
            assertEquals("PLAN-001", schedule.getPlanNo());
            assertEquals("FACTORY-01", schedule.getFactoryCode());
            assertNotNull(schedule.getCode());
            assertTrue(schedule.getCode().startsWith("SCH-"));

            // 可从 store 查询
            Schedule found = service.getSchedule(schedule.getCode());
            assertSame(schedule, found);
            assertEquals(1, service.getScheduleCount());
        }

        @Test
        @DisplayName("创建排程 — 发布 aps.schedule.created 事件")
        void shouldPublishCreatedEvent() {
            // When
            service.createSchedule("PLAN-002", "FACTORY-02");

            // Then
            long createdEvents = capturedEvents.stream()
                    .filter(e -> ApsEventTypes.SCHEDULE_CREATED.equals(e.getEventType()))
                    .count();
            assertEquals(1, createdEvents);
        }

        @Test
        @DisplayName("创建排程 — planNo 为空时抛异常")
        void shouldRejectNullPlanNo() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.createSchedule(null, "FACTORY-01"));
            assertThrows(IllegalArgumentException.class,
                    () -> service.createSchedule("  ", "FACTORY-01"));
        }

        @Test
        @DisplayName("创建排程 — factoryCode 为空时抛异常")
        void shouldRejectNullFactoryCode() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.createSchedule("PLAN-001", null));
            assertThrows(IllegalArgumentException.class,
                    () -> service.createSchedule("PLAN-001", ""));
        }

        @Test
        @DisplayName("创建排程 — 多次创建产生不同编码")
        void shouldGenerateUniqueCodes() {
            Schedule s1 = service.createSchedule("PLAN-001", "FACTORY-01");
            Schedule s2 = service.createSchedule("PLAN-002", "FACTORY-01");
            Schedule s3 = service.createSchedule("PLAN-003", "FACTORY-02");

            assertNotEquals(s1.getCode(), s2.getCode());
            assertNotEquals(s2.getCode(), s3.getCode());
            assertEquals(3, service.getScheduleCount());
        }
    }

    // ==================== optimize ====================

    @Nested
    @DisplayName("optimize 优化排程")
    class OptimizeTests {

        @Test
        @DisplayName("优化 EDD — 状态变为 OPTIMIZED，任务按交期排序")
        void optimizeEDD_shouldSortByEndTime() {
            // Given
            Schedule schedule = service.createSchedule("PLAN-001", "FACTORY-01");
            Instant now = Instant.now();
            schedule.addTask(makeTask("WO-003", now.plus(3, ChronoUnit.HOURS), 10, 50));
            schedule.addTask(makeTask("WO-001", now.plus(1, ChronoUnit.HOURS), 10, 50));
            schedule.addTask(makeTask("WO-002", now.plus(2, ChronoUnit.HOURS), 10, 50));

            // When
            Schedule optimized = service.optimize(schedule.getCode(), "EDD");

            // Then
            assertEquals(ScheduleStatus.OPTIMIZED, optimized.getStatus());
            assertEquals("EDD", optimized.getStrategy());
            assertEquals("WO-001", optimized.getTasks().get(0).getWorkOrderNo());
            assertEquals("WO-002", optimized.getTasks().get(1).getWorkOrderNo());
            assertEquals("WO-003", optimized.getTasks().get(2).getWorkOrderNo());
            assertNotNull(optimized.getScheduledDate());
        }

        @Test
        @DisplayName("优化 SPT — 任务按工时升序排列")
        void optimizeSPT_shouldSortByDuration() {
            // Given
            Schedule schedule = service.createSchedule("PLAN-001", "FACTORY-01");
            Instant now = Instant.now();
            schedule.addTask(makeTask("WO-001", now.plus(2, ChronoUnit.HOURS), 30, 120)); // 150min
            schedule.addTask(makeTask("WO-002", now.plus(2, ChronoUnit.HOURS), 5, 30));   // 35min
            schedule.addTask(makeTask("WO-003", now.plus(2, ChronoUnit.HOURS), 10, 50));  // 60min

            // When
            Schedule optimized = service.optimize(schedule.getCode(), "SPT");

            // Then
            assertEquals("SPT", optimized.getStrategy());
            assertEquals("WO-002", optimized.getTasks().get(0).getWorkOrderNo());
            assertEquals("WO-003", optimized.getTasks().get(1).getWorkOrderNo());
            assertEquals("WO-001", optimized.getTasks().get(2).getWorkOrderNo());
        }

        @Test
        @DisplayName("优化 — 不存在的排程抛异常")
        void optimize_nonexistentSchedule_shouldThrow() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.optimize("NONEXISTENT", "EDD"));
        }

        @Test
        @DisplayName("优化 — 空任务列表不抛异常")
        void optimize_emptyTasks_shouldNotThrow() {
            Schedule schedule = service.createSchedule("PLAN-001", "FACTORY-01");
            Schedule optimized = service.optimize(schedule.getCode(), "CR");
            assertEquals(ScheduleStatus.OPTIMIZED, optimized.getStatus());
            assertTrue(optimized.getTasks().isEmpty());
        }
    }

    // ==================== dispatch ====================

    @Nested
    @DisplayName("dispatch 下发排程")
    class DispatchTests {

        @Test
        @DisplayName("下发 — 状态变为 DISPATCHED，设置 horizon")
        void dispatch_shouldSetDispatchedAndHorizon() {
            // Given
            Schedule schedule = service.createSchedule("PLAN-001", "FACTORY-01");
            Instant now = Instant.now();
            schedule.addTask(makeTask("WO-001", now.plus(1, ChronoUnit.HOURS), 10, 50));
            schedule.addTask(makeTask("WO-002", now.plus(3, ChronoUnit.HOURS), 10, 50));
            service.optimize(schedule.getCode(), "EDD");

            // When
            service.dispatch(schedule.getCode());

            // Then
            Schedule dispatched = service.getSchedule(schedule.getCode());
            assertEquals(ScheduleStatus.DISPATCHED, dispatched.getStatus());
            assertNotNull(dispatched.getHorizonStart());
            assertNotNull(dispatched.getHorizonEnd());
            dispatched.getTasks().forEach(t -> assertEquals(TaskStatus.DISPATCHED, t.getStatus()));
        }

        @Test
        @DisplayName("下发 — 发布 aps.schedule.released 事件")
        void dispatch_shouldPublishReleasedEvent() {
            // Given
            Schedule schedule = service.createSchedule("PLAN-002", "FACTORY-02");
            schedule.addTask(makeTask("WO-001", Instant.now().plus(1, ChronoUnit.HOURS), 10, 50));
            service.optimize(schedule.getCode(), "SPT");

            // When
            service.dispatch(schedule.getCode());

            // Then
            long releasedEvents = capturedEvents.stream()
                    .filter(e -> ApsEventTypes.SCHEDULE_RELEASED.equals(e.getEventType()))
                    .count();
            assertEquals(1, releasedEvents);
        }

        @Test
        @DisplayName("下发 — 不存在的排程抛异常")
        void dispatch_nonexistentSchedule_shouldThrow() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.dispatch("NONEXISTENT"));
        }
    }

    // ==================== reschedule ====================

    @Nested
    @DisplayName("reschedule 重排程")
    class RescheduleTests {

        @Test
        @DisplayName("重排程 — 发布 aps.reschedule.triggered 事件")
        void reschedule_shouldPublishTriggeredEvent() {
            // Given
            Schedule schedule = service.createSchedule("PLAN-001", "FACTORY-01");
            schedule.addTask(makeTask("WO-001", Instant.now().plus(2, ChronoUnit.HOURS), 10, 50));
            schedule.addTask(makeTask("WO-002", Instant.now().plus(1, ChronoUnit.HOURS), 10, 50));
            service.optimize(schedule.getCode(), "EDD");

            // When
            Schedule result = service.reschedule(schedule.getCode(), "mes.workorder.released");

            // Then
            assertNotNull(result);
            long triggeredEvents = capturedEvents.stream()
                    .filter(e -> ApsEventTypes.RESCHEDULE_TRIGGERED.equals(e.getEventType()))
                    .count();
            assertEquals(1, triggeredEvents);
        }

        @Test
        @DisplayName("重排程 — 创建 RescheduleTrigger 记录且标记已处理")
        void reschedule_shouldCreateAndMarkTrigger() {
            // Given
            Schedule schedule = service.createSchedule("PLAN-002", "FACTORY-02");
            schedule.addTask(makeTask("WO-001", Instant.now().plus(1, ChronoUnit.HOURS), 10, 50));
            service.optimize(schedule.getCode(), "SPT");

            // When
            service.reschedule(schedule.getCode(), "equip.fault.reported");

            // Then
            assertEquals(1, service.getTriggerCount());
        }

        @Test
        @DisplayName("重排程 — 保留当前策略重新排序")
        void reschedule_shouldReSortWithCurrentStrategy() {
            // Given
            Schedule schedule = service.createSchedule("PLAN-003", "FACTORY-03");
            Instant now = Instant.now();
            // 新增一个高优先级任务（交期更早）
            schedule.addTask(makeTask("WO-001", now.plus(3, ChronoUnit.HOURS), 10, 50));
            schedule.addTask(makeTask("WO-002", now.plus(2, ChronoUnit.HOURS), 10, 50));
            service.optimize(schedule.getCode(), "EDD");
            // 此时顺序: WO-002, WO-001

            // 模拟新工单插入: 交期最紧
            Schedule liveSchedule = service.getSchedule(schedule.getCode());
            liveSchedule.addTask(makeTask("WO-003", now.plus(1, ChronoUnit.HOURS), 10, 50));

            // When: 重排程
            Schedule rescheduled = service.reschedule(schedule.getCode(), "mes.workorder.released");

            // Then: EDD 重新排序后 WO-003 应该排最前
            assertEquals("WO-003", rescheduled.getTasks().get(0).getWorkOrderNo());
        }

        @Test
        @DisplayName("重排程 — 无策略时不排序（不抛异常）")
        void reschedule_noStrategy_shouldNotThrow() {
            // Given
            Schedule schedule = service.createSchedule("PLAN-004", "FACTORY-04");
            schedule.addTask(makeTask("WO-001", Instant.now().plus(1, ChronoUnit.HOURS), 10, 50));

            // When & Then: 未优化过，无 strategy，reschedule 不应抛异常
            assertDoesNotThrow(() -> service.reschedule(schedule.getCode(), "scm.receipt.delayed"));
        }

        @Test
        @DisplayName("重排程 — 不存在的排程抛异常")
        void reschedule_nonexistentSchedule_shouldThrow() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.reschedule("NONEXISTENT", "equip.fault.reported"));
        }
    }

    // ==================== getSchedule ====================

    @Nested
    @DisplayName("getSchedule 查询排程")
    class GetScheduleTests {

        @Test
        @DisplayName("查询 — 存在时返回排程")
        void getSchedule_exists_shouldReturn() {
            Schedule created = service.createSchedule("PLAN-001", "FACTORY-01");
            Schedule found = service.getSchedule(created.getCode());
            assertSame(created, found);
        }

        @Test
        @DisplayName("查询 — 不存在时返回 null")
        void getSchedule_notExists_shouldReturnNull() {
            assertNull(service.getSchedule("NONEXISTENT"));
        }
    }

    // ==================== 事件发布验证 ====================

    @Nested
    @DisplayName("事件发布验证")
    class EventPublishingTests {

        @Test
        @DisplayName("完整生命周期 — 按顺序发布 3 个事件")
        void fullLifecycle_shouldPublishEventsInOrder() {
            // Given
            Schedule s = service.createSchedule("PLAN-001", "FACTORY-01");
            s.addTask(makeTask("WO-001", Instant.now().plus(1, ChronoUnit.HOURS), 10, 50));

            // When
            service.optimize(s.getCode(), "EDD");
            service.dispatch(s.getCode());
            service.reschedule(s.getCode(), "mes.workorder.released");

            // Then: 4 events (created + released + triggered) — optimize 不发布事件
            List<String> eventTypes = capturedEvents.stream()
                    .map(IDomainEvent::getEventType)
                    .toList();
            assertTrue(eventTypes.contains(ApsEventTypes.SCHEDULE_CREATED));
            assertTrue(eventTypes.contains(ApsEventTypes.SCHEDULE_RELEASED));
            assertTrue(eventTypes.contains(ApsEventTypes.RESCHEDULE_TRIGGERED));
        }

        @Test
        @DisplayName("事件 source — 所有事件 source 为 'aps'")
        void allEvents_shouldHaveSourceAps() {
            service.createSchedule("PLAN-001", "FACTORY-01");
            capturedEvents.forEach(e -> assertEquals("aps", e.getSource()));
        }
    }

    // ==================== IOptimizationEngine ====================

    @Nested
    @DisplayName("IOptimizationEngine 优化引擎接口")
    class OptimizationEngineTests {

        @Test
        @DisplayName("ruleBased 工厂 — EDD 引擎正确排序")
        void ruleBased_edd_shouldSortCorrectly() {
            // Given
            IOptimizationEngine<ScheduledTask> engine = IOptimizationEngine.ruleBased(
                    "EDD-Rule",
                    Map.entry("EDD", Comparator.comparing(ScheduledTask::getEndTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))));

            Instant now = Instant.now();
            ScheduledTask t1 = makeTask("WO-001", now.plus(3, ChronoUnit.HOURS), 10, 50);
            ScheduledTask t2 = makeTask("WO-002", now.plus(1, ChronoUnit.HOURS), 10, 50);
            ScheduledTask t3 = makeTask("WO-003", now.plus(2, ChronoUnit.HOURS), 10, 50);
            List<ScheduledTask> tasks = new ArrayList<>(List.of(t1, t2, t3));

            // When
            List<ScheduledTask> result = engine.optimize(tasks, "EDD", now);

            // Then
            assertEquals("EDD-Rule", engine.getEngineName());
            assertEquals("WO-002", result.get(0).getWorkOrderNo());
            assertEquals("WO-003", result.get(1).getWorkOrderNo());
            assertEquals("WO-001", result.get(2).getWorkOrderNo());
        }

        @Test
        @DisplayName("toComparator — 未知策略抛异常")
        void toComparator_unknownStrategy_shouldThrow() {
            IOptimizationEngine<ScheduledTask> engine = IOptimizationEngine.ruleBased(
                    "Test-Rule",
                    Map.entry("EDD", Comparator.comparing(ScheduledTask::getEndTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))));

            assertThrows(IllegalArgumentException.class,
                    () -> engine.toComparator("UNKNOWN", Instant.now()));
        }

        @Test
        @DisplayName("optimize — 空列表不抛异常")
        void optimize_emptyList_shouldNotThrow() {
            IOptimizationEngine<ScheduledTask> engine = IOptimizationEngine.ruleBased(
                    "Test-Rule",
                    Map.entry("EDD", Comparator.comparing(ScheduledTask::getEndTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))));

            List<ScheduledTask> result = engine.optimize(List.of(), "EDD", Instant.now());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("optimize — null 列表直接返回")
        void optimize_nullList_shouldReturnNull() {
            IOptimizationEngine<ScheduledTask> engine = IOptimizationEngine.ruleBased(
                    "Test-Rule",
                    Map.entry("EDD", Comparator.comparing(ScheduledTask::getEndTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))));

            assertNull(engine.optimize(null, "EDD", Instant.now()));
        }

        @Test
        @DisplayName("多策略引擎 — EDD 和 SPT 同时注册")
        void multiStrategy_shouldSupportMultipleRules() {
            IOptimizationEngine<ScheduledTask> engine = IOptimizationEngine.ruleBased(
                    "Multi-Rule",
                    Map.entry("EDD", Comparator.comparing(ScheduledTask::getEndTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))),
                    Map.entry("SPT", Comparator.comparingInt(ScheduledTask::getTotalDurationMin)));

            Instant now = Instant.now();
            ScheduledTask t1 = makeTask("WO-001", now.plus(3, ChronoUnit.HOURS), 10, 50);
            ScheduledTask t2 = makeTask("WO-002", now.plus(1, ChronoUnit.HOURS), 30, 120);

            // EDD: WO-002 first (earlier due)
            List<ScheduledTask> eddResult = engine.optimize(
                    new ArrayList<>(List.of(t1, t2)), "EDD", now);
            assertEquals("WO-002", eddResult.get(0).getWorkOrderNo());

            // SPT: WO-001 first (shorter duration)
            List<ScheduledTask> sptResult = engine.optimize(
                    new ArrayList<>(List.of(t1, t2)), "SPT", now);
            assertEquals("WO-001", sptResult.get(0).getWorkOrderNo());
        }
    }

    // ==================== 综合场景 ====================

    @Nested
    @DisplayName("综合场景")
    class IntegrationTests {

        @Test
        @DisplayName("端到端 — 创建→优化→下发→重排程→查询")
        void endToEnd_fullLifecycle() {
            // 1. 创建
            Schedule schedule = service.createSchedule("PLAN-E2E", "FACTORY-E2E");
            String code = schedule.getCode();
            assertEquals(ScheduleStatus.DRAFT, schedule.getStatus());

            // 2. 添加任务
            Instant now = Instant.now();
            schedule.addTask(makeTask("WO-003", now.plus(3, ChronoUnit.HOURS), 10, 50));
            schedule.addTask(makeTask("WO-001", now.plus(1, ChronoUnit.HOURS), 10, 50));
            schedule.addTask(makeTask("WO-002", now.plus(2, ChronoUnit.HOURS), 10, 50));

            // 3. 优化
            service.optimize(code, "EDD");
            Schedule afterOptimize = service.getSchedule(code);
            assertEquals(ScheduleStatus.OPTIMIZED, afterOptimize.getStatus());
            assertEquals("WO-001", afterOptimize.getTasks().get(0).getWorkOrderNo());

            // 4. 下发
            service.dispatch(code);
            Schedule afterDispatch = service.getSchedule(code);
            assertEquals(ScheduleStatus.DISPATCHED, afterDispatch.getStatus());
            assertNotNull(afterDispatch.getHorizonStart());

            // 5. 重排程（新工单插入）
            service.reschedule(code, "mes.workorder.released");
            Schedule afterReschedule = service.getSchedule(code);

            // 6. 最终状态
            assertEquals(ScheduleStatus.DISPATCHED, afterReschedule.getStatus()); // 重排程不改变状态
            assertEquals("EDD", afterReschedule.getStrategy());
            assertEquals(3, afterReschedule.getTasks().size());
        }

        @Test
        @DisplayName("清空存储 — clearAll 后 scheduleCount 为 0")
        void clearAll_shouldResetEverything() {
            service.createSchedule("PLAN-001", "FACTORY-01");
            service.createSchedule("PLAN-002", "FACTORY-02");
            assertEquals(2, service.getScheduleCount());

            service.clearAll();
            assertEquals(0, service.getScheduleCount());
            assertEquals(0, service.getTriggerCount());
            assertNull(service.getSchedule("SCH-001"));
        }
    }

    // ==================== 辅助方法 ====================

    private ScheduledTask makeTask(String wo, Instant end, int setupMin, int processMin) {
        return new ScheduledTask(wo, "PROC-001", "M-001", "OP-001",
                end.minus(processMin + setupMin, ChronoUnit.MINUTES), end,
                setupMin, processMin, List.of(), TaskStatus.SCHEDULED);
    }

}
