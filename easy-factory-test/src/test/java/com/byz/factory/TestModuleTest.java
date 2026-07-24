package com.byz.factory;

import com.byz.factory.andon.model.*;
import com.byz.factory.aps.model.*;
import com.byz.factory.batch.*;
import com.byz.factory.bi.model.*;
import com.byz.factory.dms.DocumentCategory;
import com.byz.factory.dms.DocumentStatus;
import com.byz.factory.dms.model.*;
import com.byz.factory.eam.*;
import com.byz.factory.eam.model.*;
import com.byz.factory.equip.*;
import com.byz.factory.equip.model.*;
import com.byz.factory.erp.*;
import com.byz.factory.erp.model.*;
import com.byz.factory.event.*;
import com.byz.factory.event.types.*;
import com.byz.factory.factory.*;
import com.byz.factory.factory.physical.*;
import com.byz.factory.iot.*;
import com.byz.factory.iot.model.*;
import com.byz.factory.lims.*;
import com.byz.factory.lims.model.*;
import com.byz.factory.mes.model.*;
import com.byz.factory.mps.ProductionPlanStatus;
import com.byz.factory.mps.model.*;
import com.byz.factory.operation.capability.ProcessRouteMatcher;
import com.byz.factory.operation.capacity.BottleneckDetector;
import com.byz.factory.operation.capacity.FactoryCapacityProfile;
import com.byz.factory.operation.resource.ResourceRequirementExploder;
import com.byz.factory.operation.time.*;
import com.byz.factory.plm.model.Blueprint;
import com.byz.factory.process.Action;
import com.byz.factory.process.IProcess;
import com.byz.factory.process.Process;
import com.byz.factory.qms.model.*;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.scm.*;
import com.byz.factory.scm.model.*;
import com.byz.factory.shared.Dict;
import com.byz.factory.shared.Dict.*;
import com.byz.factory.shared.UOM;
import com.byz.factory.wms.*;
import com.byz.factory.wms.model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * easy-factory-test — Phase 5 跨模块集成测试。
 * <p>
 * 覆盖全部15个业务模块的跨模块集成场景。
 *
 * @author 苏政
 */
@DisplayName("跨模块集成测试")
public class TestModuleTest {

    // ================================================================
    // 1 — 模块加载与发现
    // ================================================================

    @Nested
    @DisplayName("1. 模块加载与发现")
    class ModuleLoading {

        @Test
        @DisplayName("所有核心状态枚举应可被访问")
        void allStatusEnums_shouldBeAccessible() {
            assertNotNull(WorkOrderStatus.CREATED);
            assertNotNull(ProcessStatus.PENDING);
            assertNotNull(ScheduleStatus.DRAFT);
            assertNotNull(InspectionStatus.PENDING);
            assertNotNull(DeviationStatus.OPEN);
            assertNotNull(CapaStatus.OPEN);
            assertNotNull(AndonStatus.OPEN);
            assertNotNull(DocumentStatus.DRAFT);
            assertNotNull(FormulaStatus.DRAFT);
            assertNotNull(WeighingTaskStatus.PENDING);
            assertNotNull(BatchRecordStatus.IN_PROGRESS);
            assertNotNull(BlueprintStatus.DRAFT);
            assertNotNull(MachineStatus.IDLE);
            assertNotNull(PurchaseOrderStatus.DRAFT);
            assertNotNull(PickingTaskStatus.PENDING);
            assertNotNull(CommandStatus.QUEUED);
            assertNotNull(TransactionStatus.PENDING);
            assertNotNull(MaintenanceOrderStatus.OPEN);
        }

        @Test
        @DisplayName("所有值枚举应可被访问")
        void allValueEnums_shouldBeAccessible() {
            assertNotNull(InspectionType.IQC);
            assertNotNull(DeviationSeverity.CRITICAL);
            assertNotNull(DeviationDisposition.REWORK);
            assertNotNull(Execute.Convert);
            assertNotNull(Dict.SourceGroup.Machine);
            assertNotNull(UOM.KG);
            assertNotNull(TriggerType.EQUIPMENT_FAULT);
            assertNotNull(AndonSeverity.EMERGENCY);
            assertNotNull(KpiType.OEE);
            assertNotNull(DashboardPeriod.DAILY);
            assertNotNull(DocumentCategory.SOP);
            assertNotNull(StorageType.AMBIENT);
            assertNotNull(PickingType.FULL);
            assertNotNull(MaterialStatus.QUARANTINE);
            assertNotNull(DemandSourceType.SALES_ORDER);
            assertNotNull(MaintenanceType.PREVENTIVE);
        }

