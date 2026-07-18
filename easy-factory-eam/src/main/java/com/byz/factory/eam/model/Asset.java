package com.byz.factory.eam.model;

import com.byz.factory.batch.AssetStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产 — 继承 BaseLifecycleEntity 获得状态机（IDLE→IN_USE→UNDER_MAINTENANCE→SCRAPPED）。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Asset extends BaseLifecycleEntity<AssetStatus> {

    private String equipmentCode;
    private String category;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private LocalDate warrantyExpiry;
    private int depreciationYears;
    private BigDecimal residualValue;
    private String supplier;
    private String location;

    public Asset(String assetCode, String name, String category) {
        super(assetCode, name, AssetStatus.IDLE);
        this.category = category;
    }

}
