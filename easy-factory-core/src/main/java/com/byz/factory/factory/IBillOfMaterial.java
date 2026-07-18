package com.byz.factory.factory;

import java.util.List;

/**
 * 物料清单 (BOM) — 产品的物料组成定义。
 * <p>
 * 与 {@link IBlueprint} 互补：蓝图定义工序路线，BOM 定义物料组成。
 *
 * @author 苏政
 */
public interface IBillOfMaterial {

    /** 产品编码 */
    String getProductCode();

    /** 物料行项列表 */
    List<BillOfMaterial.MaterialLineItem> getMaterials();
}
