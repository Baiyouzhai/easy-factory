package com.byz.factory.operation.time;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

/**
 * 分类计时 — 在点估计基础上，增加精益分类和效能指标。
 * <p>
 * 用于价值流图(VSM)分析和精益改善：区分增值/非增值/浪费。
 *
 * @author 苏政
 */
public interface ICategorizedTimed extends ITimed {

    /**
     * 时间分类。
     */
    TimeCategory getCategory();

    /**
     * 设置时间占比 = setupTime / estimateTotal。
     * 高占比 → SMED 改善目标。
     */
    default BigDecimal getSetupRatio(BigDecimal batchSize) {
        Duration total = estimateTotal(batchSize);
        if (total.isZero()) return BigDecimal.ZERO;
        return BigDecimal.valueOf(getSetupTime().toMillis())
            .divide(BigDecimal.valueOf(total.toMillis()), 4, RoundingMode.HALF_UP);
    }

    /**
     * 时间效能 = 增值时间 / 总时间。
     */
    default BigDecimal getValueAddRatio(BigDecimal batchSize) {
        Duration total = estimateTotal(batchSize);
        if (total.isZero()) return BigDecimal.ZERO;

        long valueAddMs = getCategory() == TimeCategory.VALUE_ADDED
            ? getProcessingTime().toMillis()
            : 0;
        return BigDecimal.valueOf(valueAddMs)
            .divide(BigDecimal.valueOf(total.toMillis()), 4, RoundingMode.HALF_UP);
    }

    /**
     * 是否需要 SMED 改善（设置时间占比 > 20%）。
     */
    default boolean needsSmedImprovement(BigDecimal batchSize) {
        return getSetupRatio(batchSize).compareTo(new BigDecimal("0.20")) > 0;
    }
}
