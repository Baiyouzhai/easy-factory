package com.byz.factory.core;

import com.byz.factory.core.resource.IResourceModel;

/**
 * (生产)设备
 * @author 苏政
 */
public interface IMachineModel extends IResourceModel {

    @Override
    default GroupType getGroup() {
        return GroupType.Machine;
    }

}
