package com.byz.factory.qms.model;

import com.byz.factory.batch.IInspectionRecord;
import com.byz.factory.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "qms_inspection_record")
public class InspectionRecord extends BaseEntity implements IInspectionRecord {

    @Column(name = "inspection_no", nullable = false, length = 100)
    private String inspectionNo;

    @Column(name = "plan_code", length = 100)
    private String planCode;

    @Column(name = "item_code", nullable = false, length = 100)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(precision = 20, scale = 6)
    private BigDecimal usl;

    @Column(precision = 20, scale = 6)
    private BigDecimal lsl;

    @Column(precision = 20, scale = 6)
    private BigDecimal target;

    @Column(length = 20)
    private String unit;

    @Column(name = "measured_value", precision = 20, scale = 6)
    private BigDecimal measuredValue;

    @Column(length = 20)
    private String judgement;

    @Column(name = "defect_code", length = 50)
    private String defectCode;

    @Column(name = "gauge_code", length = 100)
    private String gaugeCode;

    @Column(length = 100)
    private String inspector;

    @Column(length = 500)
    private String remark;

    @Column(name = "inspected_at")
    private Instant inspectedAt;

    public InspectionRecord() {}

    public InspectionRecord(String code, String itemName) {
        super(code, itemName);
        this.itemName = itemName;
        this.inspectedAt = Instant.now();
    }

    @Override public String getInspectionNo() { return inspectionNo; }
    @Override public String getPlanCode() { return planCode; }
    @Override public String getItemCode() { return itemCode; }
    @Override public String getItemName() { return itemName; }
    @Override public BigDecimal getUsl() { return usl; }
    @Override public BigDecimal getLsl() { return lsl; }
    @Override public BigDecimal getTarget() { return target; }
    @Override public String getUnit() { return unit; }
    @Override public BigDecimal getMeasuredValue() { return measuredValue; }
    @Override public String getJudgement() { return judgement; }
    @Override public String getDefectCode() { return defectCode; }
    @Override public String getGaugeCode() { return gaugeCode; }
    @Override public String getInspector() { return inspector; }
    @Override public String getRemark() { return remark; }
    @Override public Instant getInspectedAt() { return inspectedAt; }

    public void record(BigDecimal measuredValue, String inspector, String gaugeCode) {
        this.measuredValue = measuredValue;
        this.inspector = inspector;
        this.gaugeCode = gaugeCode;
        this.inspectedAt = Instant.now();
        if (usl != null && lsl != null && measuredValue != null) {
            this.judgement = (measuredValue.compareTo(lsl) >= 0
                    && measuredValue.compareTo(usl) <= 0) ? "PASS" : "FAIL";
        } else if (measuredValue != null) {
            this.judgement = null;
        }
        markUpdated();
    }

    public void concession(String remark, String approver) {
        this.judgement = "CONCESSION"; this.remark = remark; markUpdated();
    }

    public boolean isInSpec() {
        if (measuredValue == null || usl == null || lsl == null) return true;
        return measuredValue.compareTo(lsl) >= 0 && measuredValue.compareTo(usl) <= 0;
    }

    public boolean isPassed() { return "PASS".equals(judgement); }
}
