package com.byz.factory.erp;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ERP 事务回传记录 — MES/WMS/LIMS 创建回传 ERP 的事务的编译期契约。
 * <p>
 * 其他模块通过此接口创建和查询事务回传记录，无需依赖 easy-factory-erp。
 * 典型调用链：MES 工序消耗物料 → 创建 IErpTransaction → ERP 适配器异步发送。
 *
 * @author 苏政
 * @see com.byz.factory.erp.model.Transaction
 */
public interface IErpTransaction {

    /** 事务编号 */
    String getCode();

    /** 事务类型 */
    TransactionType getTransactionType();

    /** 物料编码 */
    String getMaterialCode();

    /** 数量 */
    BigDecimal getQuantity();

    /** 单位 */
    String getUnit();

    /** 批次号 */
    String getBatchNo();

    /** ERP 移动类型 */
    String getMovementType();

    /** 参考单据（工单号等） */
    String getReferenceDoc();

    /** 过账日期 */
    LocalDate getPostingDate();

    /** 事务状态 */
    TransactionStatus getStatus();

    /** 重试次数 */
    int getRetryCount();

    /** 最后一次错误信息 */
    String getErrorMessage();

}
