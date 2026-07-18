package com.byz.factory.factory;

import com.byz.factory.resource.IResourceItem;
import com.byz.factory.shared.Dict;

/**
 * 实物产品 — 携带产品信息的物理实体
 *
 * @author 苏政
 */
public interface IProductItem extends IResourceItem {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Product;
    }

    /**
     * 产品信息
     *
     * @return 产品信息
     */
    IProductInfo getProductInfo();

}