        @Test
        @DisplayName("15个业务模块的核心实体应可被构造")
        void allBusinessModuleEntities_shouldBeConstructable() {
            // MES
            MesWorkOrder wo = new MesWorkOrder("WO-001", "AMX-500MG", new BigDecimal("1000"));
            assertEquals(WorkOrderStatus.CREATED, wo.getStatus());

            // QMS
            InspectionOrder io = new InspectionOrder("IO-001", "B001", "P005");
            assertEquals(InspectionStatus.PENDING, io.getStatus());

            Deviation dev = new Deviation("DEV-001", "偏差-001", "MES", "IO-001", DeviationSeverity.MAJOR);
            assertEquals(DeviationStatus.OPEN, dev.getStatus());

            Capa capa = new Capa("CAPA-001", "CAPA-纠正措施", "DEV-001", "问题描述", "QA-主管");
            assertEquals(CapaStatus.OPEN, capa.getStatus());

            // Equip
            Equipment equip = new Equipment("WG-001", "湿法制粒机", "HLSG-200");
            assertEquals(MachineStatus.IDLE, equip.getStatus());

            // LIMS
            Formula formula = new Formula("F-001", "阿莫西林配方", "AMX-500MG");
            assertEquals(FormulaStatus.DRAFT, formula.getStatus());

            WeighingTask task = new WeighingTask("WT-001", "称量任务-001", "F-001", "WO-001", "B001");
            assertEquals(WeighingTaskStatus.PENDING, task.getStatus());

            BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-500MG");
            assertEquals(BatchRecordStatus.IN_PROGRESS, br.getStatus());

            // WMS
            Storage storage = new Storage("LOC-001", "原料库", "A区");
            assertNotNull(storage);
            Receipt receipt = new Receipt("RCP-001", "PO-001", "SUP-001");
            assertNotNull(receipt);
            PickingTask pt = new PickingTask("PK-001", "WO-001", "B001");
            assertNotNull(pt);
            com.byz.factory.wms.model.InventorySnapshot invSnap =
                    new com.byz.factory.wms.model.InventorySnapshot("MAT-001", "B202607-001", "LOC-001");
            assertNotNull(invSnap);

            // MPS
            ProductionPlan plan = new ProductionPlan("PP-001", "MONTHLY",
                    LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
            assertNotNull(plan);
            DemandSource ds = new DemandSource("SO-001", "AMX-500MG", new BigDecimal("5000"));
            assertNotNull(ds);

            // APS
            Schedule schedule = new Schedule("SCH-001", "PP-001", "FACTORY-01");
            assertNotNull(schedule);
            ResourceCalendar cal = new ResourceCalendar("RC-001", "WG-001", ResourceType.MACHINE,
                    LocalDate.now(),
                    Instant.parse("2026-07-25T08:00:00Z"),
                    Instant.parse("2026-07-25T16:00:00Z"),
                    new BigDecimal("16"));
            assertNotNull(cal);

            // Andon
            AndonCall call = new AndonCall("AC-001", TriggerType.EQUIPMENT_FAULT,
                    AndonSeverity.CRITICAL, AndonSource.AUTO, "系统", "设备故障停机");
            assertNotNull(call);

            // ERP
            MaterialCache mat = new MaterialCache("MAT-001", "阿莫西林API", "ROH");
            assertNotNull(mat);
            com.byz.factory.erp.model.InventorySnapshot erpInv =
                    new com.byz.factory.erp.model.InventorySnapshot("MAT-001", "PLANT-01", "SL-001");
            assertNotNull(erpInv);

            // SCM
            Supplier supplier = new Supplier("SUP-001", "原料供应商A", "原料");
            assertNotNull(supplier);
            PurchaseOrder po = new PurchaseOrder("PO-001", "SUP-001");
            assertNotNull(po);

            // DMS
            Document doc = new Document("DOC-001", "SOP-制粒操作规范", DocumentCategory.SOP);
            assertNotNull(doc);

            // EAM
            Asset asset = new Asset("AST-001", "湿法制粒机", "生产设备");
            assertNotNull(asset);
            MaintenanceOrder mo = new MaintenanceOrder("MO-001", "WG-001", MaintenanceType.PREVENTIVE);
            assertNotNull(mo);

            // BI
            KpiSnapshot kpi = new KpiSnapshot("KPI-001", "2026-07", "FACTORY-01");
            assertNotNull(kpi);
            ProductionDashboard pd = ProductionDashboard.empty("FACTORY-01");
            assertNotNull(pd);
            QualityDashboard qd = QualityDashboard.empty("FACTORY-01");
            assertNotNull(qd);
            OeeDashboard od = OeeDashboard.empty("FACTORY-01");
            assertNotNull(od);
            InventoryDashboard id = InventoryDashboard.empty("FACTORY-01");
            assertNotNull(id);
        }
    }

    // ================================================================
    // 2 — 跨模块事件契约
    // ================================================================

    @Nested
    @DisplayName("2. 跨模块事件契约")
    class CrossModuleEvents {

        @Test
        @DisplayName("所有事件常量类的 PREFIX 应正确以模块名开头")
        void allEventTypePrefixes_shouldStartWithModule() {
            assertEquals("mes", MesEventTypes.PREFIX);
            assertEquals("qms", QmsEventTypes.PREFIX);
            assertEquals("equip", EquipEventTypes.PREFIX);
            assertEquals("plm", PlmEventTypes.PREFIX);
            assertEquals("lims", LimsEventTypes.PREFIX);
            assertEquals("iot", IotEventTypes.PREFIX);
            assertEquals("mps", MpsEventTypes.PREFIX);
            assertEquals("aps", ApsEventTypes.PREFIX);
            assertEquals("andon", AndonEventTypes.PREFIX);
            assertEquals("bi", BiEventTypes.PREFIX);
            assertEquals("dms", DmsEventTypes.PREFIX);
        }

        @Test
        @DisplayName("MES 事件常量应包含关键工序事件")
        void mesEventTypes_shouldContainKeyProcessEvents() {
            assertNotNull(MesEventTypes.WORKORDER_CREATED);
            assertNotNull(MesEventTypes.WORKORDER_RELEASED);
            assertNotNull(MesEventTypes.WORKORDER_STARTED);
            assertNotNull(MesEventTypes.PROCESS_STARTED);
            assertNotNull(MesEventTypes.PROCESS_INTERRUPTED);
            assertNotNull(MesEventTypes.ACTION_COMPLETED);
        }

        @Test
        @DisplayName("AndonCall 确认应发布跨模块事件")
        void andonCall_acknowledge_shouldPublishEvent() {
            AndonCall call = new AndonCall("CALL-EVT-001", TriggerType.EQUIPMENT_FAULT,
                    AndonSeverity.CRITICAL, AndonSource.AUTO, "系统", "设备故障");
            List<IDomainEvent> events = new ArrayList<>();
            DomainEventPublisher.subscribe(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, events::add);

            call.acknowledge("班组长-赵六");

            assertEquals(1, events.size());
            assertEquals(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, events.get(0).getEventType());
            DomainEventPublisher.unsubscribe(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, events::add);
        }

        @Test
        @DisplayName("AndonCall 完整事件链：ACK→ESCALATE→RESOLVE→CLOSE")
        void andonCall_fullEventChain() {
            AndonCall call = new AndonCall("CALL-EVT-002", TriggerType.QUALITY_ISSUE,
                    AndonSeverity.CRITICAL, AndonSource.AUTO, "SPC系统", "连续不良");

            List<IDomainEvent> ackEvents = new ArrayList<>();
            DomainEventPublisher.subscribe(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, ackEvents::add);
            call.acknowledge("班组长");
            assertEquals(1, ackEvents.size());
            DomainEventPublisher.unsubscribe(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, ackEvents::add);

            List<IDomainEvent> escEvents = new ArrayList<>();
            DomainEventPublisher.subscribe(AndonEventTypes.ANDON_CALL_ESCALATED, escEvents::add);
            call.escalate("超时未响应");
            assertEquals(1, escEvents.size());
            DomainEventPublisher.unsubscribe(AndonEventTypes.ANDON_CALL_ESCALATED, escEvents::add);

            List<IDomainEvent> resEvents = new ArrayList<>();
            DomainEventPublisher.subscribe(AndonEventTypes.ANDON_CALL_RESOLVED, resEvents::add);
            call.resolve("已处理");
            assertEquals(1, resEvents.size());
            DomainEventPublisher.unsubscribe(AndonEventTypes.ANDON_CALL_RESOLVED, resEvents::add);

            List<IDomainEvent> clsEvents = new ArrayList<>();
            DomainEventPublisher.subscribe(AndonEventTypes.ANDON_CALL_CLOSED, clsEvents::add);
            call.close();
            assertEquals(1, clsEvents.size());
            DomainEventPublisher.unsubscribe(AndonEventTypes.ANDON_CALL_CLOSED, clsEvents::add);
        }

