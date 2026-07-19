package com.byz.factory.mps;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 需求来源抽象 — MPS 需求来源实体的跨模块契约。
 * <p>
 * ERP 通过此接口提供销售订单数据，SCM 通过此接口提供预测数据，
 * APS 通过此接口消费需求进行排程。无需直接依赖 easy-factory-mps。
 * <p>
 * 需求来源的 {@code sourceType} 枚举值由 MPS 模块内部定义
 * （SALES_ORDER / FORECAST / SAFETY_STOCK / MANUAL），
 * 跨模块通过字符串传递，模式与 {@link ISupplier#getCategory()} 一致。
 *
 * @author 苏政
 * @see com.byz.factory.mps.model.DemandSource
 */
public interface IDemandSource {

    /** 来源类型 (SALES_ORDER / FORECAST / SAFETY_STOCK / MANUAL) */
    String getSourceType();

    /** 来源单据号（如销售订单号 SO-001） */
    String getReferenceNo();

    /** 产品编码 */
    String getProductCode();

    /** 需求数量 */
    BigDecimal getQuantity();

    /** 需求日期 */
    LocalDate getDueDate();

    /** 优先级（1=最高） */
    int getPriority();

    /** 客户（仅销售订单时有效，预测/安全库存/手工录入时为 null） */
    String getCustomer();

}
