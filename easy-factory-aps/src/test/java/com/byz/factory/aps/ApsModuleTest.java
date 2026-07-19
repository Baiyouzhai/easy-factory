package com.byz.factory.aps;

import com.byz.factory.aps.model.RescheduleTrigger;
import com.byz.factory.aps.model.ResourceCalendar;
import com.byz.factory.aps.model.ResourceType;
import com.byz.factory.aps.model.Schedule;
import com.byz.factory.aps.model.Schedule.ScheduledTask;
import com.byz.factory.aps.model.TaskStatus;
import com.byz.factory.aps.service.ApsService;
import com.byz.factory.aps.service.SchedulingRule;
import com.byz.factory.batch.ScheduleStatus;
import com.byz.factory.event.types.ApsEventTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * APS 模块综合测试。
 * <p>
 * 覆盖：Schedule 状态转换、ScheduledTask 模型、TaskStatus 枚举、
 * ResourceCalendar 可用时间计算、RescheduleTrigger 处理逻辑、
 * 排程规则（EDD/SPT/CR）、ApsEventTypes 事件命名约定、非法状态转换。
 *
 * @author 苏政
 */
@DisplayName("APS 排程模块")
class ApsModuleTest {

    // ==================== 模块加载 ====================

    @Test
    @DisplayName("模块加载 — 基础断言通过")
    void shouldLoad() {
        assertTrue(true);
    }

    // ==================== Schedule 构造 ====================

    @Nested
    @DisplayName("Schedule 排程方案")
    class ScheduleTests {

        @Test
        @DisplayName("构造排程 — 初始状态为 DRAFT")
        void constructor_shouldSetInitialStateToDraft() {
            // Given & When
            Schedule schedule = new Schedule("SCH-001", "PLAN-001", "FACTORY-01");

            // Then
            assertEquals("SCH-001", schedule.getCode());
            assertEquals("PLAN-001", schedule.getPlanNo());
            assertEquals("FACTORY-01", schedule.getFactoryCode());
            assertEquals(ScheduleStatus.DRAFT, schedule.getStatus());
            assertNotNull(schedule.getTasks());
            assertTrue(schedule.getTasks().isEmpty());
            assertNotNull(schedule.getScheduledDate());
            assertNotNull(schedule.getCreatedAt());
        }

        @Test
        @DisplayName("添加排程任务 — 任务列表应包含新任务")
        void addTask_shouldAddToTaskList() {
            // Given
            Schedule schedule = new Schedule("SCH-002", "PLAN-002", "FACTORY-02");
            ScheduledTask task = new ScheduledTask("WO-001", "PROC-001", "M-001",
                    "OP-001", Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS),
                    10, 50, List.of(), TaskStatus.SCHEDULED);

            // When
            schedule.addTask(task);

            // Then
            assertEquals(1, schedule.getTasks().size());
            assertEquals("WO-001", schedule.getTasks().get(0).getWorkOrderNo());
        }

        // ==================== 状态转换 — 正常链路 ====================

        @Test
        @DisplayName("状态转换 — DRAFT → OPTIMIZED → DISPATCHED → IN_PROGRESS → COMPLETED")
        void lifecycle_shouldFollowFullPath() {
            // Given
            Schedule schedule = new Schedule("SCH-003", "PLAN-003", "FACTORY-03");
            ScheduledTask task = new ScheduledTask("WO-001", "PROC-001", "M-001",
                    "OP-001", Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS),
                    10, 50, List.of(), TaskStatus.SCHEDULED);
            schedule.addTask(task);

            // When & Then: DRAFT → OPTIMIZED
            schedule.optimize("EDD");
            assertEquals(ScheduleStatus.OPTIMIZED, schedule.getStatus());
            assertEquals("EDD", schedule.getStrategy());
            assertEquals(TaskStatus.SCHEDULED, schedule.getTasks().get(0).getStatus());

            // OPTIMIZED → DISPATCHED
            schedule.dispatch();
            assertEquals(ScheduleStatus.DISPATCHED, schedule.getStatus());
            assertEquals(TaskStatus.DISPATCHED, schedule.getTasks().get(0).getStatus());

            // DISPATCHED → IN_PROGRESS
            schedule.start();
            assertEquals(ScheduleStatus.IN_PROGRESS, schedule.getStatus());

