package com.byz.factory.plm.bom;

/**
 * BOM 类型 — 物料清单的三个阶段。
 * <p>
 * BOM 从设计到制造经历三次转化：EBOM → PBOM → MBOM。
 *
 * @author 苏政
 */
public enum BOMType {
    /** 设计 BOM — 产品结构树（CAD 导出），定义产品"是什么" */
    EBOM,
    /** 工艺 BOM — 增加工艺辅料、虚拟件拆分、损耗率，定义"怎么做" */
    PBOM,
    /** 制造 BOM — 按工序分组、关联工位/设备，定义"在哪做" */
    MBOM;

    /** 下一步转化目标 */
    public BOMType next() {
        return switch (this) {
            case EBOM -> PBOM;
            case PBOM -> MBOM;
            case MBOM -> null;  // MBOM 是终点
        };
    }

    /** 是否有下一步 */
    public boolean hasNext() {
        return next() != null;
    }
}
