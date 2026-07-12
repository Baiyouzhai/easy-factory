package com.byz.factory.erp.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * ERP 物料主数据本地缓存 — 定时从 ERP 同步。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialCache extends DataExpand {

    private String materialCode;
    private String description;
    private String unit;
    private String materialType;        // ROH/HALB/FERT
    private boolean batchManaged;
    private int shelfLifeDays;
    private String ghsClass;
    private Instant lastSyncTime;
    private String sourceSystem;

    public MaterialCache(String materialCode, String description, String unit) {
        this.materialCode = materialCode;
        this.description = description;
        this.unit = unit;
    }

}
