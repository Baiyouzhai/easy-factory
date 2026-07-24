package com.byz.factory.bi;

import com.byz.factory.bi.model.*;
import com.byz.factory.bi.service.DashboardService;
import com.byz.factory.event.types.BiEventTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BI 模块 看板报表系统测试")
class BiModuleTest {

    // ======================== KpiType 枚举测试 ========================

    @Nested
    @DisplayName("KpiType 枚举")
    class KpiTypeTest {

        @Test
        @DisplayName("KPI类型数量 — 应有 10 种标准 KPI")
        void kpiType_count_shouldHave10Types() {
            assertEquals(10, KpiType.values().length);
        }

        @Test
        @DisplayName("PLAN_COMPLETION_RATE — 显示名、数据来源、目标值应正确")
        void planCompletionRate_metadata_shouldBeCorrect() {
            assertEquals("计划完成率", KpiType.PLAN_COMPLETION_RATE.getDisplayName());
            assertEquals("MES + MPS", KpiType.PLAN_COMPLETION_RATE.getDataSource());
            assertEquals("≥ 95%", KpiType.PLAN_COMPLETION_RATE.getTarget());
        }

        @Test
        @DisplayName("FIRST_PASS_RATE — 显示名、数据来源、目标值应正确")
        void firstPassRate_metadata_shouldBeCorrect() {
            assertEquals("一次合格率", KpiType.FIRST_PASS_RATE.getDisplayName());
            assertEquals("QMS", KpiType.FIRST_PASS_RATE.getDataSource());
            assertEquals("≥ 98%", KpiType.FIRST_PASS_RATE.getTarget());
        }

        @Test
        @DisplayName("OEE — 显示名、数据来源、目标值应正确")
        void oee_metadata_shouldBeCorrect() {
            assertEquals("设备综合效率", KpiType.OEE.getDisplayName());
            assertEquals("Equip + IoT", KpiType.OEE.getDataSource());
            assertEquals("≥ 85%", KpiType.OEE.getTarget());
        }

        @Test
        @DisplayName("MTTR — 显示名、数据来源、目标值应正确")
        void mttr_metadata_shouldBeCorrect() {
            assertEquals("平均维修时间", KpiType.MTTR.getDisplayName());
            assertEquals("EAM", KpiType.MTTR.getDataSource());
            assertEquals("< 2h", KpiType.MTTR.getTarget());
        }

        @Test
        @DisplayName("MTBF — 显示名、数据来源、目标值应正确")
        void mtbf_metadata_shouldBeCorrect() {
            assertEquals("平均故障间隔", KpiType.MTBF.getDisplayName());
            assertEquals("IoT + EAM", KpiType.MTBF.getDataSource());
            assertEquals("> 500h", KpiType.MTBF.getTarget());
        }

        @Test
        @DisplayName("BATCH_YIELD — 显示名、数据来源、目标值应正确")
        void batchYield_metadata_shouldBeCorrect() {
            assertEquals("批次收率", KpiType.BATCH_YIELD.getDisplayName());
            assertEquals("LIMS", KpiType.BATCH_YIELD.getDataSource());
            assertEquals("≥ 97%", KpiType.BATCH_YIELD.getTarget());
        }

        @Test
        @DisplayName("INVENTORY_TURNOVER — 显示名、数据来源、目标值应正确")
        void inventoryTurnover_metadata_shouldBeCorrect() {
            assertEquals("库存周转率", KpiType.INVENTORY_TURNOVER.getDisplayName());
            assertEquals("WMS", KpiType.INVENTORY_TURNOVER.getDataSource());
            assertEquals("≥ 10", KpiType.INVENTORY_TURNOVER.getTarget());
        }

        @Test
        @DisplayName("DEVIATION_CLOSURE_RATE — 显示名、数据来源、目标值应正确")
        void deviationClosureRate_metadata_shouldBeCorrect() {
            assertEquals("偏差关闭率", KpiType.DEVIATION_CLOSURE_RATE.getDisplayName());
            assertEquals("QMS", KpiType.DEVIATION_CLOSURE_RATE.getDataSource());
        }

        @Test
        @DisplayName("CAPA_CLOSURE_RATE — 显示名、数据来源、目标值应正确")
        void capaClosureRate_metadata_shouldBeCorrect() {
            assertEquals("CAPA关闭率", KpiType.CAPA_CLOSURE_RATE.getDisplayName());
            assertEquals("QMS", KpiType.CAPA_CLOSURE_RATE.getDataSource());
        }

        @Test
        @DisplayName("ANDON_RESPONSE_TIME — 显示名、数据来源、目标值应正确")
        void andonResponseTime_metadata_shouldBeCorrect() {
            assertEquals("安灯平均响应时间", KpiType.ANDON_RESPONSE_TIME.getDisplayName());
            assertEquals("Andon", KpiType.ANDON_RESPONSE_TIME.getDataSource());
        }
    }

    // ======================== DashboardPeriod 枚举测试 ========================

    @Nested
    @DisplayName("DashboardPeriod 枚举")
    class DashboardPeriodTest {

        @Test
        @DisplayName("统计周期数量 — 应有 7 种周期")
        void period_count_shouldHave7Periods() {
            assertEquals(7, DashboardPeriod.values().length);
        }

        @Test
        @DisplayName("REALTIME — 显示名和刷新间隔应正确")
        void realtime_metadata_shouldBeCorrect() {
            assertEquals("实时", DashboardPeriod.REALTIME.getDisplayName());
            assertEquals(5, DashboardPeriod.REALTIME.getRefreshIntervalSeconds());
        }

        @Test
        @DisplayName("DAILY — 显示名和刷新间隔应正确")
        void daily_metadata_shouldBeCorrect() {
            assertEquals("日", DashboardPeriod.DAILY.getDisplayName());
            assertEquals(30, DashboardPeriod.DAILY.getRefreshIntervalSeconds());
        }

        @Test
        @DisplayName("MONTHLY — 显示名和刷新间隔应正确（战略层 1h）")
        void monthly_metadata_shouldBeCorrect() {
            assertEquals("月", DashboardPeriod.MONTHLY.getDisplayName());
            assertEquals(3600, DashboardPeriod.MONTHLY.getRefreshIntervalSeconds());
        }

        @Test
        @DisplayName("YEARLY — 显示名和刷新间隔应正确")
        void yearly_metadata_shouldBeCorrect() {
            assertEquals("年", DashboardPeriod.YEARLY.getDisplayName());
            assertEquals(86400, DashboardPeriod.YEARLY.getRefreshIntervalSeconds());
        }

