package com.byz.factory.design;

import com.byz.factory.core.IProcess;

import java.util.List;

/**
 * (生产)蓝图信息
 */
public interface IBlueprint {

    /**
     * 生产工序清单
     *
     * @return
     */
    List<IProcess> getProductionProcessList();

}
