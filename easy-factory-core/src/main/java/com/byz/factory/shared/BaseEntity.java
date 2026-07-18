package com.byz.factory.shared;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * 实体基类 — 组合 HasCode + HasName + HasTimestamps + DataExpand。
 * <p>
 * 大多数业务模块模型应继承此类或其子类 {@link BaseLifecycleEntity}。
 * <p>
 * 用法：
 * <pre>{@code
 * public class ProcessTemplate extends BaseEntity implements HasVersion {
 *     private String version;
 *     private String category;
 *     // code/name/createdAt/updatedAt 自动继承
 * }
 * }</pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseEntity extends DataExpand implements HasCode, HasName, HasTimestamps {

    /** 业务编码（唯一标识） */
    private String code;

    /** 显示名称 */
    private String name;

    /** 创建时间 */
    private Instant createdAt;

    /** 最后更新时间 */
    private Instant updatedAt;

    protected BaseEntity() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    protected BaseEntity(String code, String name) {
        this();
        this.code = code;
        this.name = name;
    }

    @Override
    public void markUpdated() {
        this.updatedAt = Instant.now();
    }

}
