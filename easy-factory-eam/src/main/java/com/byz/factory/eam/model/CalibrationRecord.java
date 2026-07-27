package com.byz.factory.eam.model;

import com.byz.factory.eam.CalibrationResult;
import com.byz.factory.eam.CalibrationType;
import com.byz.factory.eam.ICalibrationRecord;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 校准记录 — 继承 BaseEntity（无生命周期状态机，仅有结果），
 * 实现 ICalibrationRecord 供 DMS 等模块编译期引用。
 *
 * <h3>便捷方法</h3>
 * <ul>
 *   <li>{@link #recordPass(String, String, LocalDate)} — 记录合格</li>
 *   <li>{@link #recordFail(String, String, LocalDate)} — 记录不合格</li>
 *   <li>{@link #recordAdjusted(String, String, String, LocalDate)} — 记录调整后合格</li>
 * </ul>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   eam.cal.standardVersion — 校准标准版本
 *   eam.cal.environment     — 校准环境条件
 *   eam.cal.uncertainty     — 测量不确定度
 * </pre>
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

    // ==================== 便捷方法 ====================

    /** 记录校准合格 */
    public void recordPass(String calibratedBy, String certificate, LocalDate nextDue) {
        this.result = CalibrationResult.PASS;
        this.calibratedBy = calibratedBy;
        this.certificate = certificate;
        this.nextDue = nextDue;
        markUpdated();
    }

    /** 记录校准不合格 */
    public void recordFail(String calibratedBy, String deviation, LocalDate nextDue) {
        this.result = CalibrationResult.FAIL;
        this.calibratedBy = calibratedBy;
        this.deviation = deviation;
        this.nextDue = nextDue;
        markUpdated();
    }

    /** 记录调整后合格 */
    public void recordAdjusted(String calibratedBy, String deviation, String certificate, LocalDate nextDue) {
        this.result = CalibrationResult.ADJUSTED;
        this.calibratedBy = calibratedBy;
        this.deviation = deviation;
        this.certificate = certificate;
        this.nextDue = nextDue;
        markUpdated();
    }

}
