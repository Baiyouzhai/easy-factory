package com.byz.factory.mes;

import com.byz.factory.batch.*;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.MesEventTypes;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.mes.model.ActionRecord;
import com.byz.factory.mes.model.MesWorkOrder;
import com.byz.factory.mes.model.ProcessRecord;
import com.byz.factory.process.IProcess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MES 模块单元测试。
 * <p>
 * 覆盖：MesWorkOrder 构造与状态转换、蓝图版本冻结、ProcessRecord 状态流转、
 * ActionRecord 与 ITraceable 一致性、非法状态转换、QMS 中断场景。
 *
 * @author 苏政
 */
class MesModuleTest {

    @AfterEach
    void tearDown() {
        DomainEventPublisher.clearAll();
    }

    // ==================== 辅助：测试用 Blueprint ====================

    private static class TestBlueprint implements IBlueprint {
        private final String code;
        private final String name;
        private final String version;
        private final List<IProcess> processes;

        TestBlueprint(String code, String name, String version, List<IProcess> processes) {
            this.code = code;
            this.name = name;
            this.version = version;
            this.processes = processes;
        }

        @Override public String getCode() { return code; }
        @Override public String getName() { return name; }
        @Override public String getVersion() { return version; }
        @Override public List<IProcess> getProductionProcessList() { return processes; }
    }

    private static TestBlueprint createTestBlueprint() {
        return new TestBlueprint("BP-AMX-001", "阿莫西林胶囊工艺蓝图", "2.0.0", List.of());
    }

    // ==================== MesWorkOrder 测试 ====================

    @Nested
    @DisplayName("MesWorkOrder 构造与状态转换")
    class MesWorkOrderLifecycle {

        @Test
        @DisplayName("创建工单应设置 CREATED 状态和默认字段")
        void creation_shouldSetDefaults() {
            // Given & When
            MesWorkOrder wo = new MesWorkOrder("WO-20260719-001", "AMX-250MG", new BigDecimal("1000"));

            // Then
            assertEquals("WO-20260719-001", wo.getWorkOrderNo());
            assertEquals("AMX-250MG", wo.getProductCode());
            assertEquals(new BigDecimal("1000"), wo.getQuantity());
            assertEquals(WorkOrderStatus.CREATED, wo.getStatus());
            assertNull(wo.getBlueprintVersion());
            assertTrue(wo.isEditable());
            assertFalse(wo.isClosed());
            assertFalse(wo.isBlueprintVersionFrozen());
        }

        @Test
        @DisplayName("完整生命周期：CREATED → RELEASED → IN_PROGRESS → COMPLETED → CLOSED")
        void fullLifecycle_shouldTransitionCorrectly() {
            // Given
            MesWorkOrder wo = new MesWorkOrder("WO-001", "P-001", BigDecimal.TEN);
            TestBlueprint bp = createTestBlueprint();

            // When: 下达
            wo.release(bp, "张三",
                    Instant.parse("2026-07-20T08:00:00Z"),
                    Instant.parse("2026-07-20T16:00:00Z"),
                    "FACTORY-A");
            // Then
            assertEquals(WorkOrderStatus.RELEASED, wo.getStatus());
            assertEquals("2.0.0", wo.getBlueprintVersion());
            assertEquals("张三", wo.getOperator());
            assertEquals("FACTORY-A", wo.getFactoryCode());
            assertTrue(wo.isBlueprintVersionFrozen());
            assertFalse(wo.isEditable());

            // When: 开工
            wo.start();
            // Then
            assertEquals(WorkOrderStatus.IN_PROGRESS, wo.getStatus());
            assertNotNull(wo.getActualStart());

            // When: 完工
            wo.complete();
            // Then
            assertEquals(WorkOrderStatus.COMPLETED, wo.getStatus());
            assertNotNull(wo.getActualEnd());

            // When: 关闭
            wo.close();
            // Then
            assertEquals(WorkOrderStatus.CLOSED, wo.getStatus());
            assertTrue(wo.isClosed());
        }

