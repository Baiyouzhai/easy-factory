package com.byz.factory.model;

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
    protected List<IAction> actions;
    protected IResourcePack resourcePack;
    protected IResourceModel[] requireResources;

    public Process(String code, String name) {
        this.code = code;
        this.name = name;
        this.actions = new ArrayList<>();
        this.resourcePack = ResourcePack.EmptyPack;
        this.requireResources = ResourceModel.Empty;
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
        if (null == resourcePack) resourcePack = ResourcePack.EmptyPack;
        return resourcePack;
    }

    @Override
    public Process setResourcePack(IResourcePack resourcePack) {
        this.resourcePack = resourcePack;
        return this;
    }

    @Override
    public IResourceModel[] requireResources() {
        if (null == requireResources) return ResourceModel.Empty;
        return requireResources;
    }

    @Override
    public Process setRequireResources(IResourceModel... resources) {
        this.requireResources = resources;
        return this;
    }

}
