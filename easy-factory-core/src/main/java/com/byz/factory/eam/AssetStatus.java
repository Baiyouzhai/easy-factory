package com.byz.factory.eam;

import com.byz.factory.lifecycle.ILifecycle;

import java.util.Set;

/**
 * 资产状态
 *
 * @author 苏政
 */
public enum AssetStatus implements ILifecycle.StatusEnum {

    /** 闲置 */
    IDLE,
    /** 使用中 */
    IN_USE,
    /** 维修中 */
    UNDER_MAINTENANCE,
    /** 已报废 */
    SCRAPPED;

    @Override
    public Set<AssetStatus> allowedTransitions() {
        return switch (this) {
            case IDLE              -> Set.of(IN_USE, UNDER_MAINTENANCE, SCRAPPED);
            case IN_USE            -> Set.of(IDLE, UNDER_MAINTENANCE, SCRAPPED);
            case UNDER_MAINTENANCE -> Set.of(IDLE, SCRAPPED);
            case SCRAPPED          -> Set.of();
        };
    }

}
