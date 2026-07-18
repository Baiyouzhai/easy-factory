package com.byz.factory.mes.service;

import com.byz.factory.factory.IBlueprint;
import com.byz.factory.mes.model.MesWorkOrder;
import com.byz.factory.process.IProcess;
import com.byz.factory.resource.IResourcePack;

import java.math.BigDecimal;

/**
 * 工单服务接口 — MES 模块的核心业务接口。
 * <p>
 * TODO 待实现：工单创建→下达→开工→工序流转→完工→关闭
 *
 * @author 苏政
 */
public interface WorkOrderService {

    /** 创建工单 */
    MesWorkOrder create(String productCode, IBlueprint blueprint, BigDecimal quantity, String factoryCode);

    /** 下达 */
    MesWorkOrder release(String workOrderNo);

    /** 开工 */
    MesWorkOrder start(String workOrderNo);

    /** 执行指定工序 */
    IResourcePack executeProcess(String workOrderNo, String processCode, IResourcePack input);

    /** 完工 */
    MesWorkOrder complete(String workOrderNo);

    /** 关闭 */
    MesWorkOrder close(String workOrderNo);

}
