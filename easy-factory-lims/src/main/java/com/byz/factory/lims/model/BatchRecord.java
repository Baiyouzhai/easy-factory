package com.byz.factory.lims.model;

import com.byz.factory.lims.BatchRecordStatus;
import com.byz.factory.lims.IBatchRecord;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.LimsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lims_batch_record")
public class BatchRecord extends BaseLifecycleEntity<BatchRecordStatus> implements IBatchRecord {

    @Column(name = "batch_no", nullable = false, unique = true, length = 100)
    private String batchNo;

    @Column(name = "work_order_id", length = 100)
    private String workOrderId;

    @Column(name = "formula_code", length = 100)
    private String formulaCode;

    @Column(name = "formula_version", length = 20)
    private String formulaVersion;

    @Column(name = "product_code", length = 100)
    private String productCode;

    @Column(name = "batch_size", precision = 20, scale = 6)
    private BigDecimal batchSize;

    @Column(precision = 10, scale = 4)
    private BigDecimal yield;

    @ElementCollection
    @CollectionTable(name = "lims_batch_process_record", joinColumns = @JoinColumn(name = "batch_id"))
    @Column(name = "record_ref")
    private List<String> processRecords = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "lims_batch_weighing_task", joinColumns = @JoinColumn(name = "batch_id"))
    @Column(name = "task_code")
    private List<String> weighingTasks = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "lims_batch_inspection", joinColumns = @JoinColumn(name = "batch_id"))
    @Column(name = "inspection_ref")
    private List<String> inspectionRecords = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "lims_batch_deviation", joinColumns = @JoinColumn(name = "batch_id"))
    @Column(name = "deviation_ref")
    private List<String> deviations = new ArrayList<>();

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    public BatchRecord() {}

    public BatchRecord(String batchNo, String workOrderId, String formulaCode,
                       String formulaVersion, String productCode) {
        super(batchNo, "Batch-" + batchNo, BatchRecordStatus.IN_PROGRESS);
        this.batchNo = batchNo;
        this.workOrderId = workOrderId;
        this.formulaCode = formulaCode;
        this.formulaVersion = formulaVersion;
        this.productCode = productCode;
    }

    @Override public String getCode() { return super.getCode(); }
    @Override public String getName() { return super.getName(); }
    @Override public String getBatchNo() { return batchNo; }
    @Override public String getWorkOrderId() { return workOrderId; }
    @Override public String getFormulaCode() { return formulaCode; }
    @Override public String getFormulaVersion() { return formulaVersion; }
    @Override public String getProductCode() { return productCode; }
    @Override public BigDecimal getBatchSize() { return batchSize; }
    @Override public BigDecimal getYield() { return yield; }
    @Override public BatchRecordStatus getStatus() { return super.getStatus(); }
    @Override public String getReviewedBy() { return reviewedBy; }
    @Override public Instant getReviewedAt() { return reviewedAt; }
    @Override public List<String> getProcessRecords() { return processRecords; }
    @Override public List<String> getWeighingTasks() { return weighingTasks; }
    @Override public List<String> getInspectionRecords() { return inspectionRecords; }
    @Override public List<String> getDeviations() { return deviations; }

    public void submitForReview() {
        transition(BatchRecordStatus.REVIEW); markUpdated();
        publish(LimsEventTypes.BATCH_RECORD_CREATED, Map.of("batchNo", batchNo, "workOrderId", workOrderId, "formulaCode", formulaCode, "formulaVersion", formulaVersion));
    }
    public void approve(String reviewedBy) {
        this.reviewedBy = reviewedBy; this.reviewedAt = Instant.now();
        transition(BatchRecordStatus.APPROVED); markUpdated();
        publish(LimsEventTypes.BATCH_RECORD_APPROVED, Map.of("batchNo", batchNo, "reviewedBy", reviewedBy, "reviewedAt", reviewedAt.toString()));
    }
    public void reject(String reason) { transition(BatchRecordStatus.IN_PROGRESS); markUpdated(); setExpandProperty("lims.batch.rejectionReason", reason); }
    public void archive() {
        transition(BatchRecordStatus.ARCHIVED); markUpdated();
        publish(LimsEventTypes.BATCH_RECORD_ARCHIVED, Map.of("batchNo", batchNo));
    }
    public void addProcessRecord(String r) { processRecords.add(r); markUpdated(); }
    public void addWeighingTask(String t) { weighingTasks.add(t); markUpdated(); }
    public void addDeviation(String d) { deviations.add(d); markUpdated(); }
    public boolean isEditable() { return getStatus() == BatchRecordStatus.IN_PROGRESS; }
    public boolean isArchived() { return getStatus() == BatchRecordStatus.ARCHIVED; }

    private void publish(String eventType, Object payload) { DomainEventPublisher.publish(IDomainEvent.of(eventType, "lims", payload)); }
}
