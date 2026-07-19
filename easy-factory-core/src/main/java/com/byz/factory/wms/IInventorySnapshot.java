package com.byz.factory.wms;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 库存快照抽象 — WMS 库存快照实体的跨模块契约。
 * <p>
 * ERP 通过此接口获取库存数据用于财务过账；MES 通过此接口查询物料可用性。
 * 可用库存 = 在手库存 − 已分配库存，由实现类动态计算。
 *
 * @author 苏政
 * @see com.byz.factory.wms.model.InventorySnapshot
 */
public interface IInventorySnapshot {

    /** 物料编码 */
    String getMaterialCode();

    /** 批次号 */
    String getBatchNo();

    /** 库位编码 */
    String getLocationCode();

    /** 在手库存 */
    BigDecimal getOnHandQty();

    /** 已分配（被工单预留） */
    BigDecimal getAllocatedQty();

    /** 可用库存 = 在手 − 已分配 */
    BigDecimal getAvailableQty();

    /** 待检库存 */
    BigDecimal getQuarantineQty();

    /** 不合格库存 */
    BigDecimal getRejectedQty();

    /** 有效期至 */
    Instant getExpiryDate();

    /** 最后盘点时间 */
    Instant getLastCounted();

    /** 物料质量状态 */
    MaterialStatus getStatus();

}
