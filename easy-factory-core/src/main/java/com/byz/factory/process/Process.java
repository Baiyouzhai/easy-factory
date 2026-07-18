package com.byz.factory.process;

import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.EmptyResourcePack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 工序实现
 *
 * @author 苏政
 */
public class Process implements IProcess {

    protected String code;
    protected String name;
    protected long order;
    protected List<IAction> actions;
    protected IResourcePack resourcePack;
    protected IResourceItem[] requireResources;

    public Process(String code, String name) {
        this.code = code;
        this.name = name;
        this.actions = new ArrayList<>();
        this.resourcePack = EmptyResourcePack.Instance;
        this.requireResources = ResourceItem.EMPTY_ARRAY;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    @Override
    public List<IAction> getActions() {
        if (null == actions) actions = new ArrayList<>();
        return actions;
    }

    /**
     * 设置动作清单
     *
     * @param actions 动作(为List子类时, 内存共享)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Process setActions(Collection<IAction> actions) {
        if (actions instanceof List) {
            this.actions = (List<IAction>) actions;
            return this;
        }
        this.actions = null == actions ? new ArrayList<>() : new ArrayList<>(actions);
        return this;
    }

    @Override
    public IResourcePack getResourcePack() {
        if (null == resourcePack) resourcePack = EmptyResourcePack.Instance;
        return resourcePack;
    }

    @Override
    public Process setResourcePack(IResourcePack resourcePack) {
        this.resourcePack = resourcePack;
        return this;
    }

    @Override
    public IResourceItem[] requireResources() {
        if (null == requireResources) return ResourceItem.EMPTY_ARRAY;
        return requireResources;
    }

    @Override
    public Process setRequireResources(IResourceItem... resources) {
        this.requireResources = resources;
        return this;
    }

    @Override
    public IResourcePack execute(IResourceItem... inputs) {
        // 前置校验
        if (null == actions || actions.isEmpty()) {
            throw new com.byz.factory.exception.ActionException("工序[" + code + "]动作清单为空");
        }
        // 调用接口默认实现（动作链遍历）
        return IProcess.super.execute(inputs);
    }

}
