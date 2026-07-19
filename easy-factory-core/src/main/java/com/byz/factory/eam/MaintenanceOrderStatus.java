package com.byz.factory.eam;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 维护工单状态 — 实现 ILifecycle.StatusEnum 获得状态转换校验。
 *
 * <pre>
 * OPEN ──────→ IN_PROGRESS ──→ COMPLETED ──→ VERIFIED (终端)
 *   │                                                  \\
 *   └────────────→ CANCELLED (终端)
 * </pre>
 *
 * @author 苏政
 */
public enum MaintenanceOrderStatus implements ILifecycle.StatusEnum {

    /** 已创建，待派工 */
    OPEN,
    /** 维修中 */
    IN_PROGRESS,
    /** 维修完成，待验证 */
    COMPLETED,
    /** 已验证 */
    VERIFIED,
    /** 已取消 */
    CANCELLED;

    @Override
    public Set<MaintenanceOrderStatus> allowedTransitions() {
        return switch (this) {
            case OPEN         -> Set.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS  -> Set.of(COMPLETED);
            case COMPLETED    -> Set.of(VERIFIED);
            case VERIFIED     -> Set.of();
            case CANCELLED    -> Set.of();
        };
    }

}
