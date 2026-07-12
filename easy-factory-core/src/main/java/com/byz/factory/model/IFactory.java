package com.byz.factory.core;

import com.byz.factory.core.action.IAction;

import java.util.ArrayList;
import java.util.List;

/**
 * 工厂
 * @author 苏政
 */
public interface IFactory {

	/**
	 * 工序清单
	 * @return 工序清单
	 */
	List<IProcess> getProcesses();

	/**
	 * 动作清单
	 * @return 动作清单
	 */
	default List<IAction> getActions() {
		List<IAction> actions = new ArrayList<>();
		List<IProcess> list = getProcesses();
		if (null != list) list.forEach(item -> {
			actions.addAll(item.getActions());
		});
		return actions;
	}

}
