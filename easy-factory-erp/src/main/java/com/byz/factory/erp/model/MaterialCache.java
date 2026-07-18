package com.byz.factory.erp.model;

import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * ERP 物料主数据本地缓存 — 继承 BaseEntity 获得 code/name/audit。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialCache extends BaseEntity {

    private String description;
    private String unit;
    private String materialType;
    private boolean batchManaged;
    private int shelfLifeDays;
    private String ghsClass;
    private Instant lastSyncTime;
    private String sourceSystem;

    public MaterialCache(String materialCode, String description, String unit) {
        super(materialCode, description);
        this.description = description;
        this.unit = unit;
    }

}
