package com.byz.factory.operation.time;

import com.byz.factory.operation.common.ReportPrinter;
import com.byz.factory.process.IAction;
import com.byz.factory.process.IProcess;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

/**
 * 工序周期时间计算器 — 计算一个工序完成一次（或一个批量）所需的时间。
 * <p>
 * 支持 SEQUENTIAL（串行）和 PARALLEL（并行）两种执行模式。
 *
 * @author 苏政
 */
public class ProcessCycleTime {

    private final ExecutionMode mode;

    public ProcessCycleTime() {
        this(ExecutionMode.SEQUENTIAL);
    }

    public ProcessCycleTime(ExecutionMode mode) {
        this.mode = mode;
    }

    /**
     * 计算工序周期时间。
     *
     * @param process         工序
     * @param durationMap     动作时长映射 (actionCode → ActionDuration)
     * @param batchSize       批量大小
     * @return 工序周期时间
     */
    public Duration calculate(IProcess process, Map<String, ? extends ITimed> durationMap,
                              BigDecimal batchSize) {
        List<IAction> actions = process.getActions();
        if (actions == null || actions.isEmpty()) {
            return Duration.ZERO;
        }

        return switch (mode) {
            case SEQUENTIAL -> calculateSequential(actions, durationMap, batchSize);
            case PARALLEL   -> calculateParallel(actions, durationMap, batchSize);
            case GROUPED    -> calculateSequential(actions, durationMap, batchSize); // default
        };
    }

    /**
     * 串行模式：总时间 = Σ(各动作总时长)
     */
    private Duration calculateSequential(List<IAction> actions,
                                          Map<String, ? extends ITimed> durationMap,
                                          BigDecimal batchSize) {
        Duration total = Duration.ZERO;
        for (IAction action : actions) {
            ITimed ad = durationMap.get(action.getCode());
            if (ad != null) {
                total = total.plus(ad.estimateTotal(batchSize));
            }
        }
        return total;
    }

    /**
     * 并行模式：总时间 = max(各动作总时长)
     */
    private Duration calculateParallel(List<IAction> actions,
                                        Map<String, ? extends ITimed> durationMap,
                                        BigDecimal batchSize) {
        Duration maxDuration = Duration.ZERO;
        for (IAction action : actions) {
            ITimed ad = durationMap.get(action.getCode());
            if (ad != null) {
                Duration d = ad.estimateTotal(batchSize);
                if (d.compareTo(maxDuration) > 0) {
                    maxDuration = d;
                }
            }
        }
        return maxDuration;
    }

    /**
     * 计算工序周期时间分解明细。
     */
    public CycleTimeBreakdown calculateBreakdown(IProcess process,
                                                  Map<String, ? extends ITimed> durationMap,
                                                  BigDecimal batchSize) {
        List<IAction> actions = process.getActions();
        Map<String, Duration> actionTimes = new LinkedHashMap<>();
        Duration total = Duration.ZERO;

        if (actions != null) {
            if (mode == ExecutionMode.PARALLEL) {
                for (IAction a : actions) {
                    ITimed ad = durationMap.get(a.getCode());
                    Duration d = ad != null ? ad.estimateTotal(batchSize) : Duration.ZERO;
                    actionTimes.put(a.getCode(), d);
                }
                total = actionTimes.values().stream().max(Duration::compareTo).orElse(Duration.ZERO);
            } else {
                for (IAction a : actions) {
                    ITimed ad = durationMap.get(a.getCode());
                    Duration d = ad != null ? ad.estimateTotal(batchSize) : Duration.ZERO;
                    actionTimes.put(a.getCode(), d);
                    total = total.plus(d);
                }
            }
        }

        return new CycleTimeBreakdown(process.getCode(), total, mode, actionTimes);
    }

    /**
     * 周期时间分解明细
     */
    public record CycleTimeBreakdown(
        String processCode,
        Duration totalDuration,
        ExecutionMode mode,
        Map<String, Duration> actionBreakdown
    ) {
        /** 格式化输出 */
        public String toReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("工序[").append(processCode).append("] ")
              .append(mode).append(" 模式: ")
              .append(ReportPrinter.formatDuration(totalDuration)).append("\n");
            actionBreakdown.forEach((code, d) ->
                sb.append("  ├─ ").append(code).append(": ").append(ReportPrinter.formatDuration(d)).append("\n"));
            return sb.toString();
        }
    }
}
