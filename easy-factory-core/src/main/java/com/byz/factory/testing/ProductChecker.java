package com.byz.factory.testing;

import com.byz.factory.design.IProductInfo;

import java.util.List;

public class ProductChecker implements IProductChecker {

    public static ProductChecker Create(IProductInfo productInfo) {
        return new ProductChecker(productInfo);
    }

    protected IProductInfo productInfo;

    public ProductChecker(IProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    @Override
    public IProductInfo getProductInfo() {
        return productInfo;
    }

}
