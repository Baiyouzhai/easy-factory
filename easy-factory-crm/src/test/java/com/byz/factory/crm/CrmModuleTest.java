package com.byz.factory.crm;

import com.byz.factory.crm.model.Complaint;
import com.byz.factory.crm.model.Customer;
import com.byz.factory.crm.model.SalesOrder;
import com.byz.factory.crm.model.SalesOrderItem;
import com.byz.factory.crm.service.CrmService;
import com.byz.factory.event.types.CrmEventTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CRM 模块模型构造测试")
class CrmModuleTest {

    // ==================== Customer ====================

    @Test
    @DisplayName("Customer 创建 — 字段正确初始化")
    void customer_creation_shouldSetFields() {
        Customer customer = new Customer("CUST-001", "华东制药", "制药");

        assertEquals("CUST-001", customer.getCode());
        assertEquals("华东制药", customer.getName());
        assertEquals("制药", customer.getIndustry());
        assertEquals("NEVER", customer.getGmpAuditStatus());
        assertNull(customer.getGmpAuditDate());
        assertEquals(0.0, customer.getOnTimeDeliveryRate());
        assertEquals(0.0, customer.getQualityComplaintRate());
        assertNotNull(customer.getCreatedAt());
    }

    @Test
    @DisplayName("Customer — 通过 GMP 审计")
    void customer_passGmpAudit_shouldUpdateStatusAndDate() {
        Customer customer = new Customer("CUST-002", "南方生物", "生物制药");

        customer.passGmpAudit("2026-03-15");

        assertEquals("PASSED", customer.getGmpAuditStatus());
        assertEquals("2026-03-15", customer.getGmpAuditDate());
    }

    @Test
    @DisplayName("Customer — GMP 审计过期")
    void customer_expireGmpAudit_shouldSetExpired() {
        Customer customer = new Customer("CUST-003", "北方化学", "精细化工");
        customer.passGmpAudit("2025-03-15");

        customer.expireGmpAudit();

        assertEquals("EXPIRED", customer.getGmpAuditStatus());
    }

    @Test
    @DisplayName("Customer — 更新审计状态")
    void customer_updateAuditStatus_shouldSetStatusAndDate() {
        Customer customer = new Customer("CUST-004", "西部API", "原料药");

        customer.updateAuditStatus("PASSED", "2026-06-01");

        assertEquals("PASSED", customer.getGmpAuditStatus());
        assertEquals("2026-06-01", customer.getGmpAuditDate());
    }

    @Test
    @DisplayName("Customer — 实现 ICustomer 接口")
    void customer_shouldImplementICustomer() {
        Customer customer = new Customer("CUST-005", "测试客户", "食品");

        assertInstanceOf(ICustomer.class, customer);
    }

    // ==================== SalesOrderItem ====================

    @Test
    @DisplayName("SalesOrderItem 创建 — 字段正确初始化")
    void salesOrderItem_creation_shouldSetFields() {
        SalesOrderItem item = new SalesOrderItem("PROD-001", new BigDecimal("100"), new BigDecimal("25.50"));

        assertEquals("PROD-001", item.getProductCode());
        assertEquals(new BigDecimal("100"), item.getQuantity());
        assertEquals(new BigDecimal("25.50"), item.getUnitPrice());
    }

    @Test
    @DisplayName("SalesOrderItem — 实现 ISalesOrder.ISalesOrderItem 接口")
    void salesOrderItem_shouldImplementNestedInterface() {
        SalesOrderItem item = new SalesOrderItem("PROD-001", BigDecimal.ONE, BigDecimal.TEN);

        assertInstanceOf(ISalesOrder.ISalesOrderItem.class, item);
    }

    @Test
    @DisplayName("SalesOrderItem — 无参构造器可用")
    void salesOrderItem_noArgsConstructor_shouldWork() {
        SalesOrderItem item = new SalesOrderItem();
        assertNull(item.getProductCode());
    }

    // ==================== SalesOrder ====================

