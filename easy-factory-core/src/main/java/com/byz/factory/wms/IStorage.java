package com.byz.factory.wms;

import java.math.BigDecimal;

/**
 * 库位抽象 — WMS 库位实体的跨模块契约。
 * <p>
 * 其他模块（MES、SCM）通过此接口引用库位信息，
 * 无需直接依赖 easy-factory-wms。
 * <p>
 * 库位的层次结构：仓库(warehouse) → 区域(zone) → 货架(rack) → 层(level) → 位(position)。
 *
 * @author 苏政
 * @see com.byz.factory.wms.model.Storage
 */
public interface IStorage {

    /** 库位编码（唯一标识） */
    String getLocationCode();

    /** 仓库 */
    String getWarehouse();

    /** 区域（原料区/包材区/成品区/待检区/不合格区） */
    String getZone();

    /** 货架 */
    String getRack();

    /** 层 */
    String getLevel();

    /** 位 */
    String getPosition();

    /** 存储类型（常温/冷藏/冷冻/危险品） */
    StorageType getStorageType();

    /** 容量 */
    BigDecimal getCapacity();

}
