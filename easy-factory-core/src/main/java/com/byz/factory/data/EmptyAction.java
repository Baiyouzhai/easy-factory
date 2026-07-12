package com.byz.factory.data;

public final class EmptyAction extends Action {

    public final static EmptyAction Instance = new EmptyAction();

    public EmptyAction() {
        super(Constant.Nothing, Constant.NothingTodo, Dict.Execute.Noting);
    }

}
