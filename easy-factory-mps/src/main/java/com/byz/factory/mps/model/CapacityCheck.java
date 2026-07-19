package com.byz.factory.mps.model;

import java.math.BigDecimal;

/**
 * 粗产能检查（RCCP）结果 — MPS 产能分析的值对象。
 * <p>
 * 不可变 record，记录瓶颈资源的产能供需对比和判定结果。
 * 由 {@link com.byz.factory.mps.service.MpsService#checkCapacity} 方法生成。
 * <p>
 * 底层计算使用 core 的 {@link com.byz.factory.operation.capacity.FactoryCapacityProfile}
 * 和 {@link com.byz.factory.operation.capacity.BottleneckDetector} 进行分析。
 *
 * @param planNo            关联计划编号
 * @param factoryCode       工厂编码
 * @param resourceType      瓶颈资源类型描述（MACHINE / PERSONNEL / MATERIAL）
 * @param requiredCapacity  需求产能（小时）
 * @param availableCapacity 可用产能（小时）
 * @param utilizationRate   产能利用率（%）
 * @param status            检查结果
 * @param bottleneck        瓶颈工序/资源描述
 * @author 苏政
 */
public record CapacityCheck(
        String planNo,
        String factoryCode,
        String resourceType,
        BigDecimal requiredCapacity,
        BigDecimal availableCapacity,
        BigDecimal utilizationRate,
        CapacityResult status,
        String bottleneck) {

    /**
     * 粗产能检查结果枚举。
     */
    public enum CapacityResult {
        /** 通过 — 产能充足 */
        PASS,
        /** 警告 — 产能接近上限（利用率 ≥ 85%） */
        WARNING,
        /** 失败 — 产能不足 */
        FAIL
    }

    /**
     * 创建 PASS 结果的便捷工厂方法。
     */
    public static CapacityCheck pass(String planNo, String factoryCode, String resourceType,
                                      BigDecimal requiredCapacity, BigDecimal availableCapacity,
                                      BigDecimal utilizationRate, String bottleneck) {
        return new CapacityCheck(planNo, factoryCode, resourceType,
                requiredCapacity, availableCapacity, utilizationRate, CapacityResult.PASS, bottleneck);
    }

    /**
     * 创建 WARNING 结果的便捷工厂方法。
     */
    public static CapacityCheck warning(String planNo, String factoryCode, String resourceType,
                                         BigDecimal requiredCapacity, BigDecimal availableCapacity,
                                         BigDecimal utilizationRate, String bottleneck) {
        return new CapacityCheck(planNo, factoryCode, resourceType,
                requiredCapacity, availableCapacity, utilizationRate, CapacityResult.WARNING, bottleneck);
    }

    /**
     * 创建 FAIL 结果的便捷工厂方法。
     */
    public static CapacityCheck fail(String planNo, String factoryCode, String resourceType,
                                      BigDecimal requiredCapacity, BigDecimal availableCapacity,
                                      BigDecimal utilizationRate, String bottleneck) {
        return new CapacityCheck(planNo, factoryCode, resourceType,
                requiredCapacity, availableCapacity, utilizationRate, CapacityResult.FAIL, bottleneck);
    }

    /**
     * 产能是否可接受（PASS 或 WARNING 均可继续）。
     */
    public boolean isAcceptable() {
        return status == CapacityResult.PASS || status == CapacityResult.WARNING;
    }

}
