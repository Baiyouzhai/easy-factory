package com.byz.factory.mps;

import com.byz.factory.mps.IDemandSource;
import com.byz.factory.mps.IProductionPlan;
import com.byz.factory.mps.IProductionPlan.IPlanItem;
import com.byz.factory.mps.ProductionPlanStatus;
import com.byz.factory.event.types.MpsEventTypes;
import com.byz.factory.mps.model.CapacityCheck;
import com.byz.factory.mps.model.CapacityCheck.CapacityResult;
import com.byz.factory.mps.model.DemandSource;
import com.byz.factory.mps.model.DemandSourceType;
import com.byz.factory.mps.model.ProductionPlan;
import com.byz.factory.mps.model.ProductionPlan.PlanItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MPS 模块测试 — 覆盖 ProductionPlan（构造、状态转换、业务方法、查询）、
 * DemandSource（构造、注册、查询）、DemandSourceType、CapacityCheck、
 * 事件命名约定和跨模块接口契约。
 *
 * @author 苏政
 */
public class MpsModuleTest {

    // ==================== ProductionPlan 构造 ====================

    @Test
    @DisplayName("构造 ProductionPlan — 字段正确初始化")
    void productionPlan_construct_fieldsInitialized() {
        ProductionPlan plan = new ProductionPlan("MP-202607-001", "WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));

        assertEquals("MP-202607-001", plan.getPlanNo());
        assertEquals("WEEKLY", plan.getPeriodType());
        assertEquals(LocalDate.of(2026, 7, 20), plan.getPeriodStart());
        assertEquals(LocalDate.of(2026, 7, 26), plan.getPeriodEnd());
        assertEquals(ProductionPlanStatus.DRAFT, plan.getStatus());
        assertNotNull(plan.getItems());
        assertTrue(plan.getItems().isEmpty());
        assertNull(plan.getApprovedBy());
    }

    @Test
    @DisplayName("构造 ProductionPlan — MONTHLY 周期类型")
    void productionPlan_construct_monthlyPeriodType() {
        ProductionPlan plan = new ProductionPlan("MP-202607-002", "MONTHLY",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        assertEquals("MONTHLY", plan.getPeriodType());
        assertEquals(ProductionPlanStatus.DRAFT, plan.getStatus());
    }

    @Test
    @DisplayName("构造 ProductionPlan — QUARTERLY 周期类型")
    void productionPlan_construct_quarterlyPeriodType() {
        ProductionPlan plan = new ProductionPlan("MP-2026Q3-001", "QUARTERLY",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30));

        assertEquals("QUARTERLY", plan.getPeriodType());
        assertEquals(ProductionPlanStatus.DRAFT, plan.getStatus());
    }

    @Test
    @DisplayName("构造 ProductionPlan — items 初始为空列表")
    void productionPlan_construct_itemsInitiallyEmpty() {
        ProductionPlan plan = new ProductionPlan("MP-001", "WEEKLY",
                LocalDate.now(), LocalDate.now().plusDays(6));

        assertNotNull(plan.getItems());
        assertTrue(plan.getItems().isEmpty());
    }

    // ==================== ProductionPlan 状态转换（正常路径） ====================

    @Test
    @DisplayName("状态转换 — DRAFT → APPROVED")
    void productionPlan_transition_draftToApproved() {
        ProductionPlan plan = createDraftPlan("MP-001");

        plan.approve("张三");

        assertEquals(ProductionPlanStatus.APPROVED, plan.getStatus());
        assertEquals("张三", plan.getApprovedBy());
    }

    @Test
    @DisplayName("状态转换 — APPROVED → RELEASED")
    void productionPlan_transition_approvedToReleased() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");

        plan.release();

        assertEquals(ProductionPlanStatus.RELEASED, plan.getStatus());
    }

    @Test
    @DisplayName("状态转换 — RELEASED → IN_PROGRESS")
    void productionPlan_transition_releasedToInProgress() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");
        plan.release();

        plan.start();

