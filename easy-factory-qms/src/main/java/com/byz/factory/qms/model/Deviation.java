package com.byz.factory.qms.model;

import com.byz.factory.batch.*;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.QmsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "qms_deviation")
public class Deviation extends BaseLifecycleEntity<DeviationStatus> implements IDeviation {

    @Column(nullable = false, length = 100)
    private String source;

    @Column(name = "inspection_no", length = 100)
    private String inspectionNo;

    @Column(name = "work_order_no", length = 100)
    private String workOrderNo;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "process_code", length = 100)
    private String processCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviationSeverity severity;

    @Column(name = "product_impact", length = 1000)
    private String productImpact;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeviationDisposition disposition;

    @Column(name = "disposition_note", length = 1000)
    private String dispositionNote;

    @Column(name = "capa_code", length = 100)
    private String capaCode;

    @Column(length = 100)
    private String investigator;

    @Column(name = "disposed_by", length = 100)
    private String disposedBy;

    @Column(name = "root_cause", length = 2000)
    private String rootCause;

    @Column(name = "closed_at")
    private Instant closedAt;

    public Deviation() {}

    public Deviation(String code, String name, String source,
                     String inspectionNo, DeviationSeverity severity) {
        super(code, name, DeviationStatus.OPEN);
        this.source = source;
        this.inspectionNo = inspectionNo;
        this.severity = severity;
    }

    @Override public String getSource() { return source; }
    @Override public String getInspectionNo() { return inspectionNo; }
    @Override public String getWorkOrderNo() { return workOrderNo; }
    @Override public String getBatchNo() { return batchNo; }
    @Override public String getProcessCode() { return processCode; }
    @Override public DeviationSeverity getSeverity() { return severity; }
    @Override public DeviationStatus getStatus() { return super.getStatus(); }
    @Override public String getProductImpact() { return productImpact; }
    @Override public DeviationDisposition getDisposition() { return disposition; }
    @Override public String getDispositionNote() { return dispositionNote; }
    @Override public String getCapaCode() { return capaCode; }
    @Override public String getInvestigator() { return investigator; }
    @Override public String getDisposedBy() { return disposedBy; }
    @Override public Instant getClosedAt() { return closedAt; }

    // ==================== 业务便捷方法 ====================

    public void startInvestigation(String investigator) {
        transition(DeviationStatus.INVESTIGATING);
        this.investigator = investigator; markUpdated();
        publish(QmsEventTypes.DEVIATION_CREATED, Map.of(
                "deviationCode", getCode(), "inspectionNo", inspectionNo,
                "severity", severity.name(), "source", source));
    }

    public void completeInvestigation(String rootCause, String productImpact) {
        this.rootCause = rootCause; this.productImpact = productImpact; markUpdated();
        publish(QmsEventTypes.DEVIATION_INVESTIGATED, Map.of(
                "deviationCode", getCode(), "rootCause", rootCause, "productImpact", productImpact));
    }

    public void dispose(DeviationDisposition disposition, String dispositionNote, String disposedBy) {
        transition(DeviationStatus.DISPOSITIONED);
        this.disposition = disposition; this.dispositionNote = dispositionNote;
        this.disposedBy = disposedBy; markUpdated();
        publish(QmsEventTypes.DEVIATION_DISPOSITIONED, Map.of(
                "deviationCode", getCode(), "disposition", disposition.name(), "disposedBy", disposedBy));
    }

    public void linkCapa(String capaCode) { this.capaCode = capaCode; markUpdated(); }

    public void resolve() {
        transition(DeviationStatus.RESOLVED); markUpdated();
        publish(QmsEventTypes.DEVIATION_RESOLVED, Map.of(
                "deviationCode", getCode(), "resolvedAt", Instant.now().toString()));
    }

    public void close() {
        transition(DeviationStatus.CLOSED); this.closedAt = Instant.now(); markUpdated();
        publish(QmsEventTypes.DEVIATION_CLOSED, Map.of(
                "deviationCode", getCode(), "closedAt", closedAt.toString()));
    }

    public void cancel(String reason) {
        transition(DeviationStatus.CANCELLED); markUpdated();
        setExpandProperty("qms.deviation.cancelReason", reason);
    }

    public boolean requiresCapa() {
        if (severity == DeviationSeverity.CRITICAL) return true;
        return severity == DeviationSeverity.MAJOR && disposition != DeviationDisposition.CONCESSION;
    }

    public boolean isEditable() { return getStatus() == DeviationStatus.OPEN; }

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "qms", payload));
    }
}
