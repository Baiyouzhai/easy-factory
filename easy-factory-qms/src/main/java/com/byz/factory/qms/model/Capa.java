package com.byz.factory.qms.model;

import com.byz.factory.batch.CapaStatus;
import com.byz.factory.batch.ICapa;
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
@Table(name = "qms_capa")
public class Capa extends BaseLifecycleEntity<CapaStatus> implements ICapa {

    @Column(name = "deviation_code", nullable = false, length = 100)
    private String deviationCode;

    @Column(name = "problem_description", nullable = false, length = 2000)
    private String problemDescription;

    @Column(name = "root_cause", length = 2000)
    private String rootCause;

    @Column(name = "corrective_action", length = 2000)
    private String correctiveAction;

    @Column(name = "preventive_action", length = 2000)
    private String preventiveAction;

    @Column(length = 2000)
    private String verification;

    @Column(name = "assign_to", nullable = false, length = 100)
    private String assignTo;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "closed_at")
    private Instant closedAt;

    public Capa() {}

    public Capa(String code, String name, String deviationCode,
                String problemDescription, String assignTo) {
        super(code, name, CapaStatus.OPEN);
        this.deviationCode = deviationCode;
        this.problemDescription = problemDescription;
        this.assignTo = assignTo;
    }

    @Override public String getDeviationCode() { return deviationCode; }
    @Override public String getProblemDescription() { return problemDescription; }
    @Override public String getRootCause() { return rootCause; }
    @Override public String getCorrectiveAction() { return correctiveAction; }
    @Override public String getPreventiveAction() { return preventiveAction; }
    @Override public String getVerification() { return verification; }
    @Override public CapaStatus getStatus() { return super.getStatus(); }
    @Override public String getAssignTo() { return assignTo; }
    @Override public String getApprovedBy() { return approvedBy; }
    @Override public Instant getDueDate() { return dueDate; }
    @Override public Instant getClosedAt() { return closedAt; }

    // ==================== 业务便捷方法 ====================

    public void analyzeRootCause(String rootCause) {
        transition(CapaStatus.ROOT_CAUSE); this.rootCause = rootCause; markUpdated();
        publish(QmsEventTypes.CAPA_ROOT_CAUSE_DONE, Map.of(
                "capaCode", getCode(), "rootCause", rootCause));
    }

    public void executeActions(String correctiveAction, String preventiveAction) {
        transition(CapaStatus.IN_PROGRESS);
        this.correctiveAction = correctiveAction; this.preventiveAction = preventiveAction;
        markUpdated();
        publish(QmsEventTypes.CAPA_IN_PROGRESS, Map.of(
                "capaCode", getCode(), "correctiveAction", correctiveAction,
                "preventiveAction", preventiveAction));
    }

    public void verify(String verification, String approvedBy) {
        transition(CapaStatus.VERIFIED);
        this.verification = verification; this.approvedBy = approvedBy; markUpdated();
        publish(QmsEventTypes.CAPA_VERIFIED, Map.of(
                "capaCode", getCode(), "verification", verification, "approvedBy", approvedBy));
    }

    public void close() {
        transition(CapaStatus.CLOSED); this.closedAt = Instant.now(); markUpdated();
        publish(QmsEventTypes.CAPA_CLOSED, Map.of("capaCode", getCode(), "closedAt", closedAt.toString()));
    }

    public void cancel(String reason) {
        transition(CapaStatus.CANCELLED); markUpdated();
        setExpandProperty("qms.capa.cancelReason", reason);
    }

    public boolean isOverdue() {
        if (dueDate == null) return false;
        CapaStatus s = getStatus();
        return !(s == CapaStatus.CLOSED || s == CapaStatus.CANCELLED) && Instant.now().isAfter(dueDate);
    }

    public boolean isEditable() {
        CapaStatus s = getStatus();
        return s == CapaStatus.OPEN || s == CapaStatus.ROOT_CAUSE;
    }

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "qms", payload));
    }
}
