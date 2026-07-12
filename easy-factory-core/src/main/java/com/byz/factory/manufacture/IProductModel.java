package com.byz.factory.manufacture;

import com.byz.factory.core.resource.IResource;
import com.byz.factory.design.IProductInfo;

/**
 * 实物产品
 */
public interface IProduct extends IResource {

    @Override
    default GroupType getGroup() {
        return GroupType.Product;
    }

    /**
     * 产品信息
     *
     * @return 产品信息
     */
    IProductInfo getProductInfo();

}