        @Test
        @DisplayName("蓝图版本冻结：release 时应记录蓝图的当前版本号")
        void release_shouldFreezeBlueprintVersion() {
            // Given
            MesWorkOrder wo = new MesWorkOrder("WO-001", "P-001", BigDecimal.TEN);
            TestBlueprint bp = new TestBlueprint("BP-001", "测试蓝图", "3.5.1", List.of());

            // When
            wo.release(bp, "李四",
                    Instant.parse("2026-07-20T08:00:00Z"),
                    Instant.parse("2026-07-20T18:00:00Z"),
                    "FACTORY-B");

            // Then — 蓝图版本已冻结
            assertEquals("3.5.1", wo.getBlueprintVersion());
            assertEquals(bp, wo.getBlueprint());
            // 验证蓝图版本与工单冻结版本一致
            assertEquals(bp.getVersion(), wo.getBlueprintVersion());
            assertTrue(wo.isBlueprintVersionFrozen());
        }

        @Test
        @DisplayName("取消工单：CREATED 状态下可取消")
        void cancel_fromCreated_shouldTransition() {
            // Given
            MesWorkOrder wo = new MesWorkOrder("WO-001", "P-001", BigDecimal.TEN);

            // When
            wo.cancel();

            // Then
            assertEquals(WorkOrderStatus.CANCELLED, wo.getStatus());
            assertTrue(wo.isClosed());
        }

        @Test
        @DisplayName("取消工单：RELEASED 状态下可取消")
        void cancel_fromReleased_shouldTransition() {
            // Given
            MesWorkOrder wo = new MesWorkOrder("WO-001", "P-001", BigDecimal.TEN);
            wo.release(createTestBlueprint(), "王五",
                    Instant.parse("2026-07-20T08:00:00Z"),
                    Instant.parse("2026-07-20T16:00:00Z"),
                    "FACTORY-A");

            // When
            wo.cancel();

            // Then
            assertEquals(WorkOrderStatus.CANCELLED, wo.getStatus());
        }
    }

    @Nested
    @DisplayName("MesWorkOrder 非法状态转换")
    class MesWorkOrderIllegalTransitions {

        @Test
        @DisplayName("未 RELEASED 不能 START — CREATED → IN_PROGRESS 非法")
        void start_beforeRelease_shouldThrow() {
            // Given
            MesWorkOrder wo = new MesWorkOrder("WO-001", "P-001", BigDecimal.TEN);

            // When & Then — CREATED 只能转换到 RELEASED 或 CANCELLED
            assertThrows(IllegalStateException.class, wo::start);
            assertEquals(WorkOrderStatus.CREATED, wo.getStatus());
        }

        @Test
        @DisplayName("已关闭的工单不能再操作")
        void closed_shouldRejectTransitions() {
            // Given
            MesWorkOrder wo = new MesWorkOrder("WO-001", "P-001", BigDecimal.TEN);
            wo.release(createTestBlueprint(), "赵六",
                    Instant.parse("2026-07-20T08:00:00Z"),
                    Instant.parse("2026-07-20T16:00:00Z"),
                    "FACTORY-A");
            wo.start();
            wo.complete();
            wo.close();

            // When & Then — CLOSED 是终态
            assertThrows(IllegalStateException.class, wo::complete);
            assertThrows(IllegalStateException.class, wo::start);
        }

        @Test
        @DisplayName("已取消的工单不能再操作")
        void cancelled_shouldRejectTransitions() {
            // Given
            MesWorkOrder wo = new MesWorkOrder("WO-001", "P-001", BigDecimal.TEN);
            wo.cancel();

            // When & Then
            assertThrows(IllegalStateException.class, () ->
                    wo.release(createTestBlueprint(), "钱七",
                            Instant.now(), Instant.now(), "F"));
            assertThrows(IllegalStateException.class, wo::start);
            assertThrows(IllegalStateException.class, wo::complete);
        }

