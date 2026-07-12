package com.byz.factory.model;

import com.byz.factory.design.IBlueprint;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 工单 — MES 模块的核心实体，代表一个生产任务。
 * <p>
 * 工单关联产品蓝图、批次和工厂，驱动工序流转。
 *
 * @author 苏政
 * @see IBatch
 * @see ILifecycle
 */
public interface IWorkOrder {

    /** 工单号 */
    String getWorkOrderNo();

    /** 产品编码 */
    String getProductCode();

    /** 产品蓝图（冻结的工艺版本） */
    IBlueprint getBlueprint();

    /** 批量 */
    BigDecimal getQuantity();

    /** 关联批号 */
    String getBatchNo();

    /** 执行工厂 */
    String getFactoryCode();

    /** 工单状态 */
    String getStatus();

    /** 计划开始时间 */
    Instant getPlannedStart();

    /** 计划结束时间 */
    Instant getPlannedEnd();

    /** 实际开始时间 */
    Instant getActualStart();

    /** 实际结束时间 */
    Instant getActualEnd();

    /** 创建时间 */
    Instant getCreatedAt();

}
