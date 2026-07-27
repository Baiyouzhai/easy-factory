package com.byz.factory.scm.service;

import com.byz.factory.scm.InboundPlanStatus;
import com.byz.factory.scm.PurchaseOrderStatus;
import com.byz.factory.scm.SupplierStatus;
import com.byz.factory.scm.model.InboundPlan;
import com.byz.factory.scm.model.PurchaseOrder;
import com.byz.factory.scm.model.Supplier;
import com.byz.factory.scm.repository.InboundPlanRepository;
import com.byz.factory.scm.repository.PurchaseOrderRepository;
import com.byz.factory.scm.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(ScmServiceImpl.class)
@DisplayName("ScmServiceImpl 服务层集成测试")
class ScmServiceImplTest {

    @Autowired
    private SupplierRepository supplierRepo;

    @Autowired
    private PurchaseOrderRepository poRepo;

    @Autowired
    private InboundPlanRepository inboundPlanRepo;

    @Autowired
    private ScmServiceImpl scmService;

    private Supplier savedSupplier;

    @BeforeEach
    void setUp() {
        savedSupplier = scmService.registerSupplier("SUP-001", "华东原料供应", "RAW_MATERIAL");
        scmService.qualifySupplier("SUP-001");
    }

    // ==================== 供应商管理 ====================

    @Test
    @DisplayName("注册供应商 — 自动获得 code/name/默认状态")
    void registerSupplier_shouldCreateWithDefaults() {
        Supplier supplier = scmService.registerSupplier("SUP-002", "南方包材", "PACKAGING");

        assertEquals("SUP-002", supplier.getCode());
        assertEquals("南方包材", supplier.getName());
        assertEquals("PACKAGING", supplier.getCategory());
        assertEquals("UNDER_REVIEW", supplier.getQualification());
        assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
        assertNotNull(supplier.getId(), "注册后应有自增 id");
    }

