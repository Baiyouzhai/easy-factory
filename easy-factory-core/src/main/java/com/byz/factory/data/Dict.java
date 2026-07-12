package com.byz.factory;

public interface Dict {

    /**
     * 动作类型
     */
    enum ActionType {
        /**
         * 动作
         */
        Action,
        /**
         * 动作组
         */
        ActionGroup,
    }

    /**
     * 等级
     */
    enum Level {
        /**
         * (重要性)可选
         */
        Optional,
        /**
         * 必要
         */
        Require,
    }

}
