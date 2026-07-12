package com.byz.factory.testing;

import com.byz.factory.design.IProductInfoModel;

public class ProductChecker implements IProductChecker {

    public static ProductChecker Create(IProductInfoModel productInfo) {
        return new ProductChecker(productInfo);
    }

    protected IProductInfoModel productInfo;

    public ProductChecker(IProductInfoModel productInfo) {
        this.productInfo = productInfo;
    }

    @Override
    public IProductInfoModel getProductInfo() {
        return productInfo;
    }

}
