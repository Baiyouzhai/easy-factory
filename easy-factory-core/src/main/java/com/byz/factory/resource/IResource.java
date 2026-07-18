package com.byz.factory.resource;

import com.byz.data.IData;
import com.byz.factory.shared.Dict;

/**
 * 资源 — 制造系统中物料/设备/人员/方法/环境/产品的基础抽象。
 * <p>
 * 继承 {@link IData} 使所有资源具备 JSON 序列化和属性拷贝能力。
 *
 * @author 苏政
 * @see Dict.SourceGroup
 * @see Dict.SourceType
 */
public interface IResource extends IData {

    /**
     * 资源名称
     */
    String getName();

    /**
     * 资源分组（人机料法环/产品）
     */
    Dict.SourceGroup getGroup();

    /**
     * 资源子类型
     */
    Dict.SourceType getType();

}
