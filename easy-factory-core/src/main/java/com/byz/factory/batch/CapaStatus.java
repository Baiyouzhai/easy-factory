package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * CAPA 状态 — QMS 模块。
 * <p>
 * 纠正与预防措施（CAPA）的生命周期：
 * <pre>
 * OPEN → ROOT_CAUSE → IN_PROGRESS → VERIFIED → CLOSED
 *                                            ← CANCELLED (任意非终态)
 * </pre>
 *
 * @author 苏政
 */
public enum CapaStatus implements ILifecycle.StatusEnum {

    /** 已创建，待分析 */
    OPEN,

    /** 根因分析 — 5Why/鱼骨图等根因分析进行中 */
    ROOT_CAUSE,

    /** 执行中 — 纠正措施和预防措施实施中 */
    IN_PROGRESS,

    /** 已验证 — 效果验证完成 */
    VERIFIED,

    /** 已关闭 — CAPA 终结 */
    CLOSED,

    /** 已取消 */
    CANCELLED;

    @Override
    public Set<CapaStatus> allowedTransitions() {
        return switch (this) {
            case OPEN        -> Set.of(ROOT_CAUSE, CANCELLED);
            case ROOT_CAUSE  -> Set.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> Set.of(VERIFIED, CANCELLED);
            case VERIFIED    -> Set.of(CLOSED);
            case CLOSED      -> Set.of();
            case CANCELLED   -> Set.of();
        };
    }

}
