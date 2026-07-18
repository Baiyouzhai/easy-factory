package com.byz.factory.factory;

import com.byz.factory.resource.IResourceItem;
import com.byz.factory.shared.Dict;

/**
 * 产品信息 — 是资源的一种，分组固定为 SourceGroup.Product
 *
 * @author 苏政
 */
public interface IProductInfo extends IResourceItem {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Product;
    }

    /**
     * 产品名称
     *
     * @return 产品名称
     */
    String getName();

    /**
     * 蓝图（产品工序路线）
     *
     * @return 蓝图
     */
    IBlueprint getBlueprint();

}
