package com.byz.factory.mes;

import com.byz.factory.batch.ProcessStatus;
import com.byz.factory.batch.WorkOrderStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.MesEventTypes;
import com.byz.factory.exception.ActionException;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.mes.model.ActionRecord;
import com.byz.factory.mes.model.MesWorkOrder;
import com.byz.factory.mes.model.ProcessRecord;
import com.byz.factory.mes.service.BlueprintProvider;
import com.byz.factory.mes.service.impl.WorkOrderServiceImpl;
import com.byz.factory.process.Action;
import com.byz.factory.process.IProcess;
import com.byz.factory.process.Process;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.Dict;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WorkOrderServiceImpl 单元测试。
 * <p>
 * 覆盖：工单全生命周期、蓝图版本冻结、工序流转与动作报工、
 * QMS 中断与恢复、事件发布验证、错误场景。
 *
 * @author 苏政
 */
class WorkOrderServiceImplTest {

    private WorkOrderServiceImpl service;
    private final Map<String, IBlueprint> blueprintStore = new ConcurrentHashMap<>();
    private final BlueprintProvider testProvider = (code, version) -> blueprintStore.get(code);

    @BeforeEach
    void setUp() {
        service = new WorkOrderServiceImpl(testProvider);
        blueprintStore.clear();
    }

    @AfterEach
    void tearDown() {
        DomainEventPublisher.clearAll();
        service.clearAll();
    }

    // ==================== 辅助方法 ====================

    private IBlueprint registerBlueprint(String code, String version, List<IProcess> processes) {
        IBlueprint bp = new IBlueprint() {
            @Override public String getCode() { return code; }
            @Override public String getName() { return "测试蓝图-" + code; }
            @Override public String getVersion() { return version; }
            @Override public List<IProcess> getProductionProcessList() { return processes; }
        };
        blueprintStore.put(code, bp);
        return bp;
    }

    private void registerSimpleBlueprint() {
        Process proc = new Process("PROC-MIX", "搅拌工序");
        proc.setActions(List.of(new Action("ACT-STIR", "搅拌", Dict.Importance.Require, 1)));
        registerBlueprint("BP-001", "1.0.0", List.of(proc));
    }

    private void registerTwoProcessBlueprint() {
        Process p1 = new Process("PROC-DISPENSE", "称量工序");
        p1.setActions(List.of(new Action("ACT-WEIGH", "称量", Dict.Importance.Require, 1)));
        Process p2 = new Process("PROC-MIX", "混合工序");
        p2.setActions(List.of(new Action("ACT-STIR", "搅拌", Dict.Importance.Require, 1)));
        registerBlueprint("BP-002", "2.0.0", List.of(p1, p2));
    }

    private IResourcePack testInput() {
        return ResourceItem.pack(
                new ResourceItem("MAT-001", Dict.SourceGroup.Material, Dict.SourceType.RawMaterial, BigDecimal.ONE));
    }

    /** 创建并下达工单，返回工单 */
    private MesWorkOrder createAndRelease() {
        registerSimpleBlueprint();
        MesWorkOrder wo = service.create("BP-001", "PROD-001", BigDecimal.TEN);
        return service.release(wo.getWorkOrderNo(), "操作工A",
                Instant.parse("2026-07-20T08:00:00Z"),
                Instant.parse("2026-07-20T16:00:00Z"),
                "FACTORY-A");
    }

    /** 创建、下达并开工，返回工单 */
    private MesWorkOrder createReleaseAndStart() {
        MesWorkOrder wo = createAndRelease();
        return service.start(wo.getWorkOrderNo());
    }

    /** 创建、下达、开工并执行默认工序，返回工单 */
    private MesWorkOrder createReleaseStartAndExecute() {
        MesWorkOrder wo = createReleaseAndStart();
        service.executeProcess(wo.getWorkOrderNo(), "PROC-MIX", testInput());
        return service.findByWorkOrderNo(wo.getWorkOrderNo());
    }

    // ==================== 工单创建 ====================

    @Nested
    @DisplayName("工单创建")
    class Create {

