package com.byz.factory.shared;

import com.byz.factory.process.Action;
import com.byz.factory.shared.Dict.Execute;
import com.byz.factory.shared.Dict.Importance;

/**
 * 空动作 — 哨兵对象，表示不执行任何操作的动作
 *
 * @author 苏政
 */
public final class EmptyAction extends Action {

    public final static EmptyAction Instance = new EmptyAction();

    public EmptyAction() {
        super(Constant.Nothing, Constant.NothingTodo, Importance.Optional, System.currentTimeMillis());
        setExecuteType(Execute.Nothing);
    }

}
