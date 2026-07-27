package com.byz.factory.scm;

import java.time.LocalDate;

/**
 * 来料计划抽象 — SCM 来料计划实体的跨模块契约。
 * <p>
 * WMS 通过此接口获取来料计划，生成收货单并准备储位。
 * 来料计划是 SCM → WMS 的关键协作契约。
 *
 * @author 苏政
 * @see com.byz.factory.scm.model.InboundPlan
 */
public interface IInboundPlan {

    /** 计划编号 */
    String getCode();

    /** 关联采购订单号 */
    String getPoNo();

    /** 供应商编码 */
    String getSupplierCode();

    /** 预计到货日期 */
    LocalDate getExpectedDate();

    /** 计划状态 */
    InboundPlanStatus getStatus();

    /** 是否已通知仓库 */
    boolean isNotifyWarehouse();

}
