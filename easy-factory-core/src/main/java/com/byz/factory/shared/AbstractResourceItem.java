package com.byz.factory.shared;

import com.byz.factory.exception.ResourceException;
import com.byz.factory.resource.IResourceItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 资源项抽象基类 — 提供 IResourceItem 的默认实现。
 * <p>
 * 4M1E 资源模型（Equipment, Material, Personnel, Method, Environment）
 * 应继承此类，只需声明 group 固定值和特定业务字段。
 * <p>
 * 用法：
 * <pre>{@code
 * public class Equipment extends AbstractResourceItem implements IMachine {
 *     public Equipment(String code, String name) {
 *         super(code, name);
 *         setGroup(Dict.SourceGroup.Machine);
 *     }
 *     // add/use/copy/put/isEmpty/require 全部自动继承
 * }
 * }</pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractResourceItem extends BaseEntity implements IResourceItem {

    /** 资源分组 */
    private Dict.SourceGroup group = Dict.SourceGroup.Other;

    /** 资源子类型 */
    private Dict.SourceType type = Dict.SourceType.Other;

    /** 资源数量 */
    private BigDecimal number = BigDecimal.ZERO;

    protected AbstractResourceItem() {
        super();
    }

    protected AbstractResourceItem(String code, String name) {
        super(code, name);
    }

    protected AbstractResourceItem(String code, String name, Dict.SourceGroup group,
                                   Dict.SourceType type, BigDecimal number) {
        super(code, name);
        this.group = group;
        this.type = type;
        setNumber(number);
    }

    // ---- IResourceItem 默认实现 ----

    @Override
    public BigDecimal getNumber() {
        if (null == number) number = BigDecimal.ZERO;
        return number;
    }

    @Override
    public AbstractResourceItem setNumber(Number number) {
        this.number = null == number ? BigDecimal.ZERO : new BigDecimal(number.toString());
        return this;
    }

    @Override
    public boolean isEmpty() {
        return BigDecimal.ZERO.compareTo(getNumber()) >= 0;
    }

    @Override
    public IResourceItem add(Number number) {
        if (null == number) return this;
        BigDecimal num = new BigDecimal(number.toString());
        this.number = getNumber().add(num);
        return this;
    }

    @Override
    public IResourceItem use(Number number) {
        if (null == number) return this;
        BigDecimal num = new BigDecimal(number.toString());
        BigDecimal result = getNumber().subtract(num);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResourceException(
                "资源数量不能为负数: " + getName() + " -> " + getNumber() + " use " + number);
        }
        this.number = result;
        return this;
    }

    @Override
    public IResourceItem put(Number number) {
        return add(number);
    }

    @Override
    public IResourceItem copy() {
        try {
            return jsonClone();
        } catch (Exception e) {
            throw new ResourceException("资源复制失败: " + getName(), e);
        }
    }

    @Override
    public boolean require() {
        return false;
    }

}
