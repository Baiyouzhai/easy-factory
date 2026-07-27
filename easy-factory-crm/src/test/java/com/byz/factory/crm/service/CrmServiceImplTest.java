package com.byz.factory.crm.service;

import com.byz.factory.crm.ComplaintStatus;
import com.byz.factory.crm.SalesOrderStatus;
import com.byz.factory.crm.model.Complaint;
import com.byz.factory.crm.model.Customer;
import com.byz.factory.crm.model.SalesOrder;
import com.byz.factory.crm.repository.ComplaintRepository;
import com.byz.factory.crm.repository.CustomerRepository;
import com.byz.factory.crm.repository.SalesOrderRepository;
import com.byz.factory.crm.service.impl.CrmServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CrmServiceImpl.class)
@DisplayName("CrmServiceImpl 服务层集成测试")
class CrmServiceImplTest {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private SalesOrderRepository salesOrderRepo;

    @Autowired
    private ComplaintRepository complaintRepo;

    @Autowired
    private CrmServiceImpl crmService;

    @BeforeEach
    void setUp() {
        // 注册测试客户
        crmService.registerCustomer("CUST-001", "华东制药", "制药");
    }

    // ════════════════════════════════════════════════════════════
    // 客户管理
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("注册客户 — 自动获得 code/name/默认状态")
    void registerCustomer_shouldCreateWithDefaults() {
        Customer customer = crmService.registerCustomer("CUST-002", "南方生物", "生物制药");

        assertEquals("CUST-002", customer.getCode());
        assertEquals("南方生物", customer.getName());
        assertEquals("生物制药", customer.getIndustry());
        assertEquals("NEVER", customer.getGmpAuditStatus());
        assertNotNull(customer.getId());
    }