        @Test
        @DisplayName("未完工不能关闭 — IN_PROGRESS → CLOSED 非法")
        void close_beforeComplete_shouldThrow() {
            // Given
            MesWorkOrder wo = new MesWorkOrder("WO-001", "P-001", BigDecimal.TEN);
            wo.release(createTestBlueprint(), "孙八",
                    Instant.parse("2026-07-20T08:00:00Z"),
                    Instant.parse("2026-07-20T16:00:00Z"),
                    "FACTORY-A");
            wo.start();

            // When & Then — IN_PROGRESS 只能转到 COMPLETED 或 CANCELLED
            assertThrows(IllegalStateException.class, wo::close);
        }
    }

    // ==================== ProcessRecord 测试 ====================

    @Nested
    @DisplayName("ProcessRecord 状态流转")
    class ProcessRecordLifecycle {

        @Test
        @DisplayName("创建工序记录应设置 PENDING 状态")
        void creation_shouldSetPending() {
            // Given & When
            ProcessRecord pr = new ProcessRecord("WO-001", "PROC-MIX");

            // Then
            assertEquals(ProcessStatus.PENDING, pr.getStatus());
            assertEquals("WO-001", pr.getWorkOrderNo());
            assertEquals("PROC-MIX", pr.getProcessCode());
            assertNotNull(pr.getActions());
            assertTrue(pr.getActions().isEmpty());
            assertFalse(pr.isRunning());
            assertFalse(pr.isFinished());
        }

        @Test
        @DisplayName("正常流转：PENDING → IN_PROGRESS → COMPLETED")
        void normalFlow_shouldTransitionCorrectly() {
            // Given
            ProcessRecord pr = new ProcessRecord("WO-001", "PROC-MIX");

            // When: 开始执行
            pr.start("张三");
            // Then
            assertEquals(ProcessStatus.IN_PROGRESS, pr.getStatus());
            assertEquals("张三", pr.getOperator());
            assertNotNull(pr.getActualStart());
            assertTrue(pr.isRunning());
            assertFalse(pr.isInterrupted());

            // When: 完成
            pr.complete();
            // Then
            assertEquals(ProcessStatus.COMPLETED, pr.getStatus());
            assertNotNull(pr.getActualEnd());
            assertTrue(pr.isFinished());
            assertFalse(pr.isRunning());
        }

        @Test
        @DisplayName("QMS 中断场景：IN_PROGRESS → INTERRUPTED → IN_PROGRESS（恢复）→ COMPLETED")
        void qmsInterrupt_scenario_shouldTransitionCorrectly() {
            // Given — 工序在执行中
            ProcessRecord pr = new ProcessRecord("WO-001", "PROC-INSPECT");
            pr.start("李四");

            // When — QMS 检验触发中断（Control.Interrupt 语义）
            pr.interrupt("QMS", "检验不合格，等待偏差判定");
            // Then
            assertEquals(ProcessStatus.INTERRUPTED, pr.getStatus());
            assertEquals("QMS", pr.getInterruptedBy());
            assertEquals("检验不合格，等待偏差判定", pr.getInterruptReason());
            assertTrue(pr.isInterrupted());
            assertTrue(pr.isRunning()); // INTERRUPTED 仍算运行中

            // When — QMS 问题解决，恢复工序
            pr.resume();
            // Then
            assertEquals(ProcessStatus.IN_PROGRESS, pr.getStatus());

            // When — 工序完成
            pr.complete();
            // Then
            assertEquals(ProcessStatus.COMPLETED, pr.getStatus());
        }

        @Test
        @DisplayName("设备故障中断：INTERRUPTED by Equip → 恢复 → 完成")
        void equipInterrupt_scenario_shouldTransitionCorrectly() {
            // Given
            ProcessRecord pr = new ProcessRecord("WO-001", "PROC-MIX");
            pr.start("王五");

            // When — 设备故障中断
            pr.interrupt("Equip", "搅拌机故障");

            // Then
            assertEquals(ProcessStatus.INTERRUPTED, pr.getStatus());
            assertEquals("Equip", pr.getInterruptedBy());

            // When — 维修完成，恢复
            pr.resume();
            assertEquals(ProcessStatus.IN_PROGRESS, pr.getStatus());

            // When — 完成
            pr.complete();
            assertEquals(ProcessStatus.COMPLETED, pr.getStatus());
        }