        @Test
        @DisplayName("HOURLY — 显示名应正确")
        void hourly_displayName_shouldBeCorrect() {
            assertEquals("小时", DashboardPeriod.HOURLY.getDisplayName());
        }

        @Test
        @DisplayName("WEEKLY — 显示名应正确")
        void weekly_displayName_shouldBeCorrect() {
            assertEquals("周", DashboardPeriod.WEEKLY.getDisplayName());
        }

        @Test
        @DisplayName("QUARTERLY — 显示名应正确")
        void quarterly_displayName_shouldBeCorrect() {
            assertEquals("季度", DashboardPeriod.QUARTERLY.getDisplayName());
        }
    }

    // ======================== KpiSnapshot 测试 ========================

    @Nested
    @DisplayName("KpiSnapshot KPI 快照")
    class KpiSnapshotTest {

        @Test
        @DisplayName("创建 — 初始所有 KPI 值应为零")
        void creation_shouldInitializeAllKpisToZero() {
            // Given & When
            KpiSnapshot snapshot = new KpiSnapshot("KPI-2026-07-F1", "2026-07", "FACTORY-01");

            // Then
            assertEquals("KPI-2026-07-F1", snapshot.getCode());
            assertEquals("2026-07", snapshot.getPeriod());
            assertEquals("FACTORY-01", snapshot.getFactoryCode());
            assertEquals(0, snapshot.getPlanCompletionRate().compareTo(BigDecimal.ZERO));
            assertEquals(0, snapshot.getFirstPassRate().compareTo(BigDecimal.ZERO));
            assertEquals(0, snapshot.getOee().compareTo(BigDecimal.ZERO));
            assertEquals(0, snapshot.getMttr().compareTo(BigDecimal.ZERO));
            assertEquals(0, snapshot.getMtbf().compareTo(BigDecimal.ZERO));
            assertEquals(0, snapshot.getBatchYield().compareTo(BigDecimal.ZERO));
            assertEquals(0, snapshot.getInventoryTurnover().compareTo(BigDecimal.ZERO));
            assertEquals(0, snapshot.getDeviationClosureRate().compareTo(BigDecimal.ZERO));
            assertEquals(0, snapshot.getCapaClosureRate().compareTo(BigDecimal.ZERO));
            assertEquals(0, snapshot.getAndonResponseTime().compareTo(BigDecimal.ZERO));
            assertNotNull(snapshot.getCreatedAt());
            assertNotNull(snapshot.getUpdatedAt());
        }

        @Test
        @DisplayName("setKpiValue — 设置 PLAN_COMPLETION_RATE 应正确更新")
        void setKpiValue_planCompletionRate_shouldUpdateCorrectly() {
            // Given
            KpiSnapshot snapshot = new KpiSnapshot("KPI-01", "2026-07", "F1");

            // When
            snapshot.setKpiValue(KpiType.PLAN_COMPLETION_RATE, new BigDecimal("85.50"));

            // Then
            assertEquals(0, snapshot.getPlanCompletionRate().compareTo(new BigDecimal("85.50")));
            assertEquals(0, snapshot.getKpiValue(KpiType.PLAN_COMPLETION_RATE).compareTo(new BigDecimal("85.50")));
        }

        @Test
        @DisplayName("setKpiValue — 设置 FIRST_PASS_RATE 应正确更新")
        void setKpiValue_firstPassRate_shouldUpdateCorrectly() {
            // Given
            KpiSnapshot snapshot = new KpiSnapshot("KPI-01", "2026-07", "F1");

            // When
            snapshot.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("98.70"));

            // Then
            assertEquals(0, snapshot.getFirstPassRate().compareTo(new BigDecimal("98.70")));
        }

        @Test
        @DisplayName("setKpiValue — 设置 OEE 应正确更新")
        void setKpiValue_oee_shouldUpdateCorrectly() {
            // Given
            KpiSnapshot snapshot = new KpiSnapshot("KPI-01", "2026-07", "F1");

            // When
            snapshot.setKpiValue(KpiType.OEE, new BigDecimal("83.20"));

            // Then
            assertEquals(0, snapshot.getOee().compareTo(new BigDecimal("83.20")));
        }

        @Test
        @DisplayName("setKpiValue — 设置所有 10 种 KPI 应全部正确更新")
        void setKpiValue_allTypes_shouldUpdateAll() {
            // Given
            KpiSnapshot snapshot = new KpiSnapshot("KPI-01", "2026-07", "F1");

            // When
            snapshot.setKpiValue(KpiType.PLAN_COMPLETION_RATE, new BigDecimal("85.5"));
            snapshot.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("98.7"));
            snapshot.setKpiValue(KpiType.OEE, new BigDecimal("83.2"));
            snapshot.setKpiValue(KpiType.MTTR, new BigDecimal("1.5"));
            snapshot.setKpiValue(KpiType.MTBF, new BigDecimal("520"));
            snapshot.setKpiValue(KpiType.BATCH_YIELD, new BigDecimal("97.3"));
            snapshot.setKpiValue(KpiType.INVENTORY_TURNOVER, new BigDecimal("12.5"));
            snapshot.setKpiValue(KpiType.DEVIATION_CLOSURE_RATE, new BigDecimal("90.0"));
            snapshot.setKpiValue(KpiType.CAPA_CLOSURE_RATE, new BigDecimal("87.5"));
            snapshot.setKpiValue(KpiType.ANDON_RESPONSE_TIME, new BigDecimal("3.2"));

