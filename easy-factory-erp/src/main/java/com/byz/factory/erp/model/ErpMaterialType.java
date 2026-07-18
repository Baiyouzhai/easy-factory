package com.byz.factory.erp.model;

/**
 * ERP 物料类型 — 对应 SAP 的物料类型分类。
 * <p>
 * 用于 {@link MaterialCache} 的 materialType 字段，替代原始 String，
 * 提供编译期类型安全。
 *
 * @author 苏政
 */
public enum ErpMaterialType {

    /** 原材料 (Raw Material) */
    ROH,
    /** 半成品 (Semifinished Product) */
    HALB,
    /** 成品 (Finished Product) */
    FERT

}
