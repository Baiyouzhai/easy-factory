package com.byz.factory.equip.model;

import com.byz.data.DataExpand;
import com.byz.factory.data.Dict;
import com.byz.factory.model.IMachineModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 设备台账 — 实现 IMachineModel，作为资源分组为 Machine。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Equipment extends DataExpand implements IMachineModel {

    private String name;
    private Dict.SourceGroup group = Dict.SourceGroup.Machine;
    private Dict.SourceType type = Dict.SourceType.Machine;
    private BigDecimal number;
    private String code;
    private String model;
    private String category;
    private String location;
    private String status;
    private String assetCode;

    /** 设备状态枚举 */
    public enum Status { IDLE, RUNNING, MAINTENANCE, FAULT, OFFLINE }

    public Equipment(String code, String name, String model) {
        this.code = code;
        this.name = name;
        this.model = model;
        this.number = BigDecimal.ONE;
        this.status = "IDLE";
    }

    @Override
    public Equipment copy() {
        return new Equipment(code, name, model);
    }

    @Override
    public BigDecimal getNumber() { return number == null ? BigDecimal.ZERO : number; }

    @Override
    public Equipment setNumber(Number number) {
        this.number = number == null ? BigDecimal.ZERO : new BigDecimal(number.toString());
        return this;
    }

}
