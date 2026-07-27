package com.byz.factory.crm.model;

import com.byz.factory.crm.ISalesOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售订单明细项 — 实现 ISalesOrder.ISalesOrderItem 供跨模块引用。
 *
 * @author 苏政
 */
@Entity
@Table(name = "crm_sales_order_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderItem implements ISalesOrder.ISalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 产品编码 */
    @Column(name = "product_code", length = 100, nullable = false)
    private String productCode;

    /** 订购数量 */
    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal quantity;

    /** 单价 */
    @Column(name = "unit_price", precision = 20, scale = 4)
    private BigDecimal unitPrice;

}
