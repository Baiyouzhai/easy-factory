package com.byz.factory.mps.model;

import com.byz.factory.batch.IDemandSource;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 需求来源 — MPS 需求侧实体，承载销售订单、预测、安全库存和手工录入的需求信息。
 * <p>
 * 继承 BaseEntity 获得审计字段，实现 IDemandSource 供 ERP/SCM/APS 等下游模块编译期引用。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   mps.sourceType   — 来源类型
 *   mps.referenceNo  — 来源单据号
 *   mps.customer     — 客户（仅销售订单时有效）
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DemandSource extends BaseEntity implements IDemandSource {

    /** 来源类型 (SALES_ORDER / FORECAST / SAFETY_STOCK / MANUAL) */
    private String sourceType;

    /** 来源单据号（如销售订单号 SO-001） */
    private String referenceNo;

    /** 产品编码 */
    private String productCode;

    /** 需求数量 */
    private BigDecimal quantity;

    /** 需求日期 */
    private LocalDate dueDate;

    /** 优先级（1=最高） */
    private int priority;

    /** 客户（仅销售订单时有效） */
    private String customer;

    /**
     * @param referenceNo 来源单据号
     * @param productCode 产品编码
     * @param quantity    需求数量
     */
    public DemandSource(String referenceNo, String productCode, BigDecimal quantity) {
        super(referenceNo, "需求-" + referenceNo);
        this.referenceNo = referenceNo;
        this.productCode = productCode;
        this.quantity = quantity;
        this.priority = 5; // 默认中等优先级
    }

    // ── 业务方法 ──

    /**
     * 注册需求，标记来源类型和客户信息。
     *
     * @param sourceType 需求来源类型
     * @param customer   客户（仅 SALES_ORDER 时有效，其他类型传 null）
     */
    public void register(DemandSourceType sourceType, String customer) {
        this.sourceType = sourceType.name();
        this.customer = customer;
        markUpdated();
    }

    /**
     * 注册需求，标记来源类型。
     *
     * @param sourceType 需求来源类型
     */
    public void register(DemandSourceType sourceType) {
        register(sourceType, null);
    }

    /**
     * 设置优先级。
     *
     * @param priority 优先级（1=最高）
     */
    public void setPriority(int priority) {
        this.priority = priority;
        markUpdated();
    }

    // ── 查询方法 ──

    /**
     * 判断是否为已确认的销售订单需求。
     */
    public boolean isSalesOrder() {
        return DemandSourceType.SALES_ORDER.name().equals(sourceType);
    }

    /**
     * 判断是否为预测需求。
     */
    public boolean isForecast() {
        return DemandSourceType.FORECAST.name().equals(sourceType);
    }

    /**
     * 判断是否关联了客户。
     */
    public boolean hasCustomer() {
        return customer != null && !customer.isBlank();
    }

}
