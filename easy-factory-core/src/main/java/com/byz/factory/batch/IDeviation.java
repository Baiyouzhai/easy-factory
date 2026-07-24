package com.byz.factory.batch;

import java.time.Instant;

/**
 * 偏差 — QMS 模块的核心实体，记录生产过程中的质量偏差。
 * <p>
 * 当检验不合格、设备异常或人为差错发生时，QMS 创建偏差记录。
 * 供 MES/Andon/DMS/EAM 等模块编译期引用。
 *
 * @author 苏政
 */
public interface IDeviation {

    /** 偏差编号 */
    String getCode();

    /** 偏差名称/摘要 */
    String getName();

    /** 偏差来源（如：检验不合格、设备异常、人为差错） */
    String getSource();

    /** 关联的检验单编号 */
    String getInspectionNo();

    /** 关联的工单号 */
    String getWorkOrderNo();

    /** 关联的批号 */
    String getBatchNo();

    /** 关联的工序编码 */
    String getProcessCode();

    /** 偏差严重程度 */
    DeviationSeverity getSeverity();

    /** 偏差状态 */
    DeviationStatus getStatus();

    /** 产品影响评估 */
    String getProductImpact();

    /** 处置方式 */
    DeviationDisposition getDisposition();

    /** 处置说明 */
    String getDispositionNote();

    /** 关联的 CAPA 编号 */
    String getCapaCode();

    /** 调查人 */
    String getInvestigator();

    /** 处置人（QA） */
    String getDisposedBy();

    /** 创建时间 */
    Instant getCreatedAt();

    /** 关闭时间 */
    Instant getClosedAt();

}
