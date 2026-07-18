package com.byz.factory.resource;

import com.byz.factory.shared.Dict;

/**
 * 方法资源 — 工艺方法、SOP、检验标准等。
 * <p>
 * 4M1E 中的"法 (Method)"，固定 {@link Dict.SourceGroup#Method}。
 *
 * @author 苏政
 */
public interface IMethod extends IResourceItem {

    @Override
    default Dict.SourceGroup getGroup() {
        return Dict.SourceGroup.Method;
    }
}
