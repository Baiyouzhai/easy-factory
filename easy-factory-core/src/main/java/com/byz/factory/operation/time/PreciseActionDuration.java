package com.byz.factory.operation.time;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * 精确动作时长 — 在 ActionDuration 基础上叠加区间估计和统计信息。
 * <p>
 * 用于 APS 精确排程和 SPC 过程控制。
 * <p>
 * 用法：
 * <pre>{@code
 * PreciseActionDuration pad = PreciseActionDuration.forAction("A016")
 *     .processingMinutes(120)          // 均值 2h
 *     .minTimeMinutes(110)             // 最少 1h50m
 *     .maxTimeMinutes(135)             // 最多 2h15m
 *     .stdDeviationMinutes(8)          // σ = 8min
 *     .build();
 * }</pre>
 *
 * @author 苏政
 */
public class PreciseActionDuration extends ActionDuration implements IIntervalTimed, IStatisticalTimed {

    private final Duration minProcessingTime;
    private final Duration maxProcessingTime;
    private final Duration stdDeviation;

    private PreciseActionDuration(Builder builder) {
        super(builder);
        this.minProcessingTime = builder.minProcessingTime;
        this.maxProcessingTime = builder.maxProcessingTime;
        this.stdDeviation = builder.stdDeviation;
    }

    @Override
    public Duration getMinTime(BigDecimal batchSize) {
        return minProcessingTime != null ? scaleByBatch(minProcessingTime, batchSize) : estimateTotal(batchSize);
    }

    @Override
    public Duration getMaxTime(BigDecimal batchSize) {
        return maxProcessingTime != null ? scaleByBatch(maxProcessingTime, batchSize) : estimateTotal(batchSize);
    }

    @Override
    public Optional<Duration> getStandardDeviation() {
        return Optional.ofNullable(stdDeviation);
    }

    /** 按批量缩放时间（LINEAR模式） */
    private Duration scaleByBatch(Duration base, BigDecimal batchSize) {
        if (isFixedTime()) return base;
        if (getStandardBatchSize() == null) return base;
        BigDecimal ratio = batchSize.divide(getStandardBatchSize(), 6, java.math.RoundingMode.HALF_UP);
        return Duration.ofMillis((long) (base.toMillis() * ratio.doubleValue()));
    }

    // ---- Builder ----

    public static Builder forAction(String actionCode) {
        return new Builder(actionCode);
    }

    public static class Builder extends ActionDuration.Builder {
        private Duration minProcessingTime;
        private Duration maxProcessingTime;
        private Duration stdDeviation;

        Builder(String actionCode) { super(actionCode); }

        public Builder minTime(Duration d) { this.minProcessingTime = d; return this; }
        public Builder minTimeMinutes(long m) { this.minProcessingTime = Duration.ofMinutes(m); return this; }
        public Builder maxTime(Duration d) { this.maxProcessingTime = d; return this; }
        public Builder maxTimeMinutes(long m) { this.maxProcessingTime = Duration.ofMinutes(m); return this; }
        public Builder stdDeviation(Duration d) { this.stdDeviation = d; return this; }
        public Builder stdDeviationMinutes(long m) { this.stdDeviation = Duration.ofMinutes(m); return this; }

        @Override
        public PreciseActionDuration build() { return new PreciseActionDuration(this); }
    }
}
