package com.byz.factory.qms.model;

import com.byz.factory.batch.InspectionType;
import com.byz.factory.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "qms_inspection_plan")
public class InspectionPlan extends BaseEntity {

    @Column(name = "product_code", nullable = false, length = 100)
    private String productCode;

    @Column(name = "process_code", length = 100)
    private String processCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_type", length = 10)
    private InspectionType inspectionType;

    @Column
    private double aql;

    @Column(name = "sample_size")
    private int sampleSize;

    @Column(length = 100)
    private String standard;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Transient
    private List<InspectionItem> items;

    public InspectionPlan() {}

    public InspectionPlan(String code, String name, String productCode,
                          String processCode, InspectionType inspectionType) {
        super(code, name);
        this.productCode = productCode;
        this.processCode = processCode;
        this.inspectionType = inspectionType;
        this.items = new ArrayList<>();
        this.version = "1.0";
    }

    public void addItem(InspectionItem item) {
        if (this.items == null) this.items = new ArrayList<>();
        this.items.add(item);
        markUpdated();
    }

    public void updateVersion(String newVersion) { this.version = newVersion; markUpdated(); }

    public void approve(String approvedBy) { this.approvedBy = approvedBy; markUpdated(); }

    public int getItemCount() { return items != null ? items.size() : 0; }

    @Data
    @Embeddable
    public static class InspectionItem {
        @Column(name = "item_code", nullable = false, length = 100)
        private String itemCode;
        @Column(name = "item_name", nullable = false, length = 255)
        private String itemName;
        @Column(name = "spec_type", length = 20)
        private String specType;
        @Column(precision = 20, scale = 6)
        private BigDecimal usl;
        @Column(precision = 20, scale = 6)
        private BigDecimal lsl;
        @Column(precision = 20, scale = 6)
        private BigDecimal target;
        @Column(length = 20)
        private String unit;
        @Column(length = 255)
        private String method;
        @Column(length = 255)
        private String sampling;
        @Column(name = "sort_order")
        private int order;
        @Column
        private boolean critical;

        public InspectionItem() {}
        public InspectionItem(String itemCode, String itemName, String specType) {
            this.itemCode = itemCode; this.itemName = itemName; this.specType = specType;
        }
    }
}