        @Test
        @DisplayName("前缀匹配订阅应可用")
        void prefixMatchingSubscription_shouldBeAvailable() {
            List<IDomainEvent> events = new ArrayList<>();
            DomainEventPublisher.subscribePrefix("andon.", events::add);

            AndonCall call = new AndonCall("CALL-PFX-001", TriggerType.EQUIPMENT_FAULT,
                    AndonSeverity.WARNING, AndonSource.AUTO, "系统", "测试前缀订阅");
            call.acknowledge("测试员");

            assertTrue(events.size() > 0);
            assertEquals(AndonEventTypes.ANDON_CALL_ACKNOWLEDGED, events.get(0).getEventType());

            // 清理
            DomainEventPublisher.subscribePrefix("andon.", null);
        }
    }

    // ================================================================
    // 3 — MES + QMS 集成
    // ================================================================

    @Nested
    @DisplayName("3. MES + QMS 集成：工单检验→偏差→CAPA")
    class MesQmsIntegration {

        @Test
        @DisplayName("工序中断→创建检验→检验失败→偏差→CAPA")
        void processInterrupted_inspectionFailed_deviation_capa() {
            MesWorkOrder wo = new MesWorkOrder("WO-IQ-001", "AMX-500MG", new BigDecimal("1000"));
            releaseWorkOrder(wo);
            wo.start();

            ProcessRecord pr = new ProcessRecord(wo.getWorkOrderNo(), "P005");
            pr.start("操作工-李四");
            pr.interrupt("QMS", "质量检验不通过");
            assertEquals(ProcessStatus.INTERRUPTED, pr.getStatus());

            InspectionOrder io = new InspectionOrder("IO-001", wo.getBatchNo(), "P005");
            io.startInspection("质检员-王五");

            InspectionRecord ir = new InspectionRecord("IR-001", "压片硬度");
            ir.setUsl(new BigDecimal("120"));
            ir.setLsl(new BigDecimal("80"));
            ir.record(new BigDecimal("75"), "质检员-王五", "硬度计-001");
            io.submitResult(ir);
            io.completeInspection();
            assertTrue(io.isFinished());
            assertFalse(io.isPassed());

            Deviation dev = new Deviation("DEV-001", "压片硬度偏差", "MES-QMS", io.getInspectionNo(), DeviationSeverity.MAJOR);
            assertTrue(dev.requiresCapa());

            dev.startInvestigation("QA-主管");
            dev.completeInvestigation("模具磨损导致片重偏差", "更换模具");
            dev.dispose(DeviationDisposition.REWORK, "重新压片", "QA-主管");
            assertEquals(DeviationStatus.DISPOSITIONED, dev.getStatus());

            Capa capa = new Capa("CAPA-001", "模具预防性维护", dev.getCode(), "模具使用寿命到期", "QA-主管");
            dev.linkCapa(capa.getCode());
            capa.analyzeRootCause("模具超使用寿命");
            capa.executeActions("建立模具定期更换SOP", "发布新SOP");
            capa.verify("验证通过", "质检员-王五");
            capa.close();

            dev.resolve();
            dev.close();
            assertEquals(DeviationStatus.CLOSED, dev.getStatus());
            assertEquals(CapaStatus.CLOSED, capa.getStatus());
        }

        @Test
        @DisplayName("偏差让步接收 — MINOR 偏差不需要 CAPA")
        void concessionDisposition_noCapaRequired() {
            Deviation dev = new Deviation("DEV-CONC", "外观轻微偏差", "QMS", "IO-010", DeviationSeverity.MINOR);
            dev.startInvestigation("QA-主管");
            dev.completeInvestigation("外观不影响质量", "无影响");
            dev.dispose(DeviationDisposition.CONCESSION, "客户同意让步接收", "QA-主管");
            assertFalse(dev.requiresCapa());
            dev.resolve();
            dev.close();
            assertEquals(DeviationStatus.CLOSED, dev.getStatus());
        }
    }

    // ================================================================
    // 4 — MES + Equip 集成
    // ================================================================

    @Nested
    @DisplayName("4. MES + Equip 集成：设备状态联动")
    class MesEquipIntegration {

        @Test
        @DisplayName("工单启动→设备占用→完成→释放")
        void workOrderStart_equipmentOccupy_complete_release() {
            Equipment machine = new Equipment("WG-001", "湿法制粒机", "HLSG-200");
            assertEquals(MachineStatus.IDLE, machine.getStatus());

            MesWorkOrder wo = new MesWorkOrder("WO-EQ-001", "AMX-500MG", new BigDecimal("1000"));
            releaseWorkOrder(wo);
            wo.start();
            machine.startProduction();
            assertEquals(MachineStatus.RUNNING, machine.getStatus());

            wo.complete();
            machine.stopProduction();
            assertEquals(MachineStatus.IDLE, machine.getStatus());
        }

        @Test
        @DisplayName("设备故障→维修→恢复")
        void equipmentFault_maintenance_restore() {
            Equipment machine = new Equipment("PT-001", "压片机", "ZP-35");
            machine.startProduction();
            machine.reportFault();
            assertEquals(MachineStatus.FAULT, machine.getStatus());

            machine.startMaintenance();
            assertEquals(MachineStatus.MAINTENANCE, machine.getStatus());

            machine.completeMaintenance();
            assertEquals(MachineStatus.IDLE, machine.getStatus());
        }

        @Test
        @DisplayName("OEE 计算：可用率×性能率×质量率")
        void oeeCalculation() {
            OEMetrics oee = OEMetrics.of("WG-001", LocalDate.now(), LocalDate.now(),
                    new BigDecimal("0.92"), new BigDecimal("0.88"), new BigDecimal("0.97"));
            BigDecimal result = oee.getOee();
            assertTrue(result.compareTo(new BigDecimal("0.78")) > 0);
            assertTrue(result.compareTo(new BigDecimal("0.79")) < 0);
            assertEquals("78.53%", oee.toPercentString());
        }
    }

    // ================================================================
    // 5 — MES + WMS 集成
    // ================================================================

    @Nested
    @DisplayName("5. MES + WMS 集成：工单驱动物料配送")
    class MesWmsIntegration {

