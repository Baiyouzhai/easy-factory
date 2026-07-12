package com.byz.factory.script;

import com.byz.factory.data.Constant;
import com.byz.factory.data.Dict;
import com.byz.factory.exception.ActionException;
import com.byz.factory.model.IAction;
import com.byz.factory.model.IProcess;
import com.byz.factory.model.IResourceModel;
import com.byz.factory.model.IResourcePack;

import java.util.Objects;

/**
 * 脚本化动作 — 实现 IAction 和 IScript，支持通过 JavaScript 脚本定义执行逻辑。
 *
 * @author 苏政
 */
public class Action implements IAction, IScript {

    protected String code;
    protected String name;
    protected Dict.Importance level;
    protected long order;
    protected String script;

    public Action(String code, String name, Dict.Importance level, long order) {
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.level = Objects.requireNonNull(level, "level cannot be null");
        this.order = order;
    }

    public Action(String code, String name) {
        this(code, name, Dict.Importance.Optional, System.currentTimeMillis());
    }

    public Action() {
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

    public Dict.Importance getImportance() {
        return level;
    }

    public void setImportance(Dict.Importance level) {
        this.level = level;
    }

    @Override
    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public Action setScript(String script) {
        if (null == script || script.isBlank()) {
            throw new ActionException("script cannot be null or blank");
        }
        this.script = script;
        return this;
    }

    @Override
    public IResourcePack execute(IProcess process, IResourceModel... resources) {
        return process.getResourcePack();
    }

}