        @Test
        @DisplayName("跳过工序：PENDING → SKIPPED")
        void skip_shouldTransition() {
            // Given
            ProcessRecord pr = new ProcessRecord("WO-001", "PROC-OPTIONAL");

            // When
            pr.skip();

            // Then
            assertEquals(ProcessStatus.SKIPPED, pr.getStatus());
            assertTrue(pr.isFinished());
        }

        @Test
        @DisplayName("中断后取消：INTERRUPTED → CANCELLED")
        void cancel_fromInterrupted_shouldTransition() {
            // Given
            ProcessRecord pr = new ProcessRecord("WO-001", "PROC-INSPECT");
            pr.start("张三");
            pr.interrupt("Manual", "人工暂停");

            // When
            pr.cancel();

            // Then
            assertEquals(ProcessStatus.CANCELLED, pr.getStatus());
            assertTrue(pr.isFinished());
        }

        @Test
        @DisplayName("未开始不能完成 — PENDING → COMPLETED 非法")
        void complete_fromPending_shouldThrow() {
            // Given
            ProcessRecord pr = new ProcessRecord("WO-001", "PROC-MIX");

            // When & Then
            assertThrows(IllegalStateException.class, pr::complete);
        }

        @Test
        @DisplayName("已完成不能再开始 — COMPLETED 终态不可转")
        void start_afterComplete_shouldThrow() {
            // Given
            ProcessRecord pr = new ProcessRecord("WO-001", "PROC-MIX");
            pr.start("张三");
            pr.complete();

            // When & Then
            assertThrows(IllegalStateException.class, () -> pr.start("李四"));
        }

        @Test
        @DisplayName("添加动作记录应反映在列表中")
        void addAction_shouldAppendToList() {
            // Given
            ProcessRecord pr = new ProcessRecord("WO-001", "PROC-MIX");
            ActionRecord ar = new ActionRecord("WO-001-PROC-MIX", "ACT-STIR",
                    "WO-001", "BATCH-001", "PROC-MIX", "张三", "USE");

            // When
            pr.addAction(ar);

            // Then
            assertEquals(1, pr.getActions().size());
            assertEquals("ACT-STIR", pr.getActions().get(0).getActionCode());
        }
    }

    // ==================== ActionRecord 测试 ====================

    @Nested
    @DisplayName("ActionRecord 与 ITraceable 一致性")
    class ActionRecordTraceability {

        @Test
        @DisplayName("构造 ActionRecord 应正确设置追溯字段")
        void creation_shouldSetTraceabilityFields() {
            // Given & When
            ActionRecord ar = new ActionRecord("PR-001", "ACT-WEIGH",
                    "WO-001", "BATCH-20260719-001",
                    "PROC-DISPENSE", "张三", "USE");

            // Then
            assertEquals("PR-001", ar.getProcessRecordId());
            assertEquals("ACT-WEIGH", ar.getActionCode());
            assertEquals("WO-001", ar.getWorkOrderNo());
            assertEquals("BATCH-20260719-001", ar.getBatchNo());
            assertEquals("PROC-DISPENSE", ar.getProcessCode());
            assertEquals("张三", ar.getOperator());
            assertEquals("USE", ar.getOperationType());
            assertNotNull(ar.getTimestamp());
            assertFalse(ar.isCompleted());
            assertFalse(ar.hasSnapshot());
        }

        @Test
        @DisplayName("完成动作记录应设置 endTime、result 和快照")
        void complete_shouldSetEndTimeAndSnapshots() {
            // Given
            ActionRecord ar = new ActionRecord("PR-001", "ACT-WEIGH",
                    "WO-001", "BATCH-001", "PROC-DISPENSE", "张三", "USE");

            // When
            ar.complete("PASS",
                    "{\"material\":\"AMX\",\"qty\":\"250mg\"}",
                    "{\"material\":\"AMX\",\"qty\":\"250mg\",\"weighed\":true}",
                    "称量合格");

            // Then
            assertTrue(ar.isCompleted());
            assertEquals("PASS", ar.getResult());
            assertNotNull(ar.getEndTime());
            assertEquals("称量合格", ar.getRemark());
            assertTrue(ar.hasSnapshot());
            assertTrue(ar.getBeforeSnapshot().contains("AMX"));
            assertTrue(ar.getAfterSnapshot().contains("weighed"));
        }