        @Test
        @DisplayName("工单发布→WMS 拣料→配送→库存消耗")
        void workOrder_pickingTask_inventoryConsumption() {
            com.byz.factory.wms.model.InventorySnapshot inv =
                    new com.byz.factory.wms.model.InventorySnapshot("MAT-API", "B202607-001", "LOC-001");
            inv.addStock(new BigDecimal("500"));
            inv.allocate(new BigDecimal("50"));
            assertEquals(new BigDecimal("500"), inv.getOnHandQty());
            assertEquals(new BigDecimal("450"), inv.getAvailableQty());

            MesWorkOrder wo = new MesWorkOrder("WO-WM-001", "AMX-500MG", new BigDecimal("1000"));
            releaseWorkOrder(wo);

            PickingTask pt = new PickingTask("PK-001", wo.getWorkOrderNo(), "B202607-001");
            pt.addItem("MAT-API", new BigDecimal("50"));
            pt.start();
            pt.pickItem("MAT-API", new BigDecimal("50"), "B202607-001", "LOC-001");
            pt.completePicking();
            pt.deliver();
            assertEquals(PickingTaskStatus.DELIVERED, pt.getStatus());

            inv.deallocate(new BigDecimal("50"));
            inv.removeStock(new BigDecimal("50"));
            assertEquals(new BigDecimal("450"), inv.getOnHandQty());
            assertEquals(new BigDecimal("0"), inv.getAllocatedQty());
        }

        @Test
        @DisplayName("收货→来料待检→放行")
        void receipt_quarantine_release() {
            Receipt receipt = new Receipt("RCP-001", "PO-001", "SUP-001");
            receipt.receive("收货员-张三");
            receipt.addItem("MAT-API", "B202607-001", new BigDecimal("50"), new BigDecimal("100"));
            receipt.acceptItem("MAT-API", "LOC-001");
            receipt.complete();
            assertTrue(receipt.isFullyProcessed());
        }
    }

    // ================================================================
    // 6 — WMS + SCM 集成
    // ================================================================

    @Nested
    @DisplayName("6. WMS + SCM 集成：采购→收货")
    class WmsScmIntegration {

        @Test
        @DisplayName("采购订单→收货→进度→完成")
        void purchaseOrder_receipt_progress_completion() {
            PurchaseOrder po = new PurchaseOrder("PO-001", "SUP-001");
            po.addItem("MAT-API", new BigDecimal("100"));
            po.approve("采购主管");
            po.send();
            po.startReceiving();

            Receipt receipt = new Receipt("RCP-001", "PO-001", "SUP-001");
            receipt.receive("收货员-张三");
            receipt.addItem("MAT-API", "B202607-001", new BigDecimal("50"), new BigDecimal("100"));
            receipt.acceptItem("MAT-API", "LOC-001");
            po.getItems().get(0).receive(new BigDecimal("50"));
            assertFalse(po.getItems().get(0).isFullyReceived());

            po.getItems().get(0).receive(new BigDecimal("50"));
            assertTrue(po.getItems().get(0).isFullyReceived());

            receipt.complete();
            po.complete();
            assertEquals(PurchaseOrderStatus.COMPLETED, po.getStatus());
        }

        @Test
        @DisplayName("供应商双维度分离：资质与运营独立")
        void supplier_qualificationAndStatus_independent() {
            Supplier supplier = new Supplier("SUP-001", "原料供应商A", "原料");
            assertEquals("UNDER_REVIEW", supplier.getQualification());
            supplier.qualify();
            assertEquals("QUALIFIED", supplier.getQualification());

            supplier.deactivate();
            assertEquals(SupplierStatus.INACTIVE, supplier.getStatus());
            supplier.reactivate();
            assertEquals(SupplierStatus.ACTIVE, supplier.getStatus());
        }
    }

    // ================================================================
    // 7 — MES + LIMS 集成
    // ================================================================

    @Nested
    @DisplayName("7. MES + LIMS 集成：工单→配方→称量→批记录")
    class MesLimsIntegration {

        @Test
        @DisplayName("配方审批激活→称量→验证→完成")
        void formulaActivation_weighing_verification() {
            Formula formula = new Formula("F-001", "阿莫西林片剂配方", "AMX-500MG");
            formula.setBatchSize(new BigDecimal("100000"));
            formula.submitForApproval();
            formula.approve("QA-主管");
            formula.activate();
            assertEquals(FormulaStatus.ACTIVE, formula.getStatus());

            MesWorkOrder wo = new MesWorkOrder("WO-LIMS-001", "AMX-500MG", new BigDecimal("1000"));
            wo.setFormulaCode(formula.getCode());
            releaseWorkOrder(wo);

            WeighingTask task = new WeighingTask("WT-001", "称量任务-001",
                    formula.getCode(), wo.getWorkOrderNo(), "B202607-001");
            WeighingItem item = new WeighingItem("MAT-API", new BigDecimal("500"), new BigDecimal("5"));
            task.addItem(item);
            task.startWeighing();

            item.recordWeighing(new BigDecimal("501.2"), "操作工-张三", "复核员-李四", "天平-001");
            assertFalse(item.isOutOfTolerance());

            task.verify();
            task.completeWeighing();
            assertEquals(WeighingTaskStatus.COMPLETE, task.getStatus());
            assertTrue(task.getDeviatedItems().isEmpty());
        }

        @Test
        @DisplayName("称量偏差检测 — 超出允差")
        void weighingDeviation_outOfTolerance() {
            WeighingTask task = new WeighingTask("WT-002", "称量-002", "F-001", "WO-001", "B001");
            WeighingItem item = new WeighingItem("MAT-API", new BigDecimal("500"), new BigDecimal("1.5"));
            task.addItem(item);
            item.recordWeighing(new BigDecimal("510"), "操作工-张三", "复核员-李四", "天平-001");
            assertTrue(item.isOutOfTolerance());
            assertTrue(task.getDeviatedItems().contains(item));
        }

        @Test
        @DisplayName("批记录：创建→审核→批准→归档")
        void batchRecord_fullLifecycle() {
            BatchRecord br = new BatchRecord("B202607-001", "WO-001", "F-001", "1.0.0", "AMX-500MG");
            br.addProcessRecord("PR-001");
            br.addWeighingTask("WT-001");
            br.submitForReview();
            br.approve("QA-主管");
            br.archive();
            assertEquals(BatchRecordStatus.ARCHIVED, br.getStatus());
        }
    }

    // ================================================================
    // 8 — Andon 多源事件集成
    // ================================================================

    @Nested
    @DisplayName("8. Andon 多源事件集成")
    class AndonMultiSourceIntegration {

