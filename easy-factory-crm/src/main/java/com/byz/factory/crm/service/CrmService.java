package com.byz.factory.crm.service;

import com.byz.factory.crm.model.Complaint;
import com.byz.factory.crm.model.Customer;
import com.byz.factory.crm.model.SalesOrder;

import java.math.BigDecimal;
import java.util.List;

/**
 * CRM 服务接口 — 客户管理、销售订单和客户投诉的业务契约。
 * <p>
 * 跨模块协作方向：
 * <ul>
 *   <li>CRM → MPS — 订单确认后注册需求来源，触发生成生产计划</li>
 *   <li>CRM → QMS — 投诉创建后通知 QMS 创建偏差/CAPA</li>
 *   <li>CRM → WMS — 订单发货后通知成品出库</li>
 *   <li>CRM → BI — 订单/投诉数据统计</li>
 * </ul>
 *
 * @author 苏政
 */
public interface CrmService {

    // ════════════════════════════════════════════════════════════
    // 客户管理
    // ════════════════════════════════════════════════════════════

    /** 注册新客户 */
    Customer registerCustomer(String code, String name, String industry);

    /** 按编码查询客户 */
    Customer getCustomer(String code);

    /** 列出所有客户 */
    List<Customer> listCustomers();

    /** 按行业查询客户 */
    List<Customer> findCustomersByIndustry(String industry);

    /** 更新 GMP 审计状态 */
    void updateAuditStatus(String customerCode, String status, String date);

    // ════════════════════════════════════════════════════════════
    // 销售订单管理
    // ════════════════════════════════════════════════════════════

    /** 创建销售订单（草稿） */
    SalesOrder createOrder(String customerCode, String productCode, BigDecimal quantity);

    /** 按订单号查询 */
    SalesOrder getOrder(String orderNo);

    /** 按客户查询订单 */
    List<SalesOrder> findOrdersByCustomer(String customerCode);

    /** 查询活跃订单（非 COMPLETED/CANCELLED） */
    List<SalesOrder> findActiveOrders();

    /** 确认订单（DRAFT → CONFIRMED，通知 MPS） */
    SalesOrder confirmOrder(String orderNo);

    /** 关联 MPS 生产计划 */
    void linkToProductionPlan(String orderNo, String planNo);

    /** 发货订单（IN_PRODUCTION → SHIPPED，通知 WMS） */
    SalesOrder shipOrder(String orderNo);

    /** 完成订单（SHIPPED → COMPLETED） */
    SalesOrder completeOrder(String orderNo);

    /** 取消订单 */
    SalesOrder cancelOrder(String orderNo);

    // ════════════════════════════════════════════════════════════
    // 投诉管理
    // ════════════════════════════════════════════════════════════

    /** 创建客户投诉，返回投诉编号 */
    String createComplaint(String customerCode, String orderNo, String batchNo,
                           String type, String description);

    /** 按投诉编号查询 */
    Complaint getComplaint(String complaintNo);

    /** 按客户查询投诉 */
    List<Complaint> findComplaintsByCustomer(String customerCode);

    /** 按订单查询投诉 */
    List<Complaint> findComplaintsByOrder(String orderNo);

    /** 查询活跃投诉（未结案） */
    List<Complaint> findActiveComplaints();

    /** 查询质量问题投诉 */
    List<Complaint> findQualityComplaints();

    /** 开始调查投诉 */
    Complaint startInvestigation(String complaintNo);

    /** 解决投诉 */
    Complaint resolveComplaint(String complaintNo, String resolution);

    /** 关闭投诉 */
    Complaint closeComplaint(String complaintNo);

    /** 取消投诉 */
    Complaint cancelComplaint(String complaintNo);

    /** 关联 CAPA */
    void linkCapa(String complaintNo, String capaCode);

}
