package com.byz.factory.scm;

/**
 * 供应商抽象 — SCM 供应商实体的跨模块契约。
 * <p>
 * 其他模块（WMS、QMS、ERP）通过此接口引用供应商信息，
 * 无需直接依赖 easy-factory-scm。
 *
 * @author 苏政
 * @see com.byz.factory.scm.model.Supplier
 */
public interface ISupplier {

    /** 供应商编码 */
    String getCode();

    /** 供应商名称 */
    String getName();

    /** 供应商类别 (RAW_MATERIAL / PACKAGING / EQUIPMENT / SERVICE) */
    String getCategory();

    /** 资质状态 (QUALIFIED / UNDER_REVIEW / DISQUALIFIED) */
    String getQualification();

    /** 运营状态 */
    SupplierStatus getStatus();

    /** 平均交货周期（天） */
    int getLeadTimeDays();

    /** 准时交货率（%） */
    double getOnTimeRate();

    /** 一次合格率（%） */
    double getQualityRate();

}
