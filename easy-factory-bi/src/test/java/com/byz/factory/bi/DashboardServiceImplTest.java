package com.byz.factory.bi;

import com.byz.factory.bi.model.*;
import com.byz.factory.bi.repository.KpiSnapshotRepository;
import com.byz.factory.bi.service.impl.DashboardServiceImpl;
import com.byz.factory.event.DomainEventPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = BiTestConfig.class)
@Import(DashboardServiceImpl.class)
@DisplayName("DashboardService 实现集成测试")
class DashboardServiceImplTest {

    @Autowired private KpiSnapshotRepository kpiRepo;
    @Autowired private DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        kpiRepo.deleteAll();
    }

    @AfterEach
    void tearDown() {
        DomainEventPublisher.clearAll();
    }

    // ==================== computeKpi ====================

    @Nested
    @DisplayName("computeKpi — KPI 快照计算")
    class ComputeKpiTest {

        @Test
        @DisplayName("computeKpi — 创建快照并持久化")
        void computeKpi_shouldCreateAndPersist() {
            KpiSnapshot snapshot = service.computeKpi("FACTORY-01", "2026-07", "系统");

            assertNotNull(snapshot);
            assertNotNull(snapshot.getCode());
            assertTrue(snapshot.getCode().startsWith("KPI-"));
            assertEquals("2026-07", snapshot.getPeriod());
            assertEquals("FACTORY-01", snapshot.getFactoryCode());
            assertEquals("系统", snapshot.getComputedBy());
            assertNotNull(snapshot.getComputedAt());
            assertEquals(0, snapshot.getPlanCompletionRate().compareTo(BigDecimal.ZERO));

            // 验证持久化
            KpiSnapshot found = kpiRepo.findByCode(snapshot.getCode());
            assertNotNull(found);
            assertEquals("系统", found.getComputedBy());
        }

        @Test
        @DisplayName("computeKpi — 已存在的快照重新计算应覆盖")
        void computeKpi_existing_shouldOverride() {
            // Given: 创建第一个快照
            KpiSnapshot first = service.computeKpi("FACTORY-01", "2026-07", "张三");
            first.setKpiValue(KpiType.OEE, new BigDecimal("85.0"));
            kpiRepo.save(first);

            // When: 重新计算同一周期
            service.computeKpi("FACTORY-01", "2026-07", "李四");

            // Then: 旧快照被删除，新快照创建
            KpiSnapshot found = kpiRepo.findByFactoryCodeAndPeriod("FACTORY-01", "2026-07");
            assertNotNull(found);
            assertEquals("李四", found.getComputedBy());
            assertEquals(0, found.getOee().compareTo(BigDecimal.ZERO)); // 新快照 KPI 值为零
        }

        @Test
        @DisplayName("computeKpi — 不同工厂同一周期各自独立")
        void computeKpi_differentFactories_shouldBeIndependent() {
            KpiSnapshot s1 = service.computeKpi("FACTORY-01", "2026-07", "系统");
            KpiSnapshot s2 = service.computeKpi("FACTORY-02", "2026-07", "系统");

            assertEquals("FACTORY-01", s1.getFactoryCode());
            assertEquals("FACTORY-02", s2.getFactoryCode());
            assertNotEquals(s1.getCode(), s2.getCode());

            // 各工厂独立查询
            assertNotNull(kpiRepo.findByFactoryCodeAndPeriod("FACTORY-01", "2026-07"));
            assertNotNull(kpiRepo.findByFactoryCodeAndPeriod("FACTORY-02", "2026-07"));
        }

        @Test
        @DisplayName("computeKpi — 同一工厂不同周期各自独立")
        void computeKpi_differentPeriods_shouldBeIndependent() {
            KpiSnapshot s1 = service.computeKpi("FACTORY-01", "2026-06", "系统");
            KpiSnapshot s2 = service.computeKpi("FACTORY-01", "2026-07", "系统");

            assertEquals("2026-06", s1.getPeriod());
            assertEquals("2026-07", s2.getPeriod());
            assertNotEquals(s1.getCode(), s2.getCode());
        }
    }

    // ==================== getKpiSnapshot ====================

    @Nested
    @DisplayName("getKpiSnapshot — KPI 快照查询")
    class GetKpiSnapshotTest {

        @Test
        @DisplayName("getKpiSnapshot — 按工厂和周期查询")
        void getKpiSnapshot_shouldReturnSnapshot() {
            KpiSnapshot created = service.computeKpi("FACTORY-01", "2026-07", "系统");

            KpiSnapshot found = service.getKpiSnapshot("FACTORY-01", "2026-07");

            assertNotNull(found);
            assertEquals(created.getCode(), found.getCode());
            assertEquals("2026-07", found.getPeriod());
        }

        @Test
        @DisplayName("getKpiSnapshot — 不存在的工厂或周期返回 null")
        void getKpiSnapshot_nonexistent_shouldReturnNull() {
            assertNull(service.getKpiSnapshot("NON-EXISTENT", "2026-07"));
            assertNull(service.getKpiSnapshot("FACTORY-01", "2099-12"));
        }
    }

    // ==================== getKpiHistory ====================

    @Nested
    @DisplayName("getKpiHistory — KPI 历史趋势")
    class GetKpiHistoryTest {

        @Test
        @DisplayName("getKpiHistory — 返回最近 N 条快照（按周期升序）")
        void getKpiHistory_shouldReturnAscendingSnapshots() {
            // 创建 3 个月的历史快照
            service.computeKpi("FACTORY-01", "2026-05", "系统");
            service.computeKpi("FACTORY-01", "2026-06", "系统");
            service.computeKpi("FACTORY-01", "2026-07", "系统");

            List<KpiSnapshot> history = service.getKpiHistory(
                    "FACTORY-01", KpiType.OEE, DashboardPeriod.MONTHLY, 12);

            assertNotNull(history);
            assertEquals(3, history.size());
            assertEquals("2026-05", history.get(0).getPeriod());
            assertEquals("2026-06", history.get(1).getPeriod());
            assertEquals("2026-07", history.get(2).getPeriod());
        }

        @Test
        @DisplayName("getKpiHistory — 没有数据返回空列表")
        void getKpiHistory_noData_shouldReturnEmptyList() {
            List<KpiSnapshot> history = service.getKpiHistory(
                    "FACTORY-01", KpiType.OEE, DashboardPeriod.MONTHLY, 12);

            assertNotNull(history);
            assertTrue(history.isEmpty());
        }

        @Test
        @DisplayName("getKpiHistory — 限制返回条数")
        void getKpiHistory_shouldRespectLimit() {
            service.computeKpi("FACTORY-01", "2026-04", "系统");
            service.computeKpi("FACTORY-01", "2026-05", "系统");
            service.computeKpi("FACTORY-01", "2026-06", "系统");
            service.computeKpi("FACTORY-01", "2026-07", "系统");

            List<KpiSnapshot> history = service.getKpiHistory(
                    "FACTORY-01", KpiType.OEE, DashboardPeriod.MONTHLY, 2);

            assertEquals(2, history.size());
            assertEquals("2026-06", history.get(0).getPeriod());
            assertEquals("2026-07", history.get(1).getPeriod());
        }
    }

    // ==================== getLatestKpi ====================

    @Nested
    @DisplayName("getLatestKpi — 最新 KPI 快照")
    class GetLatestKpiTest {

        @Test
        @DisplayName("getLatestKpi — 返回最大周期快照")
        void getLatestKpi_shouldReturnMostRecent() {
            service.computeKpi("FACTORY-01", "2026-06", "系统");
            service.computeKpi("FACTORY-01", "2026-07", "系统");
            service.computeKpi("FACTORY-01", "2026-05", "系统");

            KpiSnapshot latest = service.getLatestKpi("FACTORY-01");

            assertNotNull(latest);
            assertEquals("2026-07", latest.getPeriod());
        }

        @Test
        @DisplayName("getLatestKpi — 没有数据返回 null")
        void getLatestKpi_noData_shouldReturnNull() {
            assertNull(service.getLatestKpi("FACTORY-01"));
        }
    }

    // ==================== KPI 值管理 ====================

    @Nested
    @DisplayName("KPI 值管理 — setKpiValue/getKpiValue 持久化")
    class KpiValueManagementTest {

        @Test
        @DisplayName("setKpiValue — 设置后持久化，重启后仍存在")
        void setKpiValue_shouldPersistAcrossTransactions() {
            // Given
            KpiSnapshot snapshot = service.computeKpi("FACTORY-01", "2026-07", "系统");

            // When: 设置 KPI 值并保存
            snapshot.setKpiValue(KpiType.PLAN_COMPLETION_RATE, new BigDecimal("85.30"));
            snapshot.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("98.70"));
            snapshot.setKpiValue(KpiType.OEE, new BigDecimal("83.20"));
            kpiRepo.save(snapshot);

            // Then: 重新查询验证持久化
            KpiSnapshot found = kpiRepo.findByCode(snapshot.getCode());
            assertEquals(0, found.getPlanCompletionRate().compareTo(new BigDecimal("85.30")));
            assertEquals(0, found.getFirstPassRate().compareTo(new BigDecimal("98.70")));
            assertEquals(0, found.getOee().compareTo(new BigDecimal("83.20")));
            assertEquals(3, found.getComputedKpiCount());
        }

        @Test
        @DisplayName("setKpiValue — 全部 10 种 KPI 设置并持久化")
        void setKpiValue_all10Types_shouldPersist() {
            KpiSnapshot snapshot = service.computeKpi("FACTORY-01", "2026-07", "系统");

            snapshot.setKpiValue(KpiType.PLAN_COMPLETION_RATE, new BigDecimal("85.5"));
            snapshot.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("98.7"));
            snapshot.setKpiValue(KpiType.OEE, new BigDecimal("83.2"));
            snapshot.setKpiValue(KpiType.MTTR, new BigDecimal("1.5"));
            snapshot.setKpiValue(KpiType.MTBF, new BigDecimal("520.0"));
            snapshot.setKpiValue(KpiType.BATCH_YIELD, new BigDecimal("97.3"));
            snapshot.setKpiValue(KpiType.INVENTORY_TURNOVER, new BigDecimal("12.5"));
            snapshot.setKpiValue(KpiType.DEVIATION_CLOSURE_RATE, new BigDecimal("90.0"));
            snapshot.setKpiValue(KpiType.CAPA_CLOSURE_RATE, new BigDecimal("87.5"));
            snapshot.setKpiValue(KpiType.ANDON_RESPONSE_TIME, new BigDecimal("3.2"));
            kpiRepo.save(snapshot);

            KpiSnapshot found = kpiRepo.findByCode(snapshot.getCode());
            assertEquals(10, found.getComputedKpiCount());
            assertEquals(0, found.getKpiValue(KpiType.MTTR).compareTo(new BigDecimal("1.5")));
            assertEquals(0, found.getKpiValue(KpiType.MTBF).compareTo(new BigDecimal("520.0")));
            assertEquals(0, found.getKpiValue(KpiType.BATCH_YIELD).compareTo(new BigDecimal("97.3")));
            assertEquals(0, found.getKpiValue(KpiType.ANDON_RESPONSE_TIME).compareTo(new BigDecimal("3.2")));
        }

        @Test
        @DisplayName("getKpiValue — getter 与 setter 往返一致性")
        void getKpiValue_roundTrip_shouldBeConsistent() {
            KpiSnapshot snapshot = service.computeKpi("FACTORY-01", "2026-07", "系统");
            snapshot.setKpiValue(KpiType.CAPA_CLOSURE_RATE, new BigDecimal("87.50"));

            assertEquals(0, snapshot.getKpiValue(KpiType.CAPA_CLOSURE_RATE).compareTo(new BigDecimal("87.50")));
            assertEquals(0, snapshot.getCapaClosureRate().compareTo(new BigDecimal("87.50")));
        }

        @Test
        @DisplayName("isFullyComputed — 设置 OEE 后应返回 true")
        void isFullyComputed_afterSettingOee_shouldReturnTrue() {
            KpiSnapshot snapshot = service.computeKpi("FACTORY-01", "2026-07", "系统");

            assertFalse(snapshot.isFullyComputed());

            snapshot.setKpiValue(KpiType.OEE, new BigDecimal("85.0"));
            assertTrue(snapshot.isFullyComputed());
        }
    }

    // ==================== 看板查询 ====================

    @Nested
    @DisplayName("看板查询 — 返回占位数据")
    class DashboardQueryTest {

        @Test
        @DisplayName("getProductionDashboard — 返回空生产看板")
        void getProductionDashboard_shouldReturnEmpty() {
            ProductionDashboard dashboard = service.getProductionDashboard("FACTORY-01");

            assertNotNull(dashboard);
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.wipCount());
            assertTrue(dashboard.workOrderProgress().isEmpty());
        }

        @Test
        @DisplayName("getQualityDashboard — 返回空质量看板")
        void getQualityDashboard_shouldReturnEmpty() {
            QualityDashboard dashboard = service.getQualityDashboard("FACTORY-01");

            assertNotNull(dashboard);
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.openDeviationCount());
            assertTrue(dashboard.recentDeviations().isEmpty());
        }

        @Test
        @DisplayName("getOeeDashboard — 返回空 OEE 看板")
        void getOeeDashboard_shouldReturnEmpty() {
            OeeDashboard dashboard = service.getOeeDashboard("FACTORY-01");

            assertNotNull(dashboard);
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.getEquipmentCount());
            assertNull(dashboard.bestEquipment());
        }

        @Test
        @DisplayName("getInventoryDashboard — 返回空仓储看板")
        void getInventoryDashboard_shouldReturnEmpty() {
            InventoryDashboard dashboard = service.getInventoryDashboard("FACTORY-01");

            assertNotNull(dashboard);
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.slowMovingCount());
            assertFalse(dashboard.hasSlowMovingItems());
        }

        @Test
        @DisplayName("getWorkOrderProgressList — 返回空列表")
        void getWorkOrderProgressList_shouldReturnEmpty() {
            List<ProductionDashboard.WorkOrderProgress> list =
                    service.getWorkOrderProgressList("FACTORY-01");

            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("getDeviationStats — 返回空统计")
        void getDeviationStats_shouldReturnEmpty() {
            Map<String, Object> stats = service.getDeviationStats("FACTORY-01");

            assertNotNull(stats);
            assertTrue(stats.isEmpty());
        }

        @Test
        @DisplayName("getEquipmentOee — 返回 null")
        void getEquipmentOee_shouldReturnNull() {
            assertNull(service.getEquipmentOee("EQ-001"));
        }

        @Test
        @DisplayName("getLowOeeEquipments — 返回空列表")
        void getLowOeeEquipments_shouldReturnEmpty() {
            List<OeeDashboard.EquipmentOee> list =
                    service.getLowOeeEquipments("FACTORY-01", new BigDecimal("60"));

            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("getSlowMovingItems — 返回空列表")
        void getSlowMovingItems_shouldReturnEmpty() {
            List<InventoryDashboard.SlowMovingItem> list =
                    service.getSlowMovingItems("FACTORY-01", 90);

            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("getNearExpiryItems — 返回空列表")
        void getNearExpiryItems_shouldReturnEmpty() {
            List<InventoryDashboard.NearExpiryItem> list =
                    service.getNearExpiryItems("FACTORY-01", 30);

            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("getBatchReport — 返回未实现状态")
        void getBatchReport_shouldReturnNotImplemented() {
            Map<String, Object> report = service.getBatchReport("B20260725-001");

            assertNotNull(report);
            assertEquals("B20260725-001", report.get("batchNo"));
            assertEquals("NOT_IMPLEMENTED", report.get("status"));
        }
    }

    // ==================== Repository 基本操作 ====================

    @Nested
    @DisplayName("Repository 基本 CRUD")
    class RepositoryCrudTest {

        @Test
        @DisplayName("save — 保存后 findById 可查到")
        void save_shouldPersistAndFindById() {
            KpiSnapshot snapshot = new KpiSnapshot("KPI-2026-07-F1", "2026-07", "FACTORY-01");
            KpiSnapshot saved = kpiRepo.save(snapshot);

            assertNotNull(saved);
            assertTrue(kpiRepo.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("findByFactoryCodeAndPeriod — 精确匹配")
        void findByFactoryCodeAndPeriod_shouldMatchExact() {
            kpiRepo.save(new KpiSnapshot("KPI-06-F1", "2026-06", "FACTORY-01"));
            kpiRepo.save(new KpiSnapshot("KPI-07-F1", "2026-07", "FACTORY-01"));
            kpiRepo.save(new KpiSnapshot("KPI-07-F2", "2026-07", "FACTORY-02"));

            assertNotNull(kpiRepo.findByFactoryCodeAndPeriod("FACTORY-01", "2026-07"));
            assertNotNull(kpiRepo.findByFactoryCodeAndPeriod("FACTORY-02", "2026-07"));
            assertNull(kpiRepo.findByFactoryCodeAndPeriod("FACTORY-01", "2099-01"));
        }

        @Test
        @DisplayName("findByFactoryCodeOrderByPeriodDesc — 按周期降序")
        void findByFactoryCodeOrderByPeriodDesc_shouldSortDesc() {
            kpiRepo.save(new KpiSnapshot("KPI-05-F1", "2026-05", "FACTORY-01"));
            kpiRepo.save(new KpiSnapshot("KPI-07-F1", "2026-07", "FACTORY-01"));
            kpiRepo.save(new KpiSnapshot("KPI-06-F1", "2026-06", "FACTORY-01"));

            List<KpiSnapshot> results = kpiRepo.findByFactoryCodeOrderByPeriodDesc("FACTORY-01");
            assertEquals(3, results.size());
            assertEquals("2026-07", results.get(0).getPeriod());
            assertEquals("2026-06", results.get(1).getPeriod());
            assertEquals("2026-05", results.get(2).getPeriod());
        }

        @Test
        @DisplayName("deleteByFactoryCodeAndPeriod — 删除后查询返回 null")
        void deleteByFactoryCodeAndPeriod_shouldRemove() {
            kpiRepo.save(new KpiSnapshot("KPI-07-F1", "2026-07", "FACTORY-01"));

            kpiRepo.deleteByFactoryCodeAndPeriod("FACTORY-01", "2026-07");

            assertNull(kpiRepo.findByFactoryCodeAndPeriod("FACTORY-01", "2026-07"));
        }

        @Test
        @DisplayName("findTopByFactoryCodeOrderByPeriodDesc — 最新快照")
        void findTopByFactoryCode_shouldReturnLatest() {
            kpiRepo.save(new KpiSnapshot("KPI-05-F1", "2026-05", "FACTORY-01"));
            kpiRepo.save(new KpiSnapshot("KPI-07-F1", "2026-07", "FACTORY-01"));
            kpiRepo.save(new KpiSnapshot("KPI-06-F1", "2026-06", "FACTORY-01"));

            var latest = kpiRepo.findTopByFactoryCodeOrderByPeriodDesc("FACTORY-01");
            assertTrue(latest.isPresent());
            assertEquals("2026-07", latest.get().getPeriod());
        }

        @Test
        @DisplayName("findByCode — 不存在的编码返回 null")
        void findByCode_nonexistent_shouldReturnNull() {
            assertNull(kpiRepo.findByCode("NON-EXISTENT"));
        }
    }

    // ==================== 综合场景 ====================

    @Nested
    @DisplayName("综合场景")
    class IntegrationScenarios {

        @Test
        @DisplayName("月度 KPI 全流程 — 创建→填充→持久化→查询→历史趋势")
        void monthlyKpi_fullFlow_shouldWork() {
            // Step 1: 创建 7 月快照
            KpiSnapshot july = service.computeKpi("FACTORY-01", "2026-07", "BI引擎");

            // Step 2: 填充 KPI 数据
            july.setKpiValue(KpiType.PLAN_COMPLETION_RATE, new BigDecimal("85.30"));
            july.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("98.70"));
            july.setKpiValue(KpiType.OEE, new BigDecimal("83.20"));
            july.setKpiValue(KpiType.MTTR, new BigDecimal("1.50"));
            july.setKpiValue(KpiType.MTBF, new BigDecimal("520.0"));
            kpiRepo.save(july);

            // Step 3: 创建前几个月快照
            KpiSnapshot june = service.computeKpi("FACTORY-01", "2026-06", "BI引擎");
            june.setKpiValue(KpiType.OEE, new BigDecimal("82.00"));
            kpiRepo.save(june);

            KpiSnapshot may = service.computeKpi("FACTORY-01", "2026-05", "BI引擎");
            may.setKpiValue(KpiType.OEE, new BigDecimal("81.50"));
            kpiRepo.save(may);

            // Step 4: 验证最新快照
            KpiSnapshot latest = service.getLatestKpi("FACTORY-01");
            assertNotNull(latest);
            assertEquals("2026-07", latest.getPeriod());
            assertEquals(5, latest.getComputedKpiCount());
            assertTrue(latest.isFullyComputed());

            // Step 5: 验证历史趋势（升序）
            List<KpiSnapshot> history = service.getKpiHistory(
                    "FACTORY-01", KpiType.OEE, DashboardPeriod.MONTHLY, 12);
            assertEquals(3, history.size());
            assertEquals("2026-05", history.get(0).getPeriod());
            assertEquals("2026-07", history.get(2).getPeriod());
            // OEE 趋势: 81.50 → 82.00 → 83.20
            assertEquals(0, history.get(0).getOee().compareTo(new BigDecimal("81.50")));
            assertEquals(0, history.get(1).getOee().compareTo(new BigDecimal("82.00")));
            assertEquals(0, history.get(2).getOee().compareTo(new BigDecimal("83.20")));
        }

        @Test
        @DisplayName("多工厂 KPI 快照隔离 — 两个工厂互不影响")
        void multiFactory_isolation_shouldBeIndependent() {
            // 深圳工厂
            KpiSnapshot sz = service.computeKpi("FACTORY-SZ", "2026-07", "系统");
            sz.setKpiValue(KpiType.OEE, new BigDecimal("85.0"));
            kpiRepo.save(sz);

            // 昆山工厂
            KpiSnapshot ks = service.computeKpi("FACTORY-KS", "2026-07", "系统");
            ks.setKpiValue(KpiType.OEE, new BigDecimal("78.0"));
            kpiRepo.save(ks);

            // 验证隔离
            assertEquals(0, service.getKpiSnapshot("FACTORY-SZ", "2026-07").getOee()
                    .compareTo(new BigDecimal("85.0")));
            assertEquals(0, service.getKpiSnapshot("FACTORY-KS", "2026-07").getOee()
                    .compareTo(new BigDecimal("78.0")));

            // 各工厂最新快照
            assertEquals("FACTORY-SZ", service.getLatestKpi("FACTORY-SZ").getFactoryCode());
            assertEquals("FACTORY-KS", service.getLatestKpi("FACTORY-KS").getFactoryCode());
        }

        @Test
        @DisplayName("KPI 重新计算覆盖 — 月度修正场景")
        void kpiRecompute_shouldOverridePrevious() {
            // 初版计算（部分数据未就绪）
            KpiSnapshot v1 = service.computeKpi("FACTORY-01", "2026-07", "张三");
            v1.setKpiValue(KpiType.OEE, new BigDecimal("80.0"));
            v1.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("97.0"));
            kpiRepo.save(v1);

            // 数据就绪后重新计算
            KpiSnapshot v2 = service.computeKpi("FACTORY-01", "2026-07", "李四");
            v2.setKpiValue(KpiType.OEE, new BigDecimal("83.2"));
            v2.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("98.7"));
            v2.setKpiValue(KpiType.MTTR, new BigDecimal("1.5"));
            kpiRepo.save(v2);

            // 只有最新版本存在
            KpiSnapshot found = service.getKpiSnapshot("FACTORY-01", "2026-07");
            assertEquals("李四", found.getComputedBy());
            assertEquals(3, found.getComputedKpiCount());
            assertEquals(0, found.getOee().compareTo(new BigDecimal("83.2")));
        }
    }

}
