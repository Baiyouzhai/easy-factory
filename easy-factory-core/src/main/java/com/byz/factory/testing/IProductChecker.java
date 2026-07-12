package com.byz.factory.testing;

import com.byz.factory.model.IFactory;
import com.byz.factory.model.IProcess;
import com.byz.factory.design.IBlueprint;
import com.byz.factory.design.IProductInfoModel;

import java.util.List;

/**
 * 产品检查信息
 */
public interface IProductChecker {

    /**
     * 产品
     *
     * @return 产品
     */
    IProductInfoModel getProductInfo();

    /**
     * (产品)工序清单
     *
     * @return (产品)工序清单
     */
    default List<IProcess> getProductProcesses() {
        IBlueprint blueprint = getProductInfo().getBlueprint();
        return blueprint.getProductionProcessList();
    }

    /**
     * 工厂检查
     *
     * @param factory 工厂
     * @return 检查结果
     */
    default IProductFactoryResult check(IFactory factory) {
        return null;
    }

    /**
     * 工序检查
     *
     * @param processes 工序清单
     * @return 检查结果
     */
    default List<IProductProcessResult> check(List<IProcess> processes) {
        return null;
    }

}
