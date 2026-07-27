package com.byz.factory.aps.service.impl;

import com.byz.factory.aps.model.RescheduleTrigger;
import com.byz.factory.aps.model.Schedule;
import com.byz.factory.aps.model.Schedule.ScheduledTask;
import com.byz.factory.aps.service.ApsService;
import com.byz.factory.aps.service.SchedulingRule;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.ApsEventTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * APS 排程服务实现 — 内存存储（Phase 3 先跑通主链路，Phase 5 换数据库）。
 * <p>
 * 排程流程：
 * <ol>
 *   <li>createSchedule — 创建排程草稿（DRAFT），发布 {@code aps.schedule.created}</li>
 *   <li>optimize — 按策略（EDD/SPT/CR）排序任务并升级状态到 OPTIMIZED</li>
 *   <li>dispatch — 下发到 MES（DISPATCHED），发布 {@code aps.schedule.released}</li>
 *   <li>reschedule — 事件驱动重排程，创建 RescheduleTrigger 记录，
 *       发布 {@code aps.reschedule.triggered}</li>
 * </ol>
 * <p>
 * 约束检查（design-decisions.md §4.11）：
 * <ul>
 *   <li>createSchedule 时校验 planNo 不为空</li>
 *   <li>optimize 前校验排程状态为 DRAFT</li>
 *   <li>dispatch 前校验排程状态为 OPTIMIZED</li>
 *   <li>reschedule 校验排程存在且未终态</li>
 * </ul>
 *
 * @author 苏政
 */
public class ApsServiceImpl implements ApsService {

    /** 内存存储：排程方案 */
    private final Map<String, Schedule> scheduleStore = new ConcurrentHashMap<>();

    /** 内存存储：重排程触发记录 */
    private final Map<String, RescheduleTrigger> triggerStore = new ConcurrentHashMap<>();

    /** 排程编号自增器 */
    private final AtomicInteger scheduleSeq = new AtomicInteger(0);

    /** 触发记录编号自增器 */
    private final AtomicInteger triggerSeq = new AtomicInteger(0);

    // ── 排程生命周期 ──

    @Override
    public Schedule createSchedule(String planNo, String factoryCode) {
        if (planNo == null || planNo.isBlank()) {
            throw new IllegalArgumentException("planNo 不能为空");
        }
        if (factoryCode == null || factoryCode.isBlank()) {
            throw new IllegalArgumentException("factoryCode 不能为空");
        }

        String scheduleCode = generateScheduleCode();
        Schedule schedule = new Schedule(scheduleCode, planNo, factoryCode);
        scheduleStore.put(scheduleCode, schedule);

        publish(ApsEventTypes.SCHEDULE_CREATED, Map.of(
                "scheduleCode", scheduleCode,
                "planNo", planNo,
                "factoryCode", factoryCode));

        return schedule;
    }

    @Override
    public Schedule optimize(String scheduleCode, String strategy) {
        Schedule schedule = getRequiredSchedule(scheduleCode);

        // 应用排程规则排序
        List<ScheduledTask> tasks = schedule.getTasks();
        if (tasks != null && !tasks.isEmpty()) {
            SchedulingRule.sort(tasks, strategy, Instant.now());
        }

        // 状态转换：DRAFT → OPTIMIZED
        schedule.optimize(strategy);
        schedule.setScheduledDate(LocalDate.now());

        scheduleStore.put(scheduleCode, schedule);
        return schedule;
    }

    @Override
    public void dispatch(String scheduleCode) {
        Schedule schedule = getRequiredSchedule(scheduleCode);

        // 状态转换：OPTIMIZED → DISPATCHED
        schedule.dispatch();

        // 设置排程范围
        if (schedule.getTasks() != null && !schedule.getTasks().isEmpty()) {
            List<ScheduledTask> tasks = schedule.getTasks();
            schedule.setHorizonStart(tasks.get(0).getStartTime());
            schedule.setHorizonEnd(tasks.get(tasks.size() - 1).getEndTime());
        }

        scheduleStore.put(scheduleCode, schedule);

        publish(ApsEventTypes.SCHEDULE_RELEASED, Map.of(
                "scheduleCode", scheduleCode,
                "planNo", schedule.getPlanNo(),
                "taskCount", schedule.getTasks() != null ? schedule.getTasks().size() : 0));
    }

    @Override
    public Schedule reschedule(String scheduleCode, String triggerEvent) {
        Schedule schedule = getRequiredSchedule(scheduleCode);

        // 创建重排程触发记录
        String triggerCode = "RT-" + String.format("%04d", triggerSeq.incrementAndGet());
        RescheduleTrigger trigger = new RescheduleTrigger(triggerCode, scheduleCode, triggerEvent);
        triggerStore.put(triggerCode, trigger);

        // 发布重排程事件
        publish(ApsEventTypes.RESCHEDULE_TRIGGERED, Map.of(
                "scheduleCode", scheduleCode,
                "triggerEvent", triggerEvent,
                "triggerCode", triggerCode,
                "triggeredAt", trigger.getTriggeredAt().toString()));

        // 标记触发记录已处理
        trigger.markProcessed();

        // 重新应用当前策略排序
        if (schedule.getStrategy() != null && schedule.getTasks() != null && !schedule.getTasks().isEmpty()) {
            SchedulingRule.sort(schedule.getTasks(), schedule.getStrategy(), Instant.now());
        }

        schedule.markUpdated();
        scheduleStore.put(scheduleCode, schedule);
        return schedule;
    }

    @Override
    public Schedule getSchedule(String scheduleCode) {
        return scheduleStore.get(scheduleCode);
    }

    // ── 内部工具方法 ──

    /** 生成排程编号: SCH-yyyyMMdd-序号 */
    private String generateScheduleCode() {
        int seq = scheduleSeq.incrementAndGet();
        String datePart = LocalDate.now().toString().replace("-", "").substring(2);
        return String.format("SCH-%s-%04d", datePart, seq);
    }

    /** 获取排程（不存在时抛异常） */
    private Schedule getRequiredSchedule(String scheduleCode) {
        Schedule schedule = scheduleStore.get(scheduleCode);
        if (schedule == null) {
            throw new IllegalArgumentException("排程不存在: " + scheduleCode);
        }
        return schedule;
    }

    /** 发布领域事件 */
    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "aps", payload));
    }

    // ==================== 测试支持 ====================

    /**
     * 清空所有存储（仅供测试使用）。
     */
    public void clearAll() {
        scheduleStore.clear();
        triggerStore.clear();
        scheduleSeq.set(0);
        triggerSeq.set(0);
    }

    /**
     * 获取排程存储数量（仅供测试使用）。
     */
    public int getScheduleCount() {
        return scheduleStore.size();
    }

    /**
     * 获取触发记录存储数量（仅供测试使用）。
     */
    public int getTriggerCount() {
        return triggerStore.size();
    }

    /**
     * 获取重排程触发记录（仅供测试使用）。
     */
    public RescheduleTrigger getTrigger(String triggerCode) {
        return triggerStore.get(triggerCode);
    }

}
