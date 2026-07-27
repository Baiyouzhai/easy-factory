package com.byz.factory.erp.model;

import com.byz.factory.erp.IErpTransaction;
import com.byz.factory.erp.TransactionStatus;
import com.byz.factory.erp.TransactionType;
import com.byz.factory.shared.BaseLifecycleEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 事务回传记录 — 继承 BaseLifecycleEntity 获得 id/code/name/status/createdAt/updatedAt，
 * 实现 IErpTransaction 供 MES/WMS/LIMS 等模块编译期引用。
 * <p>
 * 参照 EAM MaintenanceOrder 模式：带状态机生命周期 + 重试能力。
 *
 * @author 苏政
 */
@Entity
@Table(name = "erp_transaction")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Transaction extends BaseLifecycleEntity<TransactionStatus> implements IErpTransaction {

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "material_code", nullable = false, length = 100)
    private String materialCode;

    @Column(precision = 20, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "movement_type", length = 10)
    private String movementType;

    @Column(name = "reference_doc", length = 100)
    private String referenceDoc;

    @Column(name = "posting_date")
    private LocalDate postingDate;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
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

    /** 标记发送成功（PENDING → SENT）。 */
    public void markSent() {
        transition(TransactionStatus.SENT);
    }

    /** 标记已确认（SENT → CONFIRMED）。 */
    public void markConfirmed() {
        transition(TransactionStatus.CONFIRMED);
    }

    /** 标记失败（SENT → FAILED），递增重试计数。 */
    public void markFailed(String errorMessage) {
        transition(TransactionStatus.FAILED);
        this.retryCount++;
        this.errorMessage = errorMessage;
    }

    /** 重试（FAILED → PENDING）。 */
    public void retry() {
        transition(TransactionStatus.PENDING);
        this.errorMessage = null;
    }

    /** 取消（PENDING → CANCELLED）。 */
    public void cancel() {
        transition(TransactionStatus.CANCELLED);
    }

}