    @Test
    @DisplayName("重复注册 — 应抛异常")
    void registerSupplier_duplicateCode_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> scmService.registerSupplier("SUP-001", "重名", "SERVICE"));
    }

    @Test
    @DisplayName("资质流转 — UNDER_REVIEW → QUALIFIED → DISQUALIFIED")
    void supplierQualificationLifecycle() {
        scmService.registerSupplier("SUP-003", "测试商", "RAW_MATERIAL");
        assertEquals("UNDER_REVIEW", scmService.findSupplier("SUP-003").getQualification());

        scmService.qualifySupplier("SUP-003");
        assertEquals("QUALIFIED", scmService.findSupplier("SUP-003").getQualification());

        scmService.disqualifySupplier("SUP-003");
        assertEquals("DISQUALIFIED", scmService.findSupplier("SUP-003").getQualification());
    }

    @Test
    @DisplayName("运营状态 — ACTIVE → INACTIVE → ACTIVE")
    void supplierStatusLifecycle() {
        assertEquals(SupplierStatus.ACTIVE, scmService.findSupplier("SUP-001").getStatus());

        scmService.deactivateSupplier("SUP-001");
        assertEquals(SupplierStatus.INACTIVE, scmService.findSupplier("SUP-001").getStatus());

        scmService.reactivateSupplier("SUP-001");
        assertEquals(SupplierStatus.ACTIVE, scmService.findSupplier("SUP-001").getStatus());
    }

    @Test
    @DisplayName("getQualifiedSuppliers — 返回所有 QUALIFIED 供应商")
    void getQualifiedSuppliers_shouldFilterByQualification() {
        scmService.registerSupplier("SUP-004", "待审核", "SERVICE");

        List<Supplier> qualified = scmService.getQualifiedSuppliers();
        assertEquals(1, qualified.size());
        assertEquals("SUP-001", qualified.get(0).getCode());
    }

    @Test
    @DisplayName("查找不存在的供应商 — 应抛异常")
    void findSupplier_notFound_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> scmService.findSupplier("NONEXISTENT"));
    }

    // ==================== 采购订单管理 ====================

    @Test
    @DisplayName("创建采购订单 — 初始状态 DRAFT")
    void createPurchaseOrder_shouldCreateAsDraft() {
        PurchaseOrder po = scmService.createPurchaseOrder("PO-001", "SUP-001");

        assertEquals("PO-001", po.getPoNo());
        assertEquals("SUP-001", po.getSupplierCode());
        assertEquals(PurchaseOrderStatus.DRAFT, po.getStatus());
        assertNotNull(po.getId());
    }

    @Test
    @DisplayName("重复 PO 号 — 应抛异常")
    void createPurchaseOrder_duplicatePoNo_shouldThrow() {
        scmService.createPurchaseOrder("PO-001", "SUP-001");

        assertThrows(IllegalArgumentException.class,
                () -> scmService.createPurchaseOrder("PO-001", "SUP-001"));
    }

    @Test
    @DisplayName("给 BLACKLISTED 供应商创建 PO — 应抛异常")
    void createPurchaseOrder_blacklistedSupplier_shouldThrow() {
        scmService.registerSupplier("SUP-005", "黑名单商", "RAW_MATERIAL");
        // 通过 repo 直接拉黑（service 无 blacklist 方法）
        Supplier s = supplierRepo.findByCode("SUP-005").orElseThrow();
        s.blacklist();
        supplierRepo.save(s);

        assertThrows(IllegalStateException.class,
                () -> scmService.createPurchaseOrder("PO-002", "SUP-005"));
    }

    @Test
    @DisplayName("PO 完整生命周期 — DRAFT → APPROVED → SENT → RECEIVING → COMPLETED")
    void purchaseOrderFullLifecycle() {
        PurchaseOrder po = scmService.createPurchaseOrder("PO-010", "SUP-001");
        po.addItem("MAT-001", new BigDecimal("100"));

        scmService.approvePurchaseOrder("PO-010", "张三");
        assertEquals(PurchaseOrderStatus.APPROVED, scmService.findPurchaseOrder("PO-010").getStatus());

        scmService.sendPurchaseOrder("PO-010");
        assertEquals(PurchaseOrderStatus.SENT, scmService.findPurchaseOrder("PO-010").getStatus());

        scmService.receivePurchaseOrder("PO-010", "MAT-001", new BigDecimal("40"));
        assertEquals(PurchaseOrderStatus.RECEIVING, scmService.findPurchaseOrder("PO-010").getStatus());

        scmService.receivePurchaseOrder("PO-010", "MAT-001", new BigDecimal("60"));
        assertEquals(PurchaseOrderStatus.COMPLETED, scmService.findPurchaseOrder("PO-010").getStatus());
    }

    @Test
    @DisplayName("PO 取消 — DRAFT → CANCELLED")
    void purchaseOrderCancel() {
        scmService.createPurchaseOrder("PO-011", "SUP-001");
        scmService.cancelPurchaseOrder("PO-011");

        assertEquals(PurchaseOrderStatus.CANCELLED, scmService.findPurchaseOrder("PO-011").getStatus());
    }

    @Test
    @DisplayName("收货不存在的物料 — 应抛异常")
    void receivePurchaseOrder_unknownMaterial_shouldThrow() {
        PurchaseOrder po = scmService.createPurchaseOrder("PO-012", "SUP-001");
        po.addItem("MAT-001", new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class,
                () -> scmService.receivePurchaseOrder("PO-012", "MAT-999", BigDecimal.ONE));
    }

    @Test
    @DisplayName("getPurchaseOrdersBySupplier — 查询供应商所有 PO")
    void getPurchaseOrdersBySupplier_shouldReturnAll() {
        scmService.createPurchaseOrder("PO-020", "SUP-001");
        scmService.createPurchaseOrder("PO-021", "SUP-001");

        List<PurchaseOrder> pos = scmService.getPurchaseOrdersBySupplier("SUP-001");
        assertEquals(2, pos.size());
    }

    // ==================== 来料计划 ====================

    @Test
    @DisplayName("创建来料计划 — 基于已审批的 PO")
    void createInboundPlan_shouldCreateFromApprovedPO() {
        PurchaseOrder po = scmService.createPurchaseOrder("PO-030", "SUP-001");
        po.addItem("MAT-001", new BigDecimal("100"));
        scmService.approvePurchaseOrder("PO-030", "张三");

        InboundPlan plan = scmService.createInboundPlan("IB-001", "PO-030",
                LocalDate.of(2026, 8, 15));

        assertEquals("IB-001", plan.getCode());
        assertEquals("PO-030", plan.getPoNo());
        assertEquals(InboundPlanStatus.CREATED, plan.getStatus());
        assertEquals(LocalDate.of(2026, 8, 15), plan.getExpectedDate());
        assertNotNull(plan.getId());
    }

    @Test
    @DisplayName("给 DRAFT 状态的 PO 创建来料计划 — 应抛异常")
    void createInboundPlan_draftPO_shouldThrow() {
        scmService.createPurchaseOrder("PO-031", "SUP-001");

        assertThrows(IllegalStateException.class,
                () -> scmService.createInboundPlan("IB-002", "PO-031", LocalDate.now()));
    }

    @Test
    @DisplayName("来料计划完整生命周期")
    void inboundPlanFullLifecycle() {
        PurchaseOrder po = scmService.createPurchaseOrder("PO-032", "SUP-001");
        po.addItem("MAT-001", new BigDecimal("100"));
        scmService.approvePurchaseOrder("PO-032", "张三");

        InboundPlan plan = scmService.createInboundPlan("IB-003", "PO-032", LocalDate.now());
        assertEquals(InboundPlanStatus.CREATED, plan.getStatus());

        scmService.notifyWarehouse("IB-003");
        assertTrue(inboundPlanRepo.findByCode("IB-003").orElseThrow().isNotifyWarehouse());
        assertEquals(InboundPlanStatus.NOTIFIED, inboundPlanRepo.findByCode("IB-003").orElseThrow().getStatus());
    }

    @Test
    @DisplayName("findInboundPlanByPoNo — 按 PO 号查询来料计划")
    void findInboundPlanByPoNo_shouldReturnPlan() {
        PurchaseOrder po = scmService.createPurchaseOrder("PO-033", "SUP-001");
        po.addItem("MAT-001", new BigDecimal("50"));
        scmService.approvePurchaseOrder("PO-033", "张三");
        scmService.createInboundPlan("IB-004", "PO-033", LocalDate.now());

        assertTrue(scmService.findInboundPlanByPoNo("PO-033").isPresent());
        assertFalse(scmService.findInboundPlanByPoNo("PO-999").isPresent());
    }

    @Test
    @DisplayName("getInboundPlansBySupplier — 查询供应商所有来料计划")
    void getInboundPlansBySupplier_shouldReturnAll() {
        scmService.registerSupplier("SUP-006", "另一个商", "EQUIPMENT");
        scmService.qualifySupplier("SUP-006");

        PurchaseOrder po1 = scmService.createPurchaseOrder("PO-034", "SUP-006");
        po1.addItem("MAT-001", new BigDecimal("10"));
        scmService.approvePurchaseOrder("PO-034", "张三");
        scmService.createInboundPlan("IB-005", "PO-034", LocalDate.now());

        PurchaseOrder po2 = scmService.createPurchaseOrder("PO-035", "SUP-006");
        po2.addItem("MAT-002", new BigDecimal("20"));
        scmService.approvePurchaseOrder("PO-035", "李四");
        scmService.createInboundPlan("IB-006", "PO-035", LocalDate.now());

        assertEquals(2, scmService.getInboundPlansBySupplier("SUP-006").size());
    }

}
