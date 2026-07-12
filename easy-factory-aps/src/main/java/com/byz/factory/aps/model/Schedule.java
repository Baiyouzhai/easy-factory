package com.byz.factory.aps.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Schedule extends DataExpand {
    private String code;
    private String planNo;           // 关联 MPS 计划
    private String version;
    private String factoryCode;
    private Instant horizonStart;
    private Instant horizonEnd;
    private String strategy;         // FORWARD/BACKWARD/BOTTLENECK
    private String optimizationGoal; // MIN_MAKESPAN/MIN_TARDINESS/MAX_UTILIZATION
    private String status;
    private List<ScheduledTask> tasks;

    public record ScheduledTask(String workOrderNo, String processCode, String machineCode,
                                 String operatorCode, Instant scheduledStart, Instant scheduledEnd,
                                 int setupTimeMin, int processTimeMin, List<String> predecessors, String status) {}
}
