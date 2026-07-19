package com.byz.factory.wms;

/**
 * 存储类型 — 库位的存储条件分类。
 * <p>
 * 不同物料需要不同的存储环境，库位的存储类型决定可存放的物料范围。
 *
 * @author 苏政
 */
public enum StorageType {

    /** 常温存储 */
    AMBIENT,
    /** 冷藏 (2–8°C) */
    COLD,
    /** 冷冻 (≤ -18°C) */
    FROZEN,
    /** 危险品存储（防爆/隔离） */
    HAZARDOUS

}