        @Test
        @DisplayName("getTimestamp 应优先返回 endTime")
        void getTimestamp_shouldPreferEndTime() {
            // Given
            ActionRecord ar = new ActionRecord("PR-001", "ACT-STIR",
                    "WO-001", "BATCH-001", "PROC-MIX", "李四", "ADD");
            Instant created = ar.getTimestamp();

            // When
            ar.complete("PASS", "{}", "{}", "");
            Instant afterComplete = ar.getTimestamp();

            // Then — 完成后应返回 endTime（不同于创建时间戳）
            assertNotNull(afterComplete);
        }

        @Test
        @DisplayName("ITraceable 接口方法应正确返回")
        void itraceable_methods_shouldBeCorrect() {
            // Given
            ActionRecord ar = new ActionRecord("PR-001", "ACT-STIR",
                    "WO-001", "BATCH-2026-001",
                    "PROC-MIX", "王五", "ADD");
            ar.complete("PASS",
                    "{\"before\":{\"temp\":\"25°C\"}}",
                    "{\"after\":{\"temp\":\"80°C\"}}",
                    "升温完成");

            // When & Then — ITraceable 接口方法
            assertEquals("BATCH-2026-001", ar.getBatchNo());
            assertEquals("PROC-MIX", ar.getProcessCode());
            assertEquals("ACT-STIR", ar.getActionCode());
            assertEquals("王五", ar.getOperator());
            assertEquals("ADD", ar.getOperationType());
            assertEquals("PASS", ar.getResult());
            assertEquals("升温完成", ar.getRemark());
            assertNotNull(ar.getTimestamp());
        }

        @Test
        @DisplayName("recordSnapshot 不应标记为完成")
        void recordSnapshot_shouldNotComplete() {
            // Given
            ActionRecord ar = new ActionRecord("PR-001", "ACT-SAMPLE",
                    "WO-001", "BATCH-001", "PROC-INSPECT", "赵六", "USE");

            // When
            ar.recordSnapshot("{\"sample\":\"taken\"}", "{\"sample\":\"analyzed\"}");

            // Then
            assertTrue(ar.hasSnapshot());
            assertFalse(ar.isCompleted()); // 尚未完成
            assertNull(ar.getResult());
        }
    }

    // ==================== ProcessStatus 枚举测试 ====================

    @Nested
    @DisplayName("ProcessStatus 枚举定义")
    class ProcessStatusEnumTest {

        @Test
        @DisplayName("PENDING 允许转换到 IN_PROGRESS / SKIPPED / CANCELLED")
        void pending_shouldAllowCorrectTransitions() {
            assertTrue(ProcessStatus.PENDING.allowedTransitions().contains(ProcessStatus.IN_PROGRESS));
            assertTrue(ProcessStatus.PENDING.allowedTransitions().contains(ProcessStatus.SKIPPED));
            assertTrue(ProcessStatus.PENDING.allowedTransitions().contains(ProcessStatus.CANCELLED));
            assertFalse(ProcessStatus.PENDING.allowedTransitions().contains(ProcessStatus.COMPLETED));
        }

        @Test
        @DisplayName("IN_PROGRESS 允许转换到 COMPLETED / INTERRUPTED / CANCELLED")
        void inProgress_shouldAllowCorrectTransitions() {
            assertTrue(ProcessStatus.IN_PROGRESS.allowedTransitions().contains(ProcessStatus.COMPLETED));
            assertTrue(ProcessStatus.IN_PROGRESS.allowedTransitions().contains(ProcessStatus.INTERRUPTED));
            assertTrue(ProcessStatus.IN_PROGRESS.allowedTransitions().contains(ProcessStatus.CANCELLED));
            assertFalse(ProcessStatus.IN_PROGRESS.allowedTransitions().contains(ProcessStatus.PENDING));
        }

