package com.byz.factory.lims.model;

import com.byz.factory.resource.IResourcePack;
import com.byz.factory.shared.BaseEntity;
import com.byz.factory.shared.HasVersion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 配方 — 继承 BaseEntity 获得 code/name/audit。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Formula extends BaseEntity implements HasVersion {

    private String productCode;
    private String version;
    private BigDecimal batchSize;
    private IResourcePack resourcePack;
    private String approvedBy;

    public Formula(String code, String name, String productCode, String version) {
        super(code, name);
        this.productCode = productCode;
        this.version = version;
    }

}
