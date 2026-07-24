package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 偏差状态 — QMS 模块。
 * <p>
 * 状态流转：
 * <pre>
 * OPEN → INVESTIGATING → DISPOSITIONED → RESOLVED → CLOSED
 *                                                   ← CANCELLED (任意非终态)
 * </pre>
 *
 * @author 苏政
 */
public enum DeviationStatus implements ILifecycle.StatusEnum {

    /** 已创建，待调查 */
    OPEN,

    /** 调查中 — 根因分析、影响评估 */
    INVESTIGATING,

    /** 已处置 — QA 已做出处置判定 */
    DISPOSITIONED,

    /** 已解决 — 处置措施已执行完成 */
    RESOLVED,

    /** 已关闭 — 偏差流程终结 */
    CLOSED,

    /** 已取消 */
    CANCELLED;

    @Override
    public Set<DeviationStatus> allowedTransitions() {
        return switch (this) {
            case OPEN          -> Set.of(INVESTIGATING, CANCELLED);
            case INVESTIGATING -> Set.of(DISPOSITIONED, CANCELLED);
            case DISPOSITIONED -> Set.of(RESOLVED, CANCELLED);
            case RESOLVED      -> Set.of(CLOSED);
            case CLOSED        -> Set.of();
            case CANCELLED     -> Set.of();
        };
    }

}
