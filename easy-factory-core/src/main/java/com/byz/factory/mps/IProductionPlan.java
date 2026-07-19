package com.byz.factory.mps;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 生产计划抽象 — MPS 生产计划实体的跨模块契约。
 * <p>
 * MES 通过此接口获取待执行的工单计划，APS 通过此接口获取排程输入，
 * ERP 通过此接口同步计划数据。无需直接依赖 easy-factory-mps。
 * <p>
 * 嵌套 {@link IPlanItem} 接口提供计划明细项的跨模块引用，
 * 模式与 {@link IPurchaseOrder#getItems()} 一致。
 *
 * @author 苏政
 * @see ProductionPlanStatus
 * @see com.byz.factory.mps.model.ProductionPlan
 */
public interface IProductionPlan {

    /** 计划编号 */
    String getPlanNo();

    /** 周期类型 (WEEKLY / MONTHLY / QUARTERLY) */
    String getPeriodType();

    /** 周期起始日期 */
    LocalDate getPeriodStart();

    /** 周期结束日期 */
    LocalDate getPeriodEnd();

    /** 计划状态 */
    ProductionPlanStatus getStatus();

    /** 计划明细列表 */
    List<? extends IPlanItem> getItems();

    /** 审批人 */
    String getApprovedBy();

    /** 创建时间 */
    Instant getCreatedAt();

    /**
     * 计划明细项 — 单个产品的一次生产计划条目。
     * <p>
     * MES 订阅 {@code mps.plan.released} 事件后，
     * 遍历 {@link IProductionPlan#getItems()} 为每个明细创建工单。
     */
    interface IPlanItem {

        /** 产品编码 */
        String getProductCode();

        /** 产品名称 */
        String getProductName();

        /** 计划数量 */
        BigDecimal getQuantity();

        /** 交付日期 */
        LocalDate getDueDate();

        /** 优先级（1=最高） */
        int getPriority();

        /** 执行工厂编码 */
        String getFactoryCode();

    }

}
