package com.byz.factory.core;

import com.byz.factory.core.action.IAction;
import com.byz.factory.core.resource.IResourceModel;
import com.byz.factory.core.resource.IResourcePack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 工序
 *
 * @author 苏政
 */
public class Process implements IProcess {

    protected String code;
    protected String name;
    protected List<IAction> actions;

    public Process(String code, String name) {
        this.code = code;
        this.name = name;
        this.actions = new ArrayList<>();
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
    public Process setActions(Collection<IAction> actions) {
        if (actions instanceof List) {
            this.actions = (List<IAction>) actions;
            return this;
        }
        this.actions = null == actions ? new ArrayList<>() : new ArrayList<>(actions);
        return this;
    }

    @Override
    public IProcess setResourcePack(IResourcePack resourcePack) {
        return null;
    }

    @Override
    public IProcess setRequireResources(IResourceModel... resources) {
        return null;
    }

}