            // IN_PROGRESS → COMPLETED
            schedule.complete();
            assertEquals(ScheduleStatus.COMPLETED, schedule.getStatus());
        }

        @Test
        @DisplayName("取消 — 从 DRAFT 状态可直接取消")
        void cancel_fromDraft_shouldTransitionToCancelled() {
            // Given
            Schedule schedule = new Schedule("SCH-004", "PLAN-004", "FACTORY-04");

            // When
            schedule.cancel();

            // Then
            assertEquals(ScheduleStatus.CANCELLED, schedule.getStatus());
        }

        @Test
        @DisplayName("取消 — 从 OPTIMIZED 状态可取消，任务全部标记 CANCELLED")
        void cancel_fromOptimized_shouldCancelAllTasks() {
            // Given
            Schedule schedule = new Schedule("SCH-005", "PLAN-005", "FACTORY-05");
            ScheduledTask task = new ScheduledTask("WO-001", "PROC-001", "M-001",
                    "OP-001", Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS),
                    10, 50, List.of(), TaskStatus.SCHEDULED);
            schedule.addTask(task);
            schedule.optimize("EDD");

            // When
            schedule.cancel();

            // Then
            assertEquals(ScheduleStatus.CANCELLED, schedule.getStatus());
            assertEquals(TaskStatus.CANCELLED, schedule.getTasks().get(0).getStatus());
        }

        @Test
        @DisplayName("取消 — 从 DISPATCHED 状态可取消")
        void cancel_fromDispatched_shouldCancel() {
            // Given
            Schedule schedule = new Schedule("SCH-006", "PLAN-006", "FACTORY-06");
            schedule.optimize("SPT");
            schedule.dispatch();

            // When
            schedule.cancel();

            // Then
            assertEquals(ScheduleStatus.CANCELLED, schedule.getStatus());
        }

        @Test
        @DisplayName("取消 — 从 IN_PROGRESS 状态可取消")
        void cancel_fromInProgress_shouldCancel() {
            // Given
            Schedule schedule = new Schedule("SCH-007", "PLAN-007", "FACTORY-07");
            schedule.optimize("SPT");
            schedule.dispatch();
            schedule.start();

            // When
            schedule.cancel();

            // Then
            assertEquals(ScheduleStatus.CANCELLED, schedule.getStatus());
        }

        // ==================== 非法状态转换 ====================

        @Test
        @DisplayName("非法转换 — DRAFT 不能直接 dispatch")
        void dispatch_fromDraft_shouldThrow() {
            // Given
            Schedule schedule = new Schedule("SCH-008", "PLAN-008", "FACTORY-08");

            // When & Then
            assertThrows(IllegalStateException.class, schedule::dispatch);
        }

        @Test
        @DisplayName("非法转换 — DRAFT 不能直接 start")
        void start_fromDraft_shouldThrow() {
            // Given
            Schedule schedule = new Schedule("SCH-009", "PLAN-009", "FACTORY-09");

            // When & Then
            assertThrows(IllegalStateException.class, schedule::start);
        }

        @Test
        @DisplayName("非法转换 — DRAFT 不能直接 complete")
        void complete_fromDraft_shouldThrow() {
            // Given
            Schedule schedule = new Schedule("SCH-010", "PLAN-010", "FACTORY-10");

            // When & Then
            assertThrows(IllegalStateException.class, schedule::complete);
        }

        @Test
        @DisplayName("非法转换 — OPTIMIZED 不能直接 start")
        void start_fromOptimized_shouldThrow() {
            // Given
            Schedule schedule = new Schedule("SCH-011", "PLAN-011", "FACTORY-11");
            schedule.optimize("EDD");

            // When & Then
            assertThrows(IllegalStateException.class, schedule::start);
        }

        @Test
        @DisplayName("非法转换 — COMPLETED 不能再 optimize")
        void optimize_fromCompleted_shouldThrow() {
            // Given
            Schedule schedule = new Schedule("SCH-012", "PLAN-012", "FACTORY-12");
            schedule.optimize("EDD");
            schedule.dispatch();
            schedule.start();
            schedule.complete();

            // When & Then
            assertThrows(IllegalStateException.class, () -> schedule.optimize("SPT"));
        }

        @Test
        @DisplayName("非法转换 — CANCELLED 不能再 dispatch")
        void dispatch_fromCancelled_shouldThrow() {
            // Given
            Schedule schedule = new Schedule("SCH-013", "PLAN-013", "FACTORY-13");
            schedule.cancel();

            // When & Then
            assertThrows(IllegalStateException.class, schedule::dispatch);
        }

        @Test
        @DisplayName("非法转换 — COMPLETED 不能再 cancel（终态不可变）")
        void cancel_fromCompleted_shouldThrow() {
            // Given
            Schedule schedule = new Schedule("SCH-014", "PLAN-014", "FACTORY-14");
            schedule.optimize("EDD");
            schedule.dispatch();
            schedule.start();
            schedule.complete();

            // When & Then — COMPLETED 不允许任何转换
            assertThrows(IllegalStateException.class, () -> schedule.cancel());
        }

        // ==================== canTransition ====================

        @Test
        @DisplayName("canTransition — DRAFT 允许到 OPTIMIZED 和 CANCELLED")
        void canTransition_fromDraft() {
            Schedule schedule = new Schedule("SCH-015", "PLAN-015", "FACTORY-15");
            assertTrue(schedule.canTransition(ScheduleStatus.OPTIMIZED));
            assertTrue(schedule.canTransition(ScheduleStatus.CANCELLED));
            assertFalse(schedule.canTransition(ScheduleStatus.DISPATCHED));
            assertFalse(schedule.canTransition(ScheduleStatus.IN_PROGRESS));
            assertFalse(schedule.canTransition(ScheduleStatus.COMPLETED));
        }

        @Test
        @DisplayName("canTransition — COMPLETED 不允许任何转换")
        void canTransition_completed_isTerminal() {
            Schedule schedule = new Schedule("SCH-016", "PLAN-016", "FACTORY-16");
            schedule.optimize("EDD");
            schedule.dispatch();
            schedule.start();
            schedule.complete();
            assertFalse(schedule.canTransition(ScheduleStatus.DRAFT));
            assertFalse(schedule.canTransition(ScheduleStatus.OPTIMIZED));
            assertFalse(schedule.canTransition(ScheduleStatus.DISPATCHED));
            assertFalse(schedule.canTransition(ScheduleStatus.IN_PROGRESS));
            assertFalse(schedule.canTransition(ScheduleStatus.CANCELLED));
        }
    }

    // ==================== ScheduledTask ====================

    @Nested
    @DisplayName("ScheduledTask 排程任务")
    class ScheduledTaskTests {

        @Test
        @DisplayName("构造 — 无参构造默认状态为 SCHEDULED")
        void defaultConstructor_shouldSetStatusToScheduled() {
            ScheduledTask task = new ScheduledTask();
            assertEquals(TaskStatus.SCHEDULED, task.getStatus());
            assertNotNull(task.getPredecessors());
            assertTrue(task.getPredecessors().isEmpty());
        }

        @Test
        @DisplayName("构造 — 全参构造应正确赋值所有字段")
        void allArgsConstructor_shouldSetAllFields() {
            // Given
            Instant start = Instant.parse("2026-07-19T08:00:00Z");
            Instant end = Instant.parse("2026-07-19T12:00:00Z");
            List<String> predecessors = List.of("TASK-001", "TASK-002");

            // When
            ScheduledTask task = new ScheduledTask("WO-001", "PROC-001", "M-001",
                    "OP-001", start, end, 15, 120, predecessors, TaskStatus.SCHEDULED);

            // Then
            assertEquals("WO-001", task.getWorkOrderNo());
            assertEquals("PROC-001", task.getProcessCode());
            assertEquals("M-001", task.getMachineCode());
            assertEquals("OP-001", task.getOperatorCode());
            assertEquals(start, task.getStartTime());
            assertEquals(end, task.getEndTime());
            assertEquals(15, task.getSetupTimeMin());
            assertEquals(120, task.getProcessTimeMin());
            assertEquals(2, task.getPredecessors().size());
            assertEquals(TaskStatus.SCHEDULED, task.getStatus());
        }

        @Test
        @DisplayName("构造 — predecessors 为 null 时初始化为空列表")
        void constructor_nullPredecessors_shouldInitEmptyList() {
            ScheduledTask task = new ScheduledTask("WO-001", "PROC-001", "M-001",
                    "OP-001", Instant.now(), Instant.now(), 0, 60, null, null);
            assertNotNull(task.getPredecessors());
            assertTrue(task.getPredecessors().isEmpty());
            assertEquals(TaskStatus.SCHEDULED, task.getStatus());
        }

        @Test
        @DisplayName("getTotalDurationMin — 返回 setupTimeMin + processTimeMin")
        void getTotalDurationMin_shouldSumSetupAndProcess() {
            ScheduledTask task = new ScheduledTask("WO-001", "PROC-001", "M-001",
                    "OP-001", Instant.now(), Instant.now(), 10, 50, List.of(), TaskStatus.SCHEDULED);
            assertEquals(60, task.getTotalDurationMin());
        }

        @Test
        @DisplayName("状态修改 — 可直接设置 TaskStatus")
        void setStatus_shouldChangeStatus() {
            ScheduledTask task = new ScheduledTask();
            task.setStatus(TaskStatus.IN_PROGRESS);
            assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
            task.setStatus(TaskStatus.COMPLETED);
            assertEquals(TaskStatus.COMPLETED, task.getStatus());
        }
    }

    // ==================== TaskStatus 枚举 ====================

    @Nested
    @DisplayName("TaskStatus 枚举")
    class TaskStatusTests {

        @Test
        @DisplayName("枚举值 — 包含 5 个状态")
        void shouldHaveFiveValues() {
            assertEquals(5, TaskStatus.values().length);
        }

        @Test
        @DisplayName("枚举值 — 包含 SCHEDULED / DISPATCHED / IN_PROGRESS / COMPLETED / CANCELLED")
        void shouldContainExpectedValues() {
            assertNotNull(TaskStatus.valueOf("SCHEDULED"));
            assertNotNull(TaskStatus.valueOf("DISPATCHED"));
            assertNotNull(TaskStatus.valueOf("IN_PROGRESS"));
            assertNotNull(TaskStatus.valueOf("COMPLETED"));
            assertNotNull(TaskStatus.valueOf("CANCELLED"));
        }
    }

    // ==================== ResourceCalendar ====================

    @Nested
    @DisplayName("ResourceCalendar 资源日历")
    class ResourceCalendarTests {

        @Test
        @DisplayName("构造 — 字段应正确赋值")
        void constructor_shouldSetAllFields() {
            // Given
            LocalDate date = LocalDate.of(2026, 7, 19);
            Instant from = Instant.parse("2026-07-19T08:00:00Z");
            Instant to = Instant.parse("2026-07-19T20:00:00Z");

            // When
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, date, from, to, BigDecimal.valueOf(12));

            // Then
            assertEquals("RC-001", cal.getCode());
            assertEquals("M-001", cal.getResourceCode());
            assertEquals(ResourceType.MACHINE, cal.getResourceType());
            assertEquals(date, cal.getDate());
            assertEquals(from, cal.getAvailableFrom());
            assertEquals(to, cal.getAvailableTo());
            assertEquals(BigDecimal.valueOf(12), cal.getCapacityHours());
        }

        @Test
        @DisplayName("getAvailableMinutes — 8:00-20:00 应为 720 分钟")
        void getAvailableMinutes_shouldReturnCorrectValue() {
            // Given
            Instant from = Instant.parse("2026-07-19T08:00:00Z");
            Instant to = Instant.parse("2026-07-19T20:00:00Z");
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(), from, to, BigDecimal.valueOf(12));

            // When
            long minutes = cal.getAvailableMinutes();

            // Then
            assertEquals(720, minutes); // 12 hours × 60
        }

        @Test
        @DisplayName("getCapacityMinutes — 12 小时产能 = 720 分钟")
        void getCapacityMinutes_shouldConvertHoursToMinutes() {
            // Given
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(),
                    Instant.now(), Instant.now().plus(12, ChronoUnit.HOURS),
                    BigDecimal.valueOf(12));

            // When
            long capMinutes = cal.getCapacityMinutes();

            // Then
            assertEquals(720, capMinutes);
        }

        @Test
        @DisplayName("getCapacityMinutes — capacityHours 为 null 时返回 0")
        void getCapacityMinutes_nullCapacity_shouldReturnZero() {
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(),
                    Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS),
                    null);
            assertEquals(0, cal.getCapacityMinutes());
        }

        @Test
        @DisplayName("canFit — 任务时间在可用窗口内时返回 true")
        void canFit_withinWindow_shouldReturnTrue() {
            // Given
            Instant from = Instant.parse("2026-07-19T08:00:00Z");
            Instant to = Instant.parse("2026-07-19T20:00:00Z");
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(), from, to, BigDecimal.valueOf(12));

            Instant taskStart = Instant.parse("2026-07-19T10:00:00Z");
            Instant taskEnd = Instant.parse("2026-07-19T12:00:00Z");

            // When
            boolean fit = cal.canFit(taskStart, taskEnd);

            // Then
            assertTrue(fit);
        }

        @Test
        @DisplayName("canFit — 任务开始早于可用窗口时返回 false")
        void canFit_startBeforeWindow_shouldReturnFalse() {
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(),
                    Instant.parse("2026-07-19T08:00:00Z"),
                    Instant.parse("2026-07-19T20:00:00Z"),
                    BigDecimal.valueOf(12));

            assertFalse(cal.canFit(
                    Instant.parse("2026-07-19T07:00:00Z"),
                    Instant.parse("2026-07-19T12:00:00Z")));
        }

        @Test
        @DisplayName("canFit — 任务结束晚于可用窗口时返回 false")
        void canFit_endAfterWindow_shouldReturnFalse() {
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(),
                    Instant.parse("2026-07-19T08:00:00Z"),
                    Instant.parse("2026-07-19T20:00:00Z"),
                    BigDecimal.valueOf(12));

            assertFalse(cal.canFit(
                    Instant.parse("2026-07-19T18:00:00Z"),
                    Instant.parse("2026-07-19T21:00:00Z")));
        }

        @Test
        @DisplayName("canFit — start 或 end 为 null 时返回 false")
        void canFit_nullParams_shouldReturnFalse() {
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(),
                    Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS),
                    BigDecimal.valueOf(12));

            assertFalse(cal.canFit(null, Instant.now()));
            assertFalse(cal.canFit(Instant.now(), null));
        }

        @Test
        @DisplayName("overlapMinutes — 完全在窗口内时返回任务时长")
        void overlapMinutes_fullyWithin_shouldReturnTaskDuration() {
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(),
                    Instant.parse("2026-07-19T08:00:00Z"),
                    Instant.parse("2026-07-19T20:00:00Z"),
                    BigDecimal.valueOf(12));

            long overlap = cal.overlapMinutes(
                    Instant.parse("2026-07-19T10:00:00Z"),
                    Instant.parse("2026-07-19T12:00:00Z"));
            assertEquals(120, overlap); // 2 hours
        }

        @Test
        @DisplayName("overlapMinutes — 部分重叠时返回重叠时长")
        void overlapMinutes_partial_shouldReturnOverlapDuration() {
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(),
                    Instant.parse("2026-07-19T08:00:00Z"),
                    Instant.parse("2026-07-19T20:00:00Z"),
                    BigDecimal.valueOf(12));

            // Task: 07:00–10:00, Window: 08:00–20:00 → overlap 08:00–10:00 = 120 min
            long overlap = cal.overlapMinutes(
                    Instant.parse("2026-07-19T07:00:00Z"),
                    Instant.parse("2026-07-19T10:00:00Z"));
            assertEquals(120, overlap);
        }

        @Test
        @DisplayName("overlapMinutes — 无重叠时返回 0")
        void overlapMinutes_noOverlap_shouldReturnZero() {
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(),
                    Instant.parse("2026-07-19T08:00:00Z"),
                    Instant.parse("2026-07-19T20:00:00Z"),
                    BigDecimal.valueOf(12));

            long overlap = cal.overlapMinutes(
                    Instant.parse("2026-07-19T05:00:00Z"),
                    Instant.parse("2026-07-19T07:00:00Z"));
            assertEquals(0, overlap);
        }

        @Test
        @DisplayName("ResourceType 枚举 — 包含 MACHINE 和 PERSONNEL")
        void resourceTypeEnum_shouldHaveTwoValues() {
            assertEquals(2, ResourceType.values().length);
            assertNotNull(ResourceType.valueOf("MACHINE"));
            assertNotNull(ResourceType.valueOf("PERSONNEL"));
        }
    }

    // ==================== RescheduleTrigger ====================

    @Nested
    @DisplayName("RescheduleTrigger 重排程触发")
    class RescheduleTriggerTests {

        @Test
        @DisplayName("构造 — 初始状态 processed=false")
        void constructor_shouldSetProcessedFalse() {
            // Given & When
            RescheduleTrigger trigger = new RescheduleTrigger("RT-001", "SCH-001",
                    "mes.workorder.released");

            // Then
            assertEquals("RT-001", trigger.getCode());
            assertEquals("SCH-001", trigger.getScheduleCode());
            assertEquals("mes.workorder.released", trigger.getTriggerEvent());
            assertNotNull(trigger.getTriggeredAt());
            assertFalse(trigger.isProcessed());
        }

        @Test
        @DisplayName("markProcessed — processed 变为 true")
        void markProcessed_shouldSetProcessedTrue() {
            // Given
            RescheduleTrigger trigger = new RescheduleTrigger("RT-002", "SCH-002",
                    "equip.fault.reported");

            // When
            trigger.markProcessed();

            // Then
            assertTrue(trigger.isProcessed());
        }

        @Test
        @DisplayName("构造 — 支持多种触发事件类型")
        void shouldSupportMultipleEventTypes() {
            RescheduleTrigger t1 = new RescheduleTrigger("RT-001", "SCH-001",
                    "mes.workorder.released");
            RescheduleTrigger t2 = new RescheduleTrigger("RT-002", "SCH-001",
                    "equip.fault.reported");
            RescheduleTrigger t3 = new RescheduleTrigger("RT-003", "SCH-001",
                    "scm.receipt.delayed");

            assertEquals("mes.workorder.released", t1.getTriggerEvent());
            assertEquals("equip.fault.reported", t2.getTriggerEvent());
            assertEquals("scm.receipt.delayed", t3.getTriggerEvent());
        }
    }

    // ==================== ApsEventTypes 事件命名约定 ====================

    @Nested
    @DisplayName("ApsEventTypes 事件常量")
    class ApsEventTypesTests {

        @Test
        @DisplayName("PREFIX — 应为 'aps'")
        void prefix_shouldBeAps() {
            assertEquals("aps", ApsEventTypes.PREFIX);
        }

        @Test
        @DisplayName("事件命名 — 所有事件常量以 'aps.' 开头")
        void allEvents_shouldStartWithApsPrefix() {
            assertTrue(ApsEventTypes.PLAN_RECEIVED.startsWith("aps."));
            assertTrue(ApsEventTypes.SCHEDULE_CREATED.startsWith("aps."));
            assertTrue(ApsEventTypes.SCHEDULE_RELEASED.startsWith("aps."));
            assertTrue(ApsEventTypes.TASK_DELAYED.startsWith("aps."));
            assertTrue(ApsEventTypes.RESCHEDULE_TRIGGERED.startsWith("aps."));
        }

        @Test
        @DisplayName("事件命名 — 遵循 {module}.{entity}.{past_tense} 格式")
        void events_shouldFollowNamingConvention() {
            // 格式验证：三个点号分隔的部分
            assertEquals("aps.plan.received", ApsEventTypes.PLAN_RECEIVED);
            assertEquals("aps.schedule.created", ApsEventTypes.SCHEDULE_CREATED);
            assertEquals("aps.schedule.released", ApsEventTypes.SCHEDULE_RELEASED);
            assertEquals("aps.task.delayed", ApsEventTypes.TASK_DELAYED);
            assertEquals("aps.reschedule.triggered", ApsEventTypes.RESCHEDULE_TRIGGERED);
        }

        @Test
        @DisplayName("事件常量 — 构造器为私有（工具类模式）")
        void constructor_shouldBePrivate() {
            // 反射验证构造器不可访问
            var constructors = ApsEventTypes.class.getDeclaredConstructors();
            assertEquals(1, constructors.length);
            assertFalse(constructors[0].canAccess(null));
        }

        @Test
        @DisplayName("事件数量 — 应有 5 个事件常量（不含 PREFIX）")
        void shouldHaveFiveEvents() {
            // PREFIX + PLAN_RECEIVED + SCHEDULE_CREATED + SCHEDULE_RELEASED + TASK_DELAYED + RESCHEDULE_TRIGGERED
            var fields = ApsEventTypes.class.getDeclaredFields();
            long eventCount = java.util.Arrays.stream(fields)
                    .filter(f -> java.lang.reflect.Modifier.isStatic(f.getModifiers()))
                    .filter(f -> java.lang.reflect.Modifier.isPublic(f.getModifiers()))
                    .filter(f -> f.getType() == String.class && !f.getName().equals("PREFIX"))
                    .count();
            assertEquals(5, eventCount);
        }
    }

    // ==================== 排程规则（EDD/SPT/CR）====================

    @Nested
    @DisplayName("SchedulingRule 排程规则")
    class SchedulingRuleTests {

        private ScheduledTask makeTask(String wo, Instant end, int setupMin, int processMin) {
            return new ScheduledTask(wo, "PROC-001", "M-001", "OP-001",
                    end.minus(processMin + setupMin, ChronoUnit.MINUTES), end,
                    setupMin, processMin, List.of(), TaskStatus.SCHEDULED);
        }

        @Test
        @DisplayName("EDD — 按 endTime 升序排列（最早交期优先）")
        void edd_shouldSortByEndTimeAscending() {
            // Given
            Instant now = Instant.now();
            ScheduledTask t1 = makeTask("WO-001", now.plus(3, ChronoUnit.HOURS), 10, 50);  // 交期最晚
            ScheduledTask t2 = makeTask("WO-002", now.plus(1, ChronoUnit.HOURS), 10, 50);  // 交期最早
            ScheduledTask t3 = makeTask("WO-003", now.plus(2, ChronoUnit.HOURS), 10, 50);  // 交期中间
            List<ScheduledTask> tasks = new ArrayList<>(List.of(t1, t2, t3));

            // When
            SchedulingRule.sort(tasks, "EDD", now);

            // Then
            assertEquals("WO-002", tasks.get(0).getWorkOrderNo()); // 最早
            assertEquals("WO-003", tasks.get(1).getWorkOrderNo()); // 中间
            assertEquals("WO-001", tasks.get(2).getWorkOrderNo()); // 最晚
        }

        @Test
        @DisplayName("SPT — 按总耗时时升序排列（最短工时优先）")
        void spt_shouldSortByTotalDurationAscending() {
            // Given
            Instant now = Instant.now();
            ScheduledTask t1 = makeTask("WO-001", now.plus(2, ChronoUnit.HOURS), 30, 120); // 150min
            ScheduledTask t2 = makeTask("WO-002", now.plus(2, ChronoUnit.HOURS), 5, 30);    // 35min — 最短
            ScheduledTask t3 = makeTask("WO-003", now.plus(2, ChronoUnit.HOURS), 10, 50);   // 60min
            List<ScheduledTask> tasks = new ArrayList<>(List.of(t1, t2, t3));

            // When
            SchedulingRule.sort(tasks, "SPT", now);

            // Then
            assertEquals("WO-002", tasks.get(0).getWorkOrderNo()); // 35min
            assertEquals("WO-003", tasks.get(1).getWorkOrderNo()); // 60min
            assertEquals("WO-001", tasks.get(2).getWorkOrderNo()); // 150min
        }

        @Test
        @DisplayName("CR — 最小关键比率优先（紧迫任务排前面）")
        void cr_shouldSortByCriticalRatioAscending() {
            // Given
            Instant now = Instant.now();
            // WO-001: end in 60min, totalDuration=30min → CR = 60/30 = 2.0
            ScheduledTask t1 = makeTask("WO-001", now.plus(60, ChronoUnit.MINUTES), 10, 20);
            // WO-002: end in 30min, totalDuration=30min → CR = 30/30 = 1.0（更紧迫）
            ScheduledTask t2 = makeTask("WO-002", now.plus(30, ChronoUnit.MINUTES), 10, 20);
            // WO-003: end in 120min, totalDuration=30min → CR = 120/30 = 4.0
            ScheduledTask t3 = makeTask("WO-003", now.plus(120, ChronoUnit.MINUTES), 10, 20);
            List<ScheduledTask> tasks = new ArrayList<>(List.of(t3, t1, t2));

            // When
            SchedulingRule.sort(tasks, "CR", now);

            // Then — CR 越小越紧迫 → 排越前面
            assertEquals("WO-002", tasks.get(0).getWorkOrderNo()); // CR ≈ 1.0
            assertEquals("WO-001", tasks.get(1).getWorkOrderNo()); // CR ≈ 2.0
            assertEquals("WO-003", tasks.get(2).getWorkOrderNo()); // CR ≈ 4.0
        }

        @Test
        @DisplayName("CR — null endTime 的任务排最后")
        void cr_nullEndTime_shouldSortLast() {
            Instant now = Instant.now();
            ScheduledTask t1 = makeTask("WO-001", now.plus(1, ChronoUnit.HOURS), 10, 50);
            ScheduledTask t2 = new ScheduledTask("WO-002", "PROC-001", "M-001", "OP-001",
                    null, null, 10, 50, List.of(), TaskStatus.SCHEDULED);
            List<ScheduledTask> tasks = new ArrayList<>(List.of(t2, t1));

            SchedulingRule.sort(tasks, "CR", now);

            assertEquals("WO-001", tasks.get(0).getWorkOrderNo());
            assertEquals("WO-002", tasks.get(1).getWorkOrderNo());
        }

        @Test
        @DisplayName("of — 未知策略抛出 IllegalArgumentException")
        void of_unknownStrategy_shouldThrow() {
            assertThrows(IllegalArgumentException.class,
                    () -> SchedulingRule.of("UNKNOWN", Instant.now()));
        }

        @Test
        @DisplayName("sort — null 或空列表不抛异常")
        void sort_nullOrEmpty_shouldNotThrow() {
            Instant now = Instant.now();
            assertDoesNotThrow(() -> SchedulingRule.sort(null, "EDD", now));
            assertDoesNotThrow(() -> SchedulingRule.sort(List.of(), "SPT", now));
        }

        @Test
        @DisplayName("of — EDD Comparator 非 null 且可比较")
        void eddComparator_shouldBeNonNull() {
            Comparator<ScheduledTask> cmp = SchedulingRule.of("EDD", Instant.now());
            assertNotNull(cmp);

            // 验证实际上可以排序
            ScheduledTask t1 = makeTask("WO-001", Instant.now().plus(2, ChronoUnit.HOURS), 10, 50);
            ScheduledTask t2 = makeTask("WO-002", Instant.now().plus(1, ChronoUnit.HOURS), 10, 50);
            assertTrue(cmp.compare(t2, t1) < 0); // t2 交期更早
        }
    }

    // ==================== ApsService 接口契约 ====================

    @Nested
    @DisplayName("ApsService 接口契约")
    class ApsServiceContractTests {

        @Test
        @DisplayName("接口方法签名 — 包含 5 个核心方法")
        void shouldDeclareFiveCoreMethods() {
            var methods = ApsService.class.getDeclaredMethods();
            assertEquals(5, methods.length);
        }

        @Test
        @DisplayName("接口方法 — createSchedule 返回 Schedule")
        void createSchedule_shouldReturnSchedule() throws NoSuchMethodException {
            var method = ApsService.class.getMethod("createSchedule", String.class, String.class);
            assertEquals(Schedule.class, method.getReturnType());
        }

        @Test
        @DisplayName("接口方法 — optimize 接受 strategy 参数")
        void optimize_shouldAcceptStrategyParam() throws NoSuchMethodException {
            var method = ApsService.class.getMethod("optimize", String.class, String.class);
            assertEquals(Schedule.class, method.getReturnType());
        }

        @Test
        @DisplayName("接口方法 — dispatch 返回 void")
        void dispatch_shouldReturnVoid() throws NoSuchMethodException {
            var method = ApsService.class.getMethod("dispatch", String.class);
            assertEquals(void.class, method.getReturnType());
        }

        @Test
        @DisplayName("接口方法 — reschedule 接受 triggerEvent 参数")
        void reschedule_shouldAcceptTriggerEventParam() throws NoSuchMethodException {
            var method = ApsService.class.getMethod("reschedule", String.class, String.class);
            assertEquals(Schedule.class, method.getReturnType());
        }

        @Test
        @DisplayName("接口方法 — getSchedule 返回 Schedule")
        void getSchedule_shouldReturnSchedule() throws NoSuchMethodException {
            var method = ApsService.class.getMethod("getSchedule", String.class);
            assertEquals(Schedule.class, method.getReturnType());
        }
    }

    // ==================== 综合场景 ====================

    @Nested
    @DisplayName("综合场景")
    class IntegrationTests {

        @Test
        @DisplayName("完整排程生命周期 — 创建→优化(EDD)→下发→执行→完成")
        void fullLifecycle_withEDDStrategy() {
            // Given: 创建排程 + 添加无序任务
            Schedule schedule = new Schedule("SCH-INT-001", "PLAN-INT-001", "FACTORY-01");
            Instant now = Instant.now();
            schedule.addTask(makeTask("WO-003", now.plus(3, ChronoUnit.HOURS), 10, 50));
            schedule.addTask(makeTask("WO-001", now.plus(1, ChronoUnit.HOURS), 10, 50));
            schedule.addTask(makeTask("WO-002", now.plus(2, ChronoUnit.HOURS), 10, 50));

            // When: DRAFT → OPTIMIZED (EDD)
            schedule.optimize("EDD");

            // Then: 状态 + 策略 + 任务状态
            assertEquals(ScheduleStatus.OPTIMIZED, schedule.getStatus());
            assertEquals("EDD", schedule.getStrategy());
            schedule.getTasks().forEach(t -> assertEquals(TaskStatus.SCHEDULED, t.getStatus()));

            // When: OPTIMIZED → DISPATCHED
            schedule.dispatch();
            assertEquals(ScheduleStatus.DISPATCHED, schedule.getStatus());
            schedule.getTasks().forEach(t -> assertEquals(TaskStatus.DISPATCHED, t.getStatus()));

            // When: DISPATCHED → IN_PROGRESS
            schedule.start();
            assertEquals(ScheduleStatus.IN_PROGRESS, schedule.getStatus());

            // When: IN_PROGRESS → COMPLETED
            schedule.complete();
            assertEquals(ScheduleStatus.COMPLETED, schedule.getStatus());
        }

        @Test
        @DisplayName("排程取消场景 — 所有任务标记 CANCELLED")
        void cancelScenario_allTasksCancelled() {
            // Given
            Schedule schedule = new Schedule("SCH-INT-002", "PLAN-INT-002", "FACTORY-02");
            schedule.addTask(makeTask("WO-001", Instant.now().plus(1, ChronoUnit.HOURS), 10, 50));
            schedule.addTask(makeTask("WO-002", Instant.now().plus(2, ChronoUnit.HOURS), 10, 50));
            schedule.optimize("SPT");
            schedule.dispatch();

            // When
            schedule.cancel();

            // Then
            assertEquals(ScheduleStatus.CANCELLED, schedule.getStatus());
            assertTrue(schedule.getTasks().stream().allMatch(
                    t -> t.getStatus() == TaskStatus.CANCELLED));
        }

        @Test
        @DisplayName("资源日历 + 排程约束 — 验证产能约束检查")
        void resourceCalendarAndConstraint() {
            // Given: 白班 8:00-20:00
            ResourceCalendar cal = new ResourceCalendar("RC-001", "M-001",
                    ResourceType.MACHINE, LocalDate.now(),
                    Instant.parse("2026-07-19T08:00:00Z"),
                    Instant.parse("2026-07-19T20:00:00Z"),
                    BigDecimal.valueOf(12));

            // When & Then: 在可用窗口内的任务
            ScheduledTask validTask = makeTask("WO-001",
                    Instant.parse("2026-07-19T12:00:00Z"), 10, 50);
            assertTrue(cal.canFit(validTask.getStartTime(), validTask.getEndTime()));

            // 超出可用窗口的任务
            ScheduledTask invalidTask = makeTask("WO-002",
                    Instant.parse("2026-07-19T22:00:00Z"), 10, 50);
            assertFalse(cal.canFit(invalidTask.getStartTime(), invalidTask.getEndTime()));

            // 产能检查：有效任务时长 = 60min < 720min 产能
            long overlap = cal.overlapMinutes(validTask.getStartTime(), validTask.getEndTime());
            assertTrue(overlap <= cal.getCapacityMinutes());
        }

        private ScheduledTask makeTask(String wo, Instant end, int setupMin, int processMin) {
            return new ScheduledTask(wo, "PROC-001", "M-001", "OP-001",
                    end.minus(processMin + setupMin, ChronoUnit.MINUTES), end,
                    setupMin, processMin, List.of(), TaskStatus.SCHEDULED);
        }
    }

    // ==================== ScheduleStatus 枚举覆盖 ====================

    @Nested
    @DisplayName("ScheduleStatus 枚举（core batch/）")
    class ScheduleStatusTests {

        @Test
        @DisplayName("全部 6 个状态值存在")
        void shouldHaveSixValues() {
            assertEquals(6, ScheduleStatus.values().length);
        }

        @Test
        @DisplayName("COMPLETED + CANCELLED 为终态（无后续转换）")
        void completedAndCancelled_areTerminal() {
            assertTrue(ScheduleStatus.COMPLETED.allowedTransitions().isEmpty());
            assertTrue(ScheduleStatus.CANCELLED.allowedTransitions().isEmpty());
        }

        @Test
        @DisplayName("DRAFT → OPTIMIZED + CANCELLED")
        void draft_allowedTransitions() {
            var allowed = ScheduleStatus.DRAFT.allowedTransitions();
            assertEquals(2, allowed.size());
            assertTrue(allowed.contains(ScheduleStatus.OPTIMIZED));
            assertTrue(allowed.contains(ScheduleStatus.CANCELLED));
        }
    }

}