        @Test
        @DisplayName("创建工单应返回 CREATED 状态并发布事件")
        void create_shouldReturnCreatedAndPublishEvent() {
            List<IDomainEvent> events = new ArrayList<>();
            DomainEventPublisher.subscribe(MesEventTypes.WORKORDER_CREATED, events::add);

            MesWorkOrder wo = service.create("BP-001", "PROD-001", new BigDecimal("100"));

            assertNotNull(wo);
            assertEquals(WorkOrderStatus.CREATED, wo.getStatus());
            assertTrue(wo.getWorkOrderNo().startsWith("WO-"));
            assertEquals("BP-001", wo.getExpandProperty("mes.blueprintCode"));
            assertEquals(1, events.size());
            assertEquals(MesEventTypes.WORKORDER_CREATED, events.get(0).getEventType());
        }
    }

    // ==================== 工单下达 ====================

    @Nested
    @DisplayName("工单下达")
    class Release {

        @Test
        @DisplayName("下达工单应冻结蓝图版本号并发布事件")
        void release_shouldFreezeBlueprintVersionAndPublishEvent() {
            registerSimpleBlueprint();
            MesWorkOrder wo = service.create("BP-001", "PROD-001", BigDecimal.TEN);
            List<IDomainEvent> events = new ArrayList<>();
            DomainEventPublisher.subscribe(MesEventTypes.WORKORDER_RELEASED, events::add);

            wo = service.release(wo.getWorkOrderNo(), "张三",
                    Instant.parse("2026-07-20T08:00:00Z"),
                    Instant.parse("2026-07-20T18:00:00Z"), "FACTORY-A");

            assertEquals(WorkOrderStatus.RELEASED, wo.getStatus());
            assertEquals("1.0.0", wo.getBlueprintVersion());
            assertEquals("张三", wo.getOperator());
            assertNotNull(wo.getBlueprint());
            assertTrue(wo.isBlueprintVersionFrozen());
            assertEquals(1, events.size());
            assertEquals(MesEventTypes.WORKORDER_RELEASED, events.get(0).getEventType());
        }

        @Test
        @DisplayName("下达不存在的工单应抛异常")
        void release_nonExistent_shouldThrow() {
            registerSimpleBlueprint();
            assertThrows(ActionException.class, () ->
                    service.release("WO-NONEXIST", "张三", Instant.now(), Instant.now(), "F"));
        }

        @Test
        @DisplayName("蓝图不存在时下达应抛异常")
        void release_blueprintNotFound_shouldThrow() {
            MesWorkOrder wo = service.create("BP-MISSING", "PROD-001", BigDecimal.TEN);
            assertThrows(ActionException.class, () ->
                    service.release(wo.getWorkOrderNo(), "张三", Instant.now(), Instant.now(), "F"));
        }

        @Test
        @DisplayName("下达已取消的工单应抛异常")
        void release_cancelled_shouldThrow() {
            registerSimpleBlueprint();
            MesWorkOrder wo = service.create("BP-001", "PROD-001", BigDecimal.TEN);
            wo.cancel(); // 手动取消
            assertThrows(IllegalStateException.class, () ->
                    service.release(wo.getWorkOrderNo(), "李四", Instant.now(), Instant.now(), "F"));
        }
    }

    // ==================== 工单开工 ====================

    @Nested
    @DisplayName("工单开工")
    class Start {

        @Test
        @DisplayName("开工应记录实际开始时间并发布事件")
        void start_shouldRecordActualStartAndPublishEvent() {
            MesWorkOrder wo = createAndRelease();
            List<IDomainEvent> events = new ArrayList<>();
            DomainEventPublisher.subscribe(MesEventTypes.WORKORDER_STARTED, events::add);

            wo = service.start(wo.getWorkOrderNo());

            assertEquals(WorkOrderStatus.IN_PROGRESS, wo.getStatus());
            assertNotNull(wo.getActualStart());
            assertEquals(1, events.size());
            assertEquals(MesEventTypes.WORKORDER_STARTED, events.get(0).getEventType());
        }

        @Test
        @DisplayName("未下达的工单不能开工")
        void start_beforeRelease_shouldThrow() {
            registerSimpleBlueprint();
            MesWorkOrder wo = service.create("BP-001", "PROD-001", BigDecimal.ONE);
            assertThrows(IllegalStateException.class, () -> service.start(wo.getWorkOrderNo()));
        }

