package com.byz.factory.operation.time;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

/**
 * 动作时长 — 不侵入 IAction 接口，通过组合方式为动作附加时间属性。
 * <p>
 * 支持两种时间模式：
 * <ul>
 *   <li><b>固定时间</b> — 无论批量大小，时间固定（如：灭菌30分钟）</li>
 *   <li><b>线性缩放</b> — 时间随批量线性增长（如：每片压片0.1秒）</li>
 * </ul>
 *
 * @author 苏政
 */
public class ActionDuration implements ITimed {

    /** 关联的动作编码 */
    private final String actionCode;

    /** 处理时间（isFixed=true时为总时间，false时为单位批量时间） */
    private final Duration processingTime;

    /** 设置/准备时间 */
    private final Duration setupTime;

    /** 拆卸/清理时间 */
    private final Duration teardownTime;

    /** 是否固定时间（true=固定总时间，false=按批量缩放） */
    private final boolean fixedTime;

    /** 标准批量大小（fixedTime=false时作为分母） */
    private final BigDecimal standardBatchSize;

    protected ActionDuration(Builder builder) {
        this.actionCode = Objects.requireNonNull(builder.actionCode, "actionCode must not be null");
        this.processingTime = Objects.requireNonNull(builder.processingTime, "processingTime must not be null");
        this.setupTime = builder.setupTime != null ? builder.setupTime : Duration.ZERO;
        this.teardownTime = builder.teardownTime != null ? builder.teardownTime : Duration.ZERO;
        this.fixedTime = builder.fixedTime;
        this.standardBatchSize = builder.standardBatchSize != null ? builder.standardBatchSize : BigDecimal.ONE;
    }

    /**
     * 计算给定批量的总时长。
     *
     * @param batchSize 批量大小
     * @return 总时长 = 设置 + 处理 + 拆卸
     */
    public Duration getTotalDuration(BigDecimal batchSize) {
        Duration scaledProcessing;
        if (fixedTime) {
            scaledProcessing = processingTime;
        } else {
            // 按批量线性缩放: processingTime × (batchSize / standardBatchSize)
            BigDecimal ratio = batchSize.divide(standardBatchSize, 6, java.math.RoundingMode.HALF_UP);
            scaledProcessing = Duration.ofMillis(
                (long) (processingTime.toMillis() * ratio.doubleValue()));
        }
        return setupTime.plus(scaledProcessing).plus(teardownTime);
    }

    /**
     * 计算单位产品的平均时长。
     *
     * @return 折算到单件的时间
     */
    public Duration getDurationPerUnit() {
        if (fixedTime) {
            return processingTime;  // 固定时间，但调用者应传入合理的批量
        }
        long millisPerStandardBatch = processingTime.toMillis();
        long millisPerUnit = millisPerStandardBatch / standardBatchSize.longValue();
        return Duration.ofMillis(Math.max(millisPerUnit, 1));
    }

    // ---- ITimed implementation ----

    @Override
    public TimeNature getTimeNature() {
        return fixedTime ? TimeNature.FIXED : TimeNature.LINEAR;
    }

    @Override
    public BigDecimal getStandardBatchSize() {
        return fixedTime ? null : standardBatchSize;
    }

    @Override
    public Duration estimateTotal(BigDecimal batchSize) {
        return getTotalDuration(batchSize);
    }

    // ---- Getters ----

    public String getActionCode() { return actionCode; }
    public Duration getProcessingTime() { return processingTime; }
    public Duration getSetupTime() { return setupTime; }
    public Duration getTeardownTime() { return teardownTime; }
    public boolean isFixedTime() { return fixedTime; }

    // ---- Builder ----

    public static Builder forAction(String actionCode) {
        return new Builder(actionCode);
    }

    public static class Builder {
        private final String actionCode;
        private Duration processingTime;
        private Duration setupTime;
        private Duration teardownTime;
        private boolean fixedTime = true;
        private BigDecimal standardBatchSize;

        protected Builder(String actionCode) { this.actionCode = actionCode; }

        /** 设置处理时间 */
        public Builder processing(Duration time) { this.processingTime = time; return this; }

        /** 设置处理时间（分钟） */
        public Builder processingMinutes(long minutes) { this.processingTime = Duration.ofMinutes(minutes); return this; }

        /** 设置处理时间（秒） */
        public Builder processingSeconds(long seconds) { this.processingTime = Duration.ofSeconds(seconds); return this; }

        /** 设置准备时间 */
        public Builder setup(Duration time) { this.setupTime = time; return this; }
        public Builder setupMinutes(long minutes) { this.setupTime = Duration.ofMinutes(minutes); return this; }

        /** 设置拆卸时间 */
        public Builder teardown(Duration time) { this.teardownTime = time; return this; }
        public Builder teardownMinutes(long minutes) { this.teardownTime = Duration.ofMinutes(minutes); return this; }

        /** 设置为固定时间（不随批量缩放） */
        public Builder fixedTime() { this.fixedTime = true; return this; }

        /** 设置为可变时间（按批量缩放），需指定标准批量 */
        public Builder variableTime(BigDecimal standardBatchSize) {
            this.fixedTime = false;
            this.standardBatchSize = standardBatchSize;
            return this;
        }

        public ActionDuration build() { return new ActionDuration(this); }
    }
}
