package com.byz.factory.mps;

import com.byz.factory.mps.model.CapacityCheck;
import com.byz.factory.mps.model.CapacityCheck.CapacityResult;
import com.byz.factory.mps.model.DemandSource;
import com.byz.factory.mps.model.DemandSourceType;
import com.byz.factory.mps.model.ProductionPlan;
import com.byz.factory.mps.model.ProductionPlan.PlanItem;
import com.byz.factory.mps.repository.DemandSourceRepository;
import com.byz.factory.mps.repository.ProductionPlanRepository;
import com.byz.factory.mps.service.MpsService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.byz.factory.mps.service.MpsService;
import com.byz.factory.mps.service.impl.MpsServiceImpl;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MPS 持久化 + 服务层集成测试 — 使用 H2 内存数据库验证 JPA 映射、Repository 查询和 Service 业务逻辑。
 *
 * @author 苏政
 */
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = MpsTestConfig.class)
@Import(MpsServiceImpl.class)
@DisplayName("MPS 持久化层集成测试")
public class MpsPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductionPlanRepository planRepo;

    @Autowired
    private DemandSourceRepository demandRepo;

    @Autowired
    private MpsService mpsService;

    // ==================== ProductionPlan 持久化 ====================

    @Test
    @DisplayName("持久化 — 保存并查询 ProductionPlan")
    void persist_productionPlan_saveAndFind() {
        ProductionPlan plan = new ProductionPlan("MP-001", "WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));
        planRepo.saveAndFlush(plan);
        em.clear();

        Optional<ProductionPlan> found = planRepo.findByPlanNo("MP-001");
        assertTrue(found.isPresent());
        assertEquals("WEEKLY", found.get().getPeriodType());
        assertEquals(ProductionPlanStatus.DRAFT, found.get().getStatus());
    }

    @Test
    @DisplayName("持久化 — 保存带 PlanItem 的 ProductionPlan")
    void persist_productionPlan_withItems() {
        ProductionPlan plan = new ProductionPlan("MP-002", "MONTHLY",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        plan.addItem(new PlanItem("P-001", "产品A",
                new BigDecimal("5000"), LocalDate.of(2026, 7, 15), 1, "FACTORY-A"));
        plan.addItem(new PlanItem("P-002", "产品B",
                new BigDecimal("3000"), LocalDate.of(2026, 7, 20), 2, "FACTORY-B"));
        planRepo.saveAndFlush(plan);
        em.clear();

        Optional<ProductionPlan> found = planRepo.findByPlanNo("MP-002");
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getItems().size());
        assertEquals("P-001", found.get().getItems().get(0).getProductCode());
        assertEquals("P-002", found.get().getItems().get(1).getProductCode());
    }

    @Test
    @DisplayName("持久化 — 状态变更后持久化")
    void persist_productionPlan_statusTransition() {
        ProductionPlan plan = new ProductionPlan("MP-003", "WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));
        planRepo.saveAndFlush(plan);

        plan.approve("张三");
        planRepo.saveAndFlush(plan);
        em.clear();

        Optional<ProductionPlan> found = planRepo.findByPlanNo("MP-003");
        assertTrue(found.isPresent());
        assertEquals(ProductionPlanStatus.APPROVED, found.get().getStatus());
        assertEquals("张三", found.get().getApprovedBy());
    }

    @Test
    @DisplayName("持久化 — 按状态查询")
    void persist_findByStatus() {
        ProductionPlan draft = new ProductionPlan("MP-D1", "WEEKLY",
                LocalDate.now(), LocalDate.now().plusDays(6));
        ProductionPlan draft2 = new ProductionPlan("MP-D2", "MONTHLY",
                LocalDate.now(), LocalDate.now().plusDays(30));
        planRepo.saveAndFlush(draft);
        planRepo.saveAndFlush(draft2);

        List<ProductionPlan> drafts = planRepo.findByStatus(ProductionPlanStatus.DRAFT);
        assertTrue(drafts.size() >= 2);
    }

    @Test
    @DisplayName("持久化 — 按周期类型查询")
    void persist_findByPeriodType() {
        ProductionPlan plan = new ProductionPlan("MP-W1", "WEEKLY",
                LocalDate.now(), LocalDate.now().plusDays(6));
        planRepo.saveAndFlush(plan);
        em.clear();

        List<ProductionPlan> weeklies = planRepo.findByPeriodType("WEEKLY");
        assertFalse(weeklies.isEmpty());
        assertTrue(weeklies.stream().allMatch(p -> "WEEKLY".equals(p.getPeriodType())));
    }

    @Test
    @DisplayName("持久化 — findByPlanNo 不存在返回 empty")
    void persist_findByPlanNo_notFound() {
        Optional<ProductionPlan> found = planRepo.findByPlanNo("NONEXISTENT");
        assertTrue(found.isEmpty());
    }

    // ==================== DemandSource 持久化 ====================

    @Test
    @DisplayName("持久化 — 保存并查询 DemandSource")
    void persist_demandSource_saveAndFind() {
        DemandSource demand = new DemandSource("SO-001", "P-001", new BigDecimal("5000"));
        demand.setDueDate(LocalDate.of(2026, 8, 1));
        demand.register(DemandSourceType.SALES_ORDER, "客户A");
        demandRepo.saveAndFlush(demand);
        em.clear();

        DemandSource found = demandRepo.findByReferenceNo("SO-001");
        assertNotNull(found);
        assertEquals("P-001", found.getProductCode());
        assertEquals(DemandSourceType.SALES_ORDER.name(), found.getSourceType());
        assertEquals("客户A", found.getCustomer());
    }

    @Test
    @DisplayName("持久化 — 按产品编码查询需求")
    void persist_findByProductCode() {
        DemandSource d1 = new DemandSource("SO-002", "P-A", new BigDecimal("1000"));
        d1.setDueDate(LocalDate.now());
        demandRepo.saveAndFlush(d1);

        DemandSource d2 = new DemandSource("FC-001", "P-A", new BigDecimal("2000"));
        d2.setDueDate(LocalDate.now());
        demandRepo.saveAndFlush(d2);
        em.clear();

        List<DemandSource> demands = demandRepo.findByProductCode("P-A");
        assertEquals(2, demands.size());
    }

    @Test
    @DisplayName("持久化 — 按来源类型查询")
    void persist_findBySourceType() {
        DemandSource demand = new DemandSource("SO-003", "P-B", new BigDecimal("3000"));
        demand.setDueDate(LocalDate.now());
        demand.register(DemandSourceType.SALES_ORDER, "客户B");
        demandRepo.saveAndFlush(demand);
        em.clear();

        List<DemandSource> salesOrders = demandRepo.findBySourceType("SALES_ORDER");
        assertFalse(salesOrders.isEmpty());
        assertTrue(salesOrders.stream().allMatch(d -> "SALES_ORDER".equals(d.getSourceType())));
    }

    @Test
    @DisplayName("持久化 — 按产品编码和来源类型查询")
    void persist_findByProductCodeAndSourceType() {
        DemandSource demand = new DemandSource("SO-004", "P-C", new BigDecimal("4000"));
        demand.setDueDate(LocalDate.now());
        demand.register(DemandSourceType.SALES_ORDER, "客户C");
        demandRepo.saveAndFlush(demand);
        em.clear();

        List<DemandSource> results = demandRepo.findByProductCodeAndSourceType("P-C", "SALES_ORDER");
        assertEquals(1, results.size());
        assertEquals("SO-004", results.get(0).getReferenceNo());
    }

    // ==================== MpsService 集成测试 ====================

    @Test
    @DisplayName("Service — 创建并查询计划")
    void service_createAndGetPlan() {
        ProductionPlan plan = mpsService.create("WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));

        assertNotNull(plan);
        assertEquals(ProductionPlanStatus.DRAFT, plan.getStatus());
        assertNotNull(plan.getPlanNo());

        Optional<ProductionPlan> found = mpsService.getPlan(plan.getPlanNo());
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Service — 添加明细项")
    void service_addPlanItem() {
        ProductionPlan plan = mpsService.create("WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));

        mpsService.addPlanItem(plan.getPlanNo(),
                new PlanItem("P-001", "产品A", new BigDecimal("5000"),
                        LocalDate.of(2026, 8, 1), 1, "FACTORY-A"));

        Optional<ProductionPlan> found = mpsService.getPlan(plan.getPlanNo());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getItems().size());
        assertEquals("P-001", found.get().getItems().get(0).getProductCode());
    }

    @Test
    @DisplayName("Service — 完整生命周期流程")
    void service_fullLifecycle() {
        ProductionPlan plan = mpsService.create("WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));
        String planNo = plan.getPlanNo();

        // DRAFT → APPROVED
        mpsService.approve(planNo, "张三");
        ProductionPlan approved = mpsService.getPlan(planNo).orElseThrow();
        assertEquals(ProductionPlanStatus.APPROVED, approved.getStatus());
        assertEquals("张三", approved.getApprovedBy());

        // APPROVED → RELEASED
        mpsService.release(planNo);
        ProductionPlan released = mpsService.getPlan(planNo).orElseThrow();
        assertEquals(ProductionPlanStatus.RELEASED, released.getStatus());

        // RELEASED → IN_PROGRESS
        mpsService.start(planNo);
        ProductionPlan started = mpsService.getPlan(planNo).orElseThrow();
        assertEquals(ProductionPlanStatus.IN_PROGRESS, started.getStatus());

        // IN_PROGRESS → COMPLETED
        mpsService.complete(planNo);
        ProductionPlan completed = mpsService.getPlan(planNo).orElseThrow();
        assertEquals(ProductionPlanStatus.COMPLETED, completed.getStatus());

        // COMPLETED → CLOSED
        mpsService.close(planNo);
        ProductionPlan closed = mpsService.getPlan(planNo).orElseThrow();
        assertEquals(ProductionPlanStatus.CLOSED, closed.getStatus());
    }

    @Test
    @DisplayName("Service — 驳回（APPROVED → DRAFT）")
    void service_reject_backToDraft() {
        ProductionPlan plan = mpsService.create("WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));
        mpsService.approve(plan.getPlanNo(), "张三");

        mpsService.reject(plan.getPlanNo());

        ProductionPlan found = mpsService.getPlan(plan.getPlanNo()).orElseThrow();
        assertEquals(ProductionPlanStatus.DRAFT, found.getStatus());
    }

    @Test
    @DisplayName("Service — 注册需求")
    void service_registerDemand() {
        DemandSource demand = mpsService.registerDemand(DemandSourceType.SALES_ORDER,
                "SO-010", "P-001", new BigDecimal("10000"),
                LocalDate.of(2026, 8, 15));

        assertNotNull(demand);
        assertEquals("SO-010", demand.getReferenceNo());
        assertEquals(DemandSourceType.SALES_ORDER.name(), demand.getSourceType());
    }

    @Test
    @DisplayName("Service — 按产品查询需求")
    void service_getDemands() {
        mpsService.registerDemand(DemandSourceType.SALES_ORDER,
                "SO-020", "P-Z", new BigDecimal("100"),
                LocalDate.of(2026, 8, 1));
        mpsService.registerDemand(DemandSourceType.FORECAST,
                "FC-020", "P-Z", new BigDecimal("200"),
                LocalDate.of(2026, 9, 1));

        List<DemandSource> demands = mpsService.getDemands("P-Z");
        assertEquals(2, demands.size());
    }

    @Test
    @DisplayName("Service — listPlans 按周期类型过滤")
    void service_listPlans_byPeriodType() {
        mpsService.create("WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));

        List<ProductionPlan> plans = mpsService.listPlans("WEEKLY", null);
        assertFalse(plans.isEmpty());
        assertTrue(plans.stream().allMatch(p -> "WEEKLY".equals(p.getPeriodType())));
    }

    @Test
    @DisplayName("Service — listPlans 按状态过滤")
    void service_listPlans_byStatus() {
        mpsService.create("WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));

        List<ProductionPlan> drafts = mpsService.listPlans(null, "DRAFT");
        assertFalse(drafts.isEmpty());
        assertTrue(drafts.stream().allMatch(p -> p.getStatus() == ProductionPlanStatus.DRAFT));
    }

    @Test
    @DisplayName("Service — listPlans 查询全部")
    void service_listPlans_all() {
        mpsService.create("WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));
        mpsService.create("MONTHLY",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        List<ProductionPlan> all = mpsService.listPlans(null, null);
        assertTrue(all.size() >= 2);
    }

    @Test
    @DisplayName("Service — checkCapacity 暂无明细时 PASS")
    void service_checkCapacity_emptyItems() {
        ProductionPlan plan = mpsService.create("WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));

        CapacityCheck check = mpsService.checkCapacity(plan.getPlanNo(), "FACTORY-A");
        assertEquals(CapacityResult.PASS, check.status());
        assertEquals("无生产明细，无需检查", check.bottleneck());
    }

    @Test
    @DisplayName("Service — checkCapacity 有明细时返回结果")
    void service_checkCapacity_withItems() {
        ProductionPlan plan = mpsService.create("WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));
        mpsService.addPlanItem(plan.getPlanNo(),
                new PlanItem("P-001", "产品A", new BigDecimal("100"),
                        LocalDate.of(2026, 8, 1), 1, "FACTORY-A"));

        CapacityCheck check = mpsService.checkCapacity(plan.getPlanNo(), "FACTORY-A");
        assertNotNull(check);
        assertEquals("FACTORY-A", check.factoryCode());
        assertNotNull(check.status());
        assertNotNull(check.utilizationRate());
    }

    @Test
    @DisplayName("Service — getPlan 不存在返回 empty")
    void service_getPlan_notFound() {
        Optional<ProductionPlan> found = mpsService.getPlan("NONEXISTENT");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Service — approve 不存在计划抛出异常")
    void service_approve_notFound() {
        assertThrows(IllegalArgumentException.class,
                () -> mpsService.approve("NONEXISTENT", "张三"));
    }

    @Test
    @DisplayName("Service — release 不存在计划抛出异常")
    void service_release_notFound() {
        assertThrows(IllegalArgumentException.class,
                () -> mpsService.release("NONEXISTENT"));
    }

    @Test
    @DisplayName("Service — checkCapacity 不存在计划抛出异常")
    void service_checkCapacity_notFound() {
        assertThrows(IllegalArgumentException.class,
                () -> mpsService.checkCapacity("NONEXISTENT", "FACTORY-A"));
    }

    @Test
    @DisplayName("Service — addPlanItem 不存在计划抛出异常")
    void service_addPlanItem_notFound() {
        assertThrows(IllegalArgumentException.class,
                () -> mpsService.addPlanItem("NONEXISTENT",
                        new PlanItem("P-001", "产品A", BigDecimal.ONE,
                                LocalDate.now(), 1, "FACTORY-A")));
    }

}
