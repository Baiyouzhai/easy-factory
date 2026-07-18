package com.byz.factory.shared;

/**
 * 可审计 — 实体记录创建人和最后更新人。
 * <p>
 * 遵循 {@link HasCode}/{@link HasName}/{@link HasTimestamps} 模式。
 * GMP 合规的基础设施——所有 GxP 实体均应实现此接口。
 *
 * @author 苏政
 */
public interface IAuditable {

    /** 创建人标识 */
    String getCreatedBy();

    /** 最后更新人标识 */
    String getUpdatedBy();

    /** 设置创建人 */
    void setCreatedBy(String createdBy);

    /** 设置最后更新人 */
    void setUpdatedBy(String updatedBy);
}
