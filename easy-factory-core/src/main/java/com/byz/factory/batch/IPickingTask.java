package com.byz.factory.batch;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 拣料任务抽象 — WMS 拣料任务实体的跨模块契约。
 * <p>
 * MES 通过此接口触发拣料；LIMS 通过此接口获取已拣物料批次信息用于称量。
 * 典型调用链：MES 工单下达 → WMS 创建 PickingTask → FIFO 选择批次 → 拣料 → 复核 → 送达线边仓 → LIMS 称量。
 *
 * @author 苏政
 * @see com.byz.factory.wms.model.PickingTask
 */
public interface IPickingTask {

    /** 拣料单号 */
    String getCode();

    /** 关联工单号（MES） */
    String getWorkOrderNo();

    /** 关联批号 */
    String getBatchNo();

    /** 拣料类型（FULL / STAGED / JIT） */
    PickingType getPickingType();

    /** 拣料状态 */
    PickingTaskStatus getStatus();

    /** 拣料明细 */
    List<? extends IPickingTaskItem> getItems();

    /** 拣料人 */
    String getPickedBy();

    /** 送达时间（线边仓） */
    Instant getDeliveredAt();

    /**
     * 拣料明细项 — 单行物料拣料信息。
     */
    interface IPickingTaskItem {

        /** 物料编码 */
        String getMaterialCode();

        /** 需求数量 */
        BigDecimal getRequiredQty();

        /** 实拣数量 */
        BigDecimal getPickedQty();

        /** 物料批次号（FIFO 选择） */
        String getBatchNo();

        /** 拣料库位 */
        String getLocationCode();

    }

}
