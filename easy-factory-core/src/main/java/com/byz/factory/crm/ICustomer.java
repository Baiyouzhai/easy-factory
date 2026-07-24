package com.byz.factory.crm;

/**
 * 客户抽象 — CRM 客户实体的跨模块契约。
 * <p>
 * 其他模块（MPS、SCM、QMS）通过此接口引用客户信息，
 * 无需直接依赖 easy-factory-crm。
 * <p>
 * GMP 审计状态用于判定是否可接受医药类订单。
 *
 * @author 苏政
 * @see com.byz.factory.crm.model.Customer
 */
public interface ICustomer {

    /** 客户编码 */
    String getCode();

    /** 客户名称 */
    String getName();

    /** 行业 */
    String getIndustry();

    /** 地区 */
    String getRegion();

    /** GMP 审计状态 (PASSED / EXPIRED / NEVER) */
    String getGmpAuditStatus();

    /** GMP 审计日期 */
    String getGmpAuditDate();

}