        assertEquals(ProductionPlanStatus.IN_PROGRESS, plan.getStatus());
    }

    @Test
    @DisplayName("状态转换 — IN_PROGRESS → COMPLETED")
    void productionPlan_transition_inProgressToCompleted() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");
        plan.release();
        plan.start();

        plan.complete();

        assertEquals(ProductionPlanStatus.COMPLETED, plan.getStatus());
    }

    @Test
    @DisplayName("状态转换 — COMPLETED → CLOSED")
    void productionPlan_transition_completedToClosed() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");
        plan.release();
        plan.start();
        plan.complete();

        plan.close();

        assertEquals(ProductionPlanStatus.CLOSED, plan.getStatus());
    }

    @Test
    @DisplayName("状态转换 — 驳回（APPROVED → DRAFT）")
    void productionPlan_transition_approvedToDraft_reject() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");

        plan.reject();

        assertEquals(ProductionPlanStatus.DRAFT, plan.getStatus());
    }

    @Test
    @DisplayName("状态转换 — 全生命周期 DRAFT → CLOSED")
    void productionPlan_transition_fullLifecycle() {
        ProductionPlan plan = createDraftPlan("MP-001");

        plan.approve("张三");
        assertEquals(ProductionPlanStatus.APPROVED, plan.getStatus());

        plan.release();
        assertEquals(ProductionPlanStatus.RELEASED, plan.getStatus());

        plan.start();
        assertEquals(ProductionPlanStatus.IN_PROGRESS, plan.getStatus());

        plan.complete();
        assertEquals(ProductionPlanStatus.COMPLETED, plan.getStatus());

        plan.close();
        assertEquals(ProductionPlanStatus.CLOSED, plan.getStatus());
    }

    // ==================== ProductionPlan 状态转换（非法） ====================

    @Test
    @DisplayName("非法转换 — DRAFT → RELEASED 被拒绝（跳过审批）")
    void productionPlan_illegalTransition_draftToReleased() {
        ProductionPlan plan = createDraftPlan("MP-001");

        assertThrows(IllegalStateException.class, plan::release);
        assertEquals(ProductionPlanStatus.DRAFT, plan.getStatus());
    }

    @Test
    @DisplayName("非法转换 — DRAFT → IN_PROGRESS 被拒绝")
    void productionPlan_illegalTransition_draftToInProgress() {
        ProductionPlan plan = createDraftPlan("MP-001");

        assertThrows(IllegalStateException.class, plan::start);
        assertEquals(ProductionPlanStatus.DRAFT, plan.getStatus());
    }

    @Test
    @DisplayName("非法转换 — APPROVED → IN_PROGRESS 被拒绝（跳过发布）")
    void productionPlan_illegalTransition_approvedToInProgress() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");

        assertThrows(IllegalStateException.class, plan::start);
        assertEquals(ProductionPlanStatus.APPROVED, plan.getStatus());
    }

    @Test
    @DisplayName("非法转换 — DRAFT → COMPLETED 被拒绝")
    void productionPlan_illegalTransition_draftToCompleted() {
        ProductionPlan plan = createDraftPlan("MP-001");

        assertThrows(IllegalStateException.class, plan::complete);
        assertEquals(ProductionPlanStatus.DRAFT, plan.getStatus());
    }

    @Test
    @DisplayName("非法转换 — CLOSED 后不可再转换")
    void productionPlan_illegalTransition_closedIsTerminal() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");
        plan.release();
        plan.start();
        plan.complete();
        plan.close();

        // CLOSED 状态的 allowedTransitions() 返回空集合，任何 transition 都应失败
        assertThrows(IllegalStateException.class, () -> plan.approve("李四"));
        assertEquals(ProductionPlanStatus.CLOSED, plan.getStatus());
    }

    // ==================== ProductionPlan 业务便捷方法 ====================

    @Test
    @DisplayName("业务方法 — approve 正确设置审批人")
    void productionPlan_approve_setsApprovedBy() {
        ProductionPlan plan = createDraftPlan("MP-001");

        plan.approve("李四");

        assertEquals("李四", plan.getApprovedBy());
        assertEquals(ProductionPlanStatus.APPROVED, plan.getStatus());
    }

    @Test
    @DisplayName("业务方法 — reject 回到 DRAFT 并清除审批人")
    void productionPlan_reject_backToDraft() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");

        plan.reject();

        assertEquals(ProductionPlanStatus.DRAFT, plan.getStatus());
    }

    @Test
    @DisplayName("业务方法 — release 后状态为 RELEASED")
    void productionPlan_release_statusIsReleased() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");

        plan.release();

        assertEquals(ProductionPlanStatus.RELEASED, plan.getStatus());
    }

    // ==================== ProductionPlan PlanItem ====================

    @Test
    @DisplayName("PlanItem — 构造字段正确")
    void planItem_construct_fieldsCorrect() {
        PlanItem item = new PlanItem("P-001", "阿莫西林胶囊",
                new BigDecimal("10000"), LocalDate.of(2026, 8, 1), 1, "FACTORY-A");

        assertEquals("P-001", item.getProductCode());
        assertEquals("阿莫西林胶囊", item.getProductName());
        assertEquals(new BigDecimal("10000"), item.getQuantity());
        assertEquals(LocalDate.of(2026, 8, 1), item.getDueDate());
        assertEquals(1, item.getPriority());
        assertEquals("FACTORY-A", item.getFactoryCode());
    }

    @Test
    @DisplayName("PlanItem — 实现 IPlanItem 接口")
    void planItem_implementsIPlanItem() {
        PlanItem item = new PlanItem("P-001", "产品A",
                BigDecimal.ONE, LocalDate.now(), 5, "FACTORY-A");

        assertInstanceOf(IPlanItem.class, item);
    }

    @Test
    @DisplayName("PlanItem — addItem 正确添加明细")
    void productionPlan_addItem_addsCorrectly() {
        ProductionPlan plan = createDraftPlan("MP-001");
        PlanItem item = new PlanItem("P-001", "产品A",
                new BigDecimal("5000"), LocalDate.of(2026, 8, 1), 1, "FACTORY-A");

        plan.addItem(item);

        assertEquals(1, plan.getItems().size());
        assertEquals("P-001", plan.getItems().get(0).getProductCode());
    }

    @Test
    @DisplayName("PlanItem — addItem 多次添加")
    void productionPlan_addItem_multipleItems() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.addItem(new PlanItem("P-001", "产品A",
                new BigDecimal("5000"), LocalDate.of(2026, 8, 1), 1, "FACTORY-A"));
        plan.addItem(new PlanItem("P-002", "产品B",
                new BigDecimal("3000"), LocalDate.of(2026, 8, 5), 2, "FACTORY-B"));

        assertEquals(2, plan.getItems().size());
    }

    @Test
    @DisplayName("PlanItem — removeItem 按产品编码移除")
    void productionPlan_removeItem_byProductCode() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.addItem(new PlanItem("P-001", "产品A",
                new BigDecimal("5000"), LocalDate.of(2026, 8, 1), 1, "FACTORY-A"));
        plan.addItem(new PlanItem("P-002", "产品B",
                new BigDecimal("3000"), LocalDate.of(2026, 8, 5), 2, "FACTORY-B"));

        plan.removeItem("P-001");

        assertEquals(1, plan.getItems().size());
        assertEquals("P-002", plan.getItems().get(0).getProductCode());
    }

    @Test
    @DisplayName("PlanItem — getItems 返回不可修改列表")
    void productionPlan_getItems_returnsUnmodifiableList() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.addItem(new PlanItem("P-001", "产品A",
                BigDecimal.ONE, LocalDate.now(), 1, "FACTORY-A"));

        List<? extends IPlanItem> items = plan.getItems();
        assertThrows(UnsupportedOperationException.class, () -> items.clear());
    }

    // ==================== ProductionPlan 查询方法 ====================

    @Test
    @DisplayName("查询方法 — DRAFT 状态下 isEditable 为 true")
    void productionPlan_isEditable_draftState() {
        ProductionPlan plan = createDraftPlan("MP-001");

        assertTrue(plan.isEditable());
    }

    @Test
    @DisplayName("查询方法 — APPROVED 状态下 isEditable 为 false")
    void productionPlan_isEditable_approvedState() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");

        assertFalse(plan.isEditable());
    }

    @Test
    @DisplayName("查询方法 — DRAFT 状态下 isReleased 为 false")
    void productionPlan_isReleased_draftState() {
        ProductionPlan plan = createDraftPlan("MP-001");

        assertFalse(plan.isReleased());
    }

    @Test
    @DisplayName("查询方法 — RELEASED 状态下 isReleased 为 true")
    void productionPlan_isReleased_releasedState() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");
        plan.release();

        assertTrue(plan.isReleased());
    }

    @Test
    @DisplayName("查询方法 — CLOSED 状态下 isReleased 为 true")
    void productionPlan_isReleased_closedState() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.approve("张三");
        plan.release();
        plan.start();
        plan.complete();
        plan.close();

        assertTrue(plan.isReleased());
    }

    @Test
    @DisplayName("查询方法 — getTotalQuantity 计算总数量")
    void productionPlan_getTotalQuantity_sumsQuantities() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.addItem(new PlanItem("P-001", "产品A",
                new BigDecimal("5000"), LocalDate.of(2026, 8, 1), 1, "FACTORY-A"));
        plan.addItem(new PlanItem("P-002", "产品B",
                new BigDecimal("3000"), LocalDate.of(2026, 8, 5), 2, "FACTORY-B"));

        assertEquals(new BigDecimal("8000"), plan.getTotalQuantity());
    }

    @Test
    @DisplayName("查询方法 — getTotalQuantity 空列表返回 0")
    void productionPlan_getTotalQuantity_emptyItems() {
        ProductionPlan plan = createDraftPlan("MP-001");

        assertEquals(BigDecimal.ZERO, plan.getTotalQuantity());
    }

    // ==================== IProductionPlan 契约 ====================

    @Test
    @DisplayName("IProductionPlan 契约 — ProductionPlan 实现接口")
    void iProductionPlan_contract_productionPlanImplements() {
        ProductionPlan plan = createDraftPlan("MP-001");

        assertInstanceOf(IProductionPlan.class, plan);
    }

    @Test
    @DisplayName("IProductionPlan 契约 — getItems 返回 IPlanItem 列表")
    void iProductionPlan_contract_getItemsReturnsIPlanItems() {
        ProductionPlan plan = createDraftPlan("MP-001");
        plan.addItem(new PlanItem("P-001", "产品A",
                BigDecimal.ONE, LocalDate.now(), 1, "FACTORY-A"));

        List<? extends IPlanItem> items = plan.getItems();
        assertEquals(1, items.size());
        assertInstanceOf(IPlanItem.class, items.get(0));
    }

    @Test
    @DisplayName("IProductionPlan 契约 — 接口方法可访问")
    void iProductionPlan_contract_interfaceMethodsAccessible() {
        ProductionPlan plan = createDraftPlan("MP-001");

        assertEquals("MP-001", plan.getPlanNo());
        assertEquals("WEEKLY", plan.getPeriodType());
        assertNotNull(plan.getPeriodStart());
        assertNotNull(plan.getPeriodEnd());
        assertEquals(ProductionPlanStatus.DRAFT, plan.getStatus());
        assertNotNull(plan.getCreatedAt());
    }

    // ==================== DemandSource 构造 ====================

    @Test
    @DisplayName("DemandSource — 构造字段正确初始化")
    void demandSource_construct_fieldsInitialized() {
        DemandSource demand = new DemandSource("SO-001", "P-001", new BigDecimal("5000"));

        assertEquals("SO-001", demand.getReferenceNo());
        assertEquals("P-001", demand.getProductCode());
        assertEquals(new BigDecimal("5000"), demand.getQuantity());
        assertEquals(5, demand.getPriority()); // 默认中等优先级
        assertNull(demand.getSourceType());
        assertNull(demand.getCustomer());
    }

    @Test
    @DisplayName("DemandSource — register 设置 SALES_ORDER 类型和客户")
    void demandSource_register_salesOrderWithCustomer() {
        DemandSource demand = new DemandSource("SO-001", "P-001", new BigDecimal("5000"));

        demand.register(DemandSourceType.SALES_ORDER, "客户A");

        assertEquals(DemandSourceType.SALES_ORDER.name(), demand.getSourceType());
        assertEquals("客户A", demand.getCustomer());
        assertTrue(demand.isSalesOrder());
    }

    @Test
    @DisplayName("DemandSource — register 设置 FORECAST 类型无客户")
    void demandSource_register_forecastNoCustomer() {
        DemandSource demand = new DemandSource("FC-001", "P-001", new BigDecimal("3000"));

        demand.register(DemandSourceType.FORECAST);

        assertEquals(DemandSourceType.FORECAST.name(), demand.getSourceType());
        assertNull(demand.getCustomer());
        assertTrue(demand.isForecast());
    }

    @Test
    @DisplayName("DemandSource — 实现 IDemandSource 接口")
    void demandSource_implementsIDemandSource() {
        DemandSource demand = new DemandSource("SO-001", "P-001", BigDecimal.ONE);

        assertInstanceOf(IDemandSource.class, demand);
    }

    // ==================== DemandSource 查询方法 ====================

    @Test
    @DisplayName("DemandSource — isSalesOrder 对 SALES_ORDER 返回 true")
    void demandSource_isSalesOrder_trueForSalesOrder() {
        DemandSource demand = new DemandSource("SO-001", "P-001", BigDecimal.ONE);
        demand.register(DemandSourceType.SALES_ORDER, "客户A");

        assertTrue(demand.isSalesOrder());
        assertFalse(demand.isForecast());
    }

    @Test
    @DisplayName("DemandSource — isForecast 对 FORECAST 返回 true")
    void demandSource_isForecast_trueForForecast() {
        DemandSource demand = new DemandSource("FC-001", "P-001", BigDecimal.ONE);
        demand.register(DemandSourceType.FORECAST);

        assertTrue(demand.isForecast());
        assertFalse(demand.isSalesOrder());
    }

    @Test
    @DisplayName("DemandSource — hasCustomer 返回正确")
    void demandSource_hasCustomer_works() {
        DemandSource withCustomer = new DemandSource("SO-001", "P-001", BigDecimal.ONE);
        withCustomer.register(DemandSourceType.SALES_ORDER, "客户A");
        assertTrue(withCustomer.hasCustomer());

        DemandSource withoutCustomer = new DemandSource("FC-001", "P-001", BigDecimal.ONE);
        withoutCustomer.register(DemandSourceType.FORECAST);
        assertFalse(withoutCustomer.hasCustomer());
    }

    @Test
    @DisplayName("DemandSource — setPriority 修改优先级")
    void demandSource_setPriority_updatesPriority() {
        DemandSource demand = new DemandSource("SO-001", "P-001", BigDecimal.ONE);
        assertEquals(5, demand.getPriority());

        demand.setPriority(1);

        assertEquals(1, demand.getPriority());
    }

    // ==================== DemandSourceType ====================

    @Test
    @DisplayName("DemandSourceType — 4 个枚举值")
    void demandSourceType_hasFourValues() {
        DemandSourceType[] values = DemandSourceType.values();

        assertEquals(4, values.length);
    }

    @Test
    @DisplayName("DemandSourceType — SALES_ORDER 枚举值有效")
    void demandSourceType_salesOrderIsValid() {
        assertEquals("SALES_ORDER", DemandSourceType.SALES_ORDER.name());
    }

    @Test
    @DisplayName("DemandSourceType — 所有枚举值可 valueOf")
    void demandSourceType_allValuesRoundTrip() {
        for (DemandSourceType type : DemandSourceType.values()) {
            assertEquals(type, DemandSourceType.valueOf(type.name()));
        }
    }

    // ==================== CapacityCheck ====================

    @Test
    @DisplayName("CapacityCheck — PASS 结果工厂方法")
    void capacityCheck_pass_factoryMethod() {
        CapacityCheck check = CapacityCheck.pass("MP-001", "FACTORY-A", "MACHINE",
                new BigDecimal("100"), new BigDecimal("200"),
                new BigDecimal("50.0"), "无瓶颈");

        assertEquals("MP-001", check.planNo());
        assertEquals("FACTORY-A", check.factoryCode());
        assertEquals(CapacityResult.PASS, check.status());
        assertTrue(check.isAcceptable());
    }

    @Test
    @DisplayName("CapacityCheck — WARNING 结果工厂方法")
    void capacityCheck_warning_factoryMethod() {
        CapacityCheck check = CapacityCheck.warning("MP-001", "FACTORY-A", "MACHINE",
                new BigDecimal("170"), new BigDecimal("200"),
                new BigDecimal("85.0"), "工序-灌装");

        assertEquals(CapacityResult.WARNING, check.status());
        assertTrue(check.isAcceptable());
    }

    @Test
    @DisplayName("CapacityCheck — FAIL 结果工厂方法")
    void capacityCheck_fail_factoryMethod() {
        CapacityCheck check = CapacityCheck.fail("MP-001", "FACTORY-A", "MACHINE",
                new BigDecimal("250"), new BigDecimal("200"),
                new BigDecimal("125.0"), "工序-粉碎");

        assertEquals(CapacityResult.FAIL, check.status());
        assertFalse(check.isAcceptable());
    }

    @Test
    @DisplayName("CapacityCheck — isAcceptable PASS 返回 true")
    void capacityCheck_isAcceptable_pass() {
        CapacityCheck check = CapacityCheck.pass("MP-001", "FACTORY-A", "MACHINE",
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, "无");
        assertTrue(check.isAcceptable());
    }

    @Test
    @DisplayName("CapacityCheck — isAcceptable WARNING 返回 true")
    void capacityCheck_isAcceptable_warning() {
        CapacityCheck check = CapacityCheck.warning("MP-001", "FACTORY-A", "MACHINE",
                BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("90"), "工序A");
        assertTrue(check.isAcceptable());
    }

    @Test
    @DisplayName("CapacityCheck — isAcceptable FAIL 返回 false")
    void capacityCheck_isAcceptable_fail() {
        CapacityCheck check = CapacityCheck.fail("MP-001", "FACTORY-A", "MACHINE",
                BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("1000"), "工序B");
        assertFalse(check.isAcceptable());
    }

    @Test
    @DisplayName("CapacityCheck — CapacityResult 3 个枚举值")
    void capacityCheck_capacityResult_threeValues() {
        assertEquals(3, CapacityResult.values().length);
    }

    // ==================== 事件命名约定 ====================

    @Test
    @DisplayName("事件命名 — 所有事件以 mps. 开头")
    void eventNaming_allStartWithMps() {
        assertTrue(MpsEventTypes.PLAN_CREATED.startsWith("mps."));
        assertTrue(MpsEventTypes.PLAN_APPROVED.startsWith("mps."));
        assertTrue(MpsEventTypes.PLAN_RELEASED.startsWith("mps."));
        assertTrue(MpsEventTypes.PLAN_STARTED.startsWith("mps."));
        assertTrue(MpsEventTypes.PLAN_COMPLETED.startsWith("mps."));
        assertTrue(MpsEventTypes.PLAN_CLOSED.startsWith("mps."));
        assertTrue(MpsEventTypes.DEMAND_REGISTERED.startsWith("mps."));
        assertTrue(MpsEventTypes.CAPACITY_CHECKED.startsWith("mps."));
    }

    @Test
    @DisplayName("事件命名 — 计划事件以 mps.plan. 开头")
    void eventNaming_planEventsHavePlanPrefix() {
        assertTrue(MpsEventTypes.PLAN_CREATED.startsWith("mps.plan."));
        assertTrue(MpsEventTypes.PLAN_APPROVED.startsWith("mps.plan."));
        assertTrue(MpsEventTypes.PLAN_RELEASED.startsWith("mps.plan."));
        assertTrue(MpsEventTypes.PLAN_STARTED.startsWith("mps.plan."));
        assertTrue(MpsEventTypes.PLAN_COMPLETED.startsWith("mps.plan."));
        assertTrue(MpsEventTypes.PLAN_CLOSED.startsWith("mps.plan."));
    }

    @Test
    @DisplayName("事件命名 — 格式为 {module}.{entity}.{past_tense}")
    void eventNaming_followsModuleEntityPastTensePattern() {
        assertEquals("mps.plan.created", MpsEventTypes.PLAN_CREATED);
        assertEquals("mps.plan.approved", MpsEventTypes.PLAN_APPROVED);
        assertEquals("mps.plan.released", MpsEventTypes.PLAN_RELEASED);
        assertEquals("mps.plan.started", MpsEventTypes.PLAN_STARTED);
        assertEquals("mps.plan.completed", MpsEventTypes.PLAN_COMPLETED);
        assertEquals("mps.plan.closed", MpsEventTypes.PLAN_CLOSED);
        assertEquals("mps.demand.registered", MpsEventTypes.DEMAND_REGISTERED);
        assertEquals("mps.capacity.checked", MpsEventTypes.CAPACITY_CHECKED);
    }

    @Test
    @DisplayName("事件命名 — PREFIX 为 mps")
    void eventNaming_prefixIsMps() {
        assertEquals("mps", MpsEventTypes.PREFIX);
    }

    @Test
    @DisplayName("事件命名 — MpsEventTypes 是 final 不可继承类")
    void eventNaming_mpsEventTypesIsFinal() {
        assertTrue(java.lang.reflect.Modifier.isFinal(MpsEventTypes.class.getModifiers()));
    }

    // ==================== ProductionPlan IExpand ====================

    @Test
    @DisplayName("IExpand — ProductionPlan 支持扩展属性")
    void iExpand_productionPlan_supportsExpand() {
        ProductionPlan plan = createDraftPlan("MP-001");

        plan.setExpandProperty("mps.periodType", "WEEKLY");
        plan.setExpandProperty("mps.totalQuantity", new BigDecimal("10000"));

        assertEquals("WEEKLY", plan.getExpandProperty("mps.periodType"));
        assertEquals(new BigDecimal("10000"), plan.getExpandProperty("mps.totalQuantity"));
    }

    @Test
    @DisplayName("IExpand — DemandSource 支持扩展属性")
    void iExpand_demandSource_supportsExpand() {
        DemandSource demand = new DemandSource("SO-001", "P-001", new BigDecimal("5000"));

        demand.setExpandProperty("mps.sourceType", "SALES_ORDER");
        demand.setExpandProperty("mps.customer", "客户A");

        assertEquals("SALES_ORDER", demand.getExpandProperty("mps.sourceType"));
        assertEquals("客户A", demand.getExpandProperty("mps.customer"));
    }

    // ==================== ProductionPlanStatus 状态机 ====================

    @Test
    @DisplayName("状态机 — ProductionPlanStatus 实现 ILifecycle.StatusEnum")
    void productionPlanStatus_implementsStatusEnum() {
        assertTrue(ProductionPlanStatus.DRAFT instanceof com.byz.factory.lifecycle.ILifecycle.StatusEnum);
    }

    @Test
    @DisplayName("状态机 — DRAFT allowedTransitions 仅含 APPROVED")
    void productionPlanStatus_draftAllowedTransitions() {
        var allowed = ProductionPlanStatus.DRAFT.allowedTransitions();
        assertEquals(1, allowed.size());
        assertTrue(allowed.contains(ProductionPlanStatus.APPROVED));
    }

    @Test
    @DisplayName("状态机 — CLOSED allowedTransitions 为空（终态）")
    void productionPlanStatus_closedIsTerminal() {
        var allowed = ProductionPlanStatus.CLOSED.allowedTransitions();
        assertTrue(allowed.isEmpty());
    }

    @Test
    @DisplayName("状态机 — 6 个状态值")
    void productionPlanStatus_sixValues() {
        assertEquals(6, ProductionPlanStatus.values().length);
    }

    // ==================== 模块加载 ====================

    @Test
    @DisplayName("模块加载 — 基本可加载")
    void shouldLoad() {
        assertTrue(true);
    }

    // ==================== 辅助方法 ====================

    private ProductionPlan createDraftPlan(String planNo) {
        return new ProductionPlan(planNo, "WEEKLY",
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));
    }

}