        @Test
        @DisplayName("安灯完整生命周期：OPEN→ACK→ESCALATED→RESOLVED→CLOSED")
        void andonCall_fullLifecycle() {
            AndonCall call = new AndonCall("AC-001", TriggerType.EQUIPMENT_FAULT,
                    AndonSeverity.CRITICAL, AndonSource.AUTO, "系统", "湿法制粒机停机");
            assertTrue(call.isActive());
            assertTrue(call.isUrgent());
            assertTrue(call.isAutoTriggered());

            call.linkWorkOrder("WO-001");
            call.linkEquipment("WG-001");
            call.acknowledge("班组长-赵六");
            assertEquals(AndonStatus.ACKNOWLEDGED, call.getStatus());

            call.escalate("超时未响应");
            assertEquals(AndonStatus.ESCALATED, call.getStatus());

            call.resolve("更换模具完成");
            call.close();
            assertEquals(AndonStatus.CLOSED, call.getStatus());
            assertFalse(call.isActive());

            assertEquals("WO-001", call.getWorkOrderNo());
            assertEquals("WG-001", call.getEquipmentCode());
        }

        @Test
        @DisplayName("紧急事件：OPEN→ESCALATED（跳过ACK）")
        void emergencyEvent_openToEscalated() {
            AndonCall call = new AndonCall("AC-E01", TriggerType.SAFETY_INCIDENT,
                    AndonSeverity.EMERGENCY, AndonSource.MANUAL, "操作工-李四", "化学品泄漏");
            call.escalate("立即疏散");
            assertEquals(AndonStatus.ESCALATED, call.getStatus());
            call.resolve("泄漏已控制");
            call.close();
            assertEquals(AndonStatus.CLOSED, call.getStatus());
        }

        @Test
        @DisplayName("上报规则配置")
        void escalationRule_configuration() {
            EscalationRule rule = new EscalationRule("ER-001", TriggerType.EQUIPMENT_FAULT, AndonSeverity.CRITICAL);
            rule.setAutoStopPolicy(EscalationRule.AutoStop.PAUSE_PROCESS);

            rule.addLevel(new EscalationRule.EscalationLevel(0, 0, EscalationRule.EscalateCondition.TIMEOUT));
            rule.addLevel(new EscalationRule.EscalationLevel(1, 5, EscalationRule.EscalateCondition.TIMEOUT));
            rule.addLevel(new EscalationRule.EscalationLevel(2, 15, EscalationRule.EscalateCondition.TIMEOUT));

            assertEquals(3, rule.getMaxLevel());
            assertEquals(0, rule.getLevel(0).getLevel());
            assertEquals(15, rule.getLevel(2).getTimeoutMinutes());
            rule.enable();
            assertTrue(rule.isEnabled());
        }
    }

    // ================================================================
    // 9 — BI 数据消费集成
    // ================================================================

    @Nested
    @DisplayName("9. BI 数据消费集成")
    class BiDataConsumption {

        @Test
        @DisplayName("KPI 快照 — 10 种标准 KPI 动态存取")
        void kpiSnapshot_all10KpiTypes() {
            KpiSnapshot kpi = new KpiSnapshot("KPI-001", "2026-07-25", "FACTORY-01");

            kpi.setKpiValue(KpiType.PLAN_COMPLETION_RATE, new BigDecimal("95.5"));
            kpi.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("98.2"));
            kpi.setKpiValue(KpiType.OEE, new BigDecimal("85.0"));
            kpi.setKpiValue(KpiType.MTTR, new BigDecimal("45"));
            kpi.setKpiValue(KpiType.MTBF, new BigDecimal("720"));
            kpi.setKpiValue(KpiType.BATCH_YIELD, new BigDecimal("97.0"));
            kpi.setKpiValue(KpiType.INVENTORY_TURNOVER, new BigDecimal("12.5"));
            kpi.setKpiValue(KpiType.DEVIATION_CLOSURE_RATE, new BigDecimal("88.0"));
            kpi.setKpiValue(KpiType.CAPA_CLOSURE_RATE, new BigDecimal("90.0"));
            kpi.setKpiValue(KpiType.ANDON_RESPONSE_TIME, new BigDecimal("3.5"));

            kpi.markComputed("张三");

            assertEquals(new BigDecimal("95.5"), kpi.getKpiValue(KpiType.PLAN_COMPLETION_RATE));
            assertEquals(new BigDecimal("85.0"), kpi.getKpiValue(KpiType.OEE));
            assertEquals(new BigDecimal("3.5"), kpi.getKpiValue(KpiType.ANDON_RESPONSE_TIME));
            assertEquals(10, kpi.getComputedKpiCount());
            assertTrue(kpi.isFullyComputed());
        }

        @Test
        @DisplayName("KPI 类型元数据正确性")
        void kpiType_metadata() {
            assertEquals("计划完成率", KpiType.PLAN_COMPLETION_RATE.getDisplayName());
            assertEquals("MES + MPS", KpiType.PLAN_COMPLETION_RATE.getDataSource());
            assertNotNull(KpiType.OEE.getDisplayName());
            assertTrue(KpiType.OEE.getDisplayName().contains("OEE") || KpiType.OEE.getDisplayName().contains("效率"));
            assertEquals("Equip + IoT", KpiType.OEE.getDataSource());
        }

        @Test
        @DisplayName("看板值对象工厂方法 — record 访问器")
        void dashboard_emptyFactoryMethods() {
            ProductionDashboard pd = ProductionDashboard.empty("FACTORY-01");
            assertEquals("FACTORY-01", pd.factoryCode());
            assertEquals(BigDecimal.ZERO, pd.planCompletionRate());

            QualityDashboard qd = QualityDashboard.empty("FACTORY-01");
            assertEquals("FACTORY-01", qd.factoryCode());

            OeeDashboard od = OeeDashboard.empty("FACTORY-01");
            assertEquals("FACTORY-01", od.factoryCode());

            InventoryDashboard id = InventoryDashboard.empty("FACTORY-01");
            assertEquals("FACTORY-01", id.factoryCode());
        }

        @Test
        @DisplayName("DashboardPeriod 刷新间隔")
        void dashboardPeriod_refreshIntervals() {
            assertEquals(5, DashboardPeriod.REALTIME.getRefreshIntervalSeconds());
            assertEquals(60, DashboardPeriod.HOURLY.getRefreshIntervalSeconds());
            assertEquals(30, DashboardPeriod.DAILY.getRefreshIntervalSeconds());
            assertEquals(3600, DashboardPeriod.MONTHLY.getRefreshIntervalSeconds());
        }
    }

    // ================================================================
    // 10 — 计划-排程-执行-仓储全流程
    // ================================================================

    @Nested
    @DisplayName("10. 计划-排程-执行-仓储全流程")
    class PlanScheduleExecuteFlow {

