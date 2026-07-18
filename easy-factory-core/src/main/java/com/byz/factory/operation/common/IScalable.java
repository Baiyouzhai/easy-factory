package com.byz.factory.operation.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 可缩放 — 为资源/数量等实体提供统一的缩放契约。
 * <p>
 * 常用于：
 * <ul>
 *   <li>资源项的批量缩放（物料需求、工时需求）</li>
 *   <li>产能的期间缩放（日产能 → 年产能）</li>
 *   <li>成本的批量折算（标准成本 → 批量成本）</li>
 * </ul>
 *
 * @param <T> 缩放后的返回类型
 * @author 苏政
 */
public interface IScalable<T> {

    /**
     * 按因子缩放。
     *
     * @param factor 缩放因子（> 0）
     * @return 缩放后的新实例
     */
    T scaleBy(BigDecimal factor);

    /**
     * 按 目标/标准 比例缩放。
     *
     * @param targetValue   目标值
     * @param standardValue 标准参照值
     * @return 缩放后的新实例
     */
    default T scaleTo(BigDecimal targetValue, BigDecimal standardValue) {
        if (standardValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("standardValue must be > 0");
        }
        BigDecimal factor = targetValue.divide(standardValue, 6, RoundingMode.HALF_UP);
        return scaleBy(factor);
    }

}
