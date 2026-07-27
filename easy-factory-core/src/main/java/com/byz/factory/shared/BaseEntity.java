package com.byz.factory.shared;

import com.byz.data.DataExpand;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 实体基类 — 组合 HasCode + HasName + HasTimestamps + DataExpand。
 * <p>
 * 大多数业务模块模型应继承此类或其子类 {@link BaseLifecycleEntity}。
 * <p>
 * 用法：
 * <pre>{@code
 * @Entity @Table(name = "{module}_{table}")
 * public class ProcessTemplate extends BaseEntity implements HasVersion {
 *     private String version;
 *     private String category;
 *     // id/code/name/createdAt/updatedAt 自动继承
 * }
 * }</pre>
 *
 * @author 苏政
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseEntity extends DataExpand implements HasCode, HasName, HasTimestamps {

    /** 主键（自增） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 业务编码（唯一标识） */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    /** 显示名称 */
    @Column(nullable = false, length = 255)
    private String name;

    /** 创建时间 */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** 最后更新时间 */
    @LastModifiedDate
    @Column(nullable = false)
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
