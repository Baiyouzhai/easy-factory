package com.byz.factory.eam.model;

import com.byz.factory.eam.IMaintenanceOrder;
import com.byz.factory.eam.MaintenanceOrderStatus;
import com.byz.factory.eam.MaintenancePriority;
import com.byz.factory.eam.MaintenanceType;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 维护工单 — 继承 BaseLifecycleEntity 获得状态机（OPEN→IN_PROGRESS→COMPLETED→VERIFIED），
 * 实现 IMaintenanceOrder 供 Andon 等模块编译期引用。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaintenanceOrder extends BaseLifecycleEntity<MaintenanceOrderStatus> implements IMaintenanceOrder {

    /** 关联资产编码 */
    private String assetCode;

    /** 维护类型 */
    private MaintenanceType type;

    /** 优先级 */
    private MaintenancePriority priority;

    /** 故障/维护描述 */
    private String description;

    /** 计划开始时间 */
    private Instant plannedStart;

    /** 计划结束时间 */
    private Instant plannedEnd;

    /** 实际开始时间 */
    private Instant actualStart;

    /** 实际结束时间 */
    private Instant actualEnd;

    /** 停机时长（分钟） */
    private double downtime;

    /** 维护成本（人工 + 备件） */
    private double cost;

    /** 维修人 */
    private String technician;

    /** 更换备件清单（备件编码列表） */
    private List<String> spareParts;

    /**
     * @param code      维护工单号
     * @param assetCode 关联资产编码
     * @param type      维护类型
     */
    public MaintenanceOrder(String code, String assetCode, MaintenanceType type) {
        super(code, "Maint-" + code, MaintenanceOrderStatus.OPEN);
        this.assetCode = assetCode;
        this.type = type;
        this.priority = MaintenancePriority.MEDIUM;
        this.spareParts = new ArrayList<>();
    }

}
