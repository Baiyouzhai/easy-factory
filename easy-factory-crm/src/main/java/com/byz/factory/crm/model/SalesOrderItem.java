package com.byz.factory.crm.model;

import com.byz.factory.crm.ISalesOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售订单明细项 — 实现 ISalesOrder.ISalesOrderItem 供跨模块引用。
 *
 * @author 苏政
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderItem implements ISalesOrder.ISalesOrderItem {

    /** 产品编码 */
    private String productCode;

    /** 订购数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal unitPrice;

}
