package com.byz.factory.batch;

/**
 * 检验类型 — QMS 模块。
 * <p>
 * 标识检验在生产流程中的位置和阶段。
 *
 * @author 苏政
 */
public enum InspectionType {

    /** 来料检验 — 供应商来料入库前检验 */
    IQC,

    /** 过程检验 — 生产过程中对半成品的检验 */
    IPQC,

    /** 最终检验 — 产品完工后的最终检验 */
    FQC,

    /** 出货检验 — 发货前的最终确认 */
    OQC

}