        @Test
        @DisplayName("MPS→APS→MES 全链路")
        void mpsToApsToMes_fullChain() {
            ProductionPlan plan = new ProductionPlan("PP-001", "MONTHLY",
                    LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
            plan.approve("计划主管");
            plan.release();
            assertEquals(ProductionPlanStatus.RELEASED, plan.getStatus());

            Schedule schedule = new Schedule("SCH-001", plan.getPlanNo(), "FACTORY-01");
            schedule.optimize("EDD");
            schedule.dispatch();
            assertEquals(ScheduleStatus.DISPATCHED, schedule.getStatus());

            MesWorkOrder wo = new MesWorkOrder("WO-MPS-001", "AMX-500MG", new BigDecimal("100000"));
            releaseWorkOrder(wo);
            wo.start();
            assertEquals(WorkOrderStatus.IN_PROGRESS, wo.getStatus());
        }

        @Test
        @DisplayName("产能检查：CapacityCheck record 值对象")
        void capacityCheck_recordValueObject() {
            CapacityCheck pass = CapacityCheck.pass("PP-001", "FACTORY-01", "MACHINE",
                    BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, null);
            assertTrue(pass.isAcceptable());

            CapacityCheck fail = CapacityCheck.fail("PP-001", "FACTORY-01", "MACHINE",
                    new BigDecimal("20"), BigDecimal.TEN, new BigDecimal("200"), "产能不足");
            assertFalse(fail.isAcceptable());
        }

        @Test
        @DisplayName("需求来源注册与优先级")
        void demandSource_registration() {
            DemandSource ds1 = new DemandSource("SO-001", "AMX-500MG", new BigDecimal("50000"));
            ds1.setPriority(1);
            ds1.setDueDate(LocalDate.of(2026, 8, 15));

            DemandSource ds2 = new DemandSource("FC-001", "AMX-500MG", new BigDecimal("30000"));
            ds2.setPriority(5);

            assertEquals(1, ds1.getPriority());
            assertEquals(5, ds2.getPriority());
            assertEquals(new BigDecimal("50000"), ds1.getQuantity());
        }
    }

    // ================================================================
    // 11 — 完整制造场景：阿莫西林片剂端到端
    // ================================================================

    @Nested
    @DisplayName("11. 完整制造场景：阿莫西林片剂端到端")
    class FullManufacturingScenario {

        static final BigDecimal BATCH = new BigDecimal("100000");

