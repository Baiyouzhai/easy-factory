package com.byz.factory.core;

import com.byz.factory.core.resource.IResourceModel;

/**
 * (生产)材料
 * @author 苏政
 */
public interface IMaterialsModel extends IResourceModel {

    @Override
    default GroupType getGroup() {
        return GroupType.Material;
    }

}
