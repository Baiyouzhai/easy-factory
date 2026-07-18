package com.byz.factory.aps.model;

import com.byz.factory.batch.ScheduleStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;

/**
 * 排程 — 继承 BaseLifecycleEntity 获得状态机（DRAFT→OPTIMIZED→DISPATCHED→IN_PROGRESS→COMPLETED）。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Schedule extends BaseLifecycleEntity<ScheduleStatus> {

    private String planNo;
    private String factoryCode;
    private Instant horizonStart;
    private Instant horizonEnd;
    private String strategy;
    private String optimizationGoal;
    private List<ScheduledTask> tasks;

    public Schedule(String code, String planNo, String factoryCode) {
        super(code, "排程-" + code, ScheduleStatus.DRAFT);
        this.planNo = planNo;
        this.factoryCode = factoryCode;
    }

    public record ScheduledTask(String workOrderNo, String processCode, String machineCode,
                                 String operatorCode, Instant scheduledStart, Instant scheduledEnd,
                                 int setupTimeMin, int processTimeMin, List<String> predecessors, String status) {}
}
