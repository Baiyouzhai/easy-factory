package com.byz.factory.erp;

/**
 * 事务类型 — ERP 回传的事务分类。
 * <p>
 * 对应 ERP 的移动类型概念，由 MES/WMS 在物料移动时创建对应的事务记录。
 *
 * @author 苏政
 */
public enum TransactionType {

    /** 发料（物料消耗） */
    GOODS_ISSUE,
    /** 入库（成品/半成品完工入库） */
    GOODS_RECEIPT,
    /** 转储（库存地点转移） */
    TRANSFER

}
