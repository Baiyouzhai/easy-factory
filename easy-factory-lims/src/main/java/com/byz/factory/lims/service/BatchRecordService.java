package com.byz.factory.lims.service;

import com.byz.factory.lims.model.BatchRecord;

import java.util.List;

/**
 * 批记录服务。
 * <p>
 * TODO 待实现：批记录自动生成、GMP 合规审核、DMS 归档集成。
 * 实现类：{@code BatchRecordServiceImpl}
 *
 * @author 苏政
 */
public interface BatchRecordService {

    /** 创建批记录 */
    BatchRecord create(String batchNo, String workOrderId, String formulaCode,
                       String formulaVersion, String productCode);

    /** 按批号查询 */
    BatchRecord getByBatchNo(String batchNo);

    /** 按工单号列出批记录 */
    List<BatchRecord> listByWorkOrder(String workOrderId);

    /** 提交审核 */
    void submitForReview(String batchNo);

    /** 批准 */
    void approve(String batchNo, String reviewedBy);

    /** 驳回重审 */
    void reject(String batchNo, String reason);

    /** 归档 */
    void archive(String batchNo);

    /** 添加偏差记录 */
    void addDeviation(String batchNo, String deviation);

}
