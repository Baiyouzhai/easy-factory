package com.byz.factory.scm.model;

import com.byz.factory.batch.IPurchaseOrder;
import com.byz.factory.batch.PurchaseOrderStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购订单 — 继承 BaseLifecycleEntity 获得状态机（DRAFT→APPROVED→SENT→RECEIVING→COMPLETED），
 * 实现 IPurchaseOrder 供 WMS/ERP 等模块编译期引用。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   scm.poNo             — 采购单号
 *   scm.supplierCode     — 供应商编码
 *   scm.approvedBy       — 审批人
 *   scm.expectedDelivery — 预计交付日期
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrder extends BaseLifecycleEntity<PurchaseOrderStatus> implements IPurchaseOrder {

    /** 采购单号 */
    private String poNo;

    /** 供应商编码 */
    private String supplierCode;

    /** 采购明细 */
    private List<PurchaseOrderItem> items;

    /** 审批人 */
    private String approvedBy;

    /** 预计交付日期 */
    private LocalDate expectedDelivery;

    /**
     * @param poNo        采购单号
     * @param supplierCode 供应商编码
     */
    public PurchaseOrder(String poNo, String supplierCode) {
        super(poNo, "PO-" + supplierCode, PurchaseOrderStatus.DRAFT);
        this.poNo = poNo;
        this.supplierCode = supplierCode;
        this.items = new ArrayList<>();
    }

    // ==================== 状态转换便捷方法 ====================

    /** 审批通过（DRAFT → APPROVED） */
    public void approve(String approvedBy) {
        transition(PurchaseOrderStatus.APPROVED);
        this.approvedBy = approvedBy;
    }

    /** 发送供应商（APPROVED → SENT） */
    public void send() {
        transition(PurchaseOrderStatus.SENT);
    }

    /** 开始收货（SENT → RECEIVING） */
    public void startReceiving() {
        transition(PurchaseOrderStatus.RECEIVING);
    }

    /** 标记完成——全部收货完毕（SENT 或 RECEIVING → COMPLETED） */
    public void complete() {
        transition(PurchaseOrderStatus.COMPLETED);
    }

    /** 取消订单（DRAFT 或 APPROVED → CANCELLED） */
    public void cancel() {
        transition(PurchaseOrderStatus.CANCELLED);
    }

    // ==================== 业务方法 ====================

    /**
     * 添加采购明细行。
     *
     * @param item 采购明细行
     */
    public void addItem(PurchaseOrderItem item) {
        this.items.add(item);
        markUpdated();
    }

    /**
     * 添加采购明细行（便捷方法）。
     *
     * @param materialCode 物料编码
     * @param quantity     采购数量
     * @return 创建的明细行
     */
    public PurchaseOrderItem addItem(String materialCode, BigDecimal quantity) {
        PurchaseOrderItem item = new PurchaseOrderItem(materialCode, quantity);
        this.items.add(item);
        markUpdated();
        return item;
    }

    /**
     * 判断所有物料是否已完全收货。
     */
    public boolean isFullyReceived() {
        return items.stream().allMatch(PurchaseOrderItem::isFullyReceived);
    }

    /**
     * 采购总金额。
     */
    public BigDecimal totalAmount() {
        return items.stream()
                .map(i -> i.getUnitPrice() != null
                        ? i.getQuantity().multiply(i.getUnitPrice())
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