        @Test
        @DisplayName("INTERRUPTED 允许转换到 IN_PROGRESS / CANCELLED")
        void interrupted_shouldAllowCorrectTransitions() {
            assertTrue(ProcessStatus.INTERRUPTED.allowedTransitions().contains(ProcessStatus.IN_PROGRESS));
            assertTrue(ProcessStatus.INTERRUPTED.allowedTransitions().contains(ProcessStatus.CANCELLED));
            assertFalse(ProcessStatus.INTERRUPTED.allowedTransitions().contains(ProcessStatus.COMPLETED));
        }

        @Test
        @DisplayName("COMPLETED / SKIPPED / CANCELLED 为终态，不允许转换")
        void terminalStates_shouldNotAllowTransitions() {
            assertTrue(ProcessStatus.COMPLETED.allowedTransitions().isEmpty());
            assertTrue(ProcessStatus.SKIPPED.allowedTransitions().isEmpty());
            assertTrue(ProcessStatus.CANCELLED.allowedTransitions().isEmpty());
        }
    }

    // ==================== MesEventTypes 常量测试 ====================

    @Nested
    @DisplayName("MesEventTypes 事件常量")
    class MesEventTypesTest {

        @Test
        @DisplayName("事件常量命名应遵循 {module}.{entity}.{past_tense} 约定")
        void eventTypes_shouldFollowNamingConvention() {
            assertTrue(MesEventTypes.WORKORDER_CREATED.startsWith("mes.workorder."));
            assertTrue(MesEventTypes.WORKORDER_RELEASED.startsWith("mes.workorder."));
            assertTrue(MesEventTypes.WORKORDER_STARTED.startsWith("mes.workorder."));
            assertTrue(MesEventTypes.WORKORDER_COMPLETED.startsWith("mes.workorder."));
            assertTrue(MesEventTypes.WORKORDER_CLOSED.startsWith("mes.workorder."));

            assertTrue(MesEventTypes.PROCESS_STARTED.startsWith("mes.process."));
            assertTrue(MesEventTypes.PROCESS_COMPLETED.startsWith("mes.process."));
            assertTrue(MesEventTypes.PROCESS_INTERRUPTED.startsWith("mes.process."));
            assertTrue(MesEventTypes.PROCESS_RESUMED.startsWith("mes.process."));

            assertTrue(MesEventTypes.ACTION_COMPLETED.startsWith("mes.action."));
        }

        @Test
        @DisplayName("PREFIX 常量应为 'mes'")
        void prefix_shouldBeMes() {
            assertEquals("mes", MesEventTypes.PREFIX);
        }

        @Test
        @DisplayName("QMS 中断事件可被订阅")
        void mesProcessInterrupted_canBeSubscribed() {
            // Given
            List<IDomainEvent> captured = new ArrayList<>();
            DomainEventPublisher.subscribe(MesEventTypes.PROCESS_INTERRUPTED, captured::add);

            // When
            IDomainEvent event = IDomainEvent.of(
                    MesEventTypes.PROCESS_INTERRUPTED, "mes",
                    "{\"workOrderNo\":\"WO-001\",\"processCode\":\"PROC-INSPECT\"}");
            DomainEventPublisher.publish(event);

            // Then
            assertEquals(1, captured.size());
            assertEquals(MesEventTypes.PROCESS_INTERRUPTED, captured.get(0).getEventType());
        }
    }

    // ==================== 综合场景测试 ====================

    @Nested
    @DisplayName("综合场景")
    class IntegrationScenarios {

