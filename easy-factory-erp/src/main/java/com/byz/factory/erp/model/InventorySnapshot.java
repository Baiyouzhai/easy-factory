package com.byz.factory.erp.model;

import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 库存快照 — ERP 库存数据的本地缓存，继承 BaseEntity 获得 code/name/audit。
 * <p>
 * 按物料+工厂+库位+批次的粒度记录库存状态，供 MES/WMS 做可用性检查。
 * 参照 EAM CalibrationRecord 模式：无生命周期状态机，纯数据记录。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InventorySnapshot extends BaseEntity {

    /** 物料编码 */
    private String materialCode;

    /** 工厂代码 */
    private String plantCode;

    /** 存储地点 */
    private String storageLocation;

    /** 批次号 */
    private String batchNo;

    /** 非限制库存 */
    private BigDecimal unrestrictedQty;

    /** 待检库存 */
    private BigDecimal inspectionQty;

    /** 冻结库存 */
    private BigDecimal blockedQty;

    /** 快照时间 */
    private Instant snapshotTime;

    /** 来源 ERP 系统 */
    private String sourceSystem;

    /**
     * @param materialCode    物料编码
     * @param plantCode       工厂代码
     * @param storageLocation 存储地点
     */
    public InventorySnapshot(String materialCode, String plantCode, String storageLocation) {
        super(materialCode + "-" + plantCode + "-" + storageLocation,
                "Inv-" + materialCode);
        this.materialCode = materialCode;
        this.plantCode = plantCode;
        this.storageLocation = storageLocation;
        this.unrestrictedQty = BigDecimal.ZERO;
        this.inspectionQty = BigDecimal.ZERO;
        this.blockedQty = BigDecimal.ZERO;
        this.snapshotTime = Instant.now();
    }

    /**
     * 计算总库存（非限制 + 待检 + 冻结）。
     *
     * @return 总库存数量
     */
    public BigDecimal getTotalQty() {
        return unrestrictedQty.add(inspectionQty).add(blockedQty);
    }

    /**
     * 可用库存 = 非限制库存。
     *
     * @return 可用库存数量
     */
    public BigDecimal getAvailableQty() {
        return unrestrictedQty;
    }

}
