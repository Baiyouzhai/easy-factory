package com.byz.factory.lims.model;

import com.byz.factory.lims.IWeighingTask;
import com.byz.factory.lims.WeighingTaskStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.LimsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lims_weighing_task")
public class WeighingTask extends BaseLifecycleEntity<WeighingTaskStatus> implements IWeighingTask {

    @Column(name = "formula_code", nullable = false, length = 100)
    private String formulaCode;

    @Column(name = "work_order_id", length = 100)
    private String workOrderId;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "task_id")
    private List<WeighingItem> items;

    public WeighingTask() {}

    public WeighingTask(String code, String name, String formulaCode, String workOrderId, String batchNo) {
        super(code, name, WeighingTaskStatus.PENDING);
        this.formulaCode = formulaCode;
        this.workOrderId = workOrderId;
        this.batchNo = batchNo;
        this.items = new ArrayList<>();
    }

    @Override public String getCode() { return super.getCode(); }
    @Override public String getFormulaCode() { return formulaCode; }
    @Override public String getWorkOrderId() { return workOrderId; }
    @Override public String getBatchNo() { return batchNo; }
    @Override public List<WeighingItem> getItems() { return items; }
    @Override public WeighingTaskStatus getStatus() { return super.getStatus(); }

    public void startWeighing() {
        transition(WeighingTaskStatus.WEIGHING); markUpdated();
        publish(LimsEventTypes.WEIGHING_TASK_CREATED, Map.of("taskCode", getCode(), "formulaCode", formulaCode, "workOrderId", workOrderId, "batchNo", batchNo));
    }
    public void verify() { transition(WeighingTaskStatus.VERIFIED); markUpdated(); }
    public void completeWeighing() {
        transition(WeighingTaskStatus.COMPLETE); markUpdated();
        publish(LimsEventTypes.WEIGHING_COMPLETED, Map.of("taskCode", getCode(), "formulaCode", formulaCode, "workOrderId", workOrderId, "batchNo", batchNo, "itemCount", items != null ? items.size() : 0));
    }

    public void addItem(WeighingItem item) { if (items == null) items = new ArrayList<>(); items.add(item); markUpdated(); }

    public boolean isAllItemsWeighed() { return items != null && items.stream().allMatch(i -> i.getActualQty() != null); }

    public List<WeighingItem> getDeviatedItems() {
        if (items == null) return List.of();
        return items.stream().filter(i -> i.getActualQty() != null && i.getTolerance() != null)
                .filter(i -> { var d = i.getActualQty().subtract(i.getFormulaQty()).abs().divide(i.getFormulaQty(), 4, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal.valueOf(100)); return d.compareTo(i.getTolerance()) > 0; }).toList();
    }

    private void publish(String eventType, Object payload) { DomainEventPublisher.publish(IDomainEvent.of(eventType, "lims", payload)); }
}
