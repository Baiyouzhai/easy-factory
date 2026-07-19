package com.byz.factory.wms.model;

import com.byz.factory.wms.IPickingTask;
import com.byz.factory.wms.PickingTaskStatus;
import com.byz.factory.wms.PickingType;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 拣料任务 — 继承 BaseLifecycleEntity 获得状态机（PENDING→IN_PROGRESS→PICKED→DELIVERED），
 * 实现 IPickingTask 供 MES/LIMS 等模块编译期引用。
 *
 * <h3>典型流程</h3>
 * <pre>
 * MES 工单下达 → WMS 创建 PickingTask(PENDING) → 开始拣料(IN_PROGRESS)
 *   → FIFO 选择批次并逐行拣料 → 拣料完成(PICKED) → 送达线边仓(DELIVERED)
 * </pre>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   wms.pickingTask     — 拣料单号
 *   wms.workOrderNo     — 关联工单号
 *   wms.batchNo         — 关联批号
 *   wms.pickingType     — 拣料类型
 *   wms.pickedBy        — 拣料人
 *   wms.deliveredAt     — 送达时间
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PickingTask extends BaseLifecycleEntity<PickingTaskStatus> implements IPickingTask {

    /** 关联工单号（MES） */
    private String workOrderNo;

    /** 关联批号 */
    private String batchNo;

    /** 拣料类型 */
    private PickingType pickingType;

    /** 拣料明细 */
    private List<PickingTaskItem> items;

    /** 拣料人 */
    private String pickedBy;

    /** 送达时间（线边仓） */
    private Instant deliveredAt;

    /**
     * @param code        拣料单号
     * @param workOrderNo 关联工单号
     * @param batchNo     关联批号
     */
    public PickingTask(String code, String workOrderNo, String batchNo) {
        super(code, "拣料-" + code, PickingTaskStatus.PENDING);
        this.workOrderNo = workOrderNo;
        this.batchNo = batchNo;
        this.pickingType = PickingType.FULL;
        this.items = new ArrayList<>();
    }

    // ==================== 状态转换便捷方法 ====================

    /** 开始拣料（PENDING → IN_PROGRESS） */
    public void start() {
        transition(PickingTaskStatus.IN_PROGRESS);
    }

    /** 拣料完成（IN_PROGRESS → PICKED） */
    public void completePicking() {
        transition(PickingTaskStatus.PICKED);
    }

    /** 送达线边仓（PICKED → DELIVERED） */
    public void deliver() {
        transition(PickingTaskStatus.DELIVERED);
        this.deliveredAt = Instant.now();
    }

    /** 取消拣料（PENDING 或 IN_PROGRESS → CANCELLED） */
    public void cancel() {
        transition(PickingTaskStatus.CANCELLED);
    }

    // ==================== 业务方法 ====================

    /**
     * 添加拣料明细行。
     *
     * @param materialCode 物料编码
     * @param requiredQty  需求数量
     * @return 创建的明细行
     */
    public PickingTaskItem addItem(String materialCode, BigDecimal requiredQty) {
        PickingTaskItem item = new PickingTaskItem(materialCode, requiredQty, BigDecimal.ZERO, null, null);
        this.items.add(item);
        markUpdated();
        return item;
    }

    /**
     * 执行拣料——指定批次和库位，记录实拣数量。
     *
     * @param materialCode 物料编码
     * @param pickedQty    实拣数量
     * @param batchNo      物料批次号（FIFO 选择）
     * @param locationCode 拣料库位
     */
    public void pickItem(String materialCode, BigDecimal pickedQty, String batchNo, String locationCode) {
        for (int i = 0; i < items.size(); i++) {
            PickingTaskItem item = items.get(i);
            if (item.getMaterialCode().equals(materialCode)) {
                items.set(i, new PickingTaskItem(item.getMaterialCode(), item.getRequiredQty(),
                        pickedQty, batchNo, locationCode));
                markUpdated();
                return;
            }
        }
    }

    /**
     * 判断所有物料是否已拣齐。
     */
    public boolean isFullyPicked() {
        return items.stream().allMatch(i -> i.getPickedQty().compareTo(i.getRequiredQty()) >= 0);
    }

    /**
     * 拣料明细 — 实现 IPickingTask.IPickingTaskItem 供跨模块引用。
     */
    @Data
    public static class PickingTaskItem implements IPickingTask.IPickingTaskItem {

        private String materialCode;
        private BigDecimal requiredQty;
        private BigDecimal pickedQty;
        private String batchNo;
        private String locationCode;

        public PickingTaskItem() {}

        public PickingTaskItem(String materialCode, BigDecimal requiredQty, BigDecimal pickedQty,
                               String batchNo, String locationCode) {
            this.materialCode = materialCode;
            this.requiredQty = requiredQty;
            this.pickedQty = pickedQty;
            this.batchNo = batchNo;
            this.locationCode = locationCode;
        }

    }

}
