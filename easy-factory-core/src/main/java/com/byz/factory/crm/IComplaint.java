package com.byz.factory.crm;

/**
 * 客户投诉抽象 — CRM 投诉实体的跨模块契约。
 * <p>
 * QMS 通过此接口获知客户投诉 → 创建偏差/CAPA；
 * Andon 通过此接口感知重大投诉 → 触发安灯呼叫。
 * 典型调用链：CRM 投诉 → QMS 偏差 → CAPA 闭环。
 *
 * @author 苏政
 * @see com.byz.factory.crm.model.Complaint
 */
public interface IComplaint {

    /** 投诉编号 */
    String getComplaintNo();

    /** 客户编码 */
    String getCustomerCode();

    /** 关联订单号 */
    String getOrderNo();

    /** 关联批次号 */
    String getBatchNo();

    /** 投诉类型 (QUALITY / DELIVERY / PACKAGING / SERVICE / OTHER) */
    String getType();

    /** 投诉描述 */
    String getDescription();

    /** 投诉状态 */
    ComplaintStatus getStatus();

    /** 处理结果/回复 */
    String getResolution();

    /** 关联 CAPA 编码 */
    String getCapaCode();

}
