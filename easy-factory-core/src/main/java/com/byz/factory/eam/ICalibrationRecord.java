package com.byz.factory.eam;

import java.time.LocalDate;

/**
 * 校准记录 — EAM 模块的校准结果实体接口。
 * <p>
 * DMS 模块通过此接口引用校准证书进行归档。
 *
 * @author 苏政
 * @see CalibrationResult
 * @see CalibrationType
 */
public interface ICalibrationRecord {

    /** 校准单号 */
    String getCode();

    /** 关联资产编码（仪器/量检具） */
    String getAssetCode();

    /** 校准类型 */
    CalibrationType getCalibrationType();

    /** 校准标准 */
    String getStandard();

    /** 校准结果 */
    CalibrationResult getResult();

    /** 校准人/机构 */
    String getCalibratedBy();

    /** 校准日期 */
    LocalDate getCalibratedAt();

    /** 下次校准日期 */
    LocalDate getNextDue();

    /** 校准证书编号 */
    String getCertificate();

    /** 偏差值 */
    String getDeviation();

}
