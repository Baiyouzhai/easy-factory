package com.byz.factory.crm.model;

import com.byz.factory.crm.ICustomer;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户 — 协作层实体，继承 BaseEntity 获得 code/name/audit，实现 ICustomer 供跨模块引用。
 *
 * <h3>GMP 审计状态</h3>
 * 客户 GMP 审计状态独立于运营状态管理：
 * <ul>
 *   <li><b>PASSED</b> — 审计通过，可接受医药类订单</li>
 *   <li><b>EXPIRED</b> — 审计过期，需重新审计</li>
 *   <li><b>NEVER</b> — 从未审计，不可接医药类订单</li>
 * </ul>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   crm.customer.industry           — 行业
 *   crm.customer.region             — 地区
 *   crm.customer.contacts           — 联系人列表(JSON)
 *   crm.customer.gmpAuditStatus     — GMP 审计状态
 *   crm.customer.gmpAuditDate       — GMP 审计日期
 *   crm.customer.onTimeDeliveryRate — 准时交货率(%)
 *   crm.customer.qualityComplaintRate — 质量投诉率(%)
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity implements ICustomer {

    /** 行业 */
    private String industry;

    /** 地区 */
    private String region;

    /** 联系人列表 (JSON: [{name, role, phone, email}]) */
    private String contacts;

    /** GMP 审计状态 (PASSED / EXPIRED / NEVER) */
    private String gmpAuditStatus;

    /** GMP 审计日期 */
    private String gmpAuditDate;

    /** 准时交货率（%） */
    private double onTimeDeliveryRate;

    /** 质量投诉率（%） */
    private double qualityComplaintRate;

    /**
     * @param code     客户编码
     * @param name     客户名称
     * @param industry 行业
     */
    public Customer(String code, String name, String industry) {
        super(code, name);
        this.industry = industry;
        this.gmpAuditStatus = "NEVER";
    }

    // ==================== 业务便捷方法 ====================

    /** 通过 GMP 审计 */
    public void passGmpAudit(String date) {
        this.gmpAuditStatus = "PASSED";
        this.gmpAuditDate = date;
        markUpdated();
    }

    /** GMP 审计过期 */
    public void expireGmpAudit() {
        this.gmpAuditStatus = "EXPIRED";
        markUpdated();
    }

    /** 更新 GMP 审计状态 */
    public void updateAuditStatus(String status, String date) {
        this.gmpAuditStatus = status;
        this.gmpAuditDate = date;
        markUpdated();
    }

}
