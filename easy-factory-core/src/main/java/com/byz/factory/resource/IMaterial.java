package com.byz.factory.resource;

import com.byz.factory.shared.Dict;

/**
 * (生产)物料 — 分组固定为 SourceGroup.Material
 *
 * @author 苏政
 */
public interface IMaterial extends IResourceItem {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Material;
    }

}
