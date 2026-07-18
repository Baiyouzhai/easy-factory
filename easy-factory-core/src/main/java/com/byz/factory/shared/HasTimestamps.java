package com.byz.factory.shared;

import java.time.Instant;

/**
 * 时间戳审计 — 为实体提供创建/更新时间追踪。
 * <p>
 * 实现类应在其持久化钩子中调用 {@link #markUpdated()}。
 *
 * @author 苏政
 */
public interface HasTimestamps {

    /**
     * 创建时间
     *
     * @return 创建时间戳
     */
    Instant getCreatedAt();

    /**
     * 最后更新时间
     *
     * @return 更新时间戳
     */
    Instant getUpdatedAt();

    /**
     * 标记为已更新（刷新 updatedAt 时间戳）。
     * 默认实现无操作，子类应覆盖以实际更新时间字段。
     */
    default void markUpdated() {
        // 子类覆盖
    }

}