            // Then
            assertEquals(0, snapshot.getKpiValue(KpiType.PLAN_COMPLETION_RATE).compareTo(new BigDecimal("85.5")));
            assertEquals(0, snapshot.getKpiValue(KpiType.OEE).compareTo(new BigDecimal("83.2")));
            assertEquals(0, snapshot.getKpiValue(KpiType.MTTR).compareTo(new BigDecimal("1.5")));
            assertEquals(0, snapshot.getKpiValue(KpiType.MTBF).compareTo(new BigDecimal("520")));
            assertEquals(0, snapshot.getKpiValue(KpiType.ANDON_RESPONSE_TIME).compareTo(new BigDecimal("3.2")));
        }

        @Test
        @DisplayName("getKpiValue — 未设置的 KPI 应返回零")
        void getKpiValue_notSet_shouldReturnZero() {
            // Given
            KpiSnapshot snapshot = new KpiSnapshot("KPI-01", "2026-07", "F1");

            // When
            BigDecimal value = snapshot.getKpiValue(KpiType.MTBF);

            // Then
            assertEquals(0, value.compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("markComputed — 应设置计算人和计算时间")
        void markComputed_shouldSetMetadata() {
            // Given
            KpiSnapshot snapshot = new KpiSnapshot("KPI-01", "2026-07", "F1");

            // When
            snapshot.markComputed("系统");

            // Then
            assertEquals("系统", snapshot.getComputedBy());
            assertNotNull(snapshot.getComputedAt());
            assertFalse(snapshot.getComputedAt().isEmpty());
        }

        @Test
        @DisplayName("isFullyComputed — 所有 KPI 为零时应返回 false")
        void isFullyComputed_allZero_shouldReturnFalse() {
            // Given
            KpiSnapshot snapshot = new KpiSnapshot("KPI-01", "2026-07", "F1");

            // When & Then
            assertFalse(snapshot.isFullyComputed());
        }

        @Test
        @DisplayName("isFullyComputed — 至少一个 KPI 大于零时应返回 true")
        void isFullyComputed_hasValue_shouldReturnTrue() {
            // Given
            KpiSnapshot snapshot = new KpiSnapshot("KPI-01", "2026-07", "F1");

            // When
            snapshot.setKpiValue(KpiType.OEE, new BigDecimal("85.0"));

            // Then
            assertTrue(snapshot.isFullyComputed());
        }

        @Test
        @DisplayName("getComputedKpiCount — 应返回已计算的 KPI 数量")
        void getComputedKpiCount_shouldReturnCorrectCount() {
            // Given
            KpiSnapshot snapshot = new KpiSnapshot("KPI-01", "2026-07", "F1");

            // When
            snapshot.setKpiValue(KpiType.PLAN_COMPLETION_RATE, new BigDecimal("85.5"));
            snapshot.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("98.7"));
            snapshot.setKpiValue(KpiType.OEE, new BigDecimal("83.2"));

            // Then
            assertEquals(3, snapshot.getComputedKpiCount());
        }

        @Test
        @DisplayName("IExpand — 应支持扩展属性")
        void iexpand_shouldSupportExtensionProperties() {
            // Given
            KpiSnapshot snapshot = new KpiSnapshot("KPI-01", "2026-07", "F1");

            // When
            snapshot.setExpandProperty("bi.kpi.remark", "Q2 整体表现良好");

            // Then
            assertEquals("Q2 整体表现良好", snapshot.getExpandProperty("bi.kpi.remark"));
        }
    }

    // ======================== ProductionDashboard 测试 ========================

    @Nested
    @DisplayName("ProductionDashboard 生产看板")
    class ProductionDashboardTest {

        @Test
        @DisplayName("创建 — 应正确保存所有字段")
        void creation_shouldStoreAllFields() {
            // Given
            List<ProductionDashboard.WorkOrderProgress> progress = List.of(
                    new ProductionDashboard.WorkOrderProgress("WO-001", "阿莫西林胶囊", 80, "压片", "IN_PROGRESS"),
                    new ProductionDashboard.WorkOrderProgress("WO-002", "头孢片", 0, "-", "CREATED")
            );

            // When
            ProductionDashboard dashboard = new ProductionDashboard(
                    "FACTORY-01", new BigDecimal("85.3"), 12,
                    new BigDecimal("9500"), 15, 8, 5, 2,
                    progress, Instant.now());

            // Then
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.planCompletionRate().compareTo(new BigDecimal("85.3")));
            assertEquals(12, dashboard.wipCount());
            assertEquals(0, dashboard.dailyOutput().compareTo(new BigDecimal("9500")));
            assertEquals(15, dashboard.totalWorkOrders());
            assertEquals(8, dashboard.completedWorkOrders());
            assertEquals(5, dashboard.inProgressWorkOrders());
            assertEquals(2, dashboard.interruptedWorkOrders());
            assertEquals(2, dashboard.workOrderProgress().size());
            assertEquals("WO-001", dashboard.workOrderProgress().get(0).workOrderNo());
            assertEquals("阿莫西林胶囊", dashboard.workOrderProgress().get(0).productName());
            assertEquals(80, dashboard.workOrderProgress().get(0).progressPercent());
        }

        @Test
        @DisplayName("empty — 应返回全零/空的看板数据")
        void empty_shouldReturnZeroedDashboard() {
            // When
            ProductionDashboard dashboard = ProductionDashboard.empty("FACTORY-01");

            // Then
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.planCompletionRate().compareTo(BigDecimal.ZERO));
            assertEquals(0, dashboard.wipCount());
            assertEquals(0, dashboard.totalWorkOrders());
            assertTrue(dashboard.workOrderProgress().isEmpty());
            assertNotNull(dashboard.refreshedAt());
        }

        @Test
        @DisplayName("hasInterruptedOrders — 有中断工单时应返回 true")
        void hasInterruptedOrders_withInterruptions_shouldReturnTrue() {
            // Given
            ProductionDashboard dashboard = new ProductionDashboard(
                    "F1", BigDecimal.ZERO, 0, BigDecimal.ZERO,
                    10, 5, 3, 2, List.of(), Instant.now());

            // When & Then
            assertTrue(dashboard.hasInterruptedOrders());
        }

        @Test
        @DisplayName("hasInterruptedOrders — 无中断工单时应返回 false")
        void hasInterruptedOrders_withoutInterruptions_shouldReturnFalse() {
            // Given
            ProductionDashboard dashboard = ProductionDashboard.empty("F1");

            // When & Then
            assertFalse(dashboard.hasInterruptedOrders());
        }

        @Test
        @DisplayName("getCompletionRatio — 应正确计算工单完成率")
        void getCompletionRatio_shouldCalculateCorrectly() {
            // Given
            ProductionDashboard dashboard = new ProductionDashboard(
                    "F1", BigDecimal.ZERO, 0, BigDecimal.ZERO,
                    10, 6, 3, 1, List.of(), Instant.now());

            // When
            double ratio = dashboard.getCompletionRatio();

            // Then
            assertEquals(0.6, ratio, 0.001);
        }

        @Test
        @DisplayName("getCompletionRatio — 总工单数为零时应返回 0")
        void getCompletionRatio_zeroTotal_shouldReturnZero() {
            // Given
            ProductionDashboard dashboard = ProductionDashboard.empty("F1");

            // When
            double ratio = dashboard.getCompletionRatio();

            // Then
            assertEquals(0.0, ratio, 0.001);
        }

        @Test
        @DisplayName("WorkOrderProgress — record 应正确保存字段")
        void workOrderProgress_record_shouldStoreFields() {
            // When
            ProductionDashboard.WorkOrderProgress wp = new ProductionDashboard.WorkOrderProgress(
                    "WO-003", "维生素C片", 45, "制粒", "IN_PROGRESS");

            // Then
            assertEquals("WO-003", wp.workOrderNo());
            assertEquals("维生素C片", wp.productName());
            assertEquals(45, wp.progressPercent());
            assertEquals("制粒", wp.currentProcess());
            assertEquals("IN_PROGRESS", wp.status());
        }
    }

    // ======================== QualityDashboard 测试 ========================

    @Nested
    @DisplayName("QualityDashboard 质量看板")
    class QualityDashboardTest {

        @Test
        @DisplayName("创建 — 应正确保存所有字段")
        void creation_shouldStoreAllFields() {
            // Given
            List<QualityDashboard.DeviationSummary> deviations = List.of(
                    new QualityDashboard.DeviationSummary("DEV-001", "压片片重偏移", "MAJOR", "OPEN", "2026-07-20"),
                    new QualityDashboard.DeviationSummary("DEV-002", "水分超标", "MINOR", "CLOSED", "2026-07-18")
            );
            Map<String, Integer> bySeverity = Map.of("MINOR", 1, "MAJOR", 1);

            // When
            QualityDashboard dashboard = new QualityDashboard(
                    "FACTORY-01", new BigDecimal("98.7"), 1, new BigDecimal("87.5"),
                    150, 148, 2, new BigDecimal("90.0"),
                    deviations, bySeverity, Instant.now());

            // Then
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.firstPassRate().compareTo(new BigDecimal("98.7")));
            assertEquals(1, dashboard.openDeviationCount());
            assertEquals(0, dashboard.capaClosureRate().compareTo(new BigDecimal("87.5")));
            assertEquals(150, dashboard.totalInspections());
            assertEquals(148, dashboard.passedInspections());
            assertEquals(2, dashboard.failedInspections());
            assertEquals(2, dashboard.recentDeviations().size());
            assertEquals("DEV-001", dashboard.recentDeviations().get(0).deviationCode());
            assertEquals("MAJOR", dashboard.recentDeviations().get(0).severity());
        }

        @Test
        @DisplayName("empty — 应返回全零/空的看板数据")
        void empty_shouldReturnZeroedDashboard() {
            // When
            QualityDashboard dashboard = QualityDashboard.empty("FACTORY-01");

            // Then
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.firstPassRate().compareTo(BigDecimal.ZERO));
            assertEquals(0, dashboard.openDeviationCount());
            assertTrue(dashboard.recentDeviations().isEmpty());
            assertNotNull(dashboard.refreshedAt());
        }

        @Test
        @DisplayName("hasOpenDeviations — 有未关闭偏差时应返回 true")
        void hasOpenDeviations_withOpen_shouldReturnTrue() {
            // Given
            QualityDashboard dashboard = new QualityDashboard(
                    "F1", BigDecimal.ZERO, 3, BigDecimal.ZERO,
                    0, 0, 0, BigDecimal.ZERO,
                    List.of(), Map.of(), Instant.now());

            // When & Then
            assertTrue(dashboard.hasOpenDeviations());
        }

        @Test
        @DisplayName("hasOpenDeviations — 无偏差时应返回 false")
        void hasOpenDeviations_none_shouldReturnFalse() {
            // Given
            QualityDashboard dashboard = QualityDashboard.empty("F1");

            // When & Then
            assertFalse(dashboard.hasOpenDeviations());
        }

        @Test
        @DisplayName("getPassRatio — 应正确计算检验通过率")
        void getPassRatio_shouldCalculateCorrectly() {
            // Given
            QualityDashboard dashboard = new QualityDashboard(
                    "F1", BigDecimal.ZERO, 0, BigDecimal.ZERO,
                    100, 95, 5, BigDecimal.ZERO,
                    List.of(), Map.of(), Instant.now());

            // When
            double ratio = dashboard.getPassRatio();

            // Then
            assertEquals(0.95, ratio, 0.001);
        }

        @Test
        @DisplayName("getPassRatio — 总检验数为零时应返回 0")
        void getPassRatio_zeroTotal_shouldReturnZero() {
            // Given
            QualityDashboard dashboard = QualityDashboard.empty("F1");

            // When
            double ratio = dashboard.getPassRatio();

            // Then
            assertEquals(0.0, ratio, 0.001);
        }

        @Test
        @DisplayName("DeviationSummary — record 应正确保存字段")
        void deviationSummary_record_shouldStoreFields() {
            // When
            QualityDashboard.DeviationSummary ds = new QualityDashboard.DeviationSummary(
                    "DEV-003", "含量偏低", "CRITICAL", "INVESTIGATING", "2026-07-25");

            // Then
            assertEquals("DEV-003", ds.deviationCode());
            assertEquals("含量偏低", ds.description());
            assertEquals("CRITICAL", ds.severity());
            assertEquals("INVESTIGATING", ds.status());
            assertEquals("2026-07-25", ds.createdAt());
        }
    }

    // ======================== OeeDashboard 测试 ========================

    @Nested
    @DisplayName("OeeDashboard OEE 看板")
    class OeeDashboardTest {

        @Test
        @DisplayName("创建 — 应正确保存所有字段")
        void creation_shouldStoreAllFields() {
            // Given
            List<OeeDashboard.EquipmentOee> metrics = List.of(
                    new OeeDashboard.EquipmentOee("WG-001", "湿法制粒机", new BigDecimal("95"), new BigDecimal("88"),
                            new BigDecimal("99"), new BigDecimal("83"), "RUNNING", "stable"),
                    new OeeDashboard.EquipmentOee("PT-001", "压片机", new BigDecimal("78"), new BigDecimal("85"),
                            new BigDecimal("97"), new BigDecimal("64"), "IDLE", "down")
            );

            // When
            OeeDashboard dashboard = new OeeDashboard(
                    "FACTORY-01", metrics,
                    new BigDecimal("86.5"), new BigDecimal("86.5"), new BigDecimal("98"), new BigDecimal("73.5"),
                    "PT-001", "WG-001", Instant.now());

            // Then
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(2, dashboard.equipmentMetrics().size());
            assertEquals("WG-001", dashboard.equipmentMetrics().get(0).equipmentCode());
            assertEquals("湿法制粒机", dashboard.equipmentMetrics().get(0).equipmentName());
            assertEquals(0, dashboard.equipmentMetrics().get(0).availability().compareTo(new BigDecimal("95")));
            assertEquals(0, dashboard.equipmentMetrics().get(0).performance().compareTo(new BigDecimal("88")));
            assertEquals(0, dashboard.equipmentMetrics().get(0).quality().compareTo(new BigDecimal("99")));
            assertEquals(0, dashboard.equipmentMetrics().get(0).oee().compareTo(new BigDecimal("83")));
            assertEquals("RUNNING", dashboard.equipmentMetrics().get(0).status());
            assertEquals("stable", dashboard.equipmentMetrics().get(0).trend());
            assertEquals(0, dashboard.avgOee().compareTo(new BigDecimal("73.5")));
            assertEquals("PT-001", dashboard.worstEquipment());
            assertEquals("WG-001", dashboard.bestEquipment());
        }

        @Test
        @DisplayName("empty — 应返回全零/空的看板数据")
        void empty_shouldReturnZeroedDashboard() {
            // When
            OeeDashboard dashboard = OeeDashboard.empty("FACTORY-01");

            // Then
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.avgOee().compareTo(BigDecimal.ZERO));
            assertTrue(dashboard.equipmentMetrics().isEmpty());
            assertNull(dashboard.worstEquipment());
            assertNull(dashboard.bestEquipment());
            assertNotNull(dashboard.refreshedAt());
        }

        @Test
        @DisplayName("hasLowOeeEquipment — 有设备 OEE 低于阈值时应返回 true")
        void hasLowOeeEquipment_belowThreshold_shouldReturnTrue() {
            // Given
            List<OeeDashboard.EquipmentOee> metrics = List.of(
                    new OeeDashboard.EquipmentOee("E1", "设备1", new BigDecimal("95"), new BigDecimal("88"),
                            new BigDecimal("99"), new BigDecimal("83"), "RUNNING", "stable"),
                    new OeeDashboard.EquipmentOee("E2", "设备2", new BigDecimal("50"), new BigDecimal("60"),
                            new BigDecimal("80"), new BigDecimal("24"), "FAULT", "down")
            );
            OeeDashboard dashboard = new OeeDashboard(
                    "F1", metrics, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, Instant.now());

            // When & Then
            assertTrue(dashboard.hasLowOeeEquipment(new BigDecimal("60")));
        }

        @Test
        @DisplayName("hasLowOeeEquipment — 所有设备 OEE 高于阈值时应返回 false")
        void hasLowOeeEquipment_allAboveThreshold_shouldReturnFalse() {
            // Given
            List<OeeDashboard.EquipmentOee> metrics = List.of(
                    new OeeDashboard.EquipmentOee("E1", "设备1", new BigDecimal("95"), new BigDecimal("88"),
                            new BigDecimal("99"), new BigDecimal("83"), "RUNNING", "stable")
            );
            OeeDashboard dashboard = new OeeDashboard(
                    "F1", metrics, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, Instant.now());

            // When & Then
            assertFalse(dashboard.hasLowOeeEquipment(new BigDecimal("60")));
        }

        @Test
        @DisplayName("getEquipmentCount — 应正确返回设备数量")
        void getEquipmentCount_shouldReturnCorrectCount() {
            // Given
            List<OeeDashboard.EquipmentOee> metrics = List.of(
                    new OeeDashboard.EquipmentOee("E1", "设备1", new BigDecimal("95"), new BigDecimal("88"),
                            new BigDecimal("99"), new BigDecimal("83"), "RUNNING", "stable"),
                    new OeeDashboard.EquipmentOee("E2", "设备2", new BigDecimal("92"), new BigDecimal("91"),
                            new BigDecimal("98"), new BigDecimal("82"), "RUNNING", "stable"),
                    new OeeDashboard.EquipmentOee("E3", "设备3", new BigDecimal("78"), new BigDecimal("85"),
                            new BigDecimal("97"), new BigDecimal("64"), "IDLE", "down")
            );
            OeeDashboard dashboard = new OeeDashboard(
                    "F1", metrics, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, Instant.now());

            // When & Then
            assertEquals(3, dashboard.getEquipmentCount());
        }

        @Test
        @DisplayName("EquipmentOee — record 所有趋势值应支持")
        void equipmentOee_record_trendValues() {
            // up trend
            OeeDashboard.EquipmentOee up = new OeeDashboard.EquipmentOee("E1", "D1",
                    BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "RUNNING", "up");
            assertEquals("up", up.trend());

            // stable trend
            OeeDashboard.EquipmentOee stable = new OeeDashboard.EquipmentOee("E2", "D2",
                    BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "RUNNING", "stable");
            assertEquals("stable", stable.trend());

            // down trend
            OeeDashboard.EquipmentOee down = new OeeDashboard.EquipmentOee("E3", "D3",
                    BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "IDLE", "down");
            assertEquals("down", down.trend());
        }
    }

    // ======================== InventoryDashboard 测试 ========================

    @Nested
    @DisplayName("InventoryDashboard 仓储看板")
    class InventoryDashboardTest {

        @Test
        @DisplayName("创建 — 应正确保存所有字段")
        void creation_shouldStoreAllFields() {
            // Given
            List<InventoryDashboard.SlowMovingItem> slowItems = List.of(
                    new InventoryDashboard.SlowMovingItem("MAT-001", "某原料", 120, new BigDecimal("500"), "A-01-01")
            );
            List<InventoryDashboard.NearExpiryItem> expiringItems = List.of(
                    new InventoryDashboard.NearExpiryItem("MAT-002", "某辅料", "B20250101", "2026-08-15", 21, new BigDecimal("200"))
            );

            // When
            InventoryDashboard dashboard = new InventoryDashboard(
                    "FACTORY-01", new BigDecimal("12.5"), 3, 5, 2,
                    new BigDecimal("10000"), new BigDecimal("3000"), new BigDecimal("6000"), new BigDecimal("1000"),
                    new BigDecimal("500000"), slowItems, expiringItems, Instant.now());

            // Then
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.turnoverRate().compareTo(new BigDecimal("12.5")));
            assertEquals(3, dashboard.slowMovingCount());
            assertEquals(5, dashboard.pendingInspectionCount());
            assertEquals(2, dashboard.nearExpiryCount());
            assertEquals(0, dashboard.onHandTotal().compareTo(new BigDecimal("10000")));
            assertEquals(0, dashboard.allocatedTotal().compareTo(new BigDecimal("3000")));
            assertEquals(0, dashboard.availableTotal().compareTo(new BigDecimal("6000")));
            assertEquals(0, dashboard.quarantineTotal().compareTo(new BigDecimal("1000")));
            assertEquals(0, dashboard.totalStockValue().compareTo(new BigDecimal("500000")));
            assertEquals(1, dashboard.slowMovingDetails().size());
            assertEquals("MAT-001", dashboard.slowMovingDetails().get(0).materialCode());
            assertEquals(120, dashboard.slowMovingDetails().get(0).daysSinceLastMove());
            assertEquals(1, dashboard.nearExpiryDetails().size());
            assertEquals("MAT-002", dashboard.nearExpiryDetails().get(0).materialCode());
            assertEquals(21, dashboard.nearExpiryDetails().get(0).daysToExpiry());
        }

        @Test
        @DisplayName("empty — 应返回全零/空的看板数据")
        void empty_shouldReturnZeroedDashboard() {
            // When
            InventoryDashboard dashboard = InventoryDashboard.empty("FACTORY-01");

            // Then
            assertEquals("FACTORY-01", dashboard.factoryCode());
            assertEquals(0, dashboard.turnoverRate().compareTo(BigDecimal.ZERO));
            assertEquals(0, dashboard.slowMovingCount());
            assertTrue(dashboard.slowMovingDetails().isEmpty());
            assertTrue(dashboard.nearExpiryDetails().isEmpty());
            assertNotNull(dashboard.refreshedAt());
        }

        @Test
        @DisplayName("hasSlowMovingItems — 有呆滞物料时应返回 true")
        void hasSlowMovingItems_withSlow_shouldReturnTrue() {
            // Given
            InventoryDashboard dashboard = new InventoryDashboard(
                    "F1", BigDecimal.ZERO, 3, 0, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of(), List.of(), Instant.now());

            // When & Then
            assertTrue(dashboard.hasSlowMovingItems());
        }

        @Test
        @DisplayName("hasNearExpiryItems — 有近效期物料时应返回 true")
        void hasNearExpiryItems_withExpiring_shouldReturnTrue() {
            // Given
            InventoryDashboard dashboard = new InventoryDashboard(
                    "F1", BigDecimal.ZERO, 0, 0, 5,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of(), List.of(), Instant.now());

            // When & Then
            assertTrue(dashboard.hasNearExpiryItems());
        }

        @Test
        @DisplayName("getAvailabilityRate — 应正确计算库存可用率")
        void getAvailabilityRate_shouldCalculateCorrectly() {
            // Given
            InventoryDashboard dashboard = new InventoryDashboard(
                    "F1", BigDecimal.ZERO, 0, 0, 0,
                    new BigDecimal("10000"), new BigDecimal("3000"), new BigDecimal("6000"), BigDecimal.ZERO,
                    BigDecimal.ZERO, List.of(), List.of(), Instant.now());

            // When
            BigDecimal rate = dashboard.getAvailabilityRate();

            // Then
            assertEquals(0, rate.compareTo(new BigDecimal("60.0000")));
        }

        @Test
        @DisplayName("getAvailabilityRate — 在手库存为零时应返回 0")
        void getAvailabilityRate_zeroOnHand_shouldReturnZero() {
            // Given
            InventoryDashboard dashboard = InventoryDashboard.empty("F1");

            // When
            BigDecimal rate = dashboard.getAvailabilityRate();

            // Then
            assertEquals(0, rate.compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("SlowMovingItem — record 应正确保存字段")
        void slowMovingItem_record_shouldStoreFields() {
            // When
            InventoryDashboard.SlowMovingItem item = new InventoryDashboard.SlowMovingItem(
                    "MAT-003", "某化学品", 150, new BigDecimal("300"), "B-02-05");

            // Then
            assertEquals("MAT-003", item.materialCode());
            assertEquals("某化学品", item.materialName());
            assertEquals(150, item.daysSinceLastMove());
            assertEquals(0, item.onHandQty().compareTo(new BigDecimal("300")));
            assertEquals("B-02-05", item.locationCode());
        }

        @Test
        @DisplayName("NearExpiryItem — record 应正确保存字段")
        void nearExpiryItem_record_shouldStoreFields() {
            // When
            InventoryDashboard.NearExpiryItem item = new InventoryDashboard.NearExpiryItem(
                    "MAT-004", "某原料药", "B20260701", "2026-08-10", 16, new BigDecimal("150"));

            // Then
            assertEquals("MAT-004", item.materialCode());
            assertEquals("某原料药", item.materialName());
            assertEquals("B20260701", item.batchNo());
            assertEquals("2026-08-10", item.expiryDate());
            assertEquals(16, item.daysToExpiry());
            assertEquals(0, item.onHandQty().compareTo(new BigDecimal("150")));
        }
    }

    // ======================== BiEventTypes 测试 ========================

    @Nested
    @DisplayName("BiEventTypes 事件类型常量")
    class BiEventTypesTest {

        @Test
        @DisplayName("PREFIX — 应为 'bi'")
        void prefix_shouldBeBi() {
            assertEquals("bi", BiEventTypes.PREFIX);
        }

        @Test
        @DisplayName("KPI_UPDATED — 命名应遵循 {module}.{entity}.{past_tense} 约定")
        void kpiUpdated_naming_shouldFollowConvention() {
            assertEquals("bi.kpi.updated", BiEventTypes.KPI_UPDATED);
        }

        @Test
        @DisplayName("KPI_COMPUTED — 命名应正确")
        void kpiComputed_naming_shouldBeCorrect() {
            assertEquals("bi.kpi.computed", BiEventTypes.KPI_COMPUTED);
        }

        @Test
        @DisplayName("PRODUCTION_DASHBOARD_REFRESHED — 命名应正确")
        void productionDashboardRefreshed_naming_shouldBeCorrect() {
            assertEquals("bi.dashboard.production_refreshed", BiEventTypes.PRODUCTION_DASHBOARD_REFRESHED);
        }

        @Test
        @DisplayName("QUALITY_DASHBOARD_REFRESHED — 命名应正确")
        void qualityDashboardRefreshed_naming_shouldBeCorrect() {
            assertEquals("bi.dashboard.quality_refreshed", BiEventTypes.QUALITY_DASHBOARD_REFRESHED);
        }

        @Test
        @DisplayName("OEE_DASHBOARD_REFRESHED — 命名应正确")
        void oeeDashboardRefreshed_naming_shouldBeCorrect() {
            assertEquals("bi.dashboard.oee_refreshed", BiEventTypes.OEE_DASHBOARD_REFRESHED);
        }

        @Test
        @DisplayName("INVENTORY_DASHBOARD_REFRESHED — 命名应正确")
        void inventoryDashboardRefreshed_naming_shouldBeCorrect() {
            assertEquals("bi.dashboard.inventory_refreshed", BiEventTypes.INVENTORY_DASHBOARD_REFRESHED);
        }

        @Test
        @DisplayName("所有事件常量 — 应以 'bi.' 开头")
        void allEvents_shouldStartWithBiPrefix() {
            assertTrue(BiEventTypes.KPI_UPDATED.startsWith("bi."));
            assertTrue(BiEventTypes.KPI_COMPUTED.startsWith("bi."));
            assertTrue(BiEventTypes.PRODUCTION_DASHBOARD_REFRESHED.startsWith("bi."));
            assertTrue(BiEventTypes.QUALITY_DASHBOARD_REFRESHED.startsWith("bi."));
            assertTrue(BiEventTypes.OEE_DASHBOARD_REFRESHED.startsWith("bi."));
            assertTrue(BiEventTypes.INVENTORY_DASHBOARD_REFRESHED.startsWith("bi."));
        }

        @Test
        @DisplayName("常量类 — 应为不可实例化（private 构造器）")
        void class_shouldNotBeInstantiable() {
            // 通过反射验证构造器为 private
            var constructors = BiEventTypes.class.getDeclaredConstructors();
            assertEquals(1, constructors.length);
            assertFalse(constructors[0].canAccess(null)); // private 构造器不可访问
        }
    }

    // ======================== DashboardService 接口契约测试 ========================

    @Nested
    @DisplayName("DashboardService 接口契约")
    class DashboardServiceTest {

        @Test
        @DisplayName("接口方法数量 — 应为 17 个方法")
        void interface_shouldHave17Methods() {
            var methods = DashboardService.class.getDeclaredMethods();
            assertEquals(17, methods.length);
        }

        @Test
        @DisplayName("getProductionDashboard — 方法签名应正确")
        void getProductionDashboard_signature_shouldBeCorrect() throws NoSuchMethodException {
            var method = DashboardService.class.getMethod("getProductionDashboard", String.class);
            assertEquals(ProductionDashboard.class, method.getReturnType());
        }

        @Test
        @DisplayName("getQualityDashboard — 方法签名应正确")
        void getQualityDashboard_signature_shouldBeCorrect() throws NoSuchMethodException {
            var method = DashboardService.class.getMethod("getQualityDashboard", String.class);
            assertEquals(QualityDashboard.class, method.getReturnType());
        }

        @Test
        @DisplayName("getOeeDashboard — 方法签名应正确")
        void getOeeDashboard_signature_shouldBeCorrect() throws NoSuchMethodException {
            var method = DashboardService.class.getMethod("getOeeDashboard", String.class);
            assertEquals(OeeDashboard.class, method.getReturnType());
        }

        @Test
        @DisplayName("getInventoryDashboard — 方法签名应正确")
        void getInventoryDashboard_signature_shouldBeCorrect() throws NoSuchMethodException {
            var method = DashboardService.class.getMethod("getInventoryDashboard", String.class);
            assertEquals(InventoryDashboard.class, method.getReturnType());
        }

        @Test
        @DisplayName("computeKpi — 方法签名应正确")
        void computeKpi_signature_shouldBeCorrect() throws NoSuchMethodException {
            var method = DashboardService.class.getMethod("computeKpi", String.class, String.class, String.class);
            assertEquals(KpiSnapshot.class, method.getReturnType());
        }

        @Test
        @DisplayName("getKpiSnapshot — 方法签名应正确")
        void getKpiSnapshot_signature_shouldBeCorrect() throws NoSuchMethodException {
            var method = DashboardService.class.getMethod("getKpiSnapshot", String.class, String.class);
            assertEquals(KpiSnapshot.class, method.getReturnType());
        }

        @Test
        @DisplayName("getKpiHistory — 方法签名应正确")
        void getKpiHistory_signature_shouldBeCorrect() throws NoSuchMethodException {
            var method = DashboardService.class.getMethod("getKpiHistory", String.class, KpiType.class, DashboardPeriod.class, int.class);
            assertEquals(List.class, method.getReturnType());
        }

        @Test
        @DisplayName("getBatchReport — 方法签名应正确")
        void getBatchReport_signature_shouldBeCorrect() throws NoSuchMethodException {
            var method = DashboardService.class.getMethod("getBatchReport", String.class);
            assertEquals(Map.class, method.getReturnType());
        }
    }

    // ======================== 综合场景测试 ========================

    @Nested
    @DisplayName("综合场景")
    class IntegrationScenarios {

        @Test
        @DisplayName("生产看板全流程 — 从空数据到含中断工单的完整看板")
        void productionDashboard_fullFlow_shouldWork() {
            // Given: 空看板
            ProductionDashboard empty = ProductionDashboard.empty("FACTORY-01");
            assertFalse(empty.hasInterruptedOrders());
            assertEquals(0.0, empty.getCompletionRatio());

            // When: 填充数据（模拟 MES 工单执行）
            List<ProductionDashboard.WorkOrderProgress> progress = List.of(
                    new ProductionDashboard.WorkOrderProgress("WO-001", "产品A", 100, "-", "COMPLETED"),
                    new ProductionDashboard.WorkOrderProgress("WO-002", "产品B", 60, "混合", "IN_PROGRESS"),
                    new ProductionDashboard.WorkOrderProgress("WO-003", "产品C", 0, "-", "INTERRUPTED")
            );
            ProductionDashboard active = new ProductionDashboard(
                    "FACTORY-01", new BigDecimal("85.3"), 2, new BigDecimal("9500"),
                    5, 2, 2, 1, progress, Instant.now());

            // Then
            assertTrue(active.hasInterruptedOrders());
            assertEquals(0.4, active.getCompletionRatio(), 0.001);
            assertEquals(3, active.workOrderProgress().size());
        }

        @Test
        @DisplayName("KPI 快照月度计算 — 从零到完整 KPI 的累积过程")
        void kpiSnapshot_monthlyComputation_shouldAccumulate() {
            // Given: 新建空快照
            KpiSnapshot snapshot = new KpiSnapshot("KPI-2026-07-F1", "2026-07", "FACTORY-01");
            assertFalse(snapshot.isFullyComputed());
            assertEquals(0, snapshot.getComputedKpiCount());

            // When: 逐步填充 KPI 数据（模拟各模块数据聚合计算）
            snapshot.setKpiValue(KpiType.PLAN_COMPLETION_RATE, new BigDecimal("85.30"));
            snapshot.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("98.70"));
            snapshot.setKpiValue(KpiType.OEE, new BigDecimal("83.20"));
            snapshot.setKpiValue(KpiType.MTTR, new BigDecimal("1.50"));
            snapshot.setKpiValue(KpiType.BATCH_YIELD, new BigDecimal("97.30"));
            snapshot.markComputed("BI引擎");

            // Then
            assertTrue(snapshot.isFullyComputed());
            assertEquals(5, snapshot.getComputedKpiCount());
            assertEquals("BI引擎", snapshot.getComputedBy());
            assertNotNull(snapshot.getComputedAt());
            assertEquals("2026-07", snapshot.getPeriod());
            assertEquals("FACTORY-01", snapshot.getFactoryCode());
        }

        @Test
        @DisplayName("OEE 看板设备趋势 — 三种趋势方向的设备应共存")
        void oeeDashboard_trendTypes_shouldCoexist() {
            // Given
            OeeDashboard.EquipmentOee improving = new OeeDashboard.EquipmentOee(
                    "E1", "设备1", new BigDecimal("90"), new BigDecimal("85"),
                    new BigDecimal("97"), new BigDecimal("74"), "RUNNING", "up");
            OeeDashboard.EquipmentOee stable = new OeeDashboard.EquipmentOee(
                    "E2", "设备2", new BigDecimal("95"), new BigDecimal("88"),
                    new BigDecimal("99"), new BigDecimal("83"), "RUNNING", "stable");
            OeeDashboard.EquipmentOee declining = new OeeDashboard.EquipmentOee(
                    "E3", "设备3", new BigDecimal("78"), new BigDecimal("85"),
                    new BigDecimal("97"), new BigDecimal("64"), "IDLE", "down");

            List<OeeDashboard.EquipmentOee> metrics = List.of(improving, stable, declining);
            OeeDashboard dashboard = new OeeDashboard(
                    "F1", metrics, new BigDecimal("82"), new BigDecimal("86"),
                    new BigDecimal("97.67"), new BigDecimal("73.67"),
                    "E3", "E2", Instant.now());

            // Then
            assertEquals(3, dashboard.getEquipmentCount());
            assertTrue(dashboard.hasLowOeeEquipment(new BigDecimal("75"))); // E1=74, E3=64 都低于 75
            assertFalse(dashboard.hasLowOeeEquipment(new BigDecimal("60"))); // 所有都 >= 60
            assertEquals("E3", dashboard.worstEquipment());
            assertEquals("E2", dashboard.bestEquipment());
        }

        @Test
        @DisplayName("质量看板偏差趋势 — 从空到有偏差的看板变化")
        void qualityDashboard_deviationTrend_shouldReflectStatus() {
            // Given: 空看板
            QualityDashboard empty = QualityDashboard.empty("FACTORY-01");
            assertFalse(empty.hasOpenDeviations());

            // When: 出现偏差
            List<QualityDashboard.DeviationSummary> deviations = List.of(
                    new QualityDashboard.DeviationSummary("DEV-001", "片重偏移超上限", "MAJOR", "OPEN", "2026-07-25T08:00:00Z"),
                    new QualityDashboard.DeviationSummary("DEV-002", "崩解时限不合格", "CRITICAL", "INVESTIGATING", "2026-07-25T09:30:00Z"),
                    new QualityDashboard.DeviationSummary("DEV-003", "外观瑕疵", "MINOR", "RESOLVED", "2026-07-24T14:00:00Z")
            );
            QualityDashboard active = new QualityDashboard(
                    "FACTORY-01", new BigDecimal("98.5"), 2, new BigDecimal("85.0"),
                    200, 197, 3, new BigDecimal("90.0"),
                    deviations, Map.of("MINOR", 1, "MAJOR", 1, "CRITICAL", 1), Instant.now());

            // Then
            assertTrue(active.hasOpenDeviations());
            assertEquals(3, active.recentDeviations().size());
            assertEquals(0.985, active.getPassRatio(), 0.001);
            assertEquals(2, active.openDeviationCount());
        }

        @Test
        @DisplayName("仓储看板风险预警 — 呆滞+近效期双风险场景")
        void inventoryDashboard_dualRisk_shouldDetectBoth() {
            // Given
            List<InventoryDashboard.SlowMovingItem> slowItems = List.of(
                    new InventoryDashboard.SlowMovingItem("MAT-OLD-01", "过期原料A", 180, new BigDecimal("200"), "A-01-01"),
                    new InventoryDashboard.SlowMovingItem("MAT-OLD-02", "闲置辅料B", 95, new BigDecimal("500"), "A-02-01")
            );
            List<InventoryDashboard.NearExpiryItem> expiringItems = List.of(
                    new InventoryDashboard.NearExpiryItem("MAT-EXP-01", "效期短的试剂C", "B20250801",
                            "2026-08-20", 26, new BigDecimal("50"))
            );

            // When
            InventoryDashboard dashboard = new InventoryDashboard(
                    "FACTORY-01", new BigDecimal("8.5"),
                    slowItems.size(), 3, expiringItems.size(),
                    new BigDecimal("8000"), new BigDecimal("2500"), new BigDecimal("4500"),
                    new BigDecimal("1000"), new BigDecimal("350000"),
                    slowItems, expiringItems, Instant.now());

            // Then
            assertTrue(dashboard.hasSlowMovingItems());
            assertTrue(dashboard.hasNearExpiryItems());
            assertEquals(2, dashboard.slowMovingCount());
            assertEquals(1, dashboard.nearExpiryCount());
            assertEquals(8.5, dashboard.turnoverRate().doubleValue(), 0.01);
            // 可用率 = 4500/8000 = 56.25%
            assertEquals(0, dashboard.getAvailabilityRate().compareTo(new BigDecimal("56.2500")));
        }
    }
}
