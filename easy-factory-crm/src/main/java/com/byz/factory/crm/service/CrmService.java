package com.byz.factory.crm.service;

import com.byz.factory.crm.model.Customer;
import com.byz.factory.crm.model.SalesOrder;

public interface CrmService {

    // 客户管理
    Customer registerCustomer(String code, String name, String industry);
    Customer getCustomer(String code);
    void updateAuditStatus(String customerCode, String status, String date);

    // 订单管理
    SalesOrder createOrder(String customerCode, String productCode, double quantity);
    SalesOrder confirmOrder(String orderNo);
    void linkToProductionPlan(String orderNo, String planNo);

    // 投诉管理
    String createComplaint(String customerCode, String orderNo, String batchNo, String type, String description);

}
