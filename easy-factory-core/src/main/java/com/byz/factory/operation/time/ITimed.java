package com.byz.factory.operation.time;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * 可计时 — 为动作/工序等实体提供统一的时间估算契约。
 * <p>
 * 三种时间本质：
 * <ul>
 *   <li><b>FIXED</b> — 固定时间，不随批量变化（如：灭菌30分钟、复核5分钟）</li>
 *   <li><b>LINEAR</b> — 线性时间，随批量等比缩放（如：每片压片0.1秒）</li>
 *   <li><b>FUZZY</b> — 模糊时间，无法精确定义（如：等待温度达标、质检直至合格）</li>
 * </ul>
 *
 * @author 苏政
 */
public interface ITimed {

    /** 时间本质 */
    enum TimeNature {
        /** 固定时间 — 与批量无关 */
        FIXED,
        /** 线性缩放 — 时间 ∝ 批量 */
        LINEAR,
        /** 模糊时间 — 无法精确定义，仅给出参考范围 */
        FUZZY
    }

    /**
     * 关联的动作编码。
     */
    String getActionCode();

    /**
     * 准备/设置时间（总是固定值）。
     */
    Duration getSetupTime();

    /**
     * 处理时间。
     * <ul>
     *   <li>FIXED: 总处理时间</li>
     *   <li>LINEAR: 标准批量下的处理时间</li>
     *   <li>FUZZY: 最佳估计值</li>
     * </ul>
     */
    Duration getProcessingTime();

    /**
     * 拆卸/清理时间（总是固定值）。
     */
    Duration getTeardownTime();

    /**
     * 时间本质类型。
     */
    TimeNature getTimeNature();

    /**
     * 标准批量（LINEAR 模式下的参照批量）。
     * FIXED/FUZZY 模式返回 null。
     */
    BigDecimal getStandardBatchSize();

    /**
     * 估算给定批量的总时长。
     * <ul>
     *   <li>FIXED: setup + processing + teardown</li>
     *   <li>LINEAR: setup + processing×(batch/standard) + teardown</li>
     *   <li>FUZZY: setup + processing(作参考) + teardown</li>
     * </ul>
     */
    Duration estimateTotal(BigDecimal batchSize);

    /**
     * 是否为模糊时间。
     */
    default boolean isFuzzy() {
        return getTimeNature() == TimeNature.FUZZY;
    }

    /**
     * 是否为线性时间。
     */
    default boolean isLinear() {
        return getTimeNature() == TimeNature.LINEAR;
    }
}
