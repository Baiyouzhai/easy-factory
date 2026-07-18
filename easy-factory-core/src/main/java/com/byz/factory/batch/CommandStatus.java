package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 指令执行状态 — 从下发到完成的完整生命周期。
 * <p>
 * 状态机：
 * <pre>
 *   QUEUED → SENT → ACKNOWLEDGED → COMPLETED
 *                  ↘ FAILED
 *   QUEUED → CANCELLED
 * </pre>
 *
 * @author 苏政
 */
public enum CommandStatus implements ILifecycle.StatusEnum {

    /** 已排队，等待下发 */
    QUEUED,

    /** 已发送至设备 */
    SENT,

    /** 设备已确认收到 */
    ACKNOWLEDGED,

    /** 指令执行完成 */
    COMPLETED,

    /** 指令执行失败 */
    FAILED,

    /** 已取消（仅 QUEUED 状态可取消） */
    CANCELLED;

    @Override
    public Set<CommandStatus> allowedTransitions() {
        return switch (this) {
            case QUEUED        -> Set.of(SENT, CANCELLED);
            case SENT          -> Set.of(ACKNOWLEDGED, FAILED);
            case ACKNOWLEDGED  -> Set.of(COMPLETED, FAILED);
            case COMPLETED     -> Set.of();
            case FAILED        -> Set.of();
            case CANCELLED     -> Set.of();
        };
    }

}
