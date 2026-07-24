package com.byz.factory.crm;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 客户投诉状态 — CRM 投诉处理生命周期。
 * <p>
 * 正向链：OPEN → INVESTIGATING → RESOLVED → CLOSED<br>
 * 取消链：OPEN → CANCELLED
 *
 * <pre>
 * OPEN ──→ INVESTIGATING ──→ RESOLVED ──→ CLOSED
 *   │
 *   └──→ CANCELLED
 * </pre>
 *
 * @author 苏政
 */
public enum ComplaintStatus implements ILifecycle.StatusEnum {

    /** 待处理 — 投诉已登记，等待调查 */
    OPEN,
    /** 调查中 — 正在收集证据、分析根因 */
    INVESTIGATING,
    /** 已解决 — 已给出处理方案/回复客户 */
    RESOLVED,
    /** 已关闭 — 客户确认结案 */
    CLOSED,
    /** 已取消 — 投诉撤销 */
    CANCELLED;

    @Override
    public Set<ComplaintStatus> allowedTransitions() {
        return switch (this) {
            case OPEN          -> Set.of(INVESTIGATING, CANCELLED);
            case INVESTIGATING -> Set.of(RESOLVED);
            case RESOLVED      -> Set.of(CLOSED);
            case CLOSED        -> Set.of();
            case CANCELLED     -> Set.of();
        };
    }

}