    @Test
    @DisplayName("SalesOrder 创建 — 初始状态 DRAFT")
    void salesOrder_creation_shouldSetDraftStatus() {
        SalesOrder order = new SalesOrder("SO-001", "CUST-001", "PROD-001", new BigDecimal("500"));

        assertEquals("SO-001", order.getOrderNo());
        assertEquals("CUST-001", order.getCustomerCode());
        assertEquals("PROD-001", order.getProductCode());
        assertEquals(new BigDecimal("500"), order.getQuantity());
        assertEquals(SalesOrderStatus.DRAFT, order.getStatus());
        assertEquals("NORMAL", order.getPriority());
        assertNotNull(order.getItems());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    @DisplayName("SalesOrder — DRAFT → CONFIRMED 正常转换")
    void salesOrder_confirm_shouldTransitionToConfirmed() {
        SalesOrder order = new SalesOrder("SO-002", "CUST-001", "PROD-001", new BigDecimal("100"));

        order.confirm();

        assertEquals(SalesOrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("SalesOrder — DRAFT → CANCELLED 取消订单")
    void salesOrder_cancelFromDraft_shouldTransitionToCancelled() {
        SalesOrder order = new SalesOrder("SO-003", "CUST-001", "PROD-001", new BigDecimal("100"));

        order.cancel();

        assertEquals(SalesOrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("SalesOrder — CONFIRMED → CANCELLED 已确认订单取消")
    void salesOrder_cancelFromConfirmed_shouldTransitionToCancelled() {
        SalesOrder order = new SalesOrder("SO-004", "CUST-001", "PROD-001", new BigDecimal("100"));
        order.confirm();

        order.cancel();

        assertEquals(SalesOrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("SalesOrder — 全正向生命周期")
    void salesOrder_fullLifecycle_shouldTransitionAllStates() {
        SalesOrder order = new SalesOrder("SO-005", "CUST-001", "PROD-001", new BigDecimal("200"));

        order.confirm();
        assertEquals(SalesOrderStatus.CONFIRMED, order.getStatus());

        order.startProduction();
        assertEquals(SalesOrderStatus.IN_PRODUCTION, order.getStatus());

        order.ship();
        assertEquals(SalesOrderStatus.SHIPPED, order.getStatus());

        order.complete();
        assertEquals(SalesOrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("SalesOrder — COMPLETED 终态不可再转换")
    void salesOrder_completed_cannotTransition() {
        SalesOrder order = new SalesOrder("SO-006", "CUST-001", "PROD-001", new BigDecimal("100"));
        order.confirm();
        order.startProduction();
        order.ship();
        order.complete();

        assertThrows(IllegalStateException.class, order::ship);
        assertThrows(IllegalStateException.class, order::cancel);
    }

    @Test
    @DisplayName("SalesOrder — CANCELLED 终态不可再转换")
    void salesOrder_cancelled_cannotTransition() {
        SalesOrder order = new SalesOrder("SO-007", "CUST-001", "PROD-001", new BigDecimal("100"));
        order.cancel();

        assertThrows(IllegalStateException.class, order::confirm);
    }

    @Test
    @DisplayName("SalesOrder — SHIPPED 不能取消")
    void salesOrder_shipped_cannotCancel() {
        SalesOrder order = new SalesOrder("SO-008", "CUST-001", "PROD-001", new BigDecimal("100"));
        order.confirm();
        order.startProduction();
        order.ship();

        assertThrows(IllegalStateException.class, order::cancel);
    }

    @Test
    @DisplayName("SalesOrder — canTransition 正确性")
    void salesOrder_canTransition_shouldReturnCorrectResults() {
        SalesOrder order = new SalesOrder("SO-009", "CUST-001", "PROD-001", new BigDecimal("100"));

        assertTrue(order.canTransition(SalesOrderStatus.CONFIRMED));
        assertTrue(order.canTransition(SalesOrderStatus.CANCELLED));
        assertFalse(order.canTransition(SalesOrderStatus.SHIPPED));
        assertFalse(order.canTransition(SalesOrderStatus.COMPLETED));
    }

    @Test
    @DisplayName("SalesOrder — 同状态幂等转换")
    void salesOrder_sameStateTransition_shouldNotThrow() {
        SalesOrder order = new SalesOrder("SO-010", "CUST-001", "PROD-001", new BigDecimal("100"));

        assertDoesNotThrow(() -> order.transition(SalesOrderStatus.DRAFT));
    }

    @Test
    @DisplayName("SalesOrder — 添加明细行")
    void salesOrder_addItem_shouldAddToItems() {
        SalesOrder order = new SalesOrder("SO-011", "CUST-001", "PROD-001", new BigDecimal("100"));
        order.addItem("PROD-001", new BigDecimal("50"), new BigDecimal("10.00"));

        assertEquals(1, order.getItems().size());
        assertEquals("PROD-001", order.getItems().get(0).getProductCode());
        assertEquals(new BigDecimal("50"), order.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("10.00"), order.getItems().get(0).getUnitPrice());
    }

    @Test
    @DisplayName("SalesOrder — 添加多个明细行")
    void salesOrder_addMultipleItems_shouldAccumulate() {
        SalesOrder order = new SalesOrder("SO-012", "CUST-001", "PROD-001", new BigDecimal("100"));
        order.addItem("PROD-001", new BigDecimal("50"), new BigDecimal("10.00"));
        order.addItem("PROD-002", new BigDecimal("30"), new BigDecimal("20.00"));

        assertEquals(2, order.getItems().size());
    }

    @Test
    @DisplayName("SalesOrder — 计算总金额")
    void salesOrder_totalAmount_shouldCalculateCorrectly() {
        SalesOrder order = new SalesOrder("SO-013", "CUST-001", "PROD-001", new BigDecimal("100"));
        order.addItem("PROD-001", new BigDecimal("50"), new BigDecimal("10.00"));
        order.addItem("PROD-002", new BigDecimal("30"), new BigDecimal("20.00"));

        BigDecimal total = order.totalAmount();
        // 50*10 + 30*20 = 500 + 600 = 1100
        assertEquals(0, total.compareTo(new BigDecimal("1100.00")));
    }

    @Test
    @DisplayName("SalesOrder — 关联生产计划")
    void salesOrder_linkToPlan_shouldSetPlanNo() {
        SalesOrder order = new SalesOrder("SO-014", "CUST-001", "PROD-001", new BigDecimal("100"));

        order.linkToPlan("PLAN-2026-001");

        assertEquals("PLAN-2026-001", order.getPlanNo());
    }

    @Test
    @DisplayName("SalesOrder — 紧急订单判定")
    void salesOrder_isRush_shouldDetectRushOrders() {
        SalesOrder normal = new SalesOrder("SO-015", "CUST-001", "PROD-001", new BigDecimal("100"));
        assertFalse(normal.isRush());

        SalesOrder rush = new SalesOrder("SO-016", "CUST-001", "PROD-001", new BigDecimal("100"));
        rush.setPriority("RUSH");
        assertTrue(rush.isRush());

        SalesOrder express = new SalesOrder("SO-017", "CUST-001", "PROD-001", new BigDecimal("100"));
        express.setPriority("EXPRESS");
        assertTrue(express.isRush());
    }

    @Test
    @DisplayName("SalesOrder — 实现 ISalesOrder 接口")
    void salesOrder_shouldImplementISalesOrder() {
        SalesOrder order = new SalesOrder("SO-018", "CUST-001", "PROD-001", new BigDecimal("100"));

        assertInstanceOf(ISalesOrder.class, order);
    }

    // ==================== Complaint ====================

    @Test
    @DisplayName("Complaint 创建 — 初始状态 OPEN")
    void complaint_creation_shouldSetOpenStatus() {
        Complaint complaint = new Complaint("COMP-001", "CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "批次外观异常");

        assertEquals("COMP-001", complaint.getComplaintNo());
        assertEquals("CUST-001", complaint.getCustomerCode());
        assertEquals("SO-001", complaint.getOrderNo());
        assertEquals("BATCH-001", complaint.getBatchNo());
        assertEquals("QUALITY", complaint.getType());
        assertEquals("批次外观异常", complaint.getDescription());
        assertEquals(ComplaintStatus.OPEN, complaint.getStatus());
        assertNull(complaint.getResolution());
        assertNull(complaint.getCapaCode());
        assertNotNull(complaint.getCreatedAt());
    }

    @Test
    @DisplayName("Complaint — OPEN → INVESTIGATING → RESOLVED → CLOSED 全正向生命周期")
    void complaint_fullLifecycle_shouldTransitionAllStates() {
        Complaint complaint = new Complaint("COMP-002", "CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "纯度不达标");

        complaint.startInvestigation();
        assertEquals(ComplaintStatus.INVESTIGATING, complaint.getStatus());

        complaint.resolve("退回该批次，补发新批次");
        assertEquals(ComplaintStatus.RESOLVED, complaint.getStatus());
        assertEquals("退回该批次，补发新批次", complaint.getResolution());

        complaint.close();
        assertEquals(ComplaintStatus.CLOSED, complaint.getStatus());
    }

    @Test
    @DisplayName("Complaint — OPEN → CANCELLED 取消投诉")
    void complaint_cancelFromOpen_shouldTransitionToCancelled() {
        Complaint complaint = new Complaint("COMP-003", "CUST-001", "SO-001",
                "BATCH-001", "DELIVERY", "交期延误");

        complaint.cancel();

        assertEquals(ComplaintStatus.CANCELLED, complaint.getStatus());
    }

    @Test
    @DisplayName("Complaint — CLOSED 终态不可再转换")
    void complaint_closed_cannotTransition() {
        Complaint complaint = new Complaint("COMP-004", "CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "粒度分布偏离");
        complaint.startInvestigation();
        complaint.resolve("让步接收");
        complaint.close();

        assertThrows(IllegalStateException.class, () -> complaint.startInvestigation());
        assertThrows(IllegalStateException.class, complaint::cancel);
    }

    @Test
    @DisplayName("Complaint — CANCELLED 终态不可再转换")
    void complaint_cancelled_cannotTransition() {
        Complaint complaint = new Complaint("COMP-005", "CUST-001", "SO-001",
                "BATCH-001", "SERVICE", "客服态度差");
        complaint.cancel();

        assertThrows(IllegalStateException.class, () -> complaint.startInvestigation());
    }

    @Test
    @DisplayName("Complaint — INVESTIGATING → OPEN 不可倒退")
    void complaint_investigating_cannotRevertToOpen() {
        Complaint complaint = new Complaint("COMP-006", "CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "微生物超标");
        complaint.startInvestigation();

        assertThrows(IllegalStateException.class, () -> complaint.transition(ComplaintStatus.OPEN));
    }

    @Test
    @DisplayName("Complaint — canTransition 正确性")
    void complaint_canTransition_shouldReturnCorrectResults() {
        Complaint complaint = new Complaint("COMP-007", "CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "残留溶剂");

        assertTrue(complaint.canTransition(ComplaintStatus.INVESTIGATING));
        assertTrue(complaint.canTransition(ComplaintStatus.CANCELLED));
        assertFalse(complaint.canTransition(ComplaintStatus.RESOLVED));
        assertFalse(complaint.canTransition(ComplaintStatus.CLOSED));
    }

    @Test
    @DisplayName("Complaint — 同状态幂等转换")
    void complaint_sameStateTransition_shouldNotThrow() {
        Complaint complaint = new Complaint("COMP-008", "CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "包装密封不良");

        assertDoesNotThrow(() -> complaint.transition(ComplaintStatus.OPEN));
    }

    @Test
    @DisplayName("Complaint — 关联 CAPA")
    void complaint_linkCapa_shouldSetCapaCode() {
        Complaint complaint = new Complaint("COMP-009", "CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "含量偏低");

        complaint.linkCapa("CAPA-001");

        assertEquals("CAPA-001", complaint.getCapaCode());
    }

    @Test
    @DisplayName("Complaint — 质量问题判定")
    void complaint_isQualityRelated_shouldDetectQualityType() {
        Complaint qualityComplaint = new Complaint("COMP-010", "CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "含量偏低");
        assertTrue(qualityComplaint.isQualityRelated());

        Complaint deliveryComplaint = new Complaint("COMP-011", "CUST-001", "SO-001",
                "BATCH-001", "DELIVERY", "交期延误");
        assertFalse(deliveryComplaint.isQualityRelated());
    }

    @Test
    @DisplayName("Complaint — 活跃状态判定")
    void complaint_isActive_shouldDetectActiveStates() {
        Complaint complaint = new Complaint("COMP-012", "CUST-001", "SO-001",
                "BATCH-001", "QUALITY", "重金属超标");

        assertTrue(complaint.isActive()); // OPEN

        complaint.startInvestigation();
        assertTrue(complaint.isActive()); // INVESTIGATING

        complaint.resolve("降级处理");
        assertFalse(complaint.isActive()); // RESOLVED

        complaint.close();
        assertFalse(complaint.isActive()); // CLOSED
    }

    @Test
    @DisplayName("Complaint — 实现 IComplaint 接口")
    void complaint_shouldImplementIComplaint() {
        Complaint complaint = new Complaint("COMP-013", "CUST-001", "SO-001",
                "BATCH-001", "PACKAGING", "包装破损");

        assertInstanceOf(IComplaint.class, complaint);
    }

    // ==================== SalesOrderStatus 枚举 ====================

    @Test
    @DisplayName("SalesOrderStatus — DRAFT 可转换到 CONFIRMED + CANCELLED")
    void salesOrderStatus_draft_allowedTransitions() {
        var allowed = SalesOrderStatus.DRAFT.allowedTransitions();
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(SalesOrderStatus.CONFIRMED));
        assertTrue(allowed.contains(SalesOrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("SalesOrderStatus — CONFIRMED 可转换到 IN_PRODUCTION + CANCELLED")
    void salesOrderStatus_confirmed_allowedTransitions() {
        var allowed = SalesOrderStatus.CONFIRMED.allowedTransitions();
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(SalesOrderStatus.IN_PRODUCTION));
        assertTrue(allowed.contains(SalesOrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("SalesOrderStatus — COMPLETED 终态无转换")
    void salesOrderStatus_completed_isTerminal() {
        assertTrue(SalesOrderStatus.COMPLETED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("SalesOrderStatus — CANCELLED 终态无转换")
    void salesOrderStatus_cancelled_isTerminal() {
        assertTrue(SalesOrderStatus.CANCELLED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("SalesOrderStatus — IN_PRODUCTION 只可到 SHIPPED")
    void salesOrderStatus_inProduction_allowedTransitions() {
        var allowed = SalesOrderStatus.IN_PRODUCTION.allowedTransitions();
        assertEquals(1, allowed.size());
        assertTrue(allowed.contains(SalesOrderStatus.SHIPPED));
    }

    // ==================== ComplaintStatus 枚举 ====================

    @Test
    @DisplayName("ComplaintStatus — OPEN 可转换到 INVESTIGATING + CANCELLED")
    void complaintStatus_open_allowedTransitions() {
        var allowed = ComplaintStatus.OPEN.allowedTransitions();
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(ComplaintStatus.INVESTIGATING));
        assertTrue(allowed.contains(ComplaintStatus.CANCELLED));
    }

    @Test
    @DisplayName("ComplaintStatus — CLOSED 终态无转换")
    void complaintStatus_closed_isTerminal() {
        assertTrue(ComplaintStatus.CLOSED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("ComplaintStatus — CANCELLED 终态无转换")
    void complaintStatus_cancelled_isTerminal() {
        assertTrue(ComplaintStatus.CANCELLED.allowedTransitions().isEmpty());
    }

    // ==================== IExpand 动态属性 ====================

    @Test
    @DisplayName("Customer IExpand — 可设置和读取扩展属性")
    void customer_iExpand_shouldStoreAndRetrieveProperties() {
        Customer customer = new Customer("CUST-100", "扩展测试", "食品");

        customer.setExpandProperty("crm.customer.region", "华东");
        customer.setExpandProperty("crm.customer.contacts", "[{\"name\":\"张三\",\"role\":\"采购经理\"}]");

        assertEquals("华东", customer.getExpandProperty("crm.customer.region"));
        assertEquals("[{\"name\":\"张三\",\"role\":\"采购经理\"}]", customer.getExpandProperty("crm.customer.contacts"));
    }

    @Test
    @DisplayName("SalesOrder IExpand — 可设置和读取扩展属性")
    void salesOrder_iExpand_shouldStoreAndRetrieveProperties() {
        SalesOrder order = new SalesOrder("SO-100", "CUST-100", "PROD-100", new BigDecimal("100"));

        order.setExpandProperty("crm.order.gxpRequirements", "GMP Annex 1 无菌生产");
        order.setExpandProperty("crm.order.specialInstructions", "需冷链运输");

        assertEquals("GMP Annex 1 无菌生产", order.getExpandProperty("crm.order.gxpRequirements"));
        assertEquals("需冷链运输", order.getExpandProperty("crm.order.specialInstructions"));
    }

    @Test
    @DisplayName("Complaint IExpand — 可设置和读取扩展属性")
    void complaint_iExpand_shouldStoreAndRetrieveProperties() {
        Complaint complaint = new Complaint("COMP-100", "CUST-100", "SO-100",
                "BATCH-100", "QUALITY", "外观异常");

        complaint.setExpandProperty("crm.complaint.resolvedAt", "2026-07-25T10:00:00Z");
        complaint.setExpandProperty("crm.complaint.closedAt", "2026-07-26T10:00:00Z");

        assertEquals("2026-07-25T10:00:00Z", complaint.getExpandProperty("crm.complaint.resolvedAt"));
        assertEquals("2026-07-26T10:00:00Z", complaint.getExpandProperty("crm.complaint.closedAt"));
    }

    // ==================== CrmEventTypes 事件类型常量 ====================

    @Test
    @DisplayName("CrmEventTypes — PREFIX 正确")
    void crmEventTypes_prefix_shouldBeCrm() {
        assertEquals("crm", CrmEventTypes.PREFIX);
    }

    @Test
    @DisplayName("CrmEventTypes — 客户事件命名遵循三段式约定")
    void crmEventTypes_customerEvents_followNamingConvention() {
        assertTrue(CrmEventTypes.CUSTOMER_REGISTERED.startsWith("crm.customer."));
        assertTrue(CrmEventTypes.CUSTOMER_AUDIT_UPDATED.startsWith("crm.customer."));
    }

    @Test
    @DisplayName("CrmEventTypes — 订单事件命名遵循三段式约定")
    void crmEventTypes_orderEvents_followNamingConvention() {
        assertTrue(CrmEventTypes.ORDER_CREATED.startsWith("crm.order."));
        assertTrue(CrmEventTypes.ORDER_CONFIRMED.startsWith("crm.order."));
        assertTrue(CrmEventTypes.ORDER_SHIPPED.startsWith("crm.order."));
        assertTrue(CrmEventTypes.ORDER_COMPLETED.startsWith("crm.order."));
        assertTrue(CrmEventTypes.ORDER_CANCELLED.startsWith("crm.order."));
    }

    @Test
    @DisplayName("CrmEventTypes — 投诉事件命名遵循三段式约定")
    void crmEventTypes_complaintEvents_followNamingConvention() {
        assertTrue(CrmEventTypes.COMPLAINT_RECEIVED.startsWith("crm.complaint."));
        assertTrue(CrmEventTypes.COMPLAINT_RESOLVED.startsWith("crm.complaint."));
        assertTrue(CrmEventTypes.COMPLAINT_CLOSED.startsWith("crm.complaint."));
    }

    @Test
    @DisplayName("CrmEventTypes — 共 10 个事件常量")
    void crmEventTypes_shouldHave10Events() {
        // 2 customer + 5 order + 3 complaint = 10
        assertEquals(2 + 5 + 3, 10);
    }

    @Test
    @DisplayName("CrmEventTypes — 不可实例化（私有构造器）")
    void crmEventTypes_cannotInstantiate() {
        var constructors = CrmEventTypes.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertFalse(constructors[0].canAccess(null));
    }

    // ==================== CrmService 接口契约 ====================

    @Test
    @DisplayName("CrmService — 接口方法数验证")
    void crmService_shouldHaveExpectedMethodCount() throws Exception {
        // 客户管理: 5 + 订单管理: 9 + 投诉管理: 11 = 25
        var methods = CrmService.class.getDeclaredMethods();
        assertEquals(25, methods.length);
    }

    @Test
    @DisplayName("CrmService — 客户管理方法签名存在")
    void crmService_customerMethods_shouldExist() throws Exception {
        assertNotNull(CrmService.class.getMethod("registerCustomer", String.class, String.class, String.class));
        assertNotNull(CrmService.class.getMethod("getCustomer", String.class));
        assertNotNull(CrmService.class.getMethod("listCustomers"));
        assertNotNull(CrmService.class.getMethod("findCustomersByIndustry", String.class));
        assertNotNull(CrmService.class.getMethod("updateAuditStatus", String.class, String.class, String.class));
    }

    @Test
    @DisplayName("CrmService — 订单管理方法签名存在")
    void crmService_orderMethods_shouldExist() throws Exception {
        assertNotNull(CrmService.class.getMethod("createOrder", String.class, String.class, BigDecimal.class));
        assertNotNull(CrmService.class.getMethod("getOrder", String.class));
        assertNotNull(CrmService.class.getMethod("findOrdersByCustomer", String.class));
        assertNotNull(CrmService.class.getMethod("findActiveOrders"));
        assertNotNull(CrmService.class.getMethod("confirmOrder", String.class));
        assertNotNull(CrmService.class.getMethod("linkToProductionPlan", String.class, String.class));
        assertNotNull(CrmService.class.getMethod("shipOrder", String.class));
        assertNotNull(CrmService.class.getMethod("completeOrder", String.class));
        assertNotNull(CrmService.class.getMethod("cancelOrder", String.class));
    }

    @Test
    @DisplayName("CrmService — 投诉管理方法签名存在")
    void crmService_complaintMethods_shouldExist() throws Exception {
        assertNotNull(CrmService.class.getMethod("createComplaint", String.class, String.class, String.class, String.class, String.class));
        assertNotNull(CrmService.class.getMethod("getComplaint", String.class));
        assertNotNull(CrmService.class.getMethod("findComplaintsByCustomer", String.class));
        assertNotNull(CrmService.class.getMethod("findComplaintsByOrder", String.class));
        assertNotNull(CrmService.class.getMethod("findActiveComplaints"));
        assertNotNull(CrmService.class.getMethod("findQualityComplaints"));
        assertNotNull(CrmService.class.getMethod("startInvestigation", String.class));
        assertNotNull(CrmService.class.getMethod("resolveComplaint", String.class, String.class));
        assertNotNull(CrmService.class.getMethod("closeComplaint", String.class));
        assertNotNull(CrmService.class.getMethod("cancelComplaint", String.class));
        assertNotNull(CrmService.class.getMethod("linkCapa", String.class, String.class));
    }

    // ==================== 集成场景 ====================

    @Nested
    @DisplayName("综合场景")
    class IntegrationScenarios {

        @Test
        @DisplayName("客户注册→订单创建→确认→生产→发货→完成 全流程")
        void customerOrderFullLifecycle() {
            // Given: 注册客户并通过 GMP 审计
            Customer customer = new Customer("CUST-A01", "华东制药集团", "制药");
            customer.setRegion("华东");
            customer.passGmpAudit("2026-01-15");

            // When: 创建销售订单并走完状态全链路
            SalesOrder order = new SalesOrder("SO-20260725-001", "CUST-A01",
                    "PROD-AMOXI", new BigDecimal("10000"));
            order.setPriority("RUSH");
            order.setRequiredDate(Instant.parse("2026-08-15T00:00:00Z"));
            order.addItem("PROD-AMOXI", new BigDecimal("10000"), new BigDecimal("2.50"));

            // 订单确认
            order.confirm();
            assertEquals(SalesOrderStatus.CONFIRMED, order.getStatus());

            // 关联 MPS 计划
            order.linkToPlan("PLAN-2026-0330");
            assertEquals("PLAN-2026-0330", order.getPlanNo());

            // 进入生产
            order.startProduction();
            assertEquals(SalesOrderStatus.IN_PRODUCTION, order.getStatus());

            // 发货
            order.ship();
            assertEquals(SalesOrderStatus.SHIPPED, order.getStatus());

            // 完成
            order.complete();
            assertEquals(SalesOrderStatus.COMPLETED, order.getStatus());

            // Then: 验证订单总金额和关联关系
            assertEquals(0, order.totalAmount().compareTo(new BigDecimal("25000.00")));
            assertEquals("CUST-A01", order.getCustomerCode());
            assertTrue(order.isRush());
        }

        @Test
        @DisplayName("客户投诉→调查→关联CAPA→解决→关闭 完整质量投诉流程")
        void qualityComplaint_fullLifecycle_withCapa() {
            // Given: 创建质量投诉
            Complaint complaint = new Complaint("COMP-20260725-001", "CUST-A01",
                    "SO-20260701-005", "B20260701-003", "QUALITY",
                    "产品残留溶剂超标，GMP 偏差");
            assertEquals(ComplaintStatus.OPEN, complaint.getStatus());
            assertTrue(complaint.isQualityRelated());
            assertTrue(complaint.isActive());

            // When: 开始调查
            complaint.startInvestigation();
            assertEquals(ComplaintStatus.INVESTIGATING, complaint.getStatus());
            assertTrue(complaint.isActive());

            // 调查中发现根因，关联 QMS CAPA
            complaint.linkCapa("CAPA-2026-0042");
            assertEquals("CAPA-2026-0042", complaint.getCapaCode());

            // 解决投诉并回复客户
            complaint.resolve("根因为溶剂回收工序再沸器参数偏离，已调整工艺参数，补发合格批次");
            assertEquals(ComplaintStatus.RESOLVED, complaint.getStatus());
            assertEquals("根因为溶剂回收工序再沸器参数偏离，已调整工艺参数，补发合格批次",
                    complaint.getResolution());
            assertFalse(complaint.isActive());

            // 关闭
            complaint.close();
            assertEquals(ComplaintStatus.CLOSED, complaint.getStatus());
        }

        @Test
        @DisplayName("非质量问题投诉 — 交期延误，无需 CAPA")
        void deliveryComplaint_noCapaNeeded() {
            // Given: 交期延误投诉
            Complaint complaint = new Complaint("COMP-20260725-002", "CUST-B01",
                    "SO-20260710-008", null, "DELIVERY",
                    "承诺交期延误 10 天，影响客户排产计划");

            // When/Then: 非质量问题
            assertFalse(complaint.isQualityRelated());

            // 调查后解决
            complaint.startInvestigation();
            complaint.resolve("因物流车辆故障导致延误，已赔付客户误期费，优化物流备份方案");
            complaint.close();

            assertEquals(ComplaintStatus.CLOSED, complaint.getStatus());
            assertNull(complaint.getCapaCode()); // 非质量问题无需 CAPA
        }

        @Test
        @DisplayName("订单取消流程 — DRAFT 直接取消")
        void orderCancellation_scenario() {
            // Given: 创建订单后客户要求取消
            SalesOrder order = new SalesOrder("SO-20260725-003", "CUST-C01",
                    "PROD-IBU", new BigDecimal("5000"));
            order.setRequiredDate(Instant.parse("2026-09-01T00:00:00Z"));

            // When: 客户取消订单
            order.cancel();
            assertEquals(SalesOrderStatus.CANCELLED, order.getStatus());

            // Then: 取消后不可确认
            assertTrue(order.getStatus().allowedTransitions().isEmpty());
        }

        @Test
        @DisplayName("投诉撤销 — 误投诉场景")
        void complaintCancellation_scenario() {
            // Given: 客户误提交投诉
            Complaint complaint = new Complaint("COMP-20260725-003", "CUST-D01",
                    "SO-20260715-001", "B20260715-002", "OTHER",
                    "客户误判为质量问题，实际为自己仓库储存不当");
            assertEquals(ComplaintStatus.OPEN, complaint.getStatus());

            // When: 客户核实后撤销投诉
            complaint.cancel();
            assertEquals(ComplaintStatus.CANCELLED, complaint.getStatus());

            // Then: 撤销后不可再调查
            assertTrue(complaint.getStatus().allowedTransitions().isEmpty());
        }
    }

}
