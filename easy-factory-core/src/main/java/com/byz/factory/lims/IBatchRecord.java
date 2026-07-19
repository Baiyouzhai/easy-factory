package com.byz.factory.lims;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 批记录抽象 — 供 MES/QMS/DMS 等模块编译期引用。
 * <p>
 * 批记录是 GMP 合规的核心文档，记录一批产品从投料到产出的完整制造过程，
 * 通过 LIMS 模块的 {@code BatchRecord} 实现。
 *
 * @author 苏政
 */
public interface IBatchRecord {

    /** 批号 */
    String getBatchNo();

    /** 关联工单号 */
    String getWorkOrderId();

    /** 配方编码 */
    String getFormulaCode();

    /** 配方版本（冻结——批记录关联的配方版本不可变） */
    String getFormulaVersion();

    /** 产品编码 */
    String getProductCode();

    /** 实际批量 */
    BigDecimal getBatchSize();

    /** 实际收率 */
    BigDecimal getYield();

    /** 批记录状态 */
    BatchRecordStatus getStatus();

    /** 审核人 */
    String getReviewedBy();

    /** 审核时间 */
    Instant getReviewedAt();

    /** 工序执行记录（引用键列表） */
    List<String> getProcessRecords();

    /** 关联称量任务号列表 */
    List<String> getWeighingTasks();

    /** 关联检验记录列表 */
    List<String> getInspectionRecords();

    /** 偏差记录列表 */
    List<String> getDeviations();

}
