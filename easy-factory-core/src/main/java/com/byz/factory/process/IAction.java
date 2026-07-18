package com.byz.factory.process;

import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.shared.Dict;

/**
 * 动作 — 工序中的最小操作单元，对输入资源执行处理并返回输出资源。
 * <p>
 * 这是所有动作的基接口。需要脚本能力的动作使用 {@link IActionModel}。
 *
 * @author 苏政
 * @see IActionModel
 * @see IProcess
 */
public interface IAction {

    /**
     * 动作编码
     */
    String getCode();

    /**
     * 动作名称，在工序下为 工序-名称
     */
    String getName();

    /**
     * 排序
     */
    long getOrder();

    /**
     * 重要性 — 可选动作失败不中断流程，必要动作失败则中断。
     */
    Dict.Importance getImportance();

    /**
     * 对输入进行处理
     *
     * @param process   工序上下文
     * @param resources 输入资源
     * @return 输出资源包
     */
    IResourcePack execute(IProcess process, IResourceItem... resources);

}
