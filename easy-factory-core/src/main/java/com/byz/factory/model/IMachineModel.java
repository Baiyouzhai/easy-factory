package com.byz.factory.model;

import com.byz.factory.data.Dict;

/**
 * (生产)设备 — 分组固定为 SourceGroup.Machine
 *
 * @author 苏政
 */
public interface IMachineModel extends IResourceModel {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Machine;
    }

}
