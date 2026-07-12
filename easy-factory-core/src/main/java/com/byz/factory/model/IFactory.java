package com.byz.factory.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 工厂 — 一个可执行的生产系统，是工序的容器。
 * <p>
 * 工厂汇总旗下所有工序的动作，提供全局视图。
 *
 * @author 苏政
 */
public interface IFactory {

    /**
     * 工序清单
     *
     * @return 工序清单
     */
    List<IProcess> getProcesses();

    /**
     * 动作清单 — 汇总所有工序的动作。
     *
     * @return 动作清单
     */
    default List<IAction> getActions() {
        List<IAction> actions = new ArrayList<>();
        List<IProcess> list = getProcesses();
        if (null != list) list.forEach(item -> actions.addAll(item.getActions()));
        return actions;
    }

}
