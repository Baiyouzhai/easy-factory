package com.byz.factory.wms.model;

import com.byz.factory.batch.IStorage;
import com.byz.factory.batch.StorageType;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 库位 — 继承 BaseEntity 获得 code/name/audit，实现 IStorage 供跨模块引用。
 *
 * <h3>层次结构</h3>
 * 库位的物理层次：仓库(warehouse) → 区域(zone) → 货架(rack) → 层(level) → 位(position)。
 * locationCode 是完整的库位编码（如 "WH-A-Z01-R03-L02-P05"），各字段为冗余分解便于查询。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   wms.location      — 库位编码
 *   wms.warehouse      — 仓库
 *   wms.zone           — 区域
 *   wms.rack           — 货架
 *   wms.level          — 层
 *   wms.position       — 位
 *   wms.storageType    — 存储类型
 *   wms.capacity       — 容量
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Storage extends BaseEntity implements IStorage {

    /** 仓库 */
    private String warehouse;

    /** 区域（原料区/包材区/成品区/待检区/不合格区） */
    private String zone;

    /** 货架 */
    private String rack;

    /** 层 */
    private String level;

    /** 位 */
    private String position;

    /** 存储类型 */
    private StorageType storageType;

    /** 容量 */
    private BigDecimal capacity;

    /**
     * @param locationCode 库位编码
     * @param warehouse    仓库
     * @param zone         区域
     */
    public Storage(String locationCode, String warehouse, String zone) {
        super(locationCode, "库位-" + locationCode);
        this.warehouse = warehouse;
        this.zone = zone;
        this.storageType = StorageType.AMBIENT;
        this.capacity = BigDecimal.ZERO;
    }

    // ==================== IStorage 实现 ====================

    @Override
    public String getLocationCode() {
        return getCode();
    }

}
