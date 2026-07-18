package com.byz.factory.resource;

import com.byz.factory.shared.Dict;

/**
 * 环境资源 — 温湿度、洁净度、压差等环境条件。
 * <p>
 * 4M1E 中的"环 (Environment)"，固定 {@link Dict.SourceGroup#Environment}。
 *
 * @author 苏政
 */
public interface IEnvironment extends IResourceItem {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Environment;
    }
}
