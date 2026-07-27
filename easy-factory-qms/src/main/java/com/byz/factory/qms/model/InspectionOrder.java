package com.byz.factory.qms.model;

import com.byz.factory.batch.IInspectionOrder;
import com.byz.factory.batch.InspectionStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.QmsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "qms_inspection_order")
public class InspectionOrder extends BaseLifecycleEntity<InspectionStatus> implements IInspectionOrder {

    @Column(name = "inspection_no", nullable = false, unique = true, length = 100)
    private String inspectionNo;

    @Column(name = "work_order_no", length = 100)
    private String workOrderNo;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "process_code", nullable = false, length = 100)
    private String processCode;

    @Column(name = "inspection_type", length = 20)
    private String inspectionType;

    @Column(name = "plan_code", length = 100)
    private String planCode;

    @Column(length = 100)
    private String inspector;

    @Column(name = "total_items")
    private int totalItems;

    @Column(name = "completed_items")
    private int completedItems;

    @Column(name = "passed_items")
    private int passedItems;

    @Transient
    private List<InspectionRecord> records;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public InspectionOrder() {}

    public InspectionOrder(String inspectionNo, String batchNo, String processCode) {
        super(inspectionNo, "检验指令-" + inspectionNo, InspectionStatus.PENDING);
        this.inspectionNo = inspectionNo;
        this.batchNo = batchNo;
        this.processCode = processCode;
        this.records = new ArrayList<>();
    }

    @Override public String getInspectionNo() { return inspectionNo; }
    @Override public String getWorkOrderNo() { return workOrderNo; }
    @Override public String getBatchNo() { return batchNo; }
    @Override public String getProcessCode() { return processCode; }
    @Override public String getInspectionType() { return inspectionType; }
    @Override public String getPlanCode() { return planCode; }
    @Override public InspectionStatus getStatus() { return super.getStatus(); }
    @Override public int getTotalItems() { return totalItems; }
    @Override public int getCompletedItems() { return completedItems; }
    @Override public int getPassedItems() { return passedItems; }

    // ==================== 业务便捷方法 ====================

    public void startInspection(String inspector) {
        transition(InspectionStatus.IN_PROGRESS);
        this.inspector = inspector;
        this.startedAt = Instant.now();
        markUpdated();
        publishEvent(QmsEventTypes.INSPECTION_STARTED,
                "inspectionNo", inspectionNo, "inspector", inspector,
                "startedAt", startedAt.toString());
    }

    public void submitResult(InspectionRecord record) {
        if (this.records == null) this.records = new ArrayList<>();
        record.setInspectionNo(this.inspectionNo);
        this.records.add(record);
        this.completedItems++;
        if ("PASS".equals(record.getJudgement())) this.passedItems++;
        markUpdated();
    }

    public void completeInspection() {
        if (passedItems == totalItems && completedItems == totalItems) {
            transition(InspectionStatus.PASSED);
            this.completedAt = Instant.now();
            markUpdated();
            publishEvent(QmsEventTypes.INSPECTION_PASSED,
                    "inspectionNo", inspectionNo, "processCode", processCode,
                    "workOrderNo", workOrderNo, "totalItems", totalItems, "passedItems", passedItems);
        } else {
            transition(InspectionStatus.FAILED);
            this.completedAt = Instant.now();
            markUpdated();
            publishEvent(QmsEventTypes.INSPECTION_FAILED,
                    "inspectionNo", inspectionNo, "processCode", processCode,
                    "workOrderNo", workOrderNo, "totalItems", totalItems,
                    "passedItems", passedItems, "failedItems", totalItems - passedItems);
        }
        publishEvent(QmsEventTypes.INSPECTION_COMPLETED,
                "inspectionNo", inspectionNo, "status", getStatus().name(),
                "totalItems", totalItems, "passedItems", passedItems);
    }

    public void close() { transition(InspectionStatus.CLOSED); markUpdated(); }

    public boolean isFinished() {
        InspectionStatus s = getStatus();
        return s == InspectionStatus.PASSED || s == InspectionStatus.FAILED || s == InspectionStatus.CLOSED;
    }

    public boolean isPassed() {
        return getStatus() == InspectionStatus.PASSED || getStatus() == InspectionStatus.CLOSED;
    }

    public int getFailedItems() { return totalItems - passedItems; }

    private void publishEvent(String eventType, Object... kvPairs) {
        Map<String, Object> payload = new HashMap<>();
        for (int i = 0; i < kvPairs.length; i += 2) payload.put((String) kvPairs[i], kvPairs[i + 1]);
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "qms", payload));
    }
}
