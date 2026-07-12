package com.byz.factory.manufacture;

import com.byz.factory.data.Dict;
import com.byz.factory.design.IProductInfoModel;
import com.byz.factory.model.IResourceModel;

/**
 * 实物产品 — 携带产品信息的物理实体
 *
 * @author 苏政
 */
public interface IProductModel extends IResourceModel {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Product;
    }

    /**
     * 产品信息
     *
     * @return 产品信息
     */
    IProductInfoModel getProductInfo();

}