        @Test
        @DisplayName("已完工的工单不能再次开工")
        void start_afterComplete_shouldThrow() {
            MesWorkOrder wo = createReleaseStartAndExecute();
            MesWorkOrder completed = service.complete(wo.getWorkOrderNo());
            assertThrows(IllegalStateException.class, () -> service.start(completed.getWorkOrderNo()));
        }
    }

    // ==================== 工序执行 ====================

    @Nested
    @DisplayName("工序执行")
    class ExecuteProcess {

        @Test
        @DisplayName("执行工序应创建 ProcessRecord 并发布事件")
        void executeProcess_shouldCreateProcessRecordAndPublishEvents() {
            MesWorkOrder wo = createReleaseAndStart();
            List<IDomainEvent> events = new ArrayList<>();
            DomainEventPublisher.subscribePrefix("mes.process", events::add);
            DomainEventPublisher.subscribePrefix("mes.action", events::add);

            IResourcePack output = service.executeProcess(wo.getWorkOrderNo(), "PROC-MIX", testInput());

            assertNotNull(output);
            List<ProcessRecord> prs = service.findProcessRecords(wo.getWorkOrderNo());
            assertEquals(1, prs.size());
            ProcessRecord pr = prs.get(0);
            assertEquals(ProcessStatus.COMPLETED, pr.getStatus());
            assertEquals("PROC-MIX", pr.getProcessCode());
            assertEquals(1, pr.getActions().size());
            assertTrue(pr.getActions().get(0).isCompleted());

            assertFalse(events.isEmpty());
        }

        @Test
        @DisplayName("执行多个工序应各自创建 ProcessRecord")
        void executeMultipleProcesses_shouldCreateSeparateRecords() {
            // 必须先注册蓝图，再创建工单
            registerTwoProcessBlueprint();
            MesWorkOrder wo = service.create("BP-002", "PROD-001", BigDecimal.TEN);
            wo = service.release(wo.getWorkOrderNo(), "操作工A",
                    Instant.parse("2026-07-20T08:00:00Z"),
                    Instant.parse("2026-07-20T16:00:00Z"), "FACTORY-A");
            wo = service.start(wo.getWorkOrderNo());
            String woNo = wo.getWorkOrderNo();

            IResourcePack mid = service.executeProcess(woNo, "PROC-DISPENSE", testInput());
            IResourcePack out = service.executeProcess(woNo, "PROC-MIX", mid);

            assertNotNull(out);
            List<ProcessRecord> prs = service.findProcessRecords(woNo);
            assertEquals(2, prs.size());
            assertTrue(prs.stream().allMatch(pr -> pr.getStatus() == ProcessStatus.COMPLETED));
        }

        @Test
        @DisplayName("工单未开工时执行工序应抛异常")
        void executeProcess_beforeStart_shouldThrow() {
            MesWorkOrder wo = createAndRelease();
            assertThrows(IllegalStateException.class, () ->
                    service.executeProcess(wo.getWorkOrderNo(), "PROC-MIX", testInput()));
        }

        @Test
        @DisplayName("不存在的工序编码应抛异常")
        void executeProcess_nonExistentProcess_shouldThrow() {
            MesWorkOrder wo = createReleaseAndStart();
            assertThrows(ActionException.class, () ->
                    service.executeProcess(wo.getWorkOrderNo(), "PROC-NONEXIST", testInput()));
        }
    }

    // ==================== 动作报工 ====================

    @Nested
    @DisplayName("动作报工")
    class ReportAction {

        @Test
        @DisplayName("报工应创建 ActionRecord 并发布事件")
        void reportAction_shouldCreateRecordAndPublishEvent() {
            MesWorkOrder wo = createReleaseStartAndExecute();
            String woNo = wo.getWorkOrderNo();
            List<IDomainEvent> events = new ArrayList<>();
            DomainEventPublisher.subscribe(MesEventTypes.ACTION_COMPLETED, events::add);

            ActionRecord ar = service.reportAction(woNo, "PROC-MIX", "ACT-VERIFY", "PASS", "复核通过");

            assertEquals("ACT-VERIFY", ar.getActionCode());
            assertEquals("PASS", ar.getResult());
            assertEquals("复核通过", ar.getRemark());
            assertTrue(ar.isCompleted());
            List<ActionRecord> ars = service.findActionRecords(woNo + "-PROC-MIX");
            assertEquals(2, ars.size());
            assertEquals(1, events.size());
        }

