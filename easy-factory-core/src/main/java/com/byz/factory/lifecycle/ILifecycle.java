package com.byz.factory.lifecycle;

import java.util.Set;

/**
 * 生命周期 — 为带有状态的实体提供标准的状态转换管理。
 * <p>
 * 子类只需声明允许的状态转换规则，框架自动校验非法转换。
 * <p>
 * 用法：
 * <pre>{@code
 * public enum WorkOrderStatus implements ILifecycle.StatusEnum {
 *     CREATED, RELEASED, IN_PROGRESS, COMPLETED, CLOSED;
 *
 *     public Set<WorkOrderStatus> allowedTransitions() {
 *         return switch (this) {
 *             case CREATED     -> Set.of(RELEASED);
 *             case RELEASED    -> Set.of(IN_PROGRESS);
 *             case IN_PROGRESS -> Set.of(COMPLETED);
 *             case COMPLETED   -> Set.of(CLOSED);
 *             case CLOSED      -> Set.of();
 *         };
 *     }
 * }
 * }</pre>
 *
 * @param <S> 状态枚举类型
 * @author 苏政
 */
public interface ILifecycle<S extends ILifecycle.StatusEnum> {

    /**
     * 状态枚举必须实现此接口以声明允许的转换
     */
    interface StatusEnum {
        /** 当前状态名称 */
        String name();
        /** 允许转换到的目标状态集合 */
        Set<? extends StatusEnum> allowedTransitions();
    }

    /**
     * 获取当前状态
     */
    S getStatus();

    /**
     * 转换到目标状态
     *
     * @param target 目标状态
     * @throws IllegalStateException 如果转换不被允许
     */
    default void transition(S target) {
        S current = getStatus();
        if (current == target) return; // 同状态不报错

        @SuppressWarnings("unchecked")
        Set<S> allowed = (Set<S>) current.allowedTransitions();
        if (allowed == null || !allowed.contains(target)) {
            throw new IllegalStateException(
                "非法状态转换: " + current.name() + " → " + target.name()
                + " (允许: " + allowed + ")");
        }
        setStatus(target);
    }

    /**
     * 检查是否允许转换到目标状态
     */
    @SuppressWarnings("unchecked")
    default boolean canTransition(S target) {
        Set<S> allowed = (Set<S>) getStatus().allowedTransitions();
        return allowed != null && allowed.contains(target);
    }

    /**
     * 设置状态（由 transition 调用，子类实现持久化逻辑）
     */
    void setStatus(S status);

}
