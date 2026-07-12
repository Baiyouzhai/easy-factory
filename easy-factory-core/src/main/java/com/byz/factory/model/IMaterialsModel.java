package com.byz.factory.model;

import com.byz.factory.data.Dict;

/**
 * (生产)物料 — 分组固定为 SourceGroup.Material
 *
 * @author 苏政
 */
public interface IMaterialsModel extends IResourceModel {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Material;
    }

}
