package com.byz.factory.core;

import cn.hutool.core.util.StrUtil;
import com.byz.factory.data.Constant;
import com.byz.factory.exception.ActionException;

import java.util.Objects;

/**
 * (生产)动作
 *
 * @author 苏政
 */
public class Action implements IAction {

    protected String code;
    protected String name;
    protected Level level;
    protected long order;
    protected String script;

    public Action(String code, String name, Level level, long order) {
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.level = Objects.requireNonNull(level, "level cannot be null");
        this.order = order;
    }
    public Action(String code, String name) {
        this(code, name, Level.Optional, System.currentTimeMillis());
    }
    public Action() {
        this(Constant.Nothing, Constant.NothingTodo, Level.Optional, System.currentTimeMillis());
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
    public Level getImportant() {
        return level;
    }

    public void setImportance(Level level) {
        this.level = level;
    }

    @Override
    public long getOrder() {
        return order;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public IAction setScript(String script) {
        if (StrUtil.isBlank(script)) throw new ActionException("script cannot be null");
        this.script = script;
        return this;
    }

    @Override
    public IAction setRequireResources(IResource... resources) {
        return null;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    @Override
    public IResourcePack execute(IProcess process, IResource... resources) {
        return process.getResourcePack();
    }


}
