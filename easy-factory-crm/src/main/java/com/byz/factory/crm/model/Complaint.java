package com.byz.factory.crm.model;

import com.byz.factory.crm.ComplaintStatus;
import com.byz.factory.crm.IComplaint;
import com.byz.factory.shared.BaseLifecycleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 客户投诉 — 协作层实体，客户投诉→QMS CAPA 闭环的起点。
 * 继承 BaseLifecycleEntity 获得状态机（OPEN→INVESTIGATING→RESOLVED→CLOSED），
 * 实现 IComplaint 供 QMS/Andon 等模块编译期引用。
 *
 * <h3>投诉类型</h3>
 * <ul>
 *   <li><b>QUALITY</b> — 质量问题（召回/不合格品）</li>
 *   <li><b>DELIVERY</b> — 交期延误</li>
 *   <li><b>PACKAGING</b> — 包装破损/污染</li>
 *   <li><b>SERVICE</b> — 客服/沟通问题</li>
 *   <li><b>OTHER</b> — 其他</li>
 * </ul>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   crm.complaint.complaintNo    — 投诉编号
 *   crm.complaint.customerCode   — 客户编码
 *   crm.complaint.orderNo        — 关联订单号
 *   crm.complaint.batchNo        — 关联批次号
 *   crm.complaint.type           — 投诉类型
 *   crm.complaint.capaCode       — 关联 CAPA 编码
 *   crm.complaint.resolvedAt     — 解决时间
 *   crm.complaint.closedAt       — 关闭时间
 * </pre>
 *
 * @author 苏政
 */
@Entity
@Table(name = "crm_complaint")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Complaint extends BaseLifecycleEntity<ComplaintStatus> implements IComplaint {

    /** 投诉编号 */
    @Column(name = "complaint_no", length = 100, nullable = false, unique = true)
    private String complaintNo;

    /** 客户编码 */
    @Column(name = "customer_code", length = 100, nullable = false)
    private String customerCode;

    /** 关联订单号 */
    @Column(name = "order_no", length = 100)
    private String orderNo;

    /** 关联批次号 */
    @Column(name = "batch_no", length = 100)
    private String batchNo;

    /** 投诉类型 (QUALITY / DELIVERY / PACKAGING / SERVICE / OTHER) */
    @Column(length = 30, nullable = false)
    private String type;

    /** 投诉描述 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 处理结果/回复 */
    @Column(columnDefinition = "TEXT")
    private String resolution;

    /** 关联 CAPA 编码 */
    @Column(name = "capa_code", length = 100)
    private String capaCode;

    /**
     * @param complaintNo  投诉编号
     * @param customerCode 客户编码
     * @param orderNo      关联订单号
     * @param batchNo      关联批次号
     * @param type         投诉类型
     * @param description  投诉描述
     */
    public Complaint(String complaintNo, String customerCode, String orderNo,
                     String batchNo, String type, String description) {
        super(complaintNo, "投诉-" + complaintNo, ComplaintStatus.OPEN);
        this.complaintNo = complaintNo;
        this.customerCode = customerCode;
        this.orderNo = orderNo;
        this.batchNo = batchNo;
        this.type = type;
        this.description = description;
    }

    // ==================== 状态转换便捷方法 ====================

    /** 开始调查（OPEN → INVESTIGATING） */
    public void startInvestigation() {
        transition(ComplaintStatus.INVESTIGATING);
    }

    /** 解决投诉（INVESTIGATING → RESOLVED） */
    public void resolve(String resolution) {
        transition(ComplaintStatus.RESOLVED);
        this.resolution = resolution;
    }

    /** 关闭投诉（RESOLVED → CLOSED） */
    public void close() {
        transition(ComplaintStatus.CLOSED);
    }

    /** 取消投诉（OPEN → CANCELLED） */
    public void cancel() {
        transition(ComplaintStatus.CANCELLED);
    }

    // ==================== 业务方法 ====================

    /** 关联 CAPA */
    public void linkCapa(String capaCode) {
        this.capaCode = capaCode;
        markUpdated();
    }

    /** 查询是否为质量问题投诉 */
    public boolean isQualityRelated() {
        return "QUALITY".equals(type);
    }

    /** 查询投诉是否已激活（未结案） */
    public boolean isActive() {
        return getStatus() == ComplaintStatus.OPEN
                || getStatus() == ComplaintStatus.INVESTIGATING;
    }

}
