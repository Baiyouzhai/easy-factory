package com.byz.factory.qms.model;

import com.byz.factory.batch.IInspectionRecord;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 检验记录 — 检验动作执行后生成的单条检验数据。
 * <p>
 * 继承 BaseEntity（无状态机）。检验记录是数据记录，生命周期由 InspectionOrder 驱动。
 * 实现 IInspectionRecord 供 MES/LIMS/DMS 等模块编译期引用。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   qms.record.measuredValue  — 实测值
 *   qms.record.judgement      — PASS | FAIL | CONCESSION
 *   qms.record.defectCode     — 不良代码
 *   qms.record.gaugeCode      — 量检具编码
 *   qms.record.inspector      — 检验员
 *   qms.record.remark         — 备注
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionRecord extends BaseEntity implements IInspectionRecord {

    /** 关联检验单编号 */
    private String inspectionNo;

    /** 关联检验方案编码 */
    private String planCode;

    /** 检验项目编码 */
    private String itemCode;

    /** 检验项目名称 */
    private String itemName;

    /** 规格上限 */
    private BigDecimal usl;

    /** 规格下限 */
    private BigDecimal lsl;

    /** 目标值 */
    private BigDecimal target;

    /** 计量单位 */
    private String unit;

    /** 实测值 */
    private BigDecimal measuredValue;

    /** 判定结果：PASS / FAIL / CONCESSION */
    private String judgement;

    /** 不良代码 */
    private String defectCode;

    /** 使用的量检具编码 */
    private String gaugeCode;

    /** 检验员 */
    private String inspector;

    /** 备注 */
    private String remark;

    /** 检验时间 */
    private Instant inspectedAt;

    /**
     * @param code     记录编号
     * @param itemName 检验项目名称
     */
    public InspectionRecord(String code, String itemName) {
        super(code, itemName);
        this.itemName = itemName;
        this.inspectedAt = Instant.now();
    }

    // ==================== 业务方法 ====================

    /**
     * 记录检验结果并自动判定。
     * <p>
     * 判定逻辑：实测值在 [LSL, USL] 范围内 → PASS，否则 → FAIL。
     *
     * @param measuredValue 实测值
     * @param inspector     检验员
     * @param gaugeCode     量检具编码
     */
    public void record(BigDecimal measuredValue, String inspector, String gaugeCode) {
        this.measuredValue = measuredValue;
        this.inspector = inspector;
        this.gaugeCode = gaugeCode;
        this.inspectedAt = Instant.now();

        // 自动判定
        if (usl != null && lsl != null && measuredValue != null) {
            boolean withinSpec = measuredValue.compareTo(lsl) >= 0
                    && measuredValue.compareTo(usl) <= 0;
            this.judgement = withinSpec ? "PASS" : "FAIL";
        } else if (measuredValue != null) {
            // 无规格限时仅记录，不做判定
            this.judgement = null;
        }
        markUpdated();
    }

    /**
     * 让步接收 — 虽然不合格但经评估后可放行。
     *
     * @param remark 让步理由
     * @param approver 批准人
     */
    public void concession(String remark, String approver) {
        this.judgement = "CONCESSION";
        this.remark = remark;
        markUpdated();
    }

    /**
     * 判定是否在规格限内。
     */
    public boolean isInSpec() {
        if (measuredValue == null || usl == null || lsl == null) {
            return true; // 无规格限制则不做判定
        }
        return measuredValue.compareTo(lsl) >= 0
                && measuredValue.compareTo(usl) <= 0;
    }

    /**
     * 判断是否合格。
     */
    public boolean isPassed() {
        return "PASS".equals(judgement);
    }

}
