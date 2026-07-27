package com.byz.factory.erp.model;

import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.BaseEntity;
import com.byz.factory.shared.Dict;
import com.byz.factory.shared.UOM;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ERP 物料主数据本地缓存 — 继承 BaseEntity 获得 id/code/name/createdAt/updatedAt。
 * <p>
 * 将 ERP 系统的物料主数据缓存到本地，其他模块通过 {@link #toResourceItem()}
 * 获取 core 统一资源模型，无需直接依赖 ERP 物料结构。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   erp.materialType   — 物料类型 (ROH/HALB/FERT)
 *   erp.batchManaged   — 是否批次管理
 *   erp.shelfLife      — 保质期(天)
 *   erp.ghsClass       — GHS 危险等级
 *   erp.sourceSystem   — 来源 ERP 系统
 *   erp.lastSyncTime   — 最后同步时间
 * </pre>
 *
 * @author 苏政
 */
@Entity
@Table(name = "erp_material_cache")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterialCache extends BaseEntity {

    @Column(length = 500)
    private String description;

    @Column(length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", length = 10, nullable = false)
    private ErpMaterialType materialType;

    @Column(name = "batch_managed", nullable = false)
    private boolean batchManaged;

    @Column(name = "shelf_life_days", nullable = false)
    private int shelfLifeDays;

    @Column(name = "ghs_class", length = 50)
    private String ghsClass;

    @Column(name = "last_sync_time")
    private Instant lastSyncTime;

    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    /**
     * @param materialCode ERP 物料编码（映射为 BaseEntity.code）
     * @param description  物料描述（映射为 BaseEntity.name）
     * @param unit         基本单位（ERP 单位编码，映射时转为 core UOM）
     */
    public MaterialCache(String materialCode, String description, String unit) {
        super(materialCode, description);
        this.description = description;
        this.unit = unit;
        this.materialType = ErpMaterialType.ROH;
        this.batchManaged = false;
        this.shelfLifeDays = 0;
    }

    /**
     * 将 ERP 物料缓存映射为 core 统一资源模型 {@link IResourceItem}。
     *
     * @return core 资源项（可用于工序投料、BOM 引用等）
     */
    public IResourceItem toResourceItem() {
        ResourceItem item = new ResourceItem();
        item.setName(getCode());
        item.setGroup(Dict.SourceGroup.Material);
        item.setType(mapSourceType());
        item.setNumber(BigDecimal.ONE);
        return item;
    }

    /** ERP 物料类型 → core 资源类型映射。 */
    private Dict.SourceType mapSourceType() {
        if (materialType == null) return Dict.SourceType.Other;
        return switch (materialType) {
            case ROH  -> Dict.SourceType.RawMaterial;
            case HALB -> Dict.SourceType.WIP;
            case FERT -> Dict.SourceType.FinishedGood;
        };
    }

    /**
     * 尝试将 ERP 单位编码映射为 core {@link UOM} 枚举。
     *
     * @return 对应的 UOM，未匹配时返回 NONE
     */
    public UOM mapUom() {
        if (unit == null) return UOM.NONE;
        return switch (unit.toUpperCase()) {
            case "KG"  -> UOM.KG;
            case "G"   -> UOM.G;
            case "MG"  -> UOM.MG;
            case "L"   -> UOM.L;
            case "ML"  -> UOM.ML;
            case "M"   -> UOM.M;
            case "CM"  -> UOM.CM;
            case "MM"  -> UOM.MM;
            case "PCS", "PC", "EA" -> UOM.PCS;
            case "TAB" -> UOM.TAB;
            case "CAP" -> UOM.CAP;
            case "BTL" -> UOM.BTL;
            case "BOX" -> UOM.BOX;
            default    -> UOM.NONE;
        };
    }

}
