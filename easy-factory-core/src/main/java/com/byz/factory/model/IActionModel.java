package com.byz.factory.core.action;

import com.byz.factory.core.IProcess;
import com.byz.factory.core.resource.ResourceModel;
import com.byz.factory.core.ScriptExecutor;
import com.byz.factory.core.resource.IResourceModel;
import com.byz.factory.core.resource.IResourcePack;

/**
 * (生产)动作
 *
 * @author 苏政
 */
public interface IActionModel {

    /**
     * 编码
     *
     * @return 编码
     */
    String getCode();

    /**
     * 名称, 在工序下为 工序-名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 排序
     *
     * @return 排序
     */
    long getOrder();

    /**
     * 要求资源
     *
     * @return 资源
     */
    default IResourceModel[] requireResources() {
        return ResourceModel.Empty;
    }

    /**
     * 设置要求资源
     *
     * @param resources 资源
     * @return this
     */
    IAction setRequireResources(IResourceModel... resources);

    /**
     * 对输入进行处理
     *
     * @param process 工序
     * @param resources 资源(输入)
     * @return 资源(输出)
     */
    default IResourcePack execute(IProcess process, IResourceModel... resources) {
        IResourcePack resourcePack = process.getResourcePack();
        IResourceModel[] requireResources = requireResources();
        if (null == requireResources) return resourcePack;
        if (0 == requireResources.length) return resourcePack;
        ScriptExecutor.Declare("\nfunction " + getName() + "(process, resources) { \n\t" + getScript() + "\n\t}");
        Object result = ScriptExecutor.Execute("\nexecute(process, resources)", process, resources);
        if (result instanceof IResourcePack) return (IResourcePack) result;
        throw new RuntimeException("脚本执行结果类型错误");
    }

}
