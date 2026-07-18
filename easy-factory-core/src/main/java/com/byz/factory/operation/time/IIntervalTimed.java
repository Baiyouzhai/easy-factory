package com.byz.factory.operation.time;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

/**
 * 区间计时 — 在点估计基础上，提供最短/最长/典型时间的区间估计。
 * <p>
 * 用于精确排程(APS)和风险分析：调度算法需要知道"最早可能"和"最晚可能"。
 *
 * @author 苏政
 */
public interface IIntervalTimed extends ITimed {

    /**
     * 最短可能时间（理想条件，如：无等待、最优参数）。
     */
    Duration getMinTime(BigDecimal batchSize);

    /**
     * 最长可能时间（最差条件，如：拥堵、设备降速）。
     */
    Duration getMaxTime(BigDecimal batchSize);

    /**
     * 最可能时间（典型条件，等同于 estimateTotal）。
     */
    default Duration getTypicalTime(BigDecimal batchSize) {
        return estimateTotal(batchSize);
    }

    /**
     * PERT 三点估算：加权时间 = (min + 4×typical + max) / 6。
     */
    default Duration getPertEstimate(BigDecimal batchSize) {
        long minMs = getMinTime(batchSize).toMillis();
        long typMs = getTypicalTime(batchSize).toMillis();
        long maxMs = getMaxTime(batchSize).toMillis();
        return Duration.ofMillis((minMs + 4 * typMs + maxMs) / 6);
    }

    /**
     * 时间不确定性 = (max - min) / typical，越大越不确定。
     */
    default BigDecimal getUncertainty(BigDecimal batchSize) {
        long range = getMaxTime(batchSize).minus(getMinTime(batchSize)).toMillis();
        long typical = getTypicalTime(batchSize).toMillis();
        if (typical == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(range).divide(BigDecimal.valueOf(typical), 4,
            java.math.RoundingMode.HALF_UP);
    }
}
