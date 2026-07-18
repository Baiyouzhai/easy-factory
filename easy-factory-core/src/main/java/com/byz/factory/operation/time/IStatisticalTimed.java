package com.byz.factory.operation.time;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Optional;

/**
 * 统计计时 — 在点估计基础上，增加标准差和变异系数。
 * <p>
 * 用于 SPC 统计过程控制和概率排程：
 * <ul>
 *   <li>±1σ → 68.3% 概率范围内</li>
 *   <li>±2σ → 95.4%</li>
 *   <li>±3σ → 99.7%（SPC 控制限）</li>
 * </ul>
 *
 * @author 苏政
 */
public interface IStatisticalTimed extends ITimed {

    /**
     * 时间标准差（基于历史数据的 σ）。
     */
    Optional<Duration> getStandardDeviation();

    /**
     * 变异系数 CV = σ / μ，衡量相对离散程度。
     * CV < 0.1 → 稳定；CV > 0.3 → 失控风险。
     */
    default Optional<BigDecimal> getCoefficientOfVariation() {
        return getStandardDeviation().map(sd -> {
            long meanMs = getProcessingTime().toMillis();
            if (meanMs == 0) return BigDecimal.ZERO;
            return BigDecimal.valueOf(sd.toMillis())
                .divide(BigDecimal.valueOf(meanMs), 4, RoundingMode.HALF_UP);
        });
    }

    /**
     * 给定σ倍数的上界估计。
     * @param sigmas σ倍数（1/2/3）
     */
    default Duration getUpperBound(int sigmas) {
        return getStandardDeviation()
            .map(sd -> getProcessingTime().plus(sd.multipliedBy(sigmas)))
            .orElse(getProcessingTime());
    }

    /**
     * 给定σ倍数的下界估计。
     */
    default Duration getLowerBound(int sigmas) {
        return getStandardDeviation()
            .map(sd -> {
                long lower = getProcessingTime().minus(sd.multipliedBy(sigmas)).toMillis();
                return Duration.ofMillis(Math.max(lower, 0));
            })
            .orElse(getProcessingTime());
    }

    /**
     * 判断过程是否稳定（CV < 0.2 视为受控）。
     */
    default boolean isStable() {
        return getCoefficientOfVariation()
            .map(cv -> cv.compareTo(new BigDecimal("0.2")) < 0)
            .orElse(false);
    }
}
