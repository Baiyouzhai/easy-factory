package com.byz.factory.operation.time;

import com.byz.factory.factory.IBlueprint;
import com.byz.factory.operation.common.ReportPrinter;
import com.byz.factory.process.IProcess;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

/**
 * 生产提前期计算器 — 计算从投料到完工的总时间。
 * <p>
 * 公式：总提前期 = Σ(工序周期时间 + 排队时间 + 转运时间)
 *
 * @author 苏政
 */
public class ProductionLeadTime {

    /** 工序间默认排队时间（有WIP时） */
    private final Duration defaultQueueTime;

    /** 工序间默认转运时间 */
    private final Duration defaultTransferTime;

    /** 每道工序的执行模式 */
    private final Map<String, ExecutionMode> processModes;

    /** 每道工序的排队时间（可覆盖默认值） */
    private final Map<String, Duration> queueTimeOverrides;

    /** 每道工序的转运时间（可覆盖默认值） */
    private final Map<String, Duration> transferTimeOverrides;

    public ProductionLeadTime() {
        this(Duration.ZERO, Duration.ZERO);
    }

    public ProductionLeadTime(Duration defaultQueueTime, Duration defaultTransferTime) {
        this.defaultQueueTime = defaultQueueTime;
        this.defaultTransferTime = defaultTransferTime;
        this.processModes = new HashMap<>();
        this.queueTimeOverrides = new HashMap<>();
        this.transferTimeOverrides = new HashMap<>();
    }

    /**
     * 设置某道工序的执行模式。
     */
    public ProductionLeadTime withProcessMode(String processCode, ExecutionMode mode) {
        processModes.put(processCode, mode);
        return this;
    }

    /**
     * 设置某道工序的排队时间。
     */
    public ProductionLeadTime withQueueTime(String processCode, Duration time) {
        queueTimeOverrides.put(processCode, time);
        return this;
    }

    /**
     * 设置某道工序的转运时间。
     */
    public ProductionLeadTime withTransferTime(String processCode, Duration time) {
        transferTimeOverrides.put(processCode, time);
        return this;
    }

    /**
     * 计算生产提前期。
     *
     * @param blueprint     产品蓝图（含工序列表）
     * @param durationMap   动作时长映射
     * @param batchSize     批量大小
     * @return 提前期计算结果
     */
    public LeadTimeResult calculate(IBlueprint blueprint,
                                     Map<String, ? extends ITimed> durationMap,
                                     BigDecimal batchSize) {
        List<IProcess> processes = blueprint.getProductionProcessList();
        if (processes == null || processes.isEmpty()) {
            return new LeadTimeResult(Duration.ZERO, Collections.emptyList());
        }

        List<ProcessLeadTime> breakdown = new ArrayList<>();
        Duration total = Duration.ZERO;

        for (int i = 0; i < processes.size(); i++) {
            IProcess process = processes.get(i);
            String code = process.getCode();

            // 工序周期时间
            ExecutionMode mode = processModes.getOrDefault(code, ExecutionMode.SEQUENTIAL);
            ProcessCycleTime pct = new ProcessCycleTime(mode);
            Duration cycleTime = pct.calculate(process, durationMap, batchSize);

            // 排队时间
            Duration queue = queueTimeOverrides.getOrDefault(code, defaultQueueTime);

            // 转运时间（第一个工序无转运）
            Duration transfer = (i > 0)
                ? transferTimeOverrides.getOrDefault(code, defaultTransferTime)
                : Duration.ZERO;

            Duration processTotal = cycleTime.plus(queue).plus(transfer);
            total = total.plus(processTotal);

            breakdown.add(new ProcessLeadTime(code, process.getName(), cycleTime, queue, transfer, processTotal));
        }

        return new LeadTimeResult(total, breakdown);
    }

    // ---- 结果类型 ----

    /**
     * 单道工序的提前期分解
     */
    public record ProcessLeadTime(
        String processCode,
        String processName,
        Duration cycleTime,
        Duration queueTime,
        Duration transferTime,
        Duration total
    ) {}

    /**
     * 总提前期结果
     */
    public record LeadTimeResult(
        Duration totalLeadTime,
        List<ProcessLeadTime> breakdown
    ) {
        public String toReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("总生产提前期: ").append(ReportPrinter.formatDuration(totalLeadTime)).append("\n\n");
            for (ProcessLeadTime p : breakdown) {
                sb.append("工序[").append(p.processCode()).append("] ").append(p.processName()).append("\n");
                sb.append("  周期: ").append(ReportPrinter.formatDuration(p.cycleTime()));
                sb.append("  排队: ").append(ReportPrinter.formatDuration(p.queueTime()));
                sb.append("  转运: ").append(ReportPrinter.formatDuration(p.transferTime()));
                sb.append("  → 小计: ").append(ReportPrinter.formatDuration(p.total())).append("\n");
            }
            return sb.toString();
        }
    }
}
