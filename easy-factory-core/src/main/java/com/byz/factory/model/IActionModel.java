package com.byz.factory.model;

import com.byz.factory.data.Dict;

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
     * 重要性
     *
     * @return 重要性等级
     */
    Dict.Importance getImportance();

    /**
     * 声明执行此动作所需的前置资源
     *
     * @return 所需资源列表
     */
    IResourceModel[] requireResources();

    /**
     * 设置所需资源
     *
     * @param resources 资源列表
     * @return this
     */
    IActionModel setRequireResources(IResourceModel... resources);

    /**
     * 对输入进行处理（默认实现：调用脚本或返回工序资源包）
     *
     * @param process   工序上下文
     * @param resources 输入资源
     * @return 输出资源包
     */
    @Override
    default IResourcePack execute(IProcess process, IResourceModel... resources) {
        IResourcePack resourcePack = process.getResourcePack();
        String script = getScript();
        if (null == script || script.isBlank()) {
            return resourcePack;
        }
        ScriptExecutor.Declare("\nfunction " + getName() + "(process, resources) { \n\t" + script + "\n}");
        Object result = ScriptExecutor.Execute("execute", process, resources);
        if (result instanceof IResourcePack pack) {
            return pack;
        }
        throw new RuntimeException("脚本执行结果类型错误: 期望 IResourcePack, 实际 " +
                (result == null ? "null" : result.getClass().getName()));
    }

}
