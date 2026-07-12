package com.byz.factory.model;

import com.byz.factory.data.Dict;

/**
 * (生产)人员 — 分组固定为 SourceGroup.Personnel
 *
 * @author 苏政
 */
public interface IPersonnelModel extends IResourceModel {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Personnel;
    }

}
