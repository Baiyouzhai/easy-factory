package com.byz.factory.resource;

import com.byz.factory.shared.Dict;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 资源项实现 — 原名 ResourceModel，DDD 重构时改名为 ResourceItem。
 *
 * @author 苏政
 */
public class ResourceItem implements IResourceItem {

    public static final ResourceItem[] EMPTY_ARRAY = new ResourceItem[0];

    public static ResourcePack pack(IResourceItem... resources) {
        ResourcePack resourcePack = new ResourcePack();
        return resourcePack.setResources(resources);
    }

    protected String name;
    protected Dict.SourceGroup group;
    protected Dict.SourceType type;
    protected BigDecimal number;

    public ResourceItem() {
    }

    public ResourceItem(String name, Dict.SourceGroup group, Dict.SourceType type, BigDecimal number) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.group = Objects.requireNonNull(group, "group cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        setNumber(number);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Dict.SourceGroup getGroup() {
        return group;
    }

    public void setGroup(Dict.SourceGroup group) {
        this.group = group;
    }

    @Override
    public Dict.SourceType getType() {
        return type;
    }

    public void setType(Dict.SourceType type) {
        this.type = type;
    }

    @Override
    public BigDecimal getNumber() {
        if (null == number) number = BigDecimal.ZERO;
        return number;
    }

    @Override
    public ResourceItem setNumber(Number number) {
        this.number = null == number ? BigDecimal.ZERO : new BigDecimal(number.toString());
        return this;
    }

    @Override
    public boolean isEmpty() {
        return BigDecimal.ZERO.compareTo(this.getNumber()) >= 0;
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
            throw new com.byz.factory.exception.ResourceException(
                "资源数量不能为负数: " + getName() + " -> " + getNumber() + " use " + number);
        }
        this.number = result;
        return this;
    }

    @Override
    public IResourceItem copy() {
        return new ResourceItem(getName(), getGroup(), getType(), getNumber());
    }

    @Override
    public boolean require() {
        return false;
    }

}
