package com.byz.factory.lims.model;

import com.byz.data.DataExpand;
import com.byz.factory.model.IResourcePack;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 配方 — 关联 IResourcePack 作为配方组分。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Formula extends DataExpand {

    private String code;
    private String name;
    private String productCode;
    private String version;
    private BigDecimal batchSize;
    private IResourcePack resourcePack;
    private String status;
    private String approvedBy;

    public Formula(String code, String name, String productCode, String version) {
        this.code = code;
        this.name = name;
        this.productCode = productCode;
        this.version = version;
        this.status = "DRAFT";
    }

}