        @Test
        @DisplayName("阿莫西林片剂全流程：蓝图→计划→排程→工单→称量→检验→批记录→KPI")
        void amoxicillinEndToEnd_fullPipeline() {
            // 1. PLM: 蓝图
            Blueprint blueprint = buildAmoxicillinBlueprint();
            blueprint.submitForReview();
            blueprint.approve("QA-主管");
            blueprint.release();
            assertEquals(BlueprintStatus.RELEASED, blueprint.getStatus());

            // 2. MPS: 计划
            ProductionPlan plan = new ProductionPlan("PP-AMX-001", "MONTHLY",
                    LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
            plan.approve("计划主管");
            plan.release();

            // 3. APS: 排程
            Schedule schedule = new Schedule("SCH-AMX-001", plan.getPlanNo(), "FACTORY-01");
            schedule.optimize("EDD");
            schedule.dispatch();

            // 4. MES: 工单
            MesWorkOrder wo = new MesWorkOrder("WO-AMX-001", "AMX-500MG", BATCH);
            wo.setBatchNo("B202607-001");
            wo.release(blueprint, "操作工-张三",
                    Instant.parse("2026-07-25T08:00:00Z"),
                    Instant.parse("2026-07-25T16:00:00Z"),
                    "FACTORY-01");
            assertTrue(wo.isBlueprintVersionFrozen());
            wo.start();

            // 5. LIMS: 配方→称量
            Formula formula = new Formula("F-AMX-001", "阿莫西林配方", "AMX-500MG");
            formula.submitForApproval();
            formula.approve("QA-主管");
            formula.activate();

            WeighingTask weighing = new WeighingTask("WT-AMX-001", "称量-AMX",
                    formula.getCode(), wo.getWorkOrderNo(), wo.getBatchNo());
            WeighingItem apiItem = new WeighingItem("MAT-API", new BigDecimal("50000"), new BigDecimal("100"));
            weighing.addItem(apiItem);
            weighing.startWeighing();
            apiItem.recordWeighing(new BigDecimal("50002.3"), "操作工-张三", "复核员-李四", "BAL-001");
            weighing.verify();
            weighing.completeWeighing();

            // 6. Equip: 设备运行
            Equipment granulator = new Equipment("WG-001", "湿法制粒机", "HLSG-200");
            granulator.startProduction();
            assertEquals(MachineStatus.RUNNING, granulator.getStatus());

            // 7. QMS: 中间体检验
            InspectionOrder inspection = new InspectionOrder("IO-AMX-001", wo.getBatchNo(), "P005");
            inspection.setTotalItems(1);
            inspection.startInspection("质检员-王五");
            InspectionRecord ir = new InspectionRecord("IR-AMX-001", "片重");
            ir.setUsl(new BigDecimal("1.05"));
            ir.setLsl(new BigDecimal("0.95"));
            ir.record(new BigDecimal("1.00"), "质检员-王五", "天平-001");
            inspection.submitResult(ir);
            inspection.completeInspection();
            assertTrue(inspection.isPassed());

            // 8. WMS: 成品入库
            com.byz.factory.wms.model.InventorySnapshot fg =
                    new com.byz.factory.wms.model.InventorySnapshot("AMX-500MG", wo.getBatchNo(), "LOC-FG-001");
            fg.addStock(BATCH);
            assertEquals(BATCH, fg.getOnHandQty());

            // 9. LIMS: 批记录归档
            BatchRecord br = new BatchRecord(wo.getBatchNo(), wo.getWorkOrderNo(),
                    formula.getCode(), formula.getVersion(), "AMX-500MG");
            br.addProcessRecord("PR-001");
            br.submitForReview();
            br.approve("QA-主管");
            br.archive();

            // 10. 收尾
            granulator.stopProduction();
            wo.complete();
            wo.close();

            // 11. BI: KPI
            KpiSnapshot kpi = new KpiSnapshot("KPI-AMX-001", "2026-07-25", "FACTORY-01");
            kpi.setKpiValue(KpiType.PLAN_COMPLETION_RATE, new BigDecimal("100"));
            kpi.setKpiValue(KpiType.BATCH_YIELD, new BigDecimal("97.5"));
            kpi.setKpiValue(KpiType.FIRST_PASS_RATE, new BigDecimal("100"));
            kpi.markComputed("系统");
            assertTrue(kpi.isFullyComputed());
        }

        @Test
        @DisplayName("操作分析：工艺路线匹配 + 提前期 + 资源需求 + 瓶颈")
        void operationAnalysis_pipeline() {
            IBlueprint bp = buildAmoxicillinBlueprint();
            Factory factory = buildFactory();
            FactoryCapacityProfile profile = new FactoryCapacityProfile("FACTORY-01", Duration.ofHours(16));
            profile.withMachine("湿法制粒机 WG-001", 960, 0.85)
                    .withMachine("压片机 PT-001", 960, 0.90);

            Map<String, ITimed> durations = new LinkedHashMap<>();
            durations.put("A001", ActionDuration.forAction("A001").processingMinutes(10).fixedTime().build());
            GmpActionDuration.Builder gmpBuilder = GmpActionDuration.forAction("A005");
            gmpBuilder.processingMinutes(90);
            gmpBuilder.maxAllowedHours(4);
            gmpBuilder.minRequiredMinutes(60);
            durations.put("A005", gmpBuilder.build());
            durations.put("A016", ActionDuration.forAction("A016")
                    .processing(Duration.ofSeconds(1)).variableTime(BATCH).build());

            ProcessRouteMatcher matcher = new ProcessRouteMatcher();
            var match = matcher.match(bp, factory);
            assertTrue(match.feasible());

            ProductionLeadTime timeCalc = new ProductionLeadTime(Duration.ofMinutes(10), Duration.ofMinutes(5));
            var lead = timeCalc.calculate(bp, durations, BATCH);
            assertTrue(lead.totalLeadTime().toMinutes() > 0);

            ResourceRequirementExploder exploder = new ResourceRequirementExploder();
            var resources = exploder.explode(bp, BATCH);
            assertTrue(resources.getResourceTypeCount() > 0);

            BottleneckDetector detector = new BottleneckDetector();
            var bottleneck = detector.detect(bp, durations, profile);
            assertNotNull(bottleneck.maxDailyThroughput());
        }

        @Test
        @DisplayName("GMP 合规：湿法制粒约束校验")
        void gmpCompliance() {
            GmpActionDuration.Builder builder = GmpActionDuration.forAction("A005");
            builder.processingMinutes(90);
            builder.maxAllowedHours(4);
            builder.minRequiredMinutes(60);
            GmpActionDuration gmpDur = builder.build();
            assertTrue(gmpDur.checkConstraints(Duration.ofMinutes(90)).passed());
            assertFalse(gmpDur.checkConstraints(Duration.ofHours(5)).passed());
            assertFalse(gmpDur.checkConstraints(Duration.ofMinutes(30)).passed());
        }

        @Test
        @DisplayName("审计追踪 & 电子签名")
        void auditTrail_signature() {
            AuditTrail trail = new AuditTrail("Batch", "B20260725-001", "TRANSITION",
                    "操作工-张三", Instant.now(),
                    "{\"status\":\"CREATED\"}", "{\"status\":\"IN_PROGRESS\"}", "开工确认完成");
            assertEquals("Batch", trail.entityType());
            assertEquals("B20260725-001", trail.entityId());
            assertNotNull(SignatureMeaning.APPROVED);
            assertNotNull(SignatureMeaning.REVIEWED);
        }

        private Blueprint buildAmoxicillinBlueprint() {
            List<IProcess> processes = new ArrayList<>();
            Process p001 = new Process("P001", "称量配料");
            p001.setActions(List.of(
                    newAction("A001", "领取原料", Execute.Add, resource("原料-阿莫西林API", SourceGroup.Material)),
                    newAction("A005", "湿法制粒", Execute.Convert, resource("湿法制粒机", SourceGroup.Machine)),
                    newAction("A006", "干燥", Execute.Change, resource("干燥机", SourceGroup.Machine))));
            processes.add(p001);
            Process p005 = new Process("P005", "压片");
            p005.setActions(List.of(
                    newAction("A016", "压片", Execute.Convert, resource("压片机", SourceGroup.Machine))));
            processes.add(p005);
            Blueprint bp = new Blueprint("AMX-TAB-v1.0", "阿莫西林片剂工艺路线", "AMX-500MG");
            bp.setProcesses(processes);
            return bp;
        }

        private Action newAction(String code, String name, Execute type, IResourceItem... resources) {
            Action a = new Action(code, name, Importance.Require, System.currentTimeMillis());
            a.setExecuteType(type);
            a.setRequireResources(resources);
            return a;
        }

        private ResourceItem resource(String name, SourceGroup group) {
            return new ResourceItem(name, group, SourceType.Other, BigDecimal.ONE);
        }

        private Factory buildFactory() {
            Factory factory = new Factory("FACTORY-01", "口服固体制剂车间");
            factory.registerWorkstation(new Workstation("W01", "称量", StationType.SINGLE)
                    .addEquipment("BAL-001", "A001"));
            factory.registerWorkstation(new Workstation("W02", "制粒", StationType.SINGLE)
                    .addEquipment("WG-001", "A005"));
            factory.registerWorkstation(new Workstation("W03", "干燥", StationType.SINGLE)
                    .addEquipment("DR-001", "A006"));
            factory.registerWorkstation(new Workstation("W04", "压片", StationType.PARALLEL)
                    .addEquipment(new EquipmentBinding("PT-001").addAction("A016").setPrimary(true)));
            ProductionLine line = new ProductionLine("LINE-01", "固体制剂线")
                    .addProcesses("P001", "P005")
                    .addNode("W01").addNode("W02").addNode("W03").addNode("W04");
            factory.addLine(line);
            return factory;
        }
    }

    // ================================================================
    // 12 — DMS + EAM + IoT + ERP + PLM 综合验证
    // ================================================================

    @Nested
    @DisplayName("12. DMS + EAM + IoT + ERP + PLM 综合验证")
    class AdditionalModuleIntegration {

        @Test
        @DisplayName("DMS 文档→审批→生效→版本升级→作废")
        void document_lifecycle() {
            Document doc = new Document("DOC-SOP-001", "SOP-制粒操作规范", DocumentCategory.SOP);
            doc.submitForReview();
            assertEquals(DocumentStatus.UNDER_REVIEW, doc.getStatus());

            doc.approve("QA-主管");
            assertEquals(DocumentStatus.APPROVED, doc.getStatus());

            doc.makeEffective(LocalDate.of(2026, 8, 1));
            assertEquals(DocumentStatus.EFFECTIVE, doc.getStatus());

            Document newDoc = doc.supersede("旧版过期");
            assertEquals(DocumentStatus.DRAFT, newDoc.getStatus());
            assertEquals(DocumentStatus.OBSOLETE, doc.getStatus());
        }

