package com.byz.factory.crm.model;

import com.byz.factory.crm.ISalesOrder;
import com.byz.factory.crm.SalesOrderStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 销售订单 — 协作层实体，客户需求→MPS 生产计划的桥梁。
 * 继承 BaseLifecycleEntity 获得状态机（DRAFT→CONFIRMED→IN_PRODUCTION→SHIPPED→COMPLETED），
 * 实现 ISalesOrder 供 MPS/WMS/BI 等模块编译期引用。
 *
 * <h3>订单优先级</h3>
 * <ul>
 *   <li><b>NORMAL</b> — 正常交期</li>
 *   <li><b>RUSH</b> — 加急（插单排产）</li>
 *   <li><b>EXPRESS</b> — 特急（需要 MPS/APS 立即调整）</li>
 * </ul>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   crm.order.orderNo             — 订单号
 *   crm.order.customerCode        — 客户编码
 *   crm.order.priority             — 优先级
 *   crm.order.gxpRequirements     — GxP 合规要求
 *   crm.order.specialInstructions — 特殊说明
 *   crm.order.planNo              — 关联生产计划号
 * </pre>
 *
 * @author 苏政
 */
@Entity
@Table(name = "crm_sales_order")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SalesOrder extends BaseLifecycleEntity<SalesOrderStatus> implements ISalesOrder {

    /** 订单号 */
    @Column(name = "order_no", length = 100, nullable = false, unique = true)
    private String orderNo;

    /** 客户编码 */
    @Column(name = "customer_code", length = 100, nullable = false)
    private String customerCode;

    /** 产品编码 */
    @Column(name = "product_code", length = 100)
    private String productCode;

    /** 订单数量 */
    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal quantity;

    /** 单价 */
    @Column(name = "unit_price", precision = 20, scale = 4)
    private BigDecimal unitPrice;

    /** 客户要求交期 */
    @Column(name = "required_date")
    private Instant requiredDate;

    /** 承诺交期 */
    @Column(name = "committed_date")
    private Instant committedDate;

    /** 优先级 (NORMAL / RUSH / EXPRESS) */
    @Column(length = 20, nullable = false)
    private String priority;

    /** GxP 合规要求 */
    @Column(name = "gxp_requirements", columnDefinition = "TEXT")
    private String gxpRequirements;

    /** 特殊说明 */
    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    /** 关联生产计划号 */
    @Column(name = "plan_no", length = 100)
    private String planNo;

    /** 订单明细 */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private List<SalesOrderItem> items = new ArrayList<>();

    /**
     * @param orderNo      订单号
     * @param customerCode 客户编码
     * @param productCode  产品编码
     * @param quantity     订单数量
     */
    public SalesOrder(String orderNo, String customerCode, String productCode, BigDecimal quantity) {
        super(orderNo, "订单-" + orderNo, SalesOrderStatus.DRAFT);
        this.orderNo = orderNo;
        this.customerCode = customerCode;
        this.productCode = productCode;
        this.quantity = quantity;
        this.priority = "NORMAL";
        this.items = new ArrayList<>();
    }

    // ==================== 状态转换便捷方法 ====================

    /** 确认订单（DRAFT → CONFIRMED） */
    public void confirm() {
        transition(SalesOrderStatus.CONFIRMED);
    }

    /** 开始生产（CONFIRMED → IN_PRODUCTION） */
    public void startProduction() {
        transition(SalesOrderStatus.IN_PRODUCTION);
    }

    /** 发货（IN_PRODUCTION → SHIPPED） */
    public void ship() {
        transition(SalesOrderStatus.SHIPPED);
    }

    /** 订单完成（SHIPPED → COMPLETED） */
    public void complete() {
        transition(SalesOrderStatus.COMPLETED);
    }

    /** 取消订单（DRAFT 或 CONFIRMED → CANCELLED） */
    public void cancel() {
        transition(SalesOrderStatus.CANCELLED);
    }

    // ==================== 业务方法 ====================

    /**
     * 添加订单明细行。
     *
     * @param item 订单明细行
     */
    public void addItem(SalesOrderItem item) {
        this.items.add(item);
        markUpdated();
    }

    /**
     * 添加订单明细行（便捷方法）。
     *
     * @param productCode 产品编码
     * @param quantity    订购数量
     * @param unitPrice   单价
     * @return 创建的明细行
     */
    public SalesOrderItem addItem(String productCode, BigDecimal quantity, BigDecimal unitPrice) {
        SalesOrderItem item = new SalesOrderItem(null, productCode, quantity, unitPrice);
        this.items.add(item);
        markUpdated();
        return item;
    }

    /** 关联 MPS 生产计划 */
    public void linkToPlan(String planNo) {
        this.planNo = planNo;
        markUpdated();
    }

    /** 订单总金额 */
    public BigDecimal totalAmount() {
        return items.stream()
                .map(i -> i.getUnitPrice() != null
                        ? i.getQuantity().multiply(i.getUnitPrice())
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** 查询是否为紧急订单 */
    public boolean isRush() {
        return "RUSH".equals(priority) || "EXPRESS".equals(priority);
    }

}
