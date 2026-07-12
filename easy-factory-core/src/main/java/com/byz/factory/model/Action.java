package com.byz.factory.model;

import com.byz.factory.data.Constant;
import com.byz.factory.data.Dict;

import java.util.Objects;

/**
 * (生产)动作
 *
 * @author 苏政
 */
public class ActionModel implements IActionModel {

    protected String code;
    protected String name;
    protected Dict.Importance importance;
    protected long order;
    protected String script;

    public ActionModel(String code, String name, Dict.Importance importance, long order) {
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.importance = Objects.requireNonNull(importance, "importance cannot be null");
        this.order = order;
    }
    public ActionModel(String code, String name) {
        this(code, name, Dict.Importance.Optional, System.currentTimeMillis());
    }
    public ActionModel() {
        this(Constant.Nothing, Constant.NothingTodo, Dict.Importance.Optional, System.currentTimeMillis());
    }

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Dict.Importance getImportant() {
        return importance;
    }

    public void setImportance(Dict.Importance importance) {
        this.importance = importance;
    }

    @Override
    public long getOrder() {
        return order;
    }

    @Override
    public IAction setRequireResources(IResourceModel... resources) {
        return null;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    @Override
    public IResourcePack execute(IProcess process, IResourceModel... resources) {
        return process.getResourcePack();
    }


}
