package com.byz.factory.batch;

/**
 * 文档类别 — GMP 受控文件的分类。
 * <p>
 * 不同类别的文档在生命周期、审批强度、复审周期上有所区别：
 * SOP 需要定期复审，BATCH_RECORD 需要永久归档，
 * DEVIATION/CAPA 需要闭环追踪。
 *
 * @author 苏政
 */
public enum DocumentCategory {

    /** 标准操作规程 */
    SOP,

    /** 批记录 */
    BATCH_RECORD,

    /** 检验报告 */
    INSPECTION,

    /** 偏差报告 */
    DEVIATION,

    /** CAPA 报告 */
    CAPA,

    /** 验证文件 */
    VALIDATION,

    /** 校准证书 */
    CALIBRATION

}
