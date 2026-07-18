package com.byz.factory.eam.model;

import com.byz.factory.batch.CalibrationResult;
import com.byz.factory.batch.CalibrationType;
import com.byz.factory.batch.ICalibrationRecord;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 校准记录 — 继承 BaseEntity（无生命周期状态机，仅有结果），
 * 实现 ICalibrationRecord 供 DMS 等模块编译期引用。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CalibrationRecord extends BaseEntity implements ICalibrationRecord {

    /** 关联资产编码（仪器/量检具） */
    private String assetCode;

    /** 校准类型 */
    private CalibrationType calibrationType;

    /** 校准标准 */
    private String standard;

    /** 校准结果 */
    private CalibrationResult result;

    /** 校准人/机构 */
    private String calibratedBy;

    /** 校准日期 */
    private LocalDate calibratedAt;

    /** 下次校准日期 */
    private LocalDate nextDue;

    /** 校准证书编号 */
    private String certificate;

    /** 偏差值 */
    private String deviation;

    /**
     * @param code           校准单号
     * @param assetCode      关联资产编码
     * @param calibrationType 校准类型
     */
    public CalibrationRecord(String code, String assetCode, CalibrationType calibrationType) {
        super(code, "Cal-" + code);
        this.assetCode = assetCode;
        this.calibrationType = calibrationType;
        this.calibratedAt = LocalDate.now();
    }

}
