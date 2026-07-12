package com.byz.factory.data;

import com.byz.factory.model.Action;

/**
 * 系统常量
 *
 * @author 苏政
 */
public interface Constant {

    String Default = "Default";
    String Empty = "Empty";
    String Nothing = "Nothing";
    String NothingTodo = "NothingTodo";

    String DefaultActionScript = "return process.getResourcePack()";

    /**
     * 空动作 — 默认占位动作，不执行任何操作
     */
    Action EMPTY_ACTION_MODEL = createEmptyAction();

    private static Action createEmptyAction() {
        Action action = new Action(Nothing, NothingTodo, Dict.Importance.Optional, System.currentTimeMillis());
        action.setScript(DefaultActionScript);
        return action;
    }

}
