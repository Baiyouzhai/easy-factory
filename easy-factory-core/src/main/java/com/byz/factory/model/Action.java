package com.byz.factory.model;

import com.byz.factory.data.Constant;
import com.byz.factory.data.Dict;

import java.util.Objects;

/**
 * (生产)动作 — IActionModel 的基础实现。
 * <p>
 * 执行逻辑：
 * <ol>
 *   <li>有脚本 → 调用接口默认方法走脚本引擎</li>
 *   <li>无脚本 → 根据 executeType 处理输入资源，产生输出资源包</li>
 * </ol>
 *
 * @author 苏政
 */
public class Action extends com.byz.factory.data.Action implements IActionModel {

    protected String code;
    protected String name;
    protected Dict.Importance importance;
    protected long order;
    protected String script;
    protected IResourceModel[] requireResources;

    public Action(String code, String name, Dict.Importance importance, long order) {
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.importance = Objects.requireNonNull(importance, "importance cannot be null");
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

    @Override
    public IResourceModel[] requireResources() {
        if (null == requireResources) return ResourceModel.Empty;
        return requireResources;
    }

    @Override
    public IActionModel setRequireResources(IResourceModel... resources) {
        this.requireResources = resources;
        return this;
    }

    /**
     * 执行动作：
     * <ol>
     *   <li>有脚本 → 调用父接口默认方法（脚本引擎）</li>
     *   <li>无脚本 → 根据 executeType 处理资源</li>
     * </ol>
     */
    @Override
    public IResourcePack execute(IProcess process, IResourceModel... resources) {
        // 有脚本 → 走脚本引擎
        String s = getScript();
        if (s != null && !s.isBlank()) {
            return IActionModel.super.execute(process, resources);
        }

        // 无脚本 → 根据 executeType 执行
        Dict.Execute type = getExecuteType();
        if (null == type) type = Dict.Execute.Noting;

        ResourcePack output = new ResourcePack();
        return switch (type) {
            case Noting  -> process.getResourcePack();
            case Create  -> {
                // 从 requireResources 创建新资源输出
                if (requireResources != null) output.merge(requireResources);
                yield output;
            }
            case Add     -> {
                // 输入资源的数量增加
                if (resources != null) {
                    for (IResourceModel r : resources) {
                        IResourceModel copy = r.copy();
                        copy.add(r.getNumber());
                        output.merge(copy);
                    }
                }
                yield output;
            }
            case Use     -> {
                // 消耗输入资源，返回剩余
                if (resources != null) {
                    for (IResourceModel r : resources) {
                        r.use(r.getNumber());
                        output.merge(r);
                    }
                }
                yield output;
            }
            case Change  -> {
                // 改变输出为工序资源包的内容
                yield process.getResourcePack();
            }
            case Convert, Split, Combine, Transfer, Hold -> {
                // 变换类：输入资源作为输出（实际变换由脚本定义）
                output.merge(resources);
                yield output;
            }
        };
    }

}
