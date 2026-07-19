package com.byz.factory.lims.service;

import com.byz.factory.lims.model.WeighingItem;
import com.byz.factory.lims.model.WeighingTask;

import java.math.BigDecimal;
import java.util.List;

/**
 * 称量任务服务。
 * <p>
 * TODO 待实现：称量任务调度、天平集成、偏差通知。
 * 实现类：{@code WeighingTaskServiceImpl}
 *
 * @author 苏政
 */
public interface WeighingTaskService {

    /** 创建称量任务 */
    WeighingTask create(String code, String name, String formulaCode, String workOrderId, String batchNo);

    /** 按编码查询 */
    WeighingTask getByCode(String code);

    /** 按工单号列出称量任务 */
    List<WeighingTask> listByWorkOrder(String workOrderId);

    /** 开始称量 */
    void startWeighing(String taskCode);

    /**
     * 记录单项目称量结果。
     *
     * @param taskCode     称量任务号
     * @param materialCode 物料编码
     * @param actualQty    实际称量量
     * @param operator     称量人
     * @param verifier     复核人
     * @param balance      使用天平
     * @return 更新后的称量项目
     */
    WeighingItem recordWeighingItem(String taskCode, String materialCode, BigDecimal actualQty,
                                    String operator, String verifier, String balance);

    /** 复核通过 */
    void verify(String taskCode);

    /** 称量完成 */
    void complete(String taskCode);

}