        @Test
        @DisplayName("EAM 资产 + 维护工单")
        void eam_asset_maintenance() {
            Asset asset = new Asset("AST-001", "湿法制粒机", "生产设备");
            assertEquals(AssetStatus.IDLE, asset.getStatus());

            asset.transition(AssetStatus.IN_USE);
            assertEquals(AssetStatus.IN_USE, asset.getStatus());

            MaintenanceOrder mo = new MaintenanceOrder("MO-001", "WG-001", MaintenanceType.PREVENTIVE);
            assertEquals(MaintenanceOrderStatus.OPEN, mo.getStatus());
            assertEquals(MaintenancePriority.MEDIUM, mo.getPriority());

            mo.transition(MaintenanceOrderStatus.IN_PROGRESS);
            assertEquals(MaintenanceOrderStatus.IN_PROGRESS, mo.getStatus());

            asset.transition(AssetStatus.UNDER_MAINTENANCE);
            assertEquals(AssetStatus.UNDER_MAINTENANCE, asset.getStatus());

            mo.transition(MaintenanceOrderStatus.COMPLETED);
            mo.transition(MaintenanceOrderStatus.VERIFIED);
            assertEquals(MaintenanceOrderStatus.VERIFIED, mo.getStatus());
        }

        @Test
        @DisplayName("IoT 设备连接→标签采集→指令下发→报警")
        void iot_device_tag_command_alarm() {
            DeviceConnection conn = new DeviceConnection("WG-001", "OPCUA",
                    "opc.tcp://192.168.1.100:4840");
            conn.setPollIntervalMs(1);
            try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            assertTrue(conn.isTimedOut());
            conn.heartbeat();
            assertFalse(conn.isTimedOut());

            TagValue tag = TagValue.of("WG-001", "TEMP", new BigDecimal("62.5"));
            assertEquals(TagValue.TagQuality.GOOD, tag.getQuality());

            TagValue badTag = TagValue.bad("WG-001", "VIB");
            assertEquals(TagValue.TagQuality.BAD, badTag.getQuality());

            TagValue uncertainTag = TagValue.uncertain("WG-001", "PRESS", "超出量程");
            assertEquals(TagValue.TagQuality.UNCERTAIN, uncertainTag.getQuality());

            Command cmd = new Command("CMD-001", "WG-001",
                    ICommand.CommandType.SET_PARAM,
                    Map.of("temperature", "60"));
            assertEquals(CommandStatus.QUEUED, cmd.getStatus());
            cmd.markSent();
            cmd.markAcknowledged();
            cmd.markCompleted("OK", "指令执行成功");
            assertEquals(CommandStatus.COMPLETED, cmd.getStatus());

            AlarmEvent alarm = new AlarmEvent("ALM-001", "WG-001",
                    IAlarmEvent.AlarmSeverity.CRITICAL,
                    new BigDecimal("75"), new BigDecimal("65"));
            alarm.addAction("自动停机");
            alarm.acknowledge("操作工-张三");
            assertTrue(alarm.isAcknowledged());
        }

        @Test
        @DisplayName("ERP 物料映射 + 事务回传")
        void erp_materialMapping_transaction() {
            MaterialCache mat = new MaterialCache("MAT-001", "阿莫西林API", "ROH");
            IResourceItem item = mat.toResourceItem();
            assertNotNull(item);
            assertEquals(SourceGroup.Material, item.getGroup());

            Transaction tx = new Transaction("TX-001", TransactionType.GOODS_ISSUE,
                    "MAT-001", new BigDecimal("50"));
            tx.markSent();
            tx.markConfirmed();
            assertEquals(TransactionStatus.CONFIRMED, tx.getStatus());
        }

        @Test
        @DisplayName("ERP 事务失败重试")
        void erp_transaction_failedRetry() {
            Transaction tx = new Transaction("TX-002", TransactionType.GOODS_RECEIPT,
                    "MAT-002", new BigDecimal("100"));
            tx.markSent();
            tx.markFailed("连接超时");
            assertEquals(TransactionStatus.FAILED, tx.getStatus());
            tx.retry();
            assertEquals(TransactionStatus.PENDING, tx.getStatus());
        }

        @Test
        @DisplayName("PLM 蓝图版本管理")
        void plm_blueprintVersionManagement() {
            Blueprint bp = buildSimpleBlueprint();
            bp.submitForReview();
            bp.approve("QA-主管");
            bp.release();
            assertEquals(BlueprintStatus.RELEASED, bp.getStatus());
            bp.obsolete();
            assertEquals(BlueprintStatus.OBSOLETED, bp.getStatus());
        }

        @Test
        @DisplayName("WMS 库存操作：入库/出库/分配/释放/待检/盘点")
        void wms_inventory_operations() {
            com.byz.factory.wms.model.InventorySnapshot inv =
                    new com.byz.factory.wms.model.InventorySnapshot("MAT-001", "B001", "LOC-001");

            inv.addStock(new BigDecimal("100"));
            assertEquals(new BigDecimal("100"), inv.getAvailableQty());

            inv.allocate(new BigDecimal("30"));
            assertEquals(new BigDecimal("30"), inv.getAllocatedQty());
            assertEquals(new BigDecimal("70"), inv.getAvailableQty());

            inv.deallocate(new BigDecimal("10"));
            assertEquals(new BigDecimal("20"), inv.getAllocatedQty());

            inv.quarantine(new BigDecimal("20"));
            assertEquals(new BigDecimal("20"), inv.getQuarantineQty());

            inv.release(new BigDecimal("10"));
            assertEquals(new BigDecimal("10"), inv.getQuarantineQty());

            inv.count(new BigDecimal("95"), Instant.now());
            assertEquals(new BigDecimal("95"), inv.getOnHandQty());
        }

        private Blueprint buildSimpleBlueprint() {
            List<IProcess> processes = new ArrayList<>();
            Process p = new Process("P001", "简单工序");
            p.setActions(List.of(new Action("A001", "动作1", Importance.Require, System.currentTimeMillis())));
            processes.add(p);
            Blueprint bp = new Blueprint("BP-001", "简单蓝图", "PROD-001");
            bp.setProcesses(processes);
            return bp;
        }
    }

    // ================================================================
    // Utility helper
    // ================================================================

    private static void releaseWorkOrder(MesWorkOrder wo) {
        List<IProcess> procs = new ArrayList<>();
        Process p = new Process("P001", "测试工序");
        p.setActions(List.of(new Action("A001", "测试动作", Importance.Require, System.currentTimeMillis())));
        procs.add(p);
        Blueprint bp = new Blueprint("BP-TEST", "测试蓝图", "PROD-TEST");
        bp.setProcesses(procs);
        bp.submitForReview();
        bp.approve("QA-主管");
        bp.release();
        wo.release(bp, "测试操作员",
                Instant.parse("2026-07-25T08:00:00Z"),
                Instant.parse("2026-07-25T16:00:00Z"),
                "FACTORY-01");
    }
}
