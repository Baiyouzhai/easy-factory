package com.byz.factory.operation.time;

import java.time.Duration;
import java.util.Optional;

/**
 * GMP 合规动作时长 — 在 ActionDuration 基础上叠加法规约束。
 * <p>
 * 用于质量合规检查：确保每道工序的实际耗时在允许范围内。
 * <p>
 * 用法：
 * <pre>{@code
 * GmpActionDuration gad = GmpActionDuration.forAction("A005")
 *     .processingMinutes(90)
 *     .maxAllowedMinutes(240)       // 法规上限 4h
 *     .minRequiredMinutes(60)       // 工艺下限 1h
 *     .shelfLifeAfterActionHours(72) // 制粒后72h内必须压片
 *     .build();
 *
 * // 运行时校验
 * boolean ok = gad.checkConstraints(actualDuration).passed();
 * }</pre>
 *
 * @author 苏政
 */
public class GmpActionDuration extends ActionDuration implements IConstrainedTimed {

    private final Duration maxAllowedTime;
    private final Duration minRequiredTime;
    private final Duration shelfLifeAfterAction;

    private GmpActionDuration(Builder builder) {
        super(builder);
        this.maxAllowedTime = builder.maxAllowedTime;
        this.minRequiredTime = builder.minRequiredTime;
        this.shelfLifeAfterAction = builder.shelfLifeAfterAction;
    }

    @Override
    public Optional<Duration> getMaxAllowedTime() {
        return Optional.ofNullable(maxAllowedTime);
    }

    @Override
    public Optional<Duration> getMinRequiredTime() {
        return Optional.ofNullable(minRequiredTime);
    }

    @Override
    public Optional<Duration> getShelfLifeAfterAction() {
        return Optional.ofNullable(shelfLifeAfterAction);
    }

    // ---- Builder ----

    public static Builder forAction(String actionCode) {
        return new Builder(actionCode);
    }

    public static class Builder extends ActionDuration.Builder {
        private Duration maxAllowedTime;
        private Duration minRequiredTime;
        private Duration shelfLifeAfterAction;

        Builder(String actionCode) { super(actionCode); }

        public Builder maxAllowed(Duration d) { this.maxAllowedTime = d; return this; }
        public Builder maxAllowedMinutes(long m) { this.maxAllowedTime = Duration.ofMinutes(m); return this; }
        public Builder maxAllowedHours(long h) { this.maxAllowedTime = Duration.ofHours(h); return this; }
        public Builder minRequired(Duration d) { this.minRequiredTime = d; return this; }
        public Builder minRequiredMinutes(long m) { this.minRequiredTime = Duration.ofMinutes(m); return this; }
        public Builder shelfLifeAfterAction(Duration d) { this.shelfLifeAfterAction = d; return this; }
        public Builder shelfLifeAfterActionHours(long h) { this.shelfLifeAfterAction = Duration.ofHours(h); return this; }

        @Override
        public GmpActionDuration build() { return new GmpActionDuration(this); }
    }
}
