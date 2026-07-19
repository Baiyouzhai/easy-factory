package com.byz.factory.crm.model;

import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户 — 协作层实体，管理客户主数据和 GMP 审计状态。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {

    private String industry;
    private String region;
    private String contacts;        // JSON: [{name, role, phone, email}]
    private String gmpAuditStatus;  // PASSED/EXPIRED/NEVER
    private String gmpAuditDate;
    private double onTimeDeliveryRate;
    private double qualityComplaintRate;

    public Customer(String code, String name, String industry) {
        super(code, name);
        this.industry = industry;
        this.gmpAuditStatus = "NEVER";
    }

}
