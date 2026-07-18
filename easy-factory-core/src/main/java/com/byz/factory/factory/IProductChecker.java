package com.byz.factory.factory;

/**
 * 可制造性检查器 — 验证给定工厂是否能生产指定产品。
 * <p>
 * 比对产品蓝图的工序列表与工厂的工序列表，逐工序返回通过/失败结果。
 *
 * @author 苏政
 */
public interface IProductChecker {

    /**
     * 检查产品在指定工厂的可制造性。
     *
     * @param product 产品信息（含蓝图工序路线）
     * @param factory 目标工厂
     * @return 逐工序检查结果
     */
    ProductFactoryResult check(IProductInfo product, IFactory factory);

}
