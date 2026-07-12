package com.byz.factory.core.resource;

import com.byz.factory.data.Dict;
import com.byz.factory.data.Resource;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 资源
 *
 * @author 苏政
 */
public class ResourceModel extends Resource implements IResourceModel {

    public static final ResourceModel[] Empty = new ResourceModel[0];

    public static ResourcePack Pack(IResourceModel... resources) {
        ResourcePack resourcePack = new ResourcePack();
        return resourcePack.setResources(resources);
    }

    public ResourceModel(String name, Dict.SourceGroup group, Dict.SourceType type, BigDecimal number) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.group = Objects.requireNonNull(group, "group cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        setNumber(number);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Dict.SourceGroup getGroup() {
        return group;
    }

    @Override
    public Dict.SourceType getType() {
        return type;
    }

    @Override
    public BigDecimal getNumber() {
        if (null == number) number = BigDecimal.ZERO;
        return number;
    }

    @Override
    public ResourceModel setNumber(Number number) {
        this.number = null == number ? BigDecimal.ZERO : new BigDecimal(number.toString());
        return this;
    }

    @Override
    public boolean isEmpty() {
        return 0 == BigDecimal.ZERO.compareTo(this.number);
    }

    @Override
    public IResourceModel add(Number number) {
        if (null == number) return this;
        BigDecimal num = new BigDecimal(number.toString());
        this.number = getNumber().add(num);
        return this;
    }

    @Override
    public IResourceModel use(Number number) {
        if (null == number) return this;
        BigDecimal num = new BigDecimal(number.toString());
        this.number = getNumber().subtract(num);
        return this;
    }

}
