package com.byz.factory.design;

import com.byz.factory.data.Dict;
import com.byz.factory.model.IResourceModel;

/**
 * 产品信息 — 是资源的一种，分组固定为 SourceGroup.Product
 *
 * @author 苏政
 */
public interface IProductInfoModel extends IResourceModel {

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
