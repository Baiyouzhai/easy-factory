package com.byz.factory.crm;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 销售订单抽象 — CRM 销售订单实体的跨模块契约。
 * <p>
 * MPS 通过此接口获取客户订单需求生成生产计划；
 * WMS 通过此接口获取发货信息。
 * 典型调用链：CRM 确认订单 → MPS 注册需求 → APS 排程 → MES 执行。
 *
 * @author 苏政
 * @see com.byz.factory.crm.model.SalesOrder
 */
public interface ISalesOrder {

    /** 订单号 */
    String getOrderNo();

    /** 客户编码 */
    String getCustomerCode();

    /** 产品编码 */
    String getProductCode();

    /** 订单数量 */
    BigDecimal getQuantity();

    /** 单价 */
    BigDecimal getUnitPrice();

    /** 客户要求交期 */
    Instant getRequiredDate();

    /** 承诺交期 */
    Instant getCommittedDate();

    /** 优先级 (NORMAL / RUSH / EXPRESS) */
    String getPriority();

    /** GxP 合规要求 */
    String getGxpRequirements();

    /** 订单状态 */
    SalesOrderStatus getStatus();

    /** 订单明细 */
    List<? extends ISalesOrderItem> getItems();

    /**
     * 销售订单明细项 — 单行产品订购信息。
     */
    interface ISalesOrderItem {

        /** 产品编码 */
        String getProductCode();

        /** 订购数量 */
        BigDecimal getQuantity();

        /** 单价 */
        BigDecimal getUnitPrice();

    }

}