        @Test
        @DisplayName("工序记录不存在时报工应抛异常")
        void reportAction_processRecordNotFound_shouldThrow() {
            MesWorkOrder wo = createReleaseAndStart();
            assertThrows(ActionException.class, () ->
                    service.reportAction(wo.getWorkOrderNo(), "PROC-MIX", "ACT-STIR", "PASS", ""));
        }
    }

    // ==================== 完工与关闭 ====================

    @Nested
    @DisplayName("完工与关闭")
    class CompleteAndClose {

        @Test
        @DisplayName("完工应发布事件")
        void complete_shouldPublishEvent() {
            MesWorkOrder wo = createReleaseStartAndExecute();
            List<IDomainEvent> events = new ArrayList<>();
            DomainEventPublisher.subscribe(MesEventTypes.WORKORDER_COMPLETED, events::add);

            wo = service.complete(wo.getWorkOrderNo());

            assertEquals(WorkOrderStatus.COMPLETED, wo.getStatus());
            assertNotNull(wo.getActualEnd());
            assertEquals(1, events.size());
        }

        @Test
        @DisplayName("工序未完成时不能完工")
        void complete_withUnfinishedProcess_shouldThrow() {
            MesWorkOrder wo = createReleaseAndStart();
            assertThrows(IllegalStateException.class, () -> service.complete(wo.getWorkOrderNo()));
        }

        @Test
        @DisplayName("关闭工单应发布事件")
        void close_shouldPublishEvent() {
            MesWorkOrder wo = createReleaseStartAndExecute();
            MesWorkOrder completed = service.complete(wo.getWorkOrderNo());
            List<IDomainEvent> events = new ArrayList<>();
            DomainEventPublisher.subscribe(MesEventTypes.WORKORDER_CLOSED, events::add);

            MesWorkOrder closed = service.close(completed.getWorkOrderNo());

            assertEquals(WorkOrderStatus.CLOSED, closed.getStatus());
            assertEquals(1, events.size());
        }

        @Test
        @DisplayName("未完工不能关闭")
        void close_beforeComplete_shouldThrow() {
            MesWorkOrder wo = createReleaseAndStart();
            assertThrows(IllegalStateException.class, () -> service.close(wo.getWorkOrderNo()));
        }
    }

    // ==================== QMS 中断与恢复 ====================

    @Nested
    @DisplayName("QMS 中断与恢复（design-decisions.md §2.4）")
    class QmsInterruptAndResume {

        @Test
        @DisplayName("工序执行期间中断应暂停动作链并发布事件")
        void interrupt_duringExecution_shouldPauseAndPublishEvent() {
            registerTwoProcessBlueprint();
            MesWorkOrder wo = createReleaseAndStart();
            String woNo = wo.getWorkOrderNo();

            List<IDomainEvent> interruptEvents = new ArrayList<>();
            DomainEventPublisher.subscribe(MesEventTypes.PROCESS_INTERRUPTED, interruptEvents::add);

            // 在工序开始时触发中断（模拟 QMS 检测到问题）
            DomainEventPublisher.subscribe(MesEventTypes.PROCESS_STARTED, event -> {
                @SuppressWarnings("unchecked")
                Map<String, String> payload = (Map<String, String>) event.getPayload();
                if ("PROC-MIX".equals(payload.get("processCode"))) {
                    service.interruptProcess(woNo, "PROC-MIX", "QMS", "检验不合格");
                }
            });

            service.executeProcess(woNo, "PROC-MIX", testInput());

            ProcessRecord pr = service.findProcessRecords(woNo).stream()
                    .filter(p -> "PROC-MIX".equals(p.getProcessCode()))
                    .findFirst().orElseThrow();
            assertTrue(pr.isInterrupted(), "工序应处于中断状态");
            assertEquals("QMS", pr.getInterruptedBy());
            assertEquals(1, interruptEvents.size());
        }

