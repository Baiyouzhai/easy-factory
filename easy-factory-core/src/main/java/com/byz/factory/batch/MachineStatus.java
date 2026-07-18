package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 设备状态 — 设备运行状态的生命周期。
 * <p>
 * 实现 {@link ILifecycle.StatusEnum}，与现有的 {@code equip/Equipment.Status}（普通枚举）
 * 不同，本枚举提供完整的状态转换校验。
 *
 * <pre>
 *   IDLE → RUNNING | SETUP | MAINTENANCE
 *   RUNNING → IDLE | FAULT
 *   SETUP → RUNNING | IDLE
 *   MAINTENANCE → IDLE
 *   FAULT → MAINTENANCE
 * </pre>
 *
 * @author 苏政
 */
public enum MachineStatus implements ILifecycle.StatusEnum {

    /** 空闲 — 可用但未运行 */
    IDLE,

    /** 运行中 — 正在生产 */
    RUNNING,

    /** 设置/换型 — 更换模具或调整参数 */
    SETUP,

    /** 维护中 — 计划或非计划维护 */
    MAINTENANCE,

    /** 故障 — 设备异常停机 */
    FAULT;

    @Override
    public Set<MachineStatus> allowedTransitions() {
        return switch (this) {
            case IDLE        -> Set.of(RUNNING, SETUP, MAINTENANCE);
            case RUNNING     -> Set.of(IDLE, FAULT);
            case SETUP       -> Set.of(RUNNING, IDLE);
            case MAINTENANCE -> Set.of(IDLE);
            case FAULT       -> Set.of(MAINTENANCE);
        };
    }
}
