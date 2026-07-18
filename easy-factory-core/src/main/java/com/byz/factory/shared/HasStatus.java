package com.byz.factory.shared;

/**
 * 状态标识 — 为实体提供泛型状态访问契约。
 * <p>
 * 配合 {@link com.byz.factory.lifecycle.ILifecycle} 使用时，
 * S 通常为实现 {@code ILifecycle.StatusEnum} 的枚举类型。
 *
 * @param <S> 状态类型
 * @author 苏政
 */
public interface HasStatus<S> {

    /**
     * 当前状态
     *
     * @return 状态
     */
    S getStatus();

}
