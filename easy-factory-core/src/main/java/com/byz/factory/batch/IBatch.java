package com.byz.factory.batch;

import com.byz.factory.factory.IBlueprint;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
     */
    String getBatchNo();

    /**
     * 产品编码
     */
    String getProductCode();

    /**
     * 使用的蓝图版本（冻结的生产工艺版本快照）
     */
    IBlueprint getBlueprint();

    /**
     * 批量（计划生产数量）
     */
    BigDecimal getBatchSize();

    /**
     * 实际收率
     */
    BigDecimal getActualYield();

    /**
     * 收率百分比
     */
    default BigDecimal getYieldPercent() {
        if (getBatchSize() == null || BigDecimal.ZERO.compareTo(getBatchSize()) >= 0) {
            return BigDecimal.ZERO;
        }
        return getActualYield()
            .divide(getBatchSize(), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }

    /**
     * 批次状态
     */
    BatchStatus getStatus();

    /**
     * 开始时间
     */
    Instant getStartTime();

    /**
     * 结束时间
     */
    Instant getEndTime();

    /**
     * 工序执行记录列表
     */
    List<? extends ITraceable> getTraceRecords();

    /**
     * 关联的工单号（MES）
     */
    String getWorkOrderNo();

    /**
     * 执行工厂
     */
    String getFactoryCode();

    // ── WIP 位置追踪 ──

    /**
     * 当前所在工序编码（WIP 位置追踪）。
     * <p>
     * 默认实现返回 null，MES 模块实现类应覆盖此方法。
     *
     * @return 当前工序编码，未知时返回 null
     */
    default String getCurrentProcessCode() {
        return null;
    }

    /**
     * 当前所在工位编码（WIP 位置追踪）。
     * <p>
     * 默认实现返回 null，MES 模块实现类应覆盖此方法。
     *
     * @return 当前工位编码，未知时返回 null
     */
    default String getCurrentWorkstationCode() {
        return null;
    }

    // ── 物料谱系 ──

    /**
     * 获取父批次列表（用于批次拆分/合并追溯）。
     * <p>
     * 合并场景：当前批次由多个父批次合并生成。
     * 默认实现返回空列表。
     *
     * @return 父批号列表
     */
    default List<String> getParentBatchNos() {
        return List.of();
    }

    /**
     * 获取子批次列表（用于批次拆分/合并追溯）。
     * <p>
     * 拆分场景：当前批次拆分为多个子批次。
     * 默认实现返回空列表。
     *
     * @return 子批号列表
     */
    default List<String> getChildBatchNos() {
        return List.of();
    }

}
