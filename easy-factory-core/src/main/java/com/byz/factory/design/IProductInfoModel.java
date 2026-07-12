package com.byz.factory.design;

import com.byz.factory.core.resource.IResource;

/**
 * 产品
 */
public interface IProductInfo extends IResource {

    default GroupType getGroup() {
        return GroupType.Product;
    }

    /**
     * 产品名称
     *
     * @return 产品名称
     */
    String getName();

    /**
     * 蓝图
     *
     * @return 蓝图
     */
    IBlueprint getBlueprint();

}
