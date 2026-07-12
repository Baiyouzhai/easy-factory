package com.byz.factory.eam.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class Asset extends DataExpand {
    private String assetCode;
    private String equipmentCode;    // 关联 Equip 设备
    private String name;
    private String category;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private LocalDate warrantyExpiry;
    private int depreciationYears;
    private BigDecimal residualValue;
    private String supplier;
    private String location;
    private String status;           // IN_USE/IDLE/MAINTENANCE/SCRAPPED

    public Asset(String assetCode, String name, String category) {
        this.assetCode = assetCode; this.name = name; this.category = category;
        this.status = "IDLE";
    }
}
