package com.byz.factory.wms;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 收货状态
 *
 * @author 苏政
 */
public enum ReceiptStatus implements ILifecycle.StatusEnum {

    /** 待收货 */
    PENDING,
    /** 部分收货 */
    PARTIAL,
    /** 已完成 */
    COMPLETED,
    /** 已关闭 */
    CLOSED;

    @Override
    public Set<ReceiptStatus> allowedTransitions() {
        return switch (this) {
            case PENDING   -> Set.of(PARTIAL, COMPLETED);
            case PARTIAL   -> Set.of(COMPLETED);
            case COMPLETED -> Set.of(CLOSED);
            case CLOSED    -> Set.of();
        };
    }

}
