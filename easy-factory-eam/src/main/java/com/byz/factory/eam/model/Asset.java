package com.byz.factory.eam.model;

import com.byz.factory.eam.AssetStatus;
import com.byz.factory.eam.IAsset;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产 — 继承 BaseLifecycleEntity 获得状态机（IDLE→IN_USE→UNDER_MAINTENANCE→SCRAPPED），
 * 实现 IAsset 供其他模块编译期引用。
 *
 * <h3>状态机</h3>
 * <pre>
 *   IDLE → IN_USE | UNDER_MAINTENANCE | SCRAPPED
 *   IN_USE → IDLE | UNDER_MAINTENANCE | SCRAPPED
 *   UNDER_MAINTENANCE → IDLE | SCRAPPED
 *   SCRAPPED → (终态)
 * </pre>
 *
 * <h3>业务便捷方法</h3>
 * <ul>
 *   <li>投产/停用：{@link #startUse()} / {@link #stopUse()}</li>
 *   <li>维护：{@link #startMaintenance()} / {@link #completeMaintenance()}</li>
 *   <li>报废：{@link #scrap()}</li>
 * </ul>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   eam.assetCode        — 固定资产编码
 *   eam.purchaseDate     — 购置日期
 *   eam.warrantyExpiry   — 质保到期日
 *   eam.depreciationYears— 折旧年限
 *   eam.residualValue    — 残值
 *   eam.supplier         — 供应商
 *   eam.maintenancePlan  — 维护计划(周期/上次/下次)
 *   eam.calibrationDue   — 下次校准日期
 * </pre>
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

    // ==================== 业务便捷方法 ====================

    /** 投产 — IDLE → IN_USE */
    public void startUse() {
        transition(AssetStatus.IN_USE);
        markUpdated();
    }

    /** 停用 — IN_USE → IDLE */
    public void stopUse() {
        transition(AssetStatus.IDLE);
        markUpdated();
    }

    /** 送修 — IDLE 或 IN_USE → UNDER_MAINTENANCE */
    public void startMaintenance() {
        transition(AssetStatus.UNDER_MAINTENANCE);
        markUpdated();
    }

    /** 修复完成 — UNDER_MAINTENANCE → IDLE */
    public void completeMaintenance() {
        transition(AssetStatus.IDLE);
        markUpdated();
    }

    /** 报废 — IDLE|IN_USE|UNDER_MAINTENANCE → SCRAPPED */
    public void scrap() {
        transition(AssetStatus.SCRAPPED);
        markUpdated();
    }

}
