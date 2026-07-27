package com.byz.factory.crm.service.impl;

import com.byz.factory.crm.ComplaintStatus;
import com.byz.factory.crm.SalesOrderStatus;
import com.byz.factory.crm.model.Complaint;
import com.byz.factory.crm.model.Customer;
import com.byz.factory.crm.model.SalesOrder;
import com.byz.factory.crm.repository.ComplaintRepository;
import com.byz.factory.crm.repository.CustomerRepository;
import com.byz.factory.crm.repository.SalesOrderRepository;
import com.byz.factory.crm.service.CrmService;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.CrmEventTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * CRM 客户关系管理服务实现。
 * <p>
 * 使用 Spring 事务管理，确保客户注册、订单管理、投诉处理
 * 等操作的原子性。事件发布在状态变更后由 Service 层统一负责。
 *
 * @author 苏政
 */
@Service
@Transactional
public class CrmServiceImpl implements CrmService {

    private final CustomerRepository customerRepo;
    private final SalesOrderRepository salesOrderRepo;
    private final ComplaintRepository complaintRepo;

    public CrmServiceImpl(CustomerRepository customerRepo,
                          SalesOrderRepository salesOrderRepo,
                          ComplaintRepository complaintRepo) {
        this.customerRepo = customerRepo;
        this.salesOrderRepo = salesOrderRepo;
        this.complaintRepo = complaintRepo;
    }

    // ════════════════════════════════════════════════════════════
    // 客户管理
    // ════════════════════════════════════════════════════════════

