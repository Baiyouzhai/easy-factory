package com.byz.factory.wms.model;

import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库位 — 继承 BaseEntity 获得 code/name/audit。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Storage extends BaseEntity {

    private String warehouse;
    private String zone;
    private String storageType;
    private double capacity;

    public Storage(String locationCode, String warehouse, String zone) {
        super(locationCode, "库位-" + locationCode);
        this.warehouse = warehouse;
        this.zone = zone;
    }

}
