package com.byz.factory.equip.model;

import com.byz.factory.equip.IEquipment;
import com.byz.factory.equip.MachineStatus;
import com.byz.factory.shared.AbstractResourceItem;
import com.byz.factory.shared.Dict;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备台账 — 继承 AbstractResourceItem 获得资源数量操作，实现 IEquipment。
 * <p>
 * {@link IEquipment} = {@link com.byz.factory.resource.IMachine}（资源视角）
 * + {@link com.byz.factory.lifecycle.ILifecycle}&lt;{@link MachineStatus}&gt;（状态机视角）
 * + 台账字段（型号、位置、资产编码等），供 EAM/MES/APS/Andon 跨模块引用。
 * <p>
 * 状态机（由 core 的 {@link MachineStatus} 提供）：
 * <pre>
 *   IDLE → RUNNING | SETUP | MAINTENANCE
 *   RUNNING → IDLE | FAULT
 *   SETUP → RUNNING | IDLE
 *   MAINTENANCE → IDLE
 *   FAULT → MAINTENANCE
 * </pre>
 *
 * <h3>业务便捷方法</h3>
 * <ul>
 *   <li>生产运行：{@link #startProduction()} / {@link #stopProduction()}</li>
 *   <li>换型调参：{@link #startSetup()} / {@link #completeSetup()}</li>
 *   <li>故障处理：{@link #reportFault()}</li>
 *   <li>维护保养：{@link #startMaintenance()} / {@link #completeMaintenance()}</li>
 * </ul>
 * 所有便捷方法内部调用 {@link #transition(MachineStatus)} + {@link #markUpdated()}，
 * 外部调用方不应直接使用 {@code setStatus()}。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   equip.status              — 设备运行状态
 *   equip.oee                 — 当前 OEE 值
 *   equip.lastMaintenanceDate — 上次保养日期
 *   equip.nextMaintenanceDate — 下次计划保养日期
 *   equip.location            — 所在工位
 *   equip.parameters          — 工艺参数设定值(JSON)
 *   equip.model               — 设备型号
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "equip_equipment")
public class Equipment extends AbstractResourceItem implements IEquipment {

    /** 设备运行状态（由 MachineStatus 状态机校验） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MachineStatus status = MachineStatus.IDLE;

    @Column(length = 100)
    private String model;

    @Column(length = 50)
    private String category;

    @Column(length = 100)
    private String location;

    @Column(length = 50)
    private String assetCode;

    /** 规格参数 */
    @Column(length = 500)
    private String specifications;

    /** 供应商 */
    @Column(length = 100)
    private String supplier;

    /** 购置日期 */
    private LocalDate purchaseDate;

    /**
     * @param code  设备编码
     * @param name  设备名称
     * @param model 设备型号
     */
    public Equipment(String code, String name, String model) {
        super(code, name, Dict.SourceGroup.Machine, Dict.SourceType.Machine, BigDecimal.ONE);
        this.model = model;
    }

    // ==================== 业务便捷方法 ====================

    /** 开始生产 — IDLE 或 SETUP → RUNNING */
    public void startProduction() {
        transition(MachineStatus.RUNNING);
        markUpdated();
    }

    /** 停止生产 — RUNNING → IDLE */
    public void stopProduction() {
        transition(MachineStatus.IDLE);
        markUpdated();
    }

    /** 开始换型/调参 — IDLE → SETUP */
    public void startSetup() {
        transition(MachineStatus.SETUP);
        markUpdated();
    }

    /** 完成换型/调参 — SETUP → RUNNING */
    public void completeSetup() {
        transition(MachineStatus.RUNNING);
        markUpdated();
    }

    /** 报告故障 — RUNNING → FAULT */
    public void reportFault() {
        transition(MachineStatus.FAULT);
        markUpdated();
    }

    /** 开始维护 — IDLE 或 FAULT → MAINTENANCE */
    public void startMaintenance() {
        transition(MachineStatus.MAINTENANCE);
        markUpdated();
    }

    /** 完成维护 — MAINTENANCE → IDLE */
    public void completeMaintenance() {
        transition(MachineStatus.IDLE);
        markUpdated();
    }

}
