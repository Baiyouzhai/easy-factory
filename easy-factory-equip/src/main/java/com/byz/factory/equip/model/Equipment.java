package com.byz.factory.equip.model;

import com.byz.factory.resource.IMachine;
import com.byz.factory.shared.AbstractResourceItem;
import com.byz.factory.shared.Dict;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 设备台账 — 继承 AbstractResourceItem 获得资源数量操作，实现 IMachine。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Equipment extends AbstractResourceItem implements IMachine {

    private String model;
    private String category;
    private String location;
    private String assetCode;

    /** 设备运行状态 */
    public enum Status { IDLE, RUNNING, MAINTENANCE, FAULT, OFFLINE }

    public Equipment(String code, String name, String model) {
        super(code, name, Dict.SourceGroup.Machine, Dict.SourceType.Machine, BigDecimal.ONE);
        this.model = model;
    }

}
