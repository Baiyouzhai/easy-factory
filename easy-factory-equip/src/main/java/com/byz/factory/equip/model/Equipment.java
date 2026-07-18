package com.byz.factory.equip.model;

import com.byz.factory.batch.MachineStatus;
import com.byz.factory.lifecycle.ILifecycle;
import com.byz.factory.resource.IMachine;
import com.byz.factory.shared.AbstractResourceItem;
import com.byz.factory.shared.Dict;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备台账 — 继承 AbstractResourceItem 获得资源数量操作，实现 IMachine + ILifecycle&lt;MachineStatus&gt;。
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
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Equipment extends AbstractResourceItem implements IMachine, ILifecycle<MachineStatus> {

    /** 设备运行状态（由 MachineStatus 状态机校验） */
    private MachineStatus status = MachineStatus.IDLE;

    private String model;
    private String category;
    private String location;
    private String assetCode;

    /** 规格参数 */
    private String specifications;

    /** 供应商 */
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

}
