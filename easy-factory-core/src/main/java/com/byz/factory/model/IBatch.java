package com.byz.factory.model;

import com.byz.factory.data.Dict;
import com.byz.factory.design.IBlueprint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 批次 — 制造追溯的基本单元。
 * <p>
 * 一个批次贯穿整个生产流程，每个资源消耗/产出都关联到批次。
 * 批次是 GMP/FDA 合规中批记录追溯的核心。
 *
 * @author 苏政
 * @see ITraceable
 */
public interface IBatch {

    /**
     * 批号（唯一标识，通常格式: 产品代码+日期+序列）
     *
     * @return 批号
     */
    String getBatchNo();

    /**
     * 产品编码
     *
     * @return 产品编码
     */
    String getProductCode();

    /**
     * 使用的蓝图版本（冻结的生产工艺版本快照）
     *
     * @return 蓝图
     */
    IBlueprint getBlueprint();

    /**
     * 批量（计划生产数量）
     *
     * @return 批量
     */
    BigDecimal getBatchSize();

    /**
     * 实际收率
     *
     * @return 实际产量
     */
    BigDecimal getActualYield();

    /**
     * 收率百分比
     *
     * @return 收率 = 实际/批量 * 100%
     */
    default BigDecimal getYieldPercent() {
        if (getBatchSize() == null || BigDecimal.ZERO.compareTo(getBatchSize()) >= 0) {
            return BigDecimal.ZERO;
        }
        return getActualYield()
            .divide(getBatchSize(), 4, java.math.RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }

    /**
     * 批次状态
     *
     * @return 状态
     */
    BatchStatus getStatus();

    /**
     * 开始时间
     *
     * @return 开始时间
     */
    Instant getStartTime();

    /**
     * 结束时间
     *
     * @return 结束时间
     */
    Instant getEndTime();

    /**
     * 工序执行记录列表
     *
     * @return 工序记录
     */
    List<? extends ITraceable> getTraceRecords();

    /**
     * 关联的工单号（MES）
     *
     * @return 工单号
     */
    String getWorkOrderNo();

    /**
     * 执行工厂
     *
     * @return 工厂标识
     */
    String getFactoryCode();

}