    @Test
    @DisplayName("注册客户 — 重复编码抛出异常")
    void registerCustomer_duplicateCode_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                crmService.registerCustomer("CUST-001", "华东制药", "化工"));
    }

    @Test
    @DisplayName("查询客户 — 按编码查找")
    void getCustomer_shouldFindByCode() {
        Customer customer = crmService.getCustomer("CUST-001");
        assertEquals("华东制药", customer.getName());
    }

    @Test
    @DisplayName("查询客户 — 不存在则抛异常")
    void getCustomer_notFound_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                crmService.getCustomer("NONEXISTENT"));
    }

    @Test
    @DisplayName("列出所有客户")
    void listCustomers_shouldReturnAll() {
        crmService.registerCustomer("CUST-003", "北方化学", "精细化工");
        List<Customer> customers = crmService.listCustomers();

        assertTrue(customers.size() >= 2);
    }

    @Test
    @DisplayName("按行业查找客户")
    void findCustomersByIndustry_shouldFilterByIndustry() {
        crmService.registerCustomer("CUST-004", "西部API", "原料药");
        List<Customer> pharmaCustomers = crmService.findCustomersByIndustry("制药");

        assertEquals(1, pharmaCustomers.size());
        assertEquals("CUST-001", pharmaCustomers.get(0).getCode());
    }

    @Test
    @DisplayName("更新 GMP 审计状态")
    void updateAuditStatus_shouldChangeStatus() {
        crmService.updateAuditStatus("CUST-001", "PASSED", "2026-06-01");

        Customer customer = crmService.getCustomer("CUST-001");
        assertEquals("PASSED", customer.getGmpAuditStatus());
        assertEquals("2026-06-01", customer.getGmpAuditDate());
    }

    // ════════════════════════════════════════════════════════════
    // 销售订单管理
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("创建订单 — 默认 DRAFT 状态")
    void createOrder_shouldCreateWithDraftStatus() {
        SalesOrder order = crmService.createOrder("CUST-001", "PROD-001", new BigDecimal("500"));

        assertNotNull(order.getOrderNo());
        assertEquals("CUST-001", order.getCustomerCode());
        assertEquals("PROD-001", order.getProductCode());
        assertEquals(new BigDecimal("500"), order.getQuantity());
        assertEquals(SalesOrderStatus.DRAFT, order.getStatus());
        assertNotNull(order.getId());
    }

    @Test
    @DisplayName("创建订单 — 客户不存在则抛异常")
    void createOrder_unknownCustomer_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                crmService.createOrder("UNKNOWN", "PROD-001", new BigDecimal("100")));
    }

    @Test
    @DisplayName("确认订单 — DRAFT → CONFIRMED")
    void confirmOrder_shouldTransitionToConfirmed() {
        SalesOrder order = crmService.createOrder("CUST-001", "PROD-001", new BigDecimal("100"));
        String orderNo = order.getOrderNo();

        order = crmService.confirmOrder(orderNo);
        assertEquals(SalesOrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("关联生产计划 — CONFIRMED → IN_PRODUCTION")
    void linkToProductionPlan_shouldStartProduction() {
        SalesOrder order = crmService.createOrder("CUST-001", "PROD-001", new BigDecimal("100"));
        String orderNo = order.getOrderNo();
        crmService.confirmOrder(orderNo);

        crmService.linkToProductionPlan(orderNo, "PLAN-2026-001");

        order = crmService.getOrder(orderNo);
        assertEquals(SalesOrderStatus.IN_PRODUCTION, order.getStatus());
        assertEquals("PLAN-2026-001", order.getPlanNo());
    }

    @Test
    @DisplayName("订单发货 — IN_PRODUCTION → SHIPPED")
    void shipOrder_shouldTransitionToShipped() {
        SalesOrder order = crmService.createOrder("CUST-001", "PROD-001", new BigDecimal("100"));
        String orderNo = order.getOrderNo();
        crmService.confirmOrder(orderNo);
        crmService.linkToProductionPlan(orderNo, "PLAN-2026-001");

        order = crmService.shipOrder(orderNo);
        assertEquals(SalesOrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    @DisplayName("订单完成 — SHIPPED → COMPLETED")
    void completeOrder_shouldTransitionToCompleted() {
        SalesOrder order = crmService.createOrder("CUST-001", "PROD-001", new BigDecimal("100"));
        String orderNo = order.getOrderNo();
        crmService.confirmOrder(orderNo);
        crmService.linkToProductionPlan(orderNo, "PLAN-2026-001");
        crmService.shipOrder(orderNo);

        order = crmService.completeOrder(orderNo);
        assertEquals(SalesOrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("订单取消 — DRAFT → CANCELLED")
    void cancelOrder_shouldTransitionToCancelled() {
        SalesOrder order = crmService.createOrder("CUST-001", "PROD-001", new BigDecimal("100"));
        String orderNo = order.getOrderNo();

        order = crmService.cancelOrder(orderNo);
        assertEquals(SalesOrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("按客户查询订单")
    void findOrdersByCustomer_shouldFilterByCustomer() {
        crmService.createOrder("CUST-001", "PROD-001", new BigDecimal("100"));
        crmService.createOrder("CUST-001", "PROD-002", new BigDecimal("200"));

        List<SalesOrder> orders = crmService.findOrdersByCustomer("CUST-001");
        assertEquals(2, orders.size());
    }

    @Test
    @DisplayName("查询活跃订单 — 排除 COMPLETED 和 CANCELLED")
    void findActiveOrders_shouldExcludeCompletedAndCancelled() {
        SalesOrder active = crmService.createOrder("CUST-001", "PROD-001", new BigDecimal("100"));
        SalesOrder cancelled = crmService.createOrder("CUST-001", "PROD-002", new BigDecimal("200"));
        crmService.cancelOrder(cancelled.getOrderNo());

        List<SalesOrder> activeOrders = crmService.findActiveOrders();
        assertTrue(activeOrders.stream().anyMatch(o -> o.getOrderNo().equals(active.getOrderNo())));
        assertTrue(activeOrders.stream().noneMatch(o -> o.getOrderNo().equals(cancelled.getOrderNo())));
    }

    // ════════════════════════════════════════════════════════════
    // 投诉管理
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("创建投诉 — 默认 OPEN 状态")
    void createComplaint_shouldCreateWithOpenStatus() {
        String complaintNo = crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "外观异常");

        Complaint complaint = crmService.getComplaint(complaintNo);
        assertEquals("CUST-001", complaint.getCustomerCode());
        assertEquals("SO-001", complaint.getOrderNo());
        assertEquals("BATCH-001", complaint.getBatchNo());
        assertEquals("QUALITY", complaint.getType());
        assertEquals(ComplaintStatus.OPEN, complaint.getStatus());
        assertNotNull(complaint.getId());
    }

    @Test
    @DisplayName("开始调查 — OPEN → INVESTIGATING")
    void startInvestigation_shouldTransitionToInvestigating() {
        String complaintNo = crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "残留溶剂");

        Complaint complaint = crmService.startInvestigation(complaintNo);
        assertEquals(ComplaintStatus.INVESTIGATING, complaint.getStatus());
    }

    @Test
    @DisplayName("解决投诉 — INVESTIGATING → RESOLVED")
    void resolveComplaint_shouldTransitionToResolved() {
        String complaintNo = crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "残留溶剂");
        crmService.startInvestigation(complaintNo);

        Complaint complaint = crmService.resolveComplaint(complaintNo, "补发新批次");
        assertEquals(ComplaintStatus.RESOLVED, complaint.getStatus());
        assertEquals("补发新批次", complaint.getResolution());
    }

    @Test
    @DisplayName("关闭投诉 — RESOLVED → CLOSED")
    void closeComplaint_shouldTransitionToClosed() {
        String complaintNo = crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "残留溶剂");
        crmService.startInvestigation(complaintNo);
        crmService.resolveComplaint(complaintNo, "补发");

        Complaint complaint = crmService.closeComplaint(complaintNo);
        assertEquals(ComplaintStatus.CLOSED, complaint.getStatus());
    }

    @Test
    @DisplayName("取消投诉 — OPEN → CANCELLED")
    void cancelComplaint_shouldTransitionToCancelled() {
        String complaintNo = crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "DELIVERY", "延误");

        Complaint complaint = crmService.cancelComplaint(complaintNo);
        assertEquals(ComplaintStatus.CANCELLED, complaint.getStatus());
    }

    @Test
    @DisplayName("关联 CAPA")
    void linkCapa_shouldSetCapaCode() {
        String complaintNo = crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "微生物超标");
        crmService.startInvestigation(complaintNo);

        crmService.linkCapa(complaintNo, "CAPA-0042");

        Complaint complaint = crmService.getComplaint(complaintNo);
        assertEquals("CAPA-0042", complaint.getCapaCode());
    }

    @Test
    @DisplayName("查询活跃投诉 — OPEN + INVESTIGATING")
    void findActiveComplaints_shouldReturnOpenAndInvestigating() {
        String comp1 = crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "问题1");
        String comp2 = crmService.createComplaint("CUST-001", "SO-002",
                "BATCH-002", "DELIVERY", "问题2");
        crmService.cancelComplaint(comp2); // comp2 已取消

        List<Complaint> active = crmService.findActiveComplaints();
        assertEquals(1, active.size());
        assertEquals(comp1, active.get(0).getComplaintNo());
    }

    @Test
    @DisplayName("按客户查询投诉")
    void findComplaintsByCustomer_shouldFilterByCustomer() {
        crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "问题1");
        crmService.createComplaint("CUST-001", "SO-002",
                "BATCH-002", "DELIVERY", "问题2");

        List<Complaint> complaints = crmService.findComplaintsByCustomer("CUST-001");
        assertEquals(2, complaints.size());
    }

    @Test
    @DisplayName("按订单查询投诉")
    void findComplaintsByOrder_shouldFilterByOrder() {
        crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "问题1");
        crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-002", "DELIVERY", "问题2");

        List<Complaint> complaints = crmService.findComplaintsByOrder("SO-001");
        assertEquals(2, complaints.size());
    }

    @Test
    @DisplayName("查询质量问题投诉")
    void findQualityComplaints_shouldReturnOnlyQualityType() {
        crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "问题1");
        crmService.createComplaint("CUST-001", "SO-002",
                "BATCH-002", "DELIVERY", "问题2");
        crmService.createComplaint("CUST-001", "SO-003",
                "BATCH-003", "SERVICE", "问题3");

        List<Complaint> quality = crmService.findQualityComplaints();
        assertEquals(1, quality.size());
        assertEquals("QUALITY", quality.get(0).getType());
    }

    // ════════════════════════════════════════════════════════════
    // 全流程集成
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("集成 — 订单完整生命周期")
    void orderFullLifecycle_shouldWork() {
        SalesOrder order = crmService.createOrder("CUST-001", "PROD-001", new BigDecimal("5000"));
        String orderNo = order.getOrderNo();

        assertEquals(SalesOrderStatus.DRAFT, order.getStatus());
        order = crmService.confirmOrder(orderNo);
        assertEquals(SalesOrderStatus.CONFIRMED, order.getStatus());
        crmService.linkToProductionPlan(orderNo, "PLAN-2026-001");
        order = crmService.getOrder(orderNo);
        assertEquals(SalesOrderStatus.IN_PRODUCTION, order.getStatus());
        order = crmService.shipOrder(orderNo);
        assertEquals(SalesOrderStatus.SHIPPED, order.getStatus());
        order = crmService.completeOrder(orderNo);
        assertEquals(SalesOrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("集成 — 投诉→调查→CAPA→解决→关闭 完整流程")
    void complaintFullLifecycle_shouldWork() {
        String complaintNo = crmService.createComplaint("CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "残留溶剂超标，GMP偏差");
        assertEquals(ComplaintStatus.OPEN, crmService.getComplaint(complaintNo).getStatus());

        Complaint complaint = crmService.startInvestigation(complaintNo);
        assertEquals(ComplaintStatus.INVESTIGATING, complaint.getStatus());

        crmService.linkCapa(complaintNo, "CAPA-2026-0042");
        assertEquals("CAPA-2026-0042", crmService.getComplaint(complaintNo).getCapaCode());

        complaint = crmService.resolveComplaint(complaintNo, "调整工艺参数，补发合格批次");
        assertEquals(ComplaintStatus.RESOLVED, complaint.getStatus());

        complaint = crmService.closeComplaint(complaintNo);
        assertEquals(ComplaintStatus.CLOSED, complaint.getStatus());
    }

}
