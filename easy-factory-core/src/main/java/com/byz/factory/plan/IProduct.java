package com.byz.factory.plan;

import com.byz.factory.design.IProductInfoModel;

public interface IProduct {

    /**
     * 产品信息
     *
     * @return 产品信息
     */
    IProductInfoModel getProductInfo();

}
