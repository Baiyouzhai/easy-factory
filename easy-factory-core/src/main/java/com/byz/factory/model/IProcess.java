package com.byz.factory.model;

import com.byz.factory.exception.ActionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 工序 — 生产流程中的一个步骤，包含一组有序动作。
 * <p>
 * 工序按 order 排序，通过 execute() 遍历动作链处理输入资源并累积输出资源。
 *
 * @author 苏政
 */
public interface IProcess {

    /**
     * 编码
     *
     * @return 编码
     */
    String getCode();

    /**
     * 名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 排序
     *
     * @return 排序
     */
    default long getOrder() {
        return 0;
    }

    /**
     * 动作清单
     *
     * @return 动作清单
     */
    default List<IAction> getActions() {
        return new ArrayList<>();
    }

    /**
     * 设置动作清单
     *
     * @param actions 动作
     */
    IProcess setActions(Collection<IAction> actions);

    /**
     * 资源包
     *
     * @return 资源包
     */
    default IResourcePack getResourcePack() {
        return ResourcePack.EmptyPack;
    }

    /**
     * 设置资源包
     *
     * @param resourcePack 资源包
     * @return this
     */
    IProcess setResourcePack(IResourcePack resourcePack);

    /**
     * 要求资源
     *
     * @return 资源
     */
    default IResourceModel[] requireResources() {
        return ResourcePack.Empty;
    }

    /**
     * 设置要求资源
     *
     * @param resources 资源
     * @return this
     */
    IProcess setRequireResources(IResourceModel... resources);

    /**
     * 执行生产动作（集合）— 按顺序执行所有动作，前一个动作的输出作为后一个动作的输入。
     *
     * @param inputs 输入，上工序输出
     * @return 本工序最终输出资源包
     */
    default IResourcePack execute(IResourceModel... inputs) {
        List<IAction> actions = getActions();
        if (null == actions) throw new ActionException("无效工序: 动作清单为null");
        if (actions.isEmpty()) throw new ActionException("动作清单为空");
        IResourcePack output = actions.get(0).execute(this, inputs);
        for (int i = 1; i < actions.size(); i++) {
            IResourceModel[] stepInput = output.getResources();
            output = actions.get(i).execute(this, stepInput);
        }
        return output;
    }

}
