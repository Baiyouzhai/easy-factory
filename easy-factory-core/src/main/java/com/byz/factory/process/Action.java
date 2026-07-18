package com.byz.factory.process;

import com.byz.factory.resource.IResourcePack;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.ResourcePack;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.script.CompiledScript;
import com.byz.factory.script.IScriptEngine;
import com.byz.factory.script.ScriptContext;
import com.byz.factory.shared.Dict;

import java.util.Objects;

/**
 * (生产)动作 — IActionModel 的基础实现。
 * <p>
 * 执行逻辑：
 * <ol>
 *   <li>有脚本 → 通过 IScriptEngine 执行</li>
 *   <li>无脚本 → 根据 executeType 处理输入资源，产生输出资源包</li>
 * </ol>
 *
 * @author 苏政
 */
public class Action implements IActionModel {

    protected String code;
    protected String name;
    protected Dict.Importance importance;
    protected long order;
    protected String script;
    protected Dict.Execute executeType;
    protected IResourceItem[] requireResources;

    public Action(String code, String name, Dict.Importance importance, long order) {
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.importance = Objects.requireNonNull(importance, "importance cannot be null");
        this.order = order;
        this.executeType = Dict.Execute.Nothing;
    }

    public Action(String code, String name) {
        this(code, name, Dict.Importance.Optional, System.currentTimeMillis());
    }

    public Action() {
        this("Nothing", "NothingTodo", Dict.Importance.Optional, System.currentTimeMillis());
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
    public Dict.Importance getImportance() {
        return importance;
    }

    public void setImportance(Dict.Importance importance) {
        this.importance = importance;
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

    public void setScript(String script) {
        this.script = script;
    }

    public Dict.Execute getExecuteType() {
        return executeType;
    }

    public void setExecuteType(Dict.Execute executeType) {
        this.executeType = executeType;
    }

    @Override
    public IResourceItem[] requireResources() {
        if (null == requireResources) return ResourceItem.EMPTY_ARRAY;
        return requireResources;
    }

    @Override
    public IActionModel setRequireResources(IResourceItem... resources) {
        this.requireResources = resources;
        return this;
    }

    /**
     * 执行动作：
     * <ol>
     *   <li>有脚本 → 调用接口默认方法（脚本引擎）</li>
     *   <li>无脚本 → 根据 executeType 处理资源</li>
     * </ol>
     */
    @Override
    public IResourcePack execute(IProcess process, IResourceItem... resources) {
        // 有脚本 → 走脚本引擎
        String s = getScript();
        if (s != null && !s.isBlank()) {
            return IActionModel.super.execute(process, resources);
        }

        // 无脚本 → 根据 executeType 执行
        Dict.Execute type = getExecuteType();
        if (null == type) type = Dict.Execute.Nothing;

        ResourcePack output = new ResourcePack();
        return switch (type) {
            case Nothing -> process.getResourcePack();
            case Create  -> {
                if (requireResources != null) output.merge(requireResources);
                yield output;
            }
            case Add     -> {
                if (resources != null) {
                    for (IResourceItem r : resources) {
                        IResourceItem copy = r.copy();
                        copy.add(r.getNumber());
                        output.merge(copy);
                    }
                }
                yield output;
            }
            case Use     -> {
                if (resources != null) {
                    for (IResourceItem r : resources) {
                        r.use(r.getNumber());
                        output.merge(r);
                    }
                }
                yield output;
            }
            case Change  -> {
                yield process.getResourcePack();
            }
            case Convert, Split, Combine, Transfer, Hold -> {
                output.merge(resources);
                yield output;
            }
        };
    }

}
