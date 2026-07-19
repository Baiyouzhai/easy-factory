package com.byz.factory.erp;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 事务回传状态 — ERP 适配器向外部 ERP 系统回传事务的生命周期。
 * <p>
 * 正常链：PENDING → SENT → CONFIRMED<br>
 * 失败链：PENDING → SENT → FAILED → PENDING（重试）<br>
 * 取消链：PENDING → CANCELLED
 *
 * @author 苏政
 */
public enum TransactionStatus implements ILifecycle.StatusEnum {

    /** 待发送 */
    PENDING,
    /** 已发送（等待 ERP 确认） */
    SENT,
    /** 已确认（ERP 过账成功） */
    CONFIRMED,
    /** 发送失败 */
    FAILED,
    /** 已取消 */
    CANCELLED;

    @Override
    public Set<TransactionStatus> allowedTransitions() {
        return switch (this) {
            case PENDING   -> Set.of(SENT, CANCELLED);
            case SENT      -> Set.of(CONFIRMED, FAILED);
            case CONFIRMED -> Set.of();
            case FAILED    -> Set.of(PENDING);
            case CANCELLED -> Set.of();
        };
    }

}