        @Test
        @DisplayName("QMS 中断 → 恢复 → 继续执行 完整流程")
        void interruptThenResume_fullFlow() {
            registerTwoProcessBlueprint();
            MesWorkOrder wo = createReleaseAndStart();
            String woNo = wo.getWorkOrderNo();

            // 工序开始时中断
            DomainEventPublisher.subscribe(MesEventTypes.PROCESS_STARTED, event -> {
                @SuppressWarnings("unchecked")
                Map<String, String> payload = (Map<String, String>) event.getPayload();
                if ("PROC-MIX".equals(payload.get("processCode"))) {
                    service.interruptProcess(woNo, "PROC-MIX", "QMS", "偏差待判");
                }
            });

            service.executeProcess(woNo, "PROC-MIX", testInput());
            ProcessRecord pr = service.findProcessRecords(woNo).stream()
                    .filter(p -> "PROC-MIX".equals(p.getProcessCode()))
                    .findFirst().orElseThrow();
            assertTrue(pr.isInterrupted());

            // 恢复
            List<IDomainEvent> resumedEvents = new ArrayList<>();
            DomainEventPublisher.subscribe(MesEventTypes.PROCESS_RESUMED, resumedEvents::add);
            ProcessRecord resumed = service.resumeProcess(woNo, "PROC-MIX");

            assertEquals(ProcessStatus.IN_PROGRESS, resumed.getStatus());
            assertEquals(1, resumedEvents.size());

            // 继续执行到完成
            service.executeProcess(woNo, "PROC-MIX", testInput());
            ProcessRecord finalPr = service.findProcessRecords(woNo).stream()
                    .filter(p -> "PROC-MIX".equals(p.getProcessCode()))
                    .findFirst().orElseThrow();
            assertEquals(ProcessStatus.COMPLETED, finalPr.getStatus());
        }

        @Test
        @DisplayName("非中断状态不能恢复")
        void resume_notInterrupted_shouldThrow() {
            MesWorkOrder wo = createReleaseStartAndExecute();
            assertThrows(IllegalStateException.class, () ->
                    service.resumeProcess(wo.getWorkOrderNo(), "PROC-MIX"));
        }

        @Test
        @DisplayName("设备故障中断场景")
        void equipFault_interrupt_scenario() {
            registerTwoProcessBlueprint();
            MesWorkOrder wo = createReleaseAndStart();
            String woNo = wo.getWorkOrderNo();

            DomainEventPublisher.subscribe(MesEventTypes.PROCESS_STARTED, event -> {
                @SuppressWarnings("unchecked")
                Map<String, String> payload = (Map<String, String>) event.getPayload();
                if ("PROC-MIX".equals(payload.get("processCode"))) {
                    service.interruptProcess(woNo, "PROC-MIX", "Equip", "搅拌机过热");
                }
            });

            service.executeProcess(woNo, "PROC-MIX", testInput());
            ProcessRecord pr = service.findProcessRecords(woNo).stream()
                    .filter(p -> "PROC-MIX".equals(p.getProcessCode()))
                    .findFirst().orElseThrow();
            assertTrue(pr.isInterrupted());
            assertEquals("Equip", pr.getInterruptedBy());
        }
    }

    // ==================== 查询 ====================

    @Nested
    @DisplayName("查询方法")
    class Query {

        @Test
        @DisplayName("findByWorkOrderNo — 存在的工单")
        void findByWorkOrderNo_shouldReturnWorkOrder() {
            MesWorkOrder wo = createAndRelease();
            MesWorkOrder found = service.findByWorkOrderNo(wo.getWorkOrderNo());
            assertNotNull(found);
            assertEquals(wo.getWorkOrderNo(), found.getWorkOrderNo());
        }

        @Test
        @DisplayName("findByWorkOrderNo — 不存在返回 null")
        void findByWorkOrderNo_notFound_shouldReturnNull() {
            assertNull(service.findByWorkOrderNo("WO-NONEXIST"));
        }

        @Test
        @DisplayName("findProcessRecords 应返回所有工序记录")
        void findProcessRecords_shouldReturnAll() {
            MesWorkOrder wo = createReleaseStartAndExecute();
            List<ProcessRecord> prs = service.findProcessRecords(wo.getWorkOrderNo());
            assertEquals(1, prs.size());
            assertEquals("PROC-MIX", prs.get(0).getProcessCode());
        }

        @Test
        @DisplayName("findProcessRecords — 无工序记录时返回空列表")
        void findProcessRecords_noRecords_shouldReturnEmpty() {
            MesWorkOrder wo = createReleaseAndStart();
            assertTrue(service.findProcessRecords(wo.getWorkOrderNo()).isEmpty());
        }

