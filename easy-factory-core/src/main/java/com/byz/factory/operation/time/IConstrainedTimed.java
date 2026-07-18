package com.byz.factory.operation.time;

import com.byz.factory.operation.common.ReportPrinter;

import java.time.Duration;
import java.util.Optional;

/**
 * 约束计时 — 在点估计基础上，增加硬性时限约束。
 * <p>
 * GMP/法规核心需求：很多工序有强制的时间窗口。
 * <ul>
 *   <li>无菌灌装 ≤ 4小时（防止微生物滋生）</li>
 *   <li>混合 ≥ 30分钟（确保均匀度）</li>
 *   <li>中间体暂存 ≤ 72小时（化学稳定性）</li>
 * </ul>
 *
 * @author 苏政
 */
public interface IConstrainedTimed extends ITimed {

    /**
     * 最大允许时间（超过即偏差/不合格）。
     */
    Optional<Duration> getMaxAllowedTime();

    /**
     * 最小要求时间（不足即不合格）。
     */
    Optional<Duration> getMinRequiredTime();

    /**
     * 执行后物料/中间体的保质期。
     * 超过此时间未进入下一工序 → 报废或复检。
     */
    default Optional<Duration> getShelfLifeAfterAction() {
        return Optional.empty();
    }

    /**
     * 是否时间关键（如生物制品、放射性药物）。
     */
    default boolean isTimeCritical() {
        return getMaxAllowedTime().isPresent() || getShelfLifeAfterAction().isPresent();
    }

    /**
     * 校验实际耗时是否在约束范围内。
     *
     * @param actualDuration 实际耗时
     * @return 约束检查结果
     */
    default ConstraintResult checkConstraints(Duration actualDuration) {
        Optional<Duration> maxAllowed = getMaxAllowedTime();
        Optional<Duration> minRequired = getMinRequiredTime();

        boolean passed = true;
        StringBuilder violation = new StringBuilder();

        if (maxAllowed.isPresent() && actualDuration.compareTo(maxAllowed.get()) > 0) {
            passed = false;
            violation.append("超时: 实际").append(ReportPrinter.formatDuration(actualDuration))
                     .append(" > 上限").append(ReportPrinter.formatDuration(maxAllowed.get())).append("; ");
        }
        if (minRequired.isPresent() && actualDuration.compareTo(minRequired.get()) < 0) {
            passed = false;
            violation.append("不足: 实际").append(ReportPrinter.formatDuration(actualDuration))
                     .append(" < 下限").append(ReportPrinter.formatDuration(minRequired.get())).append("; ");
        }

        return new ConstraintResult(passed, violation.toString().trim());
    }

    record ConstraintResult(boolean passed, String violation) {
        public boolean isViolated() { return !passed; }
    }

}
