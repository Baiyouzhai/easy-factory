package com.byz.factory.factory;

import com.byz.factory.process.IProcess;

import java.util.List;

/**
 * (生产)蓝图信息 — 产品的工序路线定义
 *
 * @author 苏政
 */
public interface IBlueprint {

    /**
     * 蓝图编码
     *
     * @return 编码
     */
    String getCode();

    /**
     * 蓝图名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 版本
     *
     * @return 版本号
     */
    String getVersion();

    /**
     * 生产工序清单
     *
     * @return 工序清单
     */
    List<IProcess> getProductionProcessList();

}
