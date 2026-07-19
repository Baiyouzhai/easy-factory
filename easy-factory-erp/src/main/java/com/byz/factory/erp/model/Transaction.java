package com.byz.factory.erp.model;

import com.byz.factory.erp.IErpTransaction;
import com.byz.factory.erp.TransactionStatus;
import com.byz.factory.erp.TransactionType;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 事务回传记录 — 继承 BaseLifecycleEntity 获得状态机（PENDING→SENT→CONFIRMED），
 * 实现 IErpTransaction 供 MES/WMS/LIMS 等模块编译期引用。
 * <p>
 * 参照 EAM MaintenanceOrder 模式：带状态机生命周期 + 重试能力。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Transaction extends BaseLifecycleEntity<TransactionStatus> implements IErpTransaction {

    /** 事务类型 */
    private TransactionType transactionType;

    /** 物料编码 */
    private String materialCode;

    /** 数量 */
    private BigDecimal quantity;

    /** 单位 */
    private String unit;

    /** 批次号 */
    private String batchNo;

    /** ERP 移动类型 */
    private String movementType;

    /** 参考单据（工单号等） */
    private String referenceDoc;

    /** 过账日期 */
    private LocalDate postingDate;

    /** 重试次数 */
    private int retryCount;

    /** 最后一次错误信息 */
    private String errorMessage;

    /**
     * @param code            事务编号
     * @param transactionType 事务类型
     * @param materialCode    物料编码
     * @param quantity        数量
     */
    public Transaction(String code, TransactionType transactionType,
                       String materialCode, BigDecimal quantity) {
        super(code, transactionType + "-" + materialCode, TransactionStatus.PENDING);
        this.transactionType = transactionType;
        this.materialCode = materialCode;
        this.quantity = quantity;
        this.retryCount = 0;
        this.postingDate = LocalDate.now();
    }

    /**
     * 标记发送成功（PENDING → SENT）。
     */
    public void markSent() {
        transition(TransactionStatus.SENT);
    }

    /**
     * 标记已确认（SENT → CONFIRMED）。
     */
    public void markConfirmed() {
        transition(TransactionStatus.CONFIRMED);
    }

    /**
     * 标记失败，记录错误信息并递增重试计数（SENT → FAILED）。
     *
     * @param errorMessage 错误信息
     */
    public void markFailed(String errorMessage) {
        transition(TransactionStatus.FAILED);
        this.retryCount++;
        this.errorMessage = errorMessage;
    }

    /**
     * 重试（FAILED → PENDING）。
     */
    public void retry() {
        transition(TransactionStatus.PENDING);
        this.errorMessage = null;
    }

    /**
     * 取消（PENDING → CANCELLED）。
     */
    public void cancel() {
        transition(TransactionStatus.CANCELLED);
    }

}
