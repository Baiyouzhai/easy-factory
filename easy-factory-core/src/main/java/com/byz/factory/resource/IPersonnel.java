package com.byz.factory.resource;

import com.byz.factory.shared.Dict;

/**
 * (生产)人员 — 分组固定为 SourceGroup.Personnel
 *
 * @author 苏政
 */
public interface IPersonnel extends IResourceItem {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Personnel;
    }

}