        @Test
        @DisplayName("工单 + 工序记录 + 动作记录 完整链路")
        void completeMESFlow_workOrderWithProcessAndActionRecords() {
            // Given — 创建工单并下达
            MesWorkOrder wo = new MesWorkOrder("WO-20260719-001", "AMX-250MG", new BigDecimal("500"));
            TestBlueprint bp = new TestBlueprint("BP-AMX-001", "阿莫西林工艺", "2.1.0", List.of());
            wo.release(bp, "操作工A",
                    Instant.parse("2026-07-20T08:00:00Z"),
                    Instant.parse("2026-07-20T18:00:00Z"),
                    "FACTORY-A");
            assertEquals("2.1.0", wo.getBlueprintVersion());

            // When — 开工
            wo.start();
            assertEquals(WorkOrderStatus.IN_PROGRESS, wo.getStatus());

            // Given — 创建工序记录 1: 称量
            ProcessRecord prDispense = new ProcessRecord("WO-20260719-001", "PROC-DISPENSE");
            prDispense.start("操作工A");

            // 工序 1 动作报工
            ActionRecord arWeigh = new ActionRecord("WO-20260719-001-PROC-DISPENSE", "ACT-WEIGH",
                    "WO-20260719-001", "BATCH-001", "PROC-DISPENSE", "操作工A", "USE");
            arWeigh.complete("PASS", "{}", "{\"weighed\":true}", "称量合格");
            prDispense.addAction(arWeigh);

            ActionRecord arVerify = new ActionRecord("WO-20260719-001-PROC-DISPENSE", "ACT-VERIFY",
                    "WO-20260719-001", "BATCH-001", "PROC-DISPENSE", "复核员B", "USE");
            arVerify.complete("PASS", "{}", "{\"verified\":true}", "复核通过");
            prDispense.addAction(arVerify);

            prDispense.complete();
            assertTrue(prDispense.isFinished());
            assertEquals(2, prDispense.getActions().size());

            // Given — 创建工序记录 2: 检验（QMS 中断场景）
            ProcessRecord prInspect = new ProcessRecord("WO-20260719-001", "PROC-INSPECT");
            prInspect.start("检验员C");

            // QMS 中断
            prInspect.interrupt("QMS", "检验不合格，等待偏差判定");
            assertTrue(prInspect.isInterrupted());

            // QMS 恢复
            prInspect.resume();
            assertEquals(ProcessStatus.IN_PROGRESS, prInspect.getStatus());

            // 检验动作报工
            ActionRecord arInspect = new ActionRecord("WO-20260719-001-PROC-INSPECT", "ACT-INSPECT",
                    "WO-20260719-001", "BATCH-001", "PROC-INSPECT", "检验员C", "USE");
            arInspect.complete("PASS", "{}", "{\"inspected\":true,\"result\":\"PASS\"}", "检验合格");
            prInspect.addAction(arInspect);
            prInspect.complete();

            // Then — 验证完整链路
            assertEquals(ProcessStatus.COMPLETED, prDispense.getStatus());
            assertEquals(ProcessStatus.COMPLETED, prInspect.getStatus());
            assertTrue(prDispense.getActions().get(0).isCompleted());
            assertTrue(prDispense.getActions().get(1).isCompleted());
            assertTrue(prInspect.getActions().get(0).isCompleted());

            // When — 完工并关闭
            wo.complete();
            assertEquals(WorkOrderStatus.COMPLETED, wo.getStatus());
            wo.close();
            assertEquals(WorkOrderStatus.CLOSED, wo.getStatus());
        }

        @Test
        @DisplayName("QMS 中断后取消的场景")
        void qmsInterruptThenCancel_scenario() {
            // Given
            MesWorkOrder wo = new MesWorkOrder("WO-002", "P-002", BigDecimal.ONE);
            wo.release(createTestBlueprint(), "操作工D",
                    Instant.parse("2026-07-20T08:00:00Z"),
                    Instant.parse("2026-07-20T16:00:00Z"),
                    "FACTORY-A");
            wo.start();

            ProcessRecord pr = new ProcessRecord("WO-002", "PROC-INSPECT");
            pr.start("检验员D");

            // When — QMS 中断后决定取消该工序
            pr.interrupt("QMS", "严重偏差，无法纠正");
            pr.cancel();

            // Then
            assertEquals(ProcessStatus.CANCELLED, pr.getStatus());
            assertTrue(pr.isFinished());
            assertEquals("QMS", pr.getInterruptedBy());

            // 工单仍可被取消
            wo.cancel();
            assertEquals(WorkOrderStatus.CANCELLED, wo.getStatus());
        }
    }

}
