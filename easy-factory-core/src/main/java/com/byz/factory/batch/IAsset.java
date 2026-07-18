package com.byz.factory.batch;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产 — EAM 模块的核心实体接口，代表企业固定资产。
 * <p>
 * 其他模块（Equip/Andon/ERP/DMS）可通过此接口编译期引用资产，
 * 无需依赖 easy-factory-eam。
 * <p>
 * EAM 关注"资产本身"（价值、维护、折旧），
 * 而 Equip 关注"设备作为生产资源"（工艺参数、OEE）。
 *
 * @author 苏政
 * @see AssetStatus
 */
public interface IAsset {

    /** 固定资产编码（业务标识） */
    String getAssetCode();

    /** 关联 Equip 设备编码 */
    String getEquipmentCode();

    /** 资产名称 */
    String getName();

    /** 资产类别 */
    String getCategory();

    /** 购置日期 */
    LocalDate getPurchaseDate();

    /** 购置成本 */
    BigDecimal getPurchaseCost();

    /** 质保到期日 */
    LocalDate getWarrantyExpiry();

    /** 折旧年限（年） */
    int getDepreciationYears();

    /** 残值 */
    BigDecimal getResidualValue();

    /** 供应商 */
    String getSupplier();

    /** 存放位置 */
    String getLocation();

    /** 资产状态（管理视角） */
    AssetStatus getStatus();

}