        @Test
        @DisplayName("findActionRecords 应返回动作记录")
        void findActionRecords_shouldReturnAll() {
            MesWorkOrder wo = createReleaseStartAndExecute();
            String prId = wo.getWorkOrderNo() + "-PROC-MIX";
            service.reportAction(wo.getWorkOrderNo(), "PROC-MIX", "ACT-VERIFY", "PASS", "复核");
            List<ActionRecord> ars = service.findActionRecords(prId);
            assertEquals(2, ars.size());
        }
    }

    // ==================== 全生命周期集成 ====================

    @Nested
    @DisplayName("全生命周期集成")
    class FullLifecycle {

        @Test
        @DisplayName("完整流程：创建 → 下达 → 开工 → 执行工序 → 报工 → 完工 → 关闭")
        void fullLifecycle_allEventsPublished() {
            // 注册两个工序的蓝图
            Process p1 = new Process("PROC-WEIGH", "称量");
            p1.setActions(List.of(
                    new Action("ACT-WEIGH", "称量", Dict.Importance.Require, 1),
                    new Action("ACT-VERIFY", "复核", Dict.Importance.Require, 2)));
            Process p2 = new Process("PROC-MIX", "混合");
            p2.setActions(List.of(new Action("ACT-STIR", "搅拌", Dict.Importance.Require, 1)));
            registerBlueprint("BP-FULL", "3.0.0", List.of(p1, p2));

            List<IDomainEvent> allEvents = new ArrayList<>();
            DomainEventPublisher.subscribePrefix("mes.", allEvents::add);

            // 1. 创建
            MesWorkOrder wo = service.create("BP-FULL", "PROD-FULL", new BigDecimal("500"));
            String woNo = wo.getWorkOrderNo();
            assertEquals(WorkOrderStatus.CREATED, wo.getStatus());

            // 2. 下达
            wo = service.release(woNo, "操作工张三",
                    Instant.parse("2026-07-20T08:00:00Z"),
                    Instant.parse("2026-07-20T18:00:00Z"), "FACTORY-FULL");
            assertEquals(WorkOrderStatus.RELEASED, wo.getStatus());
            assertEquals("3.0.0", wo.getBlueprintVersion());

            // 3. 开工
            wo = service.start(woNo);
            assertEquals(WorkOrderStatus.IN_PROGRESS, wo.getStatus());
            assertNotNull(wo.getActualStart());

            // 4. 执行工序1
            service.executeProcess(woNo, "PROC-WEIGH", testInput());
            List<ProcessRecord> prs1 = service.findProcessRecords(woNo);
            assertEquals(1, prs1.size());
            assertEquals(2, prs1.get(0).getActions().size());

            // 5. 手动报工
            ActionRecord ar = service.reportAction(woNo, "PROC-WEIGH", "ACT-CHECK", "PASS", "称量合格");
            assertEquals("PASS", ar.getResult());

            // 6. 执行工序2
            service.executeProcess(woNo, "PROC-MIX", testInput());
            assertEquals(2, service.findProcessRecords(woNo).size());

            // 7. 完工
            wo = service.complete(woNo);
            assertEquals(WorkOrderStatus.COMPLETED, wo.getStatus());

            // 8. 关闭
            wo = service.close(woNo);
            assertEquals(WorkOrderStatus.CLOSED, wo.getStatus());

            // 验证所有事件类型都已发布
            List<String> eventTypes = allEvents.stream().map(IDomainEvent::getEventType).toList();
            assertTrue(eventTypes.contains(MesEventTypes.WORKORDER_CREATED));
            assertTrue(eventTypes.contains(MesEventTypes.WORKORDER_RELEASED));
            assertTrue(eventTypes.contains(MesEventTypes.WORKORDER_STARTED));
            assertTrue(eventTypes.contains(MesEventTypes.PROCESS_STARTED));
            assertTrue(eventTypes.contains(MesEventTypes.PROCESS_COMPLETED));
            assertTrue(eventTypes.contains(MesEventTypes.ACTION_COMPLETED));
            assertTrue(eventTypes.contains(MesEventTypes.WORKORDER_COMPLETED));
            assertTrue(eventTypes.contains(MesEventTypes.WORKORDER_CLOSED));
        }
    }
}
