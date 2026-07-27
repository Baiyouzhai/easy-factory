package com.byz.factory.erp.model;

import com.byz.factory.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 库存快照 — ERP 库存数据的本地缓存，继承 BaseEntity 获得 id/code/name/createdAt/updatedAt。
 * <p>
 * 按物料+工厂+库位+批次的粒度记录库存状态，供 MES/WMS 做可用性检查。
 *
 * @author 苏政
 */
@Entity
@Table(name = "erp_inventory_snapshot")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventorySnapshot extends BaseEntity {

    @Column(name = "material_code", nullable = false, length = 100)
    private String materialCode;

    @Column(name = "plant_code", nullable = false, length = 50)
    private String plantCode;

    @Column(name = "storage_location", nullable = false, length = 50)
    private String storageLocation;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "unrestricted_qty", precision = 20, scale = 4, nullable = false)
    private BigDecimal unrestrictedQty;

    @Column(name = "inspection_qty", precision = 20, scale = 4, nullable = false)
    private BigDecimal inspectionQty;

    @Column(name = "blocked_qty", precision = 20, scale = 4, nullable = false)
    private BigDecimal blockedQty;

    @Column(name = "snapshot_time", nullable = false)
    private Instant snapshotTime;

    @Column(name = "source_system", length = 50)
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

    /** 总库存 = 非限制 + 待检 + 冻结。 */
    @Transient
    public BigDecimal getTotalQty() {
        return unrestrictedQty.add(inspectionQty).add(blockedQty);
    }

    /** 可用库存 = 非限制库存。 */
    @Transient
    public BigDecimal getAvailableQty() {
        return unrestrictedQty;
    }

}
