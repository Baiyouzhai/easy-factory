package com.byz.factory.scm.model;

import com.byz.factory.scm.IInboundPlan;
import com.byz.factory.scm.InboundPlanStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 来料计划 — 继承 BaseLifecycleEntity 获得状态机（CREATED→NOTIFIED→RECEIVING→COMPLETED），
 * 实现 IInboundPlan 供 WMS 等模块编译期引用。
 * <p>
 * 来料计划由采购订单驱动创建，通知 WMS 准备收货。
 * 与 PurchaseOrder 的区别：
 * <ul>
 *   <li>PurchaseOrder = 采购合同（供应商视角）：价格、数量、交付条款</li>
 *   <li>InboundPlan   = 收货计划（仓库视角）：什么时候、什么东西到、通知仓库准备</li>
 * </ul>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   scm.poNo           — 关联采购订单号
 *   scm.supplierCode   — 供应商编码
 *   scm.expectedDate   — 预计到货日期
 *   scm.notifyWarehouse — 是否已通知仓库
 * </pre>
 *
 * @author 苏政
 */
@Entity
@Table(name = "scm_inbound_plan")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InboundPlan extends BaseLifecycleEntity<InboundPlanStatus> implements IInboundPlan {

    /** 关联采购订单号 */
    @Column(name = "po_no", length = 100, nullable = false)
    private String poNo;

    /** 供应商编码 */
    @Column(name = "supplier_code", length = 100, nullable = false)
    private String supplierCode;

    /** 预计到货日期 */
    @Column(name = "expected_date")
    private LocalDate expectedDate;

    /** 是否已通知仓库 */
    @Column(name = "notify_warehouse")
    private boolean notifyWarehouse;

    /**
     * @param code         计划编号
     * @param poNo         关联采购订单号
     * @param supplierCode 供应商编码
     */
    public InboundPlan(String code, String poNo, String supplierCode) {
        super(code, "Inbound-" + poNo, InboundPlanStatus.CREATED);
        this.poNo = poNo;
        this.supplierCode = supplierCode;
        this.notifyWarehouse = false;
    }

    // ==================== 状态转换便捷方法 ====================

    /** 通知仓库准备收货（CREATED → NOTIFIED） */
    public void notifyWarehouse() {
        transition(InboundPlanStatus.NOTIFIED);
        this.notifyWarehouse = true;
    }

    /** 开始收货（NOTIFIED → RECEIVING） */
    public void startReceiving() {
        transition(InboundPlanStatus.RECEIVING);
    }

    /** 入库完成（RECEIVING → COMPLETED） */
    public void complete() {
        transition(InboundPlanStatus.COMPLETED);
    }

    /** 取消计划（CREATED 或 NOTIFIED → CANCELLED） */
    public void cancel() {
        transition(InboundPlanStatus.CANCELLED);
    }

}
