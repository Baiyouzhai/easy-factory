package com.byz.factory.batch;

import java.time.Instant;
import java.util.List;

/**
 * 维护工单 — EAM 模块的核心实体接口。
 * <p>
 * Andon 模块触发设备故障时创建维护工单，通过此接口引用。
 *
 * @author 苏政
 * @see MaintenanceOrderStatus
 * @see MaintenanceType
 * @see MaintenancePriority
 */
public interface IMaintenanceOrder {

    /** 维护工单号 */
    String getCode();

    /** 关联资产编码 */
    String getAssetCode();

    /** 维护类型 */
    MaintenanceType getType();

    /** 优先级 */
    MaintenancePriority getPriority();

    /** 故障/维护描述 */
    String getDescription();

    /** 工单状态 */
    MaintenanceOrderStatus getStatus();

    /** 计划开始时间 */
    Instant getPlannedStart();

    /** 计划结束时间 */
    Instant getPlannedEnd();

    /** 实际开始时间 */
    Instant getActualStart();

    /** 实际结束时间 */
    Instant getActualEnd();

    /** 停机时长（分钟） */
    double getDowntime();

    /** 维护成本（人工 + 备件） */
    double getCost();

    /** 维修人 */
    String getTechnician();

    /** 更换备件清单（备件编码列表） */
    List<String> getSpareParts();

}
