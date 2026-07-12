package com.byz.factory.core;

import com.byz.factory.core.resource.IResourceModel;

/**
 * (生产)人员
 * @author 苏政
 */
public interface IPersonnelModel extends IResourceModel {

    @Override
    default GroupType getGroup() {
        return GroupType.Personnel;
    }

}
