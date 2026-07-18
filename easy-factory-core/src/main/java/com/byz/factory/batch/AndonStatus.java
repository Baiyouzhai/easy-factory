package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 安灯呼叫状态
 *
 * @author 苏政
 */
public enum AndonStatus implements ILifecycle.StatusEnum {

    /** 已触发 */
    OPEN,
    /** 已确认 */
    ACKNOWLEDGED,
    /** 已解决 */
    RESOLVED,
    /** 已升级 */
    ESCALATED,
    /** 已关闭 */
    CLOSED;

    @Override
    public Set<AndonStatus> allowedTransitions() {
        return switch (this) {
            case OPEN         -> Set.of(ACKNOWLEDGED, CLOSED);
            case ACKNOWLEDGED -> Set.of(RESOLVED, ESCALATED);
            case RESOLVED     -> Set.of(CLOSED);
            case ESCALATED    -> Set.of(RESOLVED, CLOSED);
            case CLOSED       -> Set.of();
        };
    }

}
