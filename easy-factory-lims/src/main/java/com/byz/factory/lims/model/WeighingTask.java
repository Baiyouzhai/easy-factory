package com.byz.factory.lims.model;

import com.byz.factory.batch.IWeighingTask;
import com.byz.factory.batch.WeighingTaskStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.LimsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 称量任务 — 关联工单和配方，记录各物料称量明细。
 * <p>
 * 继承 BaseLifecycleEntity 获得状态机（PENDING→WEIGHING→VERIFIED→COMPLETE），
 * 实现 IWeighingTask 供 MES/IoT/QMS 等模块编译期引用。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   lims.weighing.formulaCode  — 配方编码
 *   lims.weighing.workOrderId  — 工单号
 *   lims.weighing.batchNo      — 批号
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WeighingTask extends BaseLifecycleEntity<WeighingTaskStatus> implements IWeighingTask {

    /** 关联配方编码 */
    private String formulaCode;

    /** 关联工单号 */
    private String workOrderId;

    /** 批号 */
    private String batchNo;

    /** 称量项目列表 */
    private List<WeighingItem> items;

    /**
     * @param code        称量任务号
     * @param name        任务名称
     * @param formulaCode 配方编码
     * @param workOrderId 工单号
     * @param batchNo     批号
     */
    public WeighingTask(String code, String name, String formulaCode, String workOrderId, String batchNo) {
        super(code, name, WeighingTaskStatus.PENDING);
        this.formulaCode = formulaCode;
        this.workOrderId = workOrderId;
        this.batchNo = batchNo;
        this.items = new ArrayList<>();
    }

    // ==================== 业务便捷方法（含事件发布） ====================

    /**
     * 开始称量（PENDING → WEIGHING）。
     */
    public void startWeighing() {
        transition(WeighingTaskStatus.WEIGHING);
        markUpdated();
        publishEvent(LimsEventTypes.WEIGHING_TASK_CREATED, Map.of(
                "taskCode", getCode(),
                "formulaCode", formulaCode,
                "workOrderId", workOrderId,
                "batchNo", batchNo));
    }

    /**
     * 复核通过（WEIGHING → VERIFIED）。
     */
    public void verify() {
        transition(WeighingTaskStatus.VERIFIED);
        markUpdated();
    }

    /**
     * 称量完成（VERIFIED → COMPLETE）。
     */
    public void completeWeighing() {
        transition(WeighingTaskStatus.COMPLETE);
        markUpdated();
        publishEvent(LimsEventTypes.WEIGHING_COMPLETED, Map.of(
                "taskCode", getCode(),
                "formulaCode", formulaCode,
                "workOrderId", workOrderId,
                "batchNo", batchNo,
                "itemCount", items != null ? items.size() : 0));
    }

    /**
     * 添加称量项目。
     *
     * @param item 称量项目
     */
    public void addItem(WeighingItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        markUpdated();
    }

    // ==================== 查询方法 ====================

    /**
     * 检查是否所有项目均已完成称量。
     */
    public boolean isAllItemsWeighed() {
        return items != null && items.stream().allMatch(i -> i.getActualQty() != null);
    }

    /**
     * 获取存在偏差的项目列表。
     */
    public List<WeighingItem> getDeviatedItems() {
        if (items == null) return List.of();
        return items.stream()
                .filter(i -> i.getActualQty() != null && i.getTolerance() != null)
                .filter(i -> {
                    var deviation = i.getActualQty().subtract(i.getFormulaQty()).abs()
                            .divide(i.getFormulaQty(), 4, java.math.RoundingMode.HALF_UP)
                            .multiply(java.math.BigDecimal.valueOf(100));
                    return deviation.compareTo(i.getTolerance()) > 0;
                })
                .toList();
    }

    // ==================== 内部 ====================

    private void publishEvent(String eventType, Object payload) {
        IDomainEvent event = IDomainEvent.of(eventType, "lims", payload);
        DomainEventPublisher.publish(event);
    }

}
