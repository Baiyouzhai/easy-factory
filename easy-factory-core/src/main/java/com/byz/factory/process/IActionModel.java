package com.byz.factory.process;

import com.byz.factory.exception.ActionException;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.script.CompiledScript;
import com.byz.factory.script.IScriptEngine;
import com.byz.factory.script.ScriptContext;
import com.byz.factory.script.ScriptEngines;

/**
 * (生产)动作模型 — 带脚本和重要性管理的动作。
 * <p>
 * 在 {@link IAction} 基础之上增加：脚本执行、资源需求声明、重要性分级。
 *
 * @author 苏政
 * @see IAction
 * @see Action
 */
public interface IActionModel extends IAction {

    /**
     * 获取执行脚本（JavaScript）
     *
     * @return 脚本内容，可能为 null（无脚本）
     */
    default String getScript() {
        return null;
    }

    /**
     * 声明执行此动作所需的前置资源
     *
     * @return 所需资源列表
     */
    IResourceItem[] requireResources();

    /**
     * 设置所需资源
     *
     * @param resources 资源列表
     * @return this
     */
    IActionModel setRequireResources(IResourceItem... resources);

    /**
     * 获取脚本引擎
     *
     * @return 脚本引擎
     */
    default IScriptEngine getScriptEngine() {
        return ScriptEngines.getDefault();
    }

    /**
     * 对输入进行处理（默认实现：有脚本走脚本引擎，无脚本返回工序资源包）
     *
     * @param process   工序上下文
     * @param resources 输入资源
     * @return 输出资源包
     */
    @Override
    default IResourcePack execute(IProcess process, IResourceItem... resources) {
        String script = getScript();
        if (script == null || script.isBlank()) {
            // 无脚本时返回工序资源包，具体路由由子类 execute() 覆盖
            return process.getResourcePack();
        }
        IScriptEngine engine = getScriptEngine();
        if (engine == null) {
            throw new ActionException("脚本引擎未配置，无法执行脚本动作: " + getCode());
        }
        CompiledScript compiled = engine.compile(getCode(), script);
        ScriptContext ctx = engine.createContext(process, resources);
        Object result = engine.execute(compiled, ctx);
        if (result instanceof IResourcePack pack) {
            return pack;
        }
        throw new ActionException("脚本执行结果类型错误: 期望 IResourcePack, 实际 " +
                (result == null ? "null" : result.getClass().getName()));
    }

}