    @Override
    public Customer registerCustomer(String code, String name, String industry) {
        if (customerRepo.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("客户编码已存在: " + code);
        }
        Customer customer = new Customer(code, name, industry);
        customer = customerRepo.save(customer);

        DomainEventPublisher.publish(IDomainEvent.of(
                CrmEventTypes.CUSTOMER_REGISTERED, "crm",
                Map.of("code", code, "name", name, "industry", industry)));
        return customer;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomer(String code) {
        return customerRepo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("客户不存在: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> listCustomers() {
        return customerRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findCustomersByIndustry(String industry) {
        return customerRepo.findByIndustry(industry);
    }

    @Override
    public void updateAuditStatus(String customerCode, String status, String date) {
        Customer customer = getCustomer(customerCode);
        customer.updateAuditStatus(status, date);
        customerRepo.save(customer);

        DomainEventPublisher.publish(IDomainEvent.of(
                CrmEventTypes.CUSTOMER_AUDIT_UPDATED, "crm",
                Map.of("customerCode", customerCode, "gmpAuditStatus", status, "gmpAuditDate", date)));
    }

    // ════════════════════════════════════════════════════════════
    // 销售订单管理
    // ════════════════════════════════════════════════════════════

    @Override
    public SalesOrder createOrder(String customerCode, String productCode, BigDecimal quantity) {
        // 验证客户存在
        if (customerRepo.findByCode(customerCode).isEmpty()) {
            throw new IllegalArgumentException("客户不存在: " + customerCode);
        }
        String orderNo = generateOrderNo(customerCode);
        SalesOrder order = new SalesOrder(orderNo, customerCode, productCode, quantity);
        order = salesOrderRepo.save(order);

        DomainEventPublisher.publish(IDomainEvent.of(
                CrmEventTypes.ORDER_CREATED, "crm",
                Map.of("orderNo", orderNo, "customerCode", customerCode,
                        "productCode", productCode, "quantity", quantity)));
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrder getOrder(String orderNo) {
        return salesOrderRepo.findByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderNo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrder> findOrdersByCustomer(String customerCode) {
        return salesOrderRepo.findByCustomerCode(customerCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrder> findActiveOrders() {
        return salesOrderRepo.findAll().stream()
                .filter(o -> o.getStatus() != SalesOrderStatus.COMPLETED
                        && o.getStatus() != SalesOrderStatus.CANCELLED)
                .toList();
    }

    @Override
    public SalesOrder confirmOrder(String orderNo) {
        SalesOrder order = getOrder(orderNo);
        order.confirm();
        order = salesOrderRepo.save(order);

        DomainEventPublisher.publish(IDomainEvent.of(
                CrmEventTypes.ORDER_CONFIRMED, "crm",
                Map.of("orderNo", orderNo, "customerCode", order.getCustomerCode(),
                        "productCode", order.getProductCode())));
        return order;
    }

    @Override
    public void linkToProductionPlan(String orderNo, String planNo) {
        SalesOrder order = getOrder(orderNo);
        order.linkToPlan(planNo);
        order.startProduction();
        salesOrderRepo.save(order);
    }

    @Override
    public SalesOrder shipOrder(String orderNo) {
        SalesOrder order = getOrder(orderNo);
        order.ship();
        order = salesOrderRepo.save(order);

        DomainEventPublisher.publish(IDomainEvent.of(
                CrmEventTypes.ORDER_SHIPPED, "crm",
                Map.of("orderNo", orderNo)));
        return order;
    }

    @Override
    public SalesOrder completeOrder(String orderNo) {
        SalesOrder order = getOrder(orderNo);
        order.complete();
        order = salesOrderRepo.save(order);

        DomainEventPublisher.publish(IDomainEvent.of(
                CrmEventTypes.ORDER_COMPLETED, "crm",
                Map.of("orderNo", orderNo)));
        return order;
    }

    @Override
    public SalesOrder cancelOrder(String orderNo) {
        SalesOrder order = getOrder(orderNo);
        order.cancel();
        order = salesOrderRepo.save(order);

        DomainEventPublisher.publish(IDomainEvent.of(
                CrmEventTypes.ORDER_CANCELLED, "crm",
                Map.of("orderNo", orderNo)));
        return order;
    }

    // ════════════════════════════════════════════════════════════
    // 投诉管理
    // ════════════════════════════════════════════════════════════

    @Override
    public String createComplaint(String customerCode, String orderNo, String batchNo,
                                   String type, String description) {
        String complaintNo = generateComplaintNo(customerCode);
        Complaint complaint = new Complaint(complaintNo, customerCode, orderNo,
                batchNo, type, description);
        complaintRepo.save(complaint);

        DomainEventPublisher.publish(IDomainEvent.of(
                CrmEventTypes.COMPLAINT_RECEIVED, "crm",
                Map.of("complaintNo", complaintNo, "customerCode", customerCode,
                        "orderNo", orderNo, "batchNo", batchNo,
                        "type", type, "description", description)));
        return complaintNo;
    }

    @Override
    @Transactional(readOnly = true)
    public Complaint getComplaint(String complaintNo) {
        return complaintRepo.findByComplaintNo(complaintNo)
                .orElseThrow(() -> new IllegalArgumentException("投诉不存在: " + complaintNo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findComplaintsByCustomer(String customerCode) {
        return complaintRepo.findByCustomerCode(customerCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findComplaintsByOrder(String orderNo) {
        return complaintRepo.findByOrderNo(orderNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findActiveComplaints() {
        return complaintRepo.findByStatusIn(List.of(ComplaintStatus.OPEN, ComplaintStatus.INVESTIGATING));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findQualityComplaints() {
        return complaintRepo.findByType("QUALITY");
    }

    @Override
    public Complaint startInvestigation(String complaintNo) {
        Complaint complaint = getComplaint(complaintNo);
        complaint.startInvestigation();
        return complaintRepo.save(complaint);
    }

    @Override
    public Complaint resolveComplaint(String complaintNo, String resolution) {
        Complaint complaint = getComplaint(complaintNo);
        complaint.resolve(resolution);
        complaint = complaintRepo.save(complaint);

        DomainEventPublisher.publish(IDomainEvent.of(
                CrmEventTypes.COMPLAINT_RESOLVED, "crm",
                Map.of("complaintNo", complaintNo, "resolution", resolution)));
        return complaint;
    }

    @Override
    public Complaint closeComplaint(String complaintNo) {
        Complaint complaint = getComplaint(complaintNo);
        complaint.close();
        complaint = complaintRepo.save(complaint);

        DomainEventPublisher.publish(IDomainEvent.of(
                CrmEventTypes.COMPLAINT_CLOSED, "crm",
                Map.of("complaintNo", complaintNo)));
        return complaint;
    }

    @Override
    public Complaint cancelComplaint(String complaintNo) {
        Complaint complaint = getComplaint(complaintNo);
        complaint.cancel();
        return complaintRepo.save(complaint);
    }

    @Override
    public void linkCapa(String complaintNo, String capaCode) {
        Complaint complaint = getComplaint(complaintNo);
        complaint.linkCapa(capaCode);
        complaintRepo.save(complaint);
    }

    // ════════════════════════════════════════════════════════════
    // 内部工具方法
    // ════════════════════════════════════════════════════════════

    private static final java.util.concurrent.atomic.AtomicLong SEQ = new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis());

    /** 生成订单号 */
    private String generateOrderNo(String customerCode) {
        return String.format("SO-%s-%d", customerCode, SEQ.incrementAndGet());
    }

    /** 生成投诉编号 */
    private String generateComplaintNo(String customerCode) {
        return String.format("COMP-%s-%d", customerCode, SEQ.incrementAndGet());
    }

}
