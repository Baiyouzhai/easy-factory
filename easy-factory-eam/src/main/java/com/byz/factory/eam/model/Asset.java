package com.byz.factory.eam.model;

import com.byz.factory.batch.AssetStatus;
import com.byz.factory.batch.IAsset;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产 — 继承 BaseLifecycleEntity 获得状态机（IDLE→IN_USE→UNDER_MAINTENANCE→SCRAPPED），
 * 实现 IAsset 供其他模块编译期引用。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Asset extends BaseLifecycleEntity<AssetStatus> implements IAsset {

    /** 固定资产编码（业务标识，区别于继承的 code 系统标识） */
    private String assetCode;

    /** 关联 Equip 设备编码 */
    private String equipmentCode;

    /** 资产类别 */
    private String category;

    /** 购置日期 */
    private LocalDate purchaseDate;

    /** 购置成本 */
    private BigDecimal purchaseCost;

    /** 质保到期日 */
    private LocalDate warrantyExpiry;

    /** 折旧年限（年） */
    private int depreciationYears;

    /** 残值 */
    private BigDecimal residualValue;

    /** 供应商 */
    private String supplier;

    /** 存放位置 */
    private String location;

    /**
     * @param assetCode 固定资产编码
     * @param name      资产名称
     * @param category  资产类别
     */
    public Asset(String assetCode, String name, String category) {
        super(assetCode, name, AssetStatus.IDLE);
        this.assetCode = assetCode;
        this.category = category;
    }

}
