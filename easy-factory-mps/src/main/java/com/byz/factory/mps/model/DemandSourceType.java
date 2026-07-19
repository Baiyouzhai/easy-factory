package com.byz.factory.mps.model;

/**
 * 需求来源类型 — MPS 模块内部的需求分类。
 * <p>
 * 不同来源类型影响 MPS 的优先级排序和产能分配策略。
 * 此枚举限定于 MPS 模块内部，跨模块通过 {@code IDemandSource.getSourceType()} 的字符串形式传递。
 *
 * @author 苏政
 */
public enum DemandSourceType {

    /** 销售订单 — 已确认的客户需求，优先级最高 */
    SALES_ORDER,

    /** 预测 — 基于历史数据的预估需求，优先级次之 */
    FORECAST,

    /** 安全库存补货 — 维持最低库存水平的需求 */
    SAFETY_STOCK,

    /** 手工录入 — 计划员手动添加的需求 */
    MANUAL

}
