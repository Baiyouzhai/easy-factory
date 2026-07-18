package com.byz.factory.shared;

/**
 * 计量单位 (Unit of Measure) — 制造资源的物理单位。
 * <p>
 * 覆盖制药/食品/电子制造常见单位。
 *
 * @author 苏政
 */
public enum UOM {

    // ── 质量 ──
    /** 千克 */ KG,
    /** 克 */   G,
    /** 毫克 */ MG,

    // ── 体积 ──
    /** 升 */   L,
    /** 毫升 */ ML,

    // ── 长度 ──
    /** 米 */   M,
    /** 厘米 */ CM,
    /** 毫米 */ MM,

    // ── 计数 ──
    /** 个/件 */ PCS,
    /** 片 */    TAB,
    /** 粒 */    CAP,
    /** 瓶 */    BTL,
    /** 箱 */    BOX,

    // ── 特殊 ──
    /** 摄氏度 */ CELSIUS,
    /** 百分比 */ PERCENT,
    /** 批 */     BATCH,

    /** 无单位 */ NONE
}
