package com.byz.factory.resource;

import com.byz.factory.shared.Dict;

/**
 * (生产)设备 — 分组固定为 SourceGroup.Machine
 *
 * @author 苏政
 */
public interface IMachine extends IResourceItem {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Machine;
    }

}
