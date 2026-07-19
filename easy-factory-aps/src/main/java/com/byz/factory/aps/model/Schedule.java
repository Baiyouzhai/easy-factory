package com.byz.factory.aps.model;

import com.byz.factory.batch.ScheduleStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 排程方案 — 继承 BaseLifecycleEntity 获得状态机（DRAFT→OPTIMIZED→DISPATCHED→IN_PROGRESS→COMPLETED）。
 * <p>
 * MPS 出周计划 → APS 排到日/小时 → MES 执行。排程方案是 MPS 计划到 MES 工单的桥梁，
 * 将 MPS 的 PlanItem 展开为具体设备/人员/时间段上的 ScheduledTask。
 * <p>
 * 业务便捷方法封装了状态转换 + 时间戳更新，调用方不直接操作 {@code setStatus()}。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Schedule extends BaseLifecycleEntity<ScheduleStatus> {

    /** 关联 MPS 计划编号 */
    private String planNo;

    /** 工厂编码 */
    private String factoryCode;

    /** 排程范围起始 */
    private Instant horizonStart;

    /** 排程范围结束 */
    private Instant horizonEnd;

    /** 排程策略（FORWARD/BACKWARD/BOTTLENECK） */
    private String strategy;

    /** 优化目标（MIN_MAKESPAN/MIN_TARDINESS/MAX_UTILIZATION） */
    private String optimizationGoal;

    /** 排程日期 */
    private LocalDate scheduledDate;

    /** 排程任务列表 */
    private List<ScheduledTask> tasks;

    // ==================== 构造器 ====================

    public Schedule(String code, String planNo, String factoryCode) {
        super(code, "排程-" + code, ScheduleStatus.DRAFT);
        this.planNo = planNo;
        this.factoryCode = factoryCode;
        this.tasks = new ArrayList<>();
        this.scheduledDate = LocalDate.now();
    }

    // ==================== 业务便捷方法 ====================

    /**
     * 执行优化 — DRAFT → OPTIMIZED。
     * 应用指定的排程策略（EDD/SPT/CR），对任务列表重新排序。
     *
     * @param strategy 排程策略名称
     */
    public void optimize(String strategy) {
        transition(ScheduleStatus.OPTIMIZED);
        this.strategy = strategy;
        // 优化后将所有任务标记为已排程
        if (tasks != null) {
            tasks.forEach(t -> t.setStatus(TaskStatus.SCHEDULED));
        }
        markUpdated();
    }

    /**
     * 下发到 MES — OPTIMIZED → DISPATCHED。
     */
    public void dispatch() {
        transition(ScheduleStatus.DISPATCHED);
        // 下发后将所有任务标记为已下发
        if (tasks != null) {
            tasks.forEach(t -> t.setStatus(TaskStatus.DISPATCHED));
        }
        markUpdated();
    }

    /**
     * 开始执行 — DISPATCHED → IN_PROGRESS。
     */
    public void start() {
        transition(ScheduleStatus.IN_PROGRESS);
        markUpdated();
    }

    /**
     * 完成 — IN_PROGRESS → COMPLETED。
     */
    public void complete() {
        transition(ScheduleStatus.COMPLETED);
        markUpdated();
    }

    /**
     * 取消 — 任意非终态 → CANCELLED。
     */
    public void cancel() {
        transition(ScheduleStatus.CANCELLED);
        if (tasks != null) {
            tasks.forEach(t -> t.setStatus(TaskStatus.CANCELLED));
        }
        markUpdated();
    }

    /**
     * 添加排程任务。
     *
     * @param task 排程任务
     */
    public void addTask(ScheduledTask task) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        this.tasks.add(task);
        markUpdated();
    }

    // ==================== 排程任务内嵌类 ====================

    /**
     * 排程任务 — 排程方案中单个工序在特定设备/时间段上的安排。
     * <p>
     * 每个 ScheduledTask 对应 MES 工单中的一道工序，包含分配的设备/人员、
     * 计划起止时间、前置依赖和当前状态。
     */
    @Data
    public static class ScheduledTask {

        /** 工单号 */
        private String workOrderNo;

        /** 工序编码 */
        private String processCode;

        /** 分配设备 */
        private String machineCode;

        /** 分配操作人 */
        private String operatorCode;

        /** 计划开始时间 */
        private Instant startTime;

        /** 计划结束时间 */
        private Instant endTime;

        /** 换型时间（分钟） */
        private int setupTimeMin;

        /** 加工时间（分钟） */
        private int processTimeMin;

        /** 前置任务列表（工序依赖） */
        private List<String> predecessors;

        /** 任务状态 */
        private TaskStatus status;

        public ScheduledTask() {
            this.status = TaskStatus.SCHEDULED;
            this.predecessors = new ArrayList<>();
        }

        public ScheduledTask(String workOrderNo, String processCode, String machineCode,
                             String operatorCode, Instant startTime, Instant endTime,
                             int setupTimeMin, int processTimeMin,
                             List<String> predecessors, TaskStatus status) {
            this.workOrderNo = workOrderNo;
            this.processCode = processCode;
            this.machineCode = machineCode;
            this.operatorCode = operatorCode;
            this.startTime = startTime;
            this.endTime = endTime;
            this.setupTimeMin = setupTimeMin;
            this.processTimeMin = processTimeMin;
            this.predecessors = predecessors != null ? predecessors : new ArrayList<>();
            this.status = status != null ? status : TaskStatus.SCHEDULED;
        }

        /**
         * 获取总耗时（分钟）：换型时间 + 加工时间。
         */
        public int getTotalDurationMin() {
            return setupTimeMin + processTimeMin;
        }

    }

}
