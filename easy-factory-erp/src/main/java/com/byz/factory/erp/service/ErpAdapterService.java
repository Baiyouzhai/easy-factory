package com.byz.factory.erp.service;

import com.byz.factory.erp.model.MaterialCache;
import com.byz.factory.resource.IResourceItem;

import java.math.BigDecimal;

/**
 * ERP 适配服务 — 对接外部 ERP 系统。
 * <p>
 * TODO 待实现：物料主数据同步、库存查询、事务回传
 *
 * @author 苏政
 */
public interface ErpAdapterService {

    /** 同步物料主数据 */
    void syncMaterials();

    /** 查询物料可用库存 */
    BigDecimal queryStock(String materialCode, String plantCode);

    /** 物料消耗回传 ERP */
    String postGoodsIssue(String materialCode, BigDecimal quantity, String batchNo, String referenceDoc);

    /** 成品入库回传 ERP */
    String postGoodsReceipt(String materialCode, BigDecimal quantity, String batchNo, String referenceDoc);

    /** 根据物料编码查找本地缓存 */
    MaterialCache findByCode(String materialCode);

}
