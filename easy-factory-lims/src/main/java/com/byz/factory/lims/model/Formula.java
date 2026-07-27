package com.byz.factory.lims.model;

import com.byz.factory.lims.FormulaStatus;
import com.byz.factory.lims.IFormula;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.LimsEventTypes;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.shared.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lims_formula")
public class Formula extends BaseLifecycleEntity<FormulaStatus> implements IFormula, HasVersion {

    @Column(name = "product_code", nullable = false, length = 100)
    private String productCode;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(name = "batch_size", precision = 20, scale = 6)
    private BigDecimal batchSize;

    @Transient
    private IResourcePack resourcePack;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(length = 50)
    private String yield;

    @Transient
    @CollectionTable(name = "lims_formula_phase", joinColumns = @JoinColumn(name = "formula_id"))
    private List<FormulaPhase> phases = new ArrayList<>();

    @Transient
    private IVersionStrategy versionStrategy = new SemanticVersionStrategy(BumpType.MINOR);

    public Formula() {}

    public Formula(String code, String name, String productCode) {
        super(code, name, FormulaStatus.DRAFT);
        this.productCode = productCode;
        this.versionStrategy = new SemanticVersionStrategy(BumpType.MINOR);
        this.version = versionStrategy.initialVersion();
    }

    @Override public String getCode() { return super.getCode(); }
    @Override public String getName() { return super.getName(); }
    @Override public String getProductCode() { return productCode; }
    @Override public String getVersion() { return version; }
    @Override public BigDecimal getBatchSize() { return batchSize; }
    @Override public FormulaStatus getStatus() { return super.getStatus(); }
    @Override public String getApprovedBy() { return approvedBy; }
    @Override public String getYield() { return yield; }
    @Override public List<FormulaPhase> getPhases() { return phases; }

    public String bumpVersion(BumpType bumpType) {
        this.version = new SemanticVersionStrategy(bumpType).nextVersion(this.version);
        markUpdated();
        return this.version;
    }

    public Formula createNewVersion(String newVersion) {
        Formula next = new Formula(this.getCode(), this.getName(), this.productCode);
        next.setVersion(newVersion);
        next.setBatchSize(this.batchSize);
        next.setYield(this.yield);
        next.setResourcePack(this.resourcePack != null ? this.resourcePack.copy() : null);
        List<FormulaPhase> copied = new ArrayList<>();
        for (FormulaPhase p : this.phases) copied.add(new FormulaPhase(p.getPhaseNo(), p.getPhaseName()));
        next.setPhases(copied);
        next.setExpandProperty("lims.previousVersion", this.version);
        return next;
    }

    public void submitForApproval() {
        transition(FormulaStatus.APPROVED); markUpdated();
        publish(LimsEventTypes.FORMULA_CREATED, Map.of("formulaCode", getCode(), "productCode", productCode, "version", version));
    }
    public void approve(String approvedBy) {
        this.approvedBy = approvedBy; markUpdated();
        publish(LimsEventTypes.FORMULA_APPROVED, Map.of("formulaCode", getCode(), "version", version, "approvedBy", approvedBy));
    }
    public void activate() {
        transition(FormulaStatus.ACTIVE); markUpdated();
        publish(LimsEventTypes.FORMULA_ACTIVATED, Map.of("formulaCode", getCode(), "version", version));
    }
    public void reject(String reason) { transition(FormulaStatus.DRAFT); markUpdated(); setExpandProperty("lims.rejectionReason", reason); }
    public void retire(String reason) {
        transition(FormulaStatus.RETIRED); markUpdated(); setExpandProperty("lims.retireReason", reason);
        publish(LimsEventTypes.FORMULA_RETIRED, Map.of("formulaCode", getCode(), "version", version, "reason", reason));
    }
    public void addPhase(FormulaPhase phase) { this.phases.add(phase); markUpdated(); }
    public boolean isEditable() { return getStatus() == FormulaStatus.DRAFT; }
    public boolean isActive() { return getStatus() == FormulaStatus.ACTIVE; }

    private void publish(String eventType, Object payload) { DomainEventPublisher.publish(IDomainEvent.of(eventType, "lims", payload)); }

    @Data
    @Embeddable
    public static class FormulaPhase implements IFormula.IFormulaPhase {
        @Column(name = "phase_no", nullable = false)
        private int phaseNo;
        @Column(name = "phase_name", nullable = false, length = 100)
        private String phaseName;
        @Transient
        private List<String> actions = new ArrayList<>();
        @Column(length = 1000)
        private String conditions;
        public FormulaPhase() {}
        public FormulaPhase(int phaseNo, String phaseName) { this.phaseNo = phaseNo; this.phaseName = phaseName; }
        @Override public int getPhaseNo() { return phaseNo; }
        @Override public String getPhaseName() { return phaseName; }
    }
}
