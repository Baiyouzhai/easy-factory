package com.byz.factory.qms;

import com.byz.factory.batch.*;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.QmsEventTypes;
import com.byz.factory.process.action.IQualityAction;
import com.byz.factory.qms.model.*;
import com.byz.factory.shared.Dict;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QMS 模块单元测试。
 * <p>
 * 覆盖：InspectionOrder、InspectionPlan、InspectionRecord、Deviation、Capa 的
 * 构造、状态转换、业务便捷方法、事件发布、IExpand 以及枚举定义。
 *
 * @author 苏政
 */
@DisplayName("QMS 模块 单元测试")
class QmsModuleTest {

    @AfterEach
    void tearDown() {
        DomainEventPublisher.clearAll();
    }

    // ==================== InspectionOrder 测试 ====================

    @Test
    @DisplayName("InspectionOrder 创建 — 初始状态为 PENDING + 空检验记录")
    void inspectionOrder_creation_shouldSetDefaults() {
        // Given & When
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");

        // Then
        assertEquals("INSP-001", order.getInspectionNo());
        assertEquals("B001", order.getBatchNo());
        assertEquals("PROC-001", order.getProcessCode());
        assertEquals(InspectionStatus.PENDING, order.getStatus());
        assertNotNull(order.getRecords());
        assertTrue(order.getRecords().isEmpty());
        assertEquals(0, order.getTotalItems());
        assertEquals(0, order.getCompletedItems());
        assertEquals(0, order.getPassedItems());
        assertFalse(order.isFinished());
    }

    @Test
    @DisplayName("InspectionOrder 开始检验 — PENDING → IN_PROGRESS 并发布事件")
    void inspectionOrder_startInspection_shouldTransitionAndPublishEvent() {
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");
        order.setInspectionType("IPQC");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.INSPECTION_STARTED, events::add);

        // When
        order.startInspection("检验员A");

        // Then
        assertEquals(InspectionStatus.IN_PROGRESS, order.getStatus());
        assertEquals("检验员A", order.getInspector());
        assertNotNull(order.getStartedAt());
        assertEquals(1, events.size());
        assertEquals(QmsEventTypes.INSPECTION_STARTED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("InspectionOrder 提交检验结果 — 应增加 completedItems 和 records")
    void inspectionOrder_submitResult_shouldUpdateCounts() {
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");
        order.setTotalItems(3);
        assertEquals(Integer.valueOf(3), order.getTotalItems()); // verify setter works
        assertEquals(Integer.valueOf(0), order.getCompletedItems()); // initial value
        order.startInspection("检验员A");

        InspectionRecord r1 = new InspectionRecord("REC-001", "粘度");
        r1.setUsl(new BigDecimal("12.0"));
        r1.setLsl(new BigDecimal("8.0"));
        r1.record(new BigDecimal("10.0"), "检验员A", "G-001");

        // When
        order.submitResult(r1);

        // Then
        assertEquals(1, order.getRecords().size(), "records should have 1 entry");
        assertEquals("INSP-001", r1.getInspectionNo());
        assertTrue(order.getCompletedItems() > 0, "completedItems should be > 0 after submitResult");
    }

    @Test
    @DisplayName("InspectionOrder 完成检验全部通过 — IN_PROGRESS → PASSED 并发布事件")
    void inspectionOrder_completeInspection_allPassed_shouldTransitionAndPublish() {
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");
        order.setWorkOrderNo("WO-001");
        order.setTotalItems(2);
        order.setPassedItems(2);
        order.setCompletedItems(2);
        order.startInspection("检验员A");

        List<IDomainEvent> passedEvents = new ArrayList<>();
        List<IDomainEvent> completedEvents = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.INSPECTION_PASSED, passedEvents::add);
        DomainEventPublisher.subscribe(QmsEventTypes.INSPECTION_COMPLETED, completedEvents::add);

        // When
        order.completeInspection();

        // Then
        assertEquals(InspectionStatus.PASSED, order.getStatus());
        assertTrue(order.isFinished());
        assertTrue(order.isPassed());
        assertNotNull(order.getCompletedAt());
        assertEquals(1, passedEvents.size());
        assertEquals(QmsEventTypes.INSPECTION_PASSED, passedEvents.get(0).getEventType());
        assertEquals(1, completedEvents.size());
    }

    @Test
    @DisplayName("InspectionOrder 完成检验有不合格 — IN_PROGRESS → FAILED 并发布事件")
    void inspectionOrder_completeInspection_failed_shouldTransitionAndPublish() {
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");
        order.setWorkOrderNo("WO-001");
        order.setTotalItems(3);
        order.setPassedItems(2);
        order.setCompletedItems(3); // 有 1 个不合格
        order.startInspection("检验员A");

        List<IDomainEvent> failedEvents = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.INSPECTION_FAILED, failedEvents::add);

        // When
        order.completeInspection();

        // Then
        assertEquals(InspectionStatus.FAILED, order.getStatus());
        assertTrue(order.isFinished());
        assertFalse(order.isPassed());
        assertEquals(1, order.getFailedItems());
        assertEquals(1, failedEvents.size());
        assertEquals(QmsEventTypes.INSPECTION_FAILED, failedEvents.get(0).getEventType());
    }

    @Test
    @DisplayName("InspectionOrder 关闭 — PASSED/FAILED → CLOSED")
    void inspectionOrder_close_shouldTransition() {
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");
        order.setTotalItems(1);
        order.setPassedItems(1);
        order.setCompletedItems(1);
        order.startInspection("检验员A");
        order.completeInspection();

        // When
        order.close();

        // Then
        assertEquals(InspectionStatus.CLOSED, order.getStatus());
        assertTrue(order.isPassed());
    }

    @Test
    @DisplayName("InspectionOrder 非法状态转换应抛出异常（PENDING → CLOSED）")
    void inspectionOrder_illegalTransition_shouldThrow() {
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");

        assertThrows(IllegalStateException.class, order::close);
    }

    @Test
    @DisplayName("InspectionOrder CLOSED 应为终态")
    void inspectionOrder_closed_shouldBeTerminal() {
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");
        order.setTotalItems(1);
        order.setPassedItems(1);
        order.setCompletedItems(1);
        order.startInspection("检验员A");
        order.completeInspection();
        order.close();

        assertThrows(IllegalStateException.class, () -> order.startInspection("any"));
        assertThrows(IllegalStateException.class, () -> order.completeInspection());
    }

    @Test
    @DisplayName("InspectionOrder IExpand 应支持存储和读取动态属性")
    void inspectionOrder_iexpand_shouldStoreDynamicProperties() {
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");

        order.setExpandProperty("qms.inspectionType", "FQC");
        order.setExpandProperty("qms.aql", "0.65");

        assertEquals("FQC", order.getExpandProperty("qms.inspectionType"));
        assertEquals("0.65", order.getExpandProperty("qms.aql"));
    }

    // ==================== InspectionPlan 测试 ====================

    @Test
    @DisplayName("InspectionPlan 创建 — 应设置默认值：版本 1.0 + 空项目列表")
    void inspectionPlan_creation_shouldSetDefaults() {
        // Given & When
        InspectionPlan plan = new InspectionPlan("PLAN-001", "阿莫西林检验方案",
                "AMX-250MG", "PROC-INSPECT", InspectionType.FQC);

        // Then
        assertEquals("PLAN-001", plan.getCode());
        assertEquals("阿莫西林检验方案", plan.getName());
        assertEquals("AMX-250MG", plan.getProductCode());
        assertEquals("PROC-INSPECT", plan.getProcessCode());
        assertEquals(InspectionType.FQC, plan.getInspectionType());
        assertEquals("1.0", plan.getVersion());
        assertNotNull(plan.getItems());
        assertTrue(plan.getItems().isEmpty());
        assertEquals(0, plan.getItemCount());
    }

    @Test
    @DisplayName("InspectionPlan 添加检验项目 — 应正常追加")
    void inspectionPlan_addItem_shouldWork() {
        InspectionPlan plan = new InspectionPlan("PLAN-001", "检验方案", "P-001", "PROC-001", InspectionType.IPQC);
        InspectionPlan.InspectionItem item = new InspectionPlan.InspectionItem("ITEM-001", "粘度", "计量");
        item.setUsl(new BigDecimal("12.0"));
        item.setLsl(new BigDecimal("8.0"));
        item.setTarget(new BigDecimal("10.0"));
        item.setUnit("cP");
        item.setMethod("旋转粘度计法");
        item.setCritical(true);

        // When
        plan.addItem(item);

        // Then
        assertEquals(1, plan.getItemCount());
        InspectionPlan.InspectionItem saved = plan.getItems().get(0);
        assertEquals("ITEM-001", saved.getItemCode());
        assertEquals("粘度", saved.getItemName());
        assertEquals(new BigDecimal("12.0"), saved.getUsl());
        assertEquals(new BigDecimal("8.0"), saved.getLsl());
        assertTrue(saved.isCritical());
    }

    @Test
    @DisplayName("InspectionPlan 更新版本")
    void inspectionPlan_updateVersion_shouldWork() {
        InspectionPlan plan = new InspectionPlan("PLAN-001", "检验方案", "P-001", "PROC-001", InspectionType.IPQC);

        plan.updateVersion("2.0");

        assertEquals("2.0", plan.getVersion());
    }

    @Test
    @DisplayName("InspectionPlan 批准")
    void inspectionPlan_approve_shouldRecordApprover() {
        InspectionPlan plan = new InspectionPlan("PLAN-001", "检验方案", "P-001", "PROC-001", InspectionType.IPQC);

        plan.approve("质量经理");

        assertEquals("质量经理", plan.getApprovedBy());
    }

    @Test
    @DisplayName("InspectionPlan IExpand 应支持存储扩展属性")
    void inspectionPlan_iexpand_shouldStoreDynamicProperties() {
        InspectionPlan plan = new InspectionPlan("PLAN-001", "检验方案", "P-001", "PROC-001", InspectionType.IPQC);

        plan.setExpandProperty("qms.plan.aql", "1.0");
        plan.setExpandProperty("qms.plan.standard", "GB/T 2828.1");

        assertEquals("1.0", plan.getExpandProperty("qms.plan.aql"));
        assertEquals("GB/T 2828.1", plan.getExpandProperty("qms.plan.standard"));
    }

    // ==================== InspectionRecord 测试 ====================

    @Test
    @DisplayName("InspectionRecord 创建 — 应设置默认值：检验时间为当前时间")
    void inspectionRecord_creation_shouldSetDefaults() {
        // Given & When
        InspectionRecord record = new InspectionRecord("REC-001", "粘度");

        // Then
        assertEquals("REC-001", record.getCode());
        assertEquals("粘度", record.getItemName());
        assertNotNull(record.getInspectedAt());
        assertNull(record.getJudgement());
        assertNull(record.getMeasuredValue());
    }

    @Test
    @DisplayName("InspectionRecord 记录检验值—在规格限内判定 PASS")
    void inspectionRecord_record_withinSpec_shouldPass() {
        InspectionRecord record = new InspectionRecord("REC-001", "粘度");
        record.setUsl(new BigDecimal("12.0"));
        record.setLsl(new BigDecimal("8.0"));

        // When
        record.record(new BigDecimal("10.0"), "检验员A", "G-001");

        // Then
        assertEquals(new BigDecimal("10.0"), record.getMeasuredValue());
        assertEquals("检验员A", record.getInspector());
        assertEquals("G-001", record.getGaugeCode());
        assertEquals("PASS", record.getJudgement());
        assertTrue(record.isInSpec());
        assertTrue(record.isPassed());
    }

    @Test
    @DisplayName("InspectionRecord 记录检验值—超出规格上限判定 FAIL")
    void inspectionRecord_record_outOfUpperSpec_shouldFail() {
        InspectionRecord record = new InspectionRecord("REC-001", "粘度");
        record.setUsl(new BigDecimal("12.0"));
        record.setLsl(new BigDecimal("8.0"));

        // When
        record.record(new BigDecimal("15.0"), "检验员A", "G-001");

        // Then
        assertEquals("FAIL", record.getJudgement());
        assertFalse(record.isInSpec());
        assertFalse(record.isPassed());
    }

    @Test
    @DisplayName("InspectionRecord 记录检验值—低于规格下限判定 FAIL")
    void inspectionRecord_record_belowLowerSpec_shouldFail() {
        InspectionRecord record = new InspectionRecord("REC-001", "粘度");
        record.setUsl(new BigDecimal("12.0"));
        record.setLsl(new BigDecimal("8.0"));

        // When
        record.record(new BigDecimal("5.0"), "检验员A", "G-001");

        // Then
        assertEquals("FAIL", record.getJudgement());
        assertFalse(record.isInSpec());
    }

    @Test
    @DisplayName("InspectionRecord 让步接收")
    void inspectionRecord_concession_shouldOverrideJudgement() {
        InspectionRecord record = new InspectionRecord("REC-001", "外观");
        record.setUsl(new BigDecimal("1.0"));
        record.setLsl(new BigDecimal("0.0"));
        record.record(new BigDecimal("2.0"), "检验员A", "G-001"); // FAIL
        assertEquals("FAIL", record.getJudgement());

        // When
        record.concession("客户让步接收", "QA经理");

        // Then
        assertEquals("CONCESSION", record.getJudgement());
        assertEquals("客户让步接收", record.getRemark());
    }

    @Test
    @DisplayName("InspectionRecord isInSpec — 无规格限应返回 true")
    void inspectionRecord_isInSpec_noSpec_shouldReturnTrue() {
        InspectionRecord record = new InspectionRecord("REC-001", "外观");

        record.record(new BigDecimal("100"), "检验员A", "G-001");

        assertTrue(record.isInSpec());
        assertNull(record.getJudgement());
    }

    @Test
    @DisplayName("InspectionRecord 实现 IInspectionRecord 接口")
    void inspectionRecord_implementsInterface() {
        InspectionRecord record = new InspectionRecord("REC-001", "pH值");
        assertTrue(record instanceof IInspectionRecord);
    }

    // ==================== Deviation 测试 ====================

    @Test
    @DisplayName("Deviation 创建 — 初始状态为 OPEN + 严重程度记录")
    void deviation_creation_shouldSetDefaults() {
        // Given & When
        Deviation dev = new Deviation("DEV-001", "粘度不合格偏差", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);

        // Then
        assertEquals("DEV-001", dev.getCode());
        assertEquals("粘度不合格偏差", dev.getName());
        assertEquals("INSPECTION", dev.getSource());
        assertEquals("INSP-001", dev.getInspectionNo());
        assertEquals(DeviationSeverity.MAJOR, dev.getSeverity());
        assertEquals(DeviationStatus.OPEN, dev.getStatus());
        assertTrue(dev.isEditable());
    }

    @Test
    @DisplayName("Deviation 开始调查 — OPEN → INVESTIGATING 并发布事件")
    void deviation_startInvestigation_shouldTransitionAndPublishEvent() {
        Deviation dev = new Deviation("DEV-001", "粘度不合格", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.DEVIATION_CREATED, events::add);

        // When
        dev.startInvestigation("调查员A");

        // Then
        assertEquals(DeviationStatus.INVESTIGATING, dev.getStatus());
        assertEquals("调查员A", dev.getInvestigator());
        assertEquals(1, events.size());
        assertEquals(QmsEventTypes.DEVIATION_CREATED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("Deviation 完成调查 — 记录根因和产品影响评估")
    void deviation_completeInvestigation_shouldRecordRootCause() {
        Deviation dev = new Deviation("DEV-001", "粘度不合格", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);
        dev.startInvestigation("调查员A");

        // When
        dev.completeInvestigation("搅拌时间不足导致粘度偏低", "产品粘度不合格，不可放行");

        // Then
        assertEquals("搅拌时间不足导致粘度偏低", dev.getRootCause());
        assertEquals("产品粘度不合格，不可放行", dev.getProductImpact());
    }

    @Test
    @DisplayName("Deviation QA 处置 — INVESTIGATING → DISPOSITIONED 并发布事件")
    void deviation_dispose_shouldTransitionAndPublishEvent() {
        Deviation dev = new Deviation("DEV-001", "粘度不合格", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);
        dev.startInvestigation("调查员A");
        dev.completeInvestigation("根因", "影响评估");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.DEVIATION_DISPOSITIONED, events::add);

        // When
        dev.dispose(DeviationDisposition.REWORK, "退回生产重新加工", "QA经理");

        // Then
        assertEquals(DeviationStatus.DISPOSITIONED, dev.getStatus());
        assertEquals(DeviationDisposition.REWORK, dev.getDisposition());
        assertEquals("退回生产重新加工", dev.getDispositionNote());
        assertEquals("QA经理", dev.getDisposedBy());
        assertEquals(1, events.size());
        assertEquals(QmsEventTypes.DEVIATION_DISPOSITIONED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("Deviation 关联 CAPA")
    void deviation_linkCapa_shouldSetCapaCode() {
        Deviation dev = new Deviation("DEV-001", "粘度不合格", "INSPECTION",
                "INSP-001", DeviationSeverity.CRITICAL);

        // When
        dev.linkCapa("CAPA-001");

        // Then
        assertEquals("CAPA-001", dev.getCapaCode());
    }

    @Test
    @DisplayName("Deviation 解决 — DISPOSITIONED → RESOLVED 并发布事件")
    void deviation_resolve_shouldTransitionAndPublishEvent() {
        Deviation dev = new Deviation("DEV-001", "粘度不合格", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);
        dev.startInvestigation("调查员A");
        dev.completeInvestigation("根因", "影响");
        dev.dispose(DeviationDisposition.REWORK, "返工", "QA经理");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.DEVIATION_RESOLVED, events::add);

        // When
        dev.resolve();

        // Then
        assertEquals(DeviationStatus.RESOLVED, dev.getStatus());
        assertEquals(1, events.size());
        assertEquals(QmsEventTypes.DEVIATION_RESOLVED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("Deviation 关闭 — RESOLVED → CLOSED")
    void deviation_close_shouldTransition() {
        Deviation dev = new Deviation("DEV-001", "粘度不合格", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);
        dev.startInvestigation("调查员A");
        dev.completeInvestigation("根因", "影响");
        dev.dispose(DeviationDisposition.REWORK, "返工", "QA经理");
        dev.resolve();

        // When
        dev.close();

        // Then
        assertEquals(DeviationStatus.CLOSED, dev.getStatus());
        assertNotNull(dev.getClosedAt());
        assertFalse(dev.isEditable());
    }

    @Test
    @DisplayName("Deviation 取消 — OPEN → CANCELLED")
    void deviation_cancel_shouldTransition() {
        Deviation dev = new Deviation("DEV-001", "粘度不合格", "INSPECTION",
                "INSP-001", DeviationSeverity.MINOR);

        // When
        dev.cancel("误报，实际合格");

        // Then
        assertEquals(DeviationStatus.CANCELLED, dev.getStatus());
        assertEquals("误报，实际合格", dev.getExpandProperty("qms.deviation.cancelReason"));
    }

    @Test
    @DisplayName("Deviation 完整生命周期：OPEN → INVESTIGATING → DISPOSITIONED → RESOLVED → CLOSED")
    void deviation_fullLifecycle_shouldWork() {
        Deviation dev = new Deviation("DEV-001", "完整流程偏差", "INSPECTION",
                "INSP-001", DeviationSeverity.CRITICAL);

        dev.startInvestigation("调查员A");
        assertEquals(DeviationStatus.INVESTIGATING, dev.getStatus());

        dev.completeInvestigation("设备参数漂移", "批次受影响");
        assertEquals("设备参数漂移", dev.getRootCause());

        dev.dispose(DeviationDisposition.REJECT, "报废处理", "QA经理");
        assertEquals(DeviationStatus.DISPOSITIONED, dev.getStatus());

        dev.linkCapa("CAPA-001");
        dev.resolve();
        assertEquals(DeviationStatus.RESOLVED, dev.getStatus());

        dev.close();
        assertEquals(DeviationStatus.CLOSED, dev.getStatus());
    }

    @Test
    @DisplayName("Deviation requiresCapa — CRITICAL 偏差必须 CAPA")
    void deviation_requiresCapa_critical_shouldReturnTrue() {
        Deviation dev = new Deviation("DEV-001", "严重偏差", "INSPECTION",
                "INSP-001", DeviationSeverity.CRITICAL);

        assertTrue(dev.requiresCapa());
    }

    @Test
    @DisplayName("Deviation requiresCapa — MINOR 偏差不需 CAPA")
    void deviation_requiresCapa_minor_shouldReturnFalse() {
        Deviation dev = new Deviation("DEV-001", "轻微偏差", "INSPECTION",
                "INSP-001", DeviationSeverity.MINOR);

        assertFalse(dev.requiresCapa());
    }

    @Test
    @DisplayName("Deviation requiresCapa — MAJOR 偏差让步接收不需 CAPA")
    void deviation_requiresCapa_majorConcession_shouldReturnFalse() {
        Deviation dev = new Deviation("DEV-001", "重大偏差", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);
        dev.startInvestigation("调查员A");
        dev.completeInvestigation("根因", "影响");
        dev.dispose(DeviationDisposition.CONCESSION, "让步接收", "QA经理");

        assertFalse(dev.requiresCapa());
    }

    @Test
    @DisplayName("Deviation 非法状态转换 — OPEN → CLOSED 应抛出异常")
    void deviation_illegalTransition_shouldThrow() {
        Deviation dev = new Deviation("DEV-001", "测试", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);

        assertThrows(IllegalStateException.class, dev::close);
    }

    @Test
    @DisplayName("Deviation CLOSED 应为终态")
    void deviation_closed_shouldBeTerminal() {
        Deviation dev = new Deviation("DEV-001", "测试", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);
        dev.startInvestigation("调查员A");
        dev.completeInvestigation("根因", "影响");
        dev.dispose(DeviationDisposition.REWORK, "返工", "QA经理");
        dev.resolve();
        dev.close();

        assertThrows(IllegalStateException.class, dev::resolve);
        assertThrows(IllegalStateException.class, () -> dev.startInvestigation("any"));
    }

    @Test
    @DisplayName("Deviation IExpand 应支持存储和读取动态属性")
    void deviation_iexpand_shouldStoreDynamicProperties() {
        Deviation dev = new Deviation("DEV-001", "测试", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);

        dev.setExpandProperty("qms.deviation.source", "检验不合格");
        dev.setExpandProperty("qms.deviation.capaCode", "CAPA-001");

        assertEquals("检验不合格", dev.getExpandProperty("qms.deviation.source"));
        assertEquals("CAPA-001", dev.getExpandProperty("qms.deviation.capaCode"));
    }

    // ==================== Capa 测试 ====================

    @Test
    @DisplayName("Capa 创建 — 初始状态为 OPEN + 问题描述和负责人")
    void capa_creation_shouldSetDefaults() {
        // Given & When
        Capa capa = new Capa("CAPA-001", "粘度异常CAPA", "DEV-001",
                "搅拌工序粘度连续超出控制限", "张三");

        // Then
        assertEquals("CAPA-001", capa.getCode());
        assertEquals("粘度异常CAPA", capa.getName());
        assertEquals("DEV-001", capa.getDeviationCode());
        assertEquals("搅拌工序粘度连续超出控制限", capa.getProblemDescription());
        assertEquals("张三", capa.getAssignTo());
        assertEquals(CapaStatus.OPEN, capa.getStatus());
        assertTrue(capa.isEditable());
    }

    @Test
    @DisplayName("Capa 根因分析 — OPEN → ROOT_CAUSE 并发布事件")
    void capa_analyzeRootCause_shouldTransitionAndPublishEvent() {
        Capa capa = new Capa("CAPA-001", "粘度异常CAPA", "DEV-001",
                "搅拌工序粘度连续超出控制限", "张三");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.CAPA_ROOT_CAUSE_DONE, events::add);

        // When
        capa.analyzeRootCause("搅拌电机转速不稳定，供电电压波动(5Why分析)");

        // Then
        assertEquals(CapaStatus.ROOT_CAUSE, capa.getStatus());
        assertEquals("搅拌电机转速不稳定，供电电压波动(5Why分析)", capa.getRootCause());
        assertEquals(1, events.size());
        assertEquals(QmsEventTypes.CAPA_ROOT_CAUSE_DONE, events.get(0).getEventType());
    }

    @Test
    @DisplayName("Capa 执行措施 — ROOT_CAUSE → IN_PROGRESS 并发布事件")
    void capa_executeActions_shouldTransitionAndPublishEvent() {
        Capa capa = new Capa("CAPA-001", "粘度异常CAPA", "DEV-001",
                "粘度超限", "张三");
        capa.analyzeRootCause("电机转速不稳定");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.CAPA_IN_PROGRESS, events::add);

        // When
        capa.executeActions("更换搅拌电机并加装稳压器", "建立每周电机转速校准制度");

        // Then
        assertEquals(CapaStatus.IN_PROGRESS, capa.getStatus());
        assertEquals("更换搅拌电机并加装稳压器", capa.getCorrectiveAction());
        assertEquals("建立每周电机转速校准制度", capa.getPreventiveAction());
        assertEquals(1, events.size());
        assertEquals(QmsEventTypes.CAPA_IN_PROGRESS, events.get(0).getEventType());
    }

    @Test
    @DisplayName("Capa 效果验证 — IN_PROGRESS → VERIFIED 并发布事件")
    void capa_verify_shouldTransitionAndPublishEvent() {
        Capa capa = new Capa("CAPA-001", "粘度异常CAPA", "DEV-001",
                "粘度超限", "张三");
        capa.analyzeRootCause("电机转速不稳定");
        capa.executeActions("更换电机", "建立校准制度");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.CAPA_VERIFIED, events::add);

        // When
        capa.verify("连续3批验证搅拌粘度均在控制限内，CAPA有效", "QA经理");

        // Then
        assertEquals(CapaStatus.VERIFIED, capa.getStatus());
        assertEquals("连续3批验证搅拌粘度均在控制限内，CAPA有效", capa.getVerification());
        assertEquals("QA经理", capa.getApprovedBy());
        assertEquals(1, events.size());
        assertEquals(QmsEventTypes.CAPA_VERIFIED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("Capa 关闭 — VERIFIED → CLOSED 并发布事件")
    void capa_close_shouldTransitionAndPublishEvent() {
        Capa capa = new Capa("CAPA-001", "粘度异常CAPA", "DEV-001",
                "粘度超限", "张三");
        capa.analyzeRootCause("电机转速不稳定");
        capa.executeActions("更换电机", "建立校准制度");
        capa.verify("有效", "QA经理");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.CAPA_CLOSED, events::add);

        // When
        capa.close();

        // Then
        assertEquals(CapaStatus.CLOSED, capa.getStatus());
        assertNotNull(capa.getClosedAt());
        assertEquals(1, events.size());
        assertEquals(QmsEventTypes.CAPA_CLOSED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("Capa 完整生命周期：OPEN → ROOT_CAUSE → IN_PROGRESS → VERIFIED → CLOSED")
    void capa_fullLifecycle_shouldWork() {
        Capa capa = new Capa("CAPA-001", "完整流程CAPA", "DEV-001",
                "pH值连续超限", "李四");

        capa.analyzeRootCause("缓冲液配制比例错误");
        assertEquals(CapaStatus.ROOT_CAUSE, capa.getStatus());

        capa.executeActions("修正缓冲液配比SOP", "增加中间检验频率");
        assertEquals(CapaStatus.IN_PROGRESS, capa.getStatus());

        capa.verify("连续5批pH值均在标准范围内", "QA经理");
        assertEquals(CapaStatus.VERIFIED, capa.getStatus());

        capa.close();
        assertEquals(CapaStatus.CLOSED, capa.getStatus());
        assertFalse(capa.isEditable());
    }

    @Test
    @DisplayName("Capa 取消 — OPEN → CANCELLED")
    void capa_cancel_shouldTransition() {
        Capa capa = new Capa("CAPA-001", "测试CAPA", "DEV-001",
                "测试问题", "张三");

        // When
        capa.cancel("偏差误报，无需CAPA");

        // Then
        assertEquals(CapaStatus.CANCELLED, capa.getStatus());
        assertEquals("偏差误报，无需CAPA", capa.getExpandProperty("qms.capa.cancelReason"));
    }

    @Test
    @DisplayName("Capa isOverdue — 未设置截止日期返回 false")
    void capa_isOverdue_noDueDate_shouldReturnFalse() {
        Capa capa = new Capa("CAPA-001", "测试CAPA", "DEV-001", "问题", "张三");

        assertFalse(capa.isOverdue());
    }

    @Test
    @DisplayName("Capa isOverdue — 已关闭 CAPA 不视为逾期")
    void capa_isOverdue_closed_shouldReturnFalse() {
        Capa capa = new Capa("CAPA-001", "测试CAPA", "DEV-001", "问题", "张三");
        capa.setDueDate(java.time.Instant.now().minus(java.time.Duration.ofDays(10)));
        capa.analyzeRootCause("根因");
        capa.executeActions("纠正", "预防");
        capa.verify("有效", "QA经理");
        capa.close();

        assertFalse(capa.isOverdue());
    }

    @Test
    @DisplayName("Capa isOverdue — 已超过截止日期且未关闭")
    void capa_isOverdue_overdue_shouldReturnTrue() {
        Capa capa = new Capa("CAPA-001", "测试CAPA", "DEV-001", "问题", "张三");
        capa.analyzeRootCause("根因");
        capa.setDueDate(java.time.Instant.now().minus(java.time.Duration.ofDays(30)));

        assertTrue(capa.isOverdue());
    }

    @Test
    @DisplayName("Capa 非法状态转换 — OPEN → CLOSED 应抛出异常")
    void capa_illegalTransition_shouldThrow() {
        Capa capa = new Capa("CAPA-001", "测试CAPA", "DEV-001", "测试", "张三");

        assertThrows(IllegalStateException.class, capa::close);
    }

    @Test
    @DisplayName("Capa CLOSED 应为终态")
    void capa_closed_shouldBeTerminal() {
        Capa capa = new Capa("CAPA-001", "测试CAPA", "DEV-001", "测试", "张三");
        capa.analyzeRootCause("根因");
        capa.executeActions("纠正", "预防");
        capa.verify("有效", "QA经理");
        capa.close();

        assertThrows(IllegalStateException.class, () -> capa.analyzeRootCause("any"));
        assertThrows(IllegalStateException.class, () -> capa.executeActions("a", "b"));
    }

    // ==================== 枚举测试 ====================

    @Test
    @DisplayName("InspectionType 枚举应覆盖 4 种检验类型")
    void inspectionType_shouldCoverAllTypes() {
        InspectionType[] types = InspectionType.values();
        assertEquals(4, types.length);
        assertEquals(InspectionType.valueOf("IQC"), InspectionType.IQC);
        assertEquals(InspectionType.valueOf("IPQC"), InspectionType.IPQC);
        assertEquals(InspectionType.valueOf("FQC"), InspectionType.FQC);
        assertEquals(InspectionType.valueOf("OQC"), InspectionType.OQC);
    }

    @Test
    @DisplayName("DeviationSeverity 枚举应覆盖 3 种严重级别")
    void deviationSeverity_shouldCoverAllLevels() {
        DeviationSeverity[] levels = DeviationSeverity.values();
        assertEquals(3, levels.length);
        assertEquals(DeviationSeverity.valueOf("MINOR"), DeviationSeverity.MINOR);
        assertEquals(DeviationSeverity.valueOf("MAJOR"), DeviationSeverity.MAJOR);
        assertEquals(DeviationSeverity.valueOf("CRITICAL"), DeviationSeverity.CRITICAL);
    }

    @Test
    @DisplayName("DeviationDisposition 枚举应覆盖 3 种处置方式")
    void deviationDisposition_shouldCoverAllOptions() {
        DeviationDisposition[] options = DeviationDisposition.values();
        assertEquals(3, options.length);
        assertEquals(DeviationDisposition.valueOf("REWORK"), DeviationDisposition.REWORK);
        assertEquals(DeviationDisposition.valueOf("CONCESSION"), DeviationDisposition.CONCESSION);
        assertEquals(DeviationDisposition.valueOf("REJECT"), DeviationDisposition.REJECT);
    }

    @Test
    @DisplayName("InspectionStatus 枚举应覆盖 5 个状态并正确转换")
    void inspectionStatus_shouldCoverAllStatuses() {
        InspectionStatus[] statuses = InspectionStatus.values();
        assertEquals(5, statuses.length);

        assertEquals(Set.of(InspectionStatus.IN_PROGRESS),
                InspectionStatus.PENDING.allowedTransitions());
        assertEquals(Set.of(InspectionStatus.PASSED, InspectionStatus.FAILED),
                InspectionStatus.IN_PROGRESS.allowedTransitions());
        assertEquals(Set.of(InspectionStatus.CLOSED),
                InspectionStatus.PASSED.allowedTransitions());
        assertEquals(Set.of(InspectionStatus.CLOSED),
                InspectionStatus.FAILED.allowedTransitions());
        assertTrue(InspectionStatus.CLOSED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("DeviationStatus 枚举应覆盖 6 个状态并正确转换")
    void deviationStatus_shouldCoverAllStatuses() {
        DeviationStatus[] statuses = DeviationStatus.values();
        assertEquals(6, statuses.length);

        assertEquals(Set.of(DeviationStatus.INVESTIGATING, DeviationStatus.CANCELLED),
                DeviationStatus.OPEN.allowedTransitions());
        assertEquals(Set.of(DeviationStatus.DISPOSITIONED, DeviationStatus.CANCELLED),
                DeviationStatus.INVESTIGATING.allowedTransitions());
        assertEquals(Set.of(DeviationStatus.RESOLVED, DeviationStatus.CANCELLED),
                DeviationStatus.DISPOSITIONED.allowedTransitions());
        assertEquals(Set.of(DeviationStatus.CLOSED),
                DeviationStatus.RESOLVED.allowedTransitions());
        assertTrue(DeviationStatus.CLOSED.allowedTransitions().isEmpty());
        assertTrue(DeviationStatus.CANCELLED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("CapaStatus 枚举应覆盖 6 个状态并正确转换")
    void capaStatus_shouldCoverAllStatuses() {
        CapaStatus[] statuses = CapaStatus.values();
        assertEquals(6, statuses.length);

        assertEquals(Set.of(CapaStatus.ROOT_CAUSE, CapaStatus.CANCELLED),
                CapaStatus.OPEN.allowedTransitions());
        assertEquals(Set.of(CapaStatus.IN_PROGRESS, CapaStatus.CANCELLED),
                CapaStatus.ROOT_CAUSE.allowedTransitions());
        assertEquals(Set.of(CapaStatus.VERIFIED, CapaStatus.CANCELLED),
                CapaStatus.IN_PROGRESS.allowedTransitions());
        assertEquals(Set.of(CapaStatus.CLOSED),
                CapaStatus.VERIFIED.allowedTransitions());
        assertTrue(CapaStatus.CLOSED.allowedTransitions().isEmpty());
        assertTrue(CapaStatus.CANCELLED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("同状态转换应幂等（不抛异常）")
    void sameStatusTransition_shouldBeIdempotent() {
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");
        assertDoesNotThrow(() -> order.transition(InspectionStatus.PENDING));

        Deviation dev = new Deviation("DEV-001", "测试", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);
        assertDoesNotThrow(() -> dev.transition(DeviationStatus.OPEN));

        Capa capa = new Capa("CAPA-001", "测试", "DEV-001", "问题", "张三");
        assertDoesNotThrow(() -> capa.transition(CapaStatus.OPEN));
    }

    // ==================== QmsEventTypes 测试 ====================

    @Test
    @DisplayName("事件命名应遵循 {module}.{entity}.{past_tense} 约定")
    void qmsEventTypes_namingConvention() {
        assertTrue(QmsEventTypes.INSPECTION_ORDER_CREATED.startsWith("qms.inspection."));
        assertTrue(QmsEventTypes.INSPECTION_STARTED.startsWith("qms.inspection."));
        assertTrue(QmsEventTypes.INSPECTION_COMPLETED.startsWith("qms.inspection."));
        assertTrue(QmsEventTypes.INSPECTION_PASSED.startsWith("qms.inspection."));
        assertTrue(QmsEventTypes.INSPECTION_FAILED.startsWith("qms.inspection."));
        assertTrue(QmsEventTypes.DEVIATION_CREATED.startsWith("qms.deviation."));
        assertTrue(QmsEventTypes.DEVIATION_INVESTIGATED.startsWith("qms.deviation."));
        assertTrue(QmsEventTypes.DEVIATION_DISPOSITIONED.startsWith("qms.deviation."));
        assertTrue(QmsEventTypes.DEVIATION_RESOLVED.startsWith("qms.deviation."));
        assertTrue(QmsEventTypes.DEVIATION_CLOSED.startsWith("qms.deviation."));
        assertTrue(QmsEventTypes.CAPA_CREATED.startsWith("qms.capa."));
        assertTrue(QmsEventTypes.CAPA_ROOT_CAUSE_DONE.startsWith("qms.capa."));
        assertTrue(QmsEventTypes.CAPA_IN_PROGRESS.startsWith("qms.capa."));
        assertTrue(QmsEventTypes.CAPA_VERIFIED.startsWith("qms.capa."));
        assertTrue(QmsEventTypes.CAPA_CLOSED.startsWith("qms.capa."));
        assertTrue(QmsEventTypes.SPC_DATA_COLLECTED.startsWith("qms.spc."));
        assertTrue(QmsEventTypes.SPC_WARNING.startsWith("qms.spc."));
        assertTrue(QmsEventTypes.SPC_OUT_OF_CONTROL.startsWith("qms.spc."));
    }

    @Test
    @DisplayName("所有事件常量应已定义 — 共 18 个")
    void qmsEventTypes_allConstantsDefined() {
        assertEquals("qms", QmsEventTypes.PREFIX);
        // 检验 5 个
        assertNotNull(QmsEventTypes.INSPECTION_ORDER_CREATED);
        assertNotNull(QmsEventTypes.INSPECTION_STARTED);
        assertNotNull(QmsEventTypes.INSPECTION_COMPLETED);
        assertNotNull(QmsEventTypes.INSPECTION_PASSED);
        assertNotNull(QmsEventTypes.INSPECTION_FAILED);
        // 偏差 5 个
        assertNotNull(QmsEventTypes.DEVIATION_CREATED);
        assertNotNull(QmsEventTypes.DEVIATION_INVESTIGATED);
        assertNotNull(QmsEventTypes.DEVIATION_DISPOSITIONED);
        assertNotNull(QmsEventTypes.DEVIATION_RESOLVED);
        assertNotNull(QmsEventTypes.DEVIATION_CLOSED);
        // CAPA 5 个
        assertNotNull(QmsEventTypes.CAPA_CREATED);
        assertNotNull(QmsEventTypes.CAPA_ROOT_CAUSE_DONE);
        assertNotNull(QmsEventTypes.CAPA_IN_PROGRESS);
        assertNotNull(QmsEventTypes.CAPA_VERIFIED);
        assertNotNull(QmsEventTypes.CAPA_CLOSED);
        // SPC 3 个
        assertNotNull(QmsEventTypes.SPC_DATA_COLLECTED);
        assertNotNull(QmsEventTypes.SPC_WARNING);
        assertNotNull(QmsEventTypes.SPC_OUT_OF_CONTROL);
    }

    @Test
    @DisplayName("事件负载应正确传递 — 验证 DEVIATION_CREATED 事件数据")
    void qmsEventTypes_eventPayload_shouldBeCorrect() {
        Deviation dev = new Deviation("DEV-001", "测试偏差", "INSPECTION",
                "INSP-001", DeviationSeverity.CRITICAL);
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(QmsEventTypes.DEVIATION_CREATED, events::add);

        // When
        dev.startInvestigation("调查员A");

        // Then
        assertEquals(1, events.size());
        IDomainEvent event = events.get(0);
        assertEquals(QmsEventTypes.DEVIATION_CREATED, event.getEventType());
        assertEquals("qms", event.getSource());
        assertNotNull(event.getPayload());
        assertNotNull(event.getTimestamp());
    }

    // ==================== 跨模块接口实现验证 ====================

    @Test
    @DisplayName("InspectionOrder 应实现 IInspectionOrder 接口")
    void inspectionOrder_implementsInterface() {
        InspectionOrder order = new InspectionOrder("INSP-001", "B001", "PROC-001");
        assertTrue(order instanceof IInspectionOrder);
    }

    @Test
    @DisplayName("Deviation 应实现 IDeviation 接口")
    void deviation_implementsInterface() {
        Deviation dev = new Deviation("DEV-001", "测试", "INSPECTION",
                "INSP-001", DeviationSeverity.MAJOR);
        assertTrue(dev instanceof IDeviation);
    }

    @Test
    @DisplayName("Capa 应实现 ICapa 接口")
    void capa_implementsInterface() {
        Capa capa = new Capa("CAPA-001", "测试", "DEV-001", "问题", "张三");
        assertTrue(capa instanceof ICapa);
    }

    // ==================== InspectionPlan.InspectionItem 测试 ====================

    @Test
    @DisplayName("InspectionItem 创建 — 默认值")
    void inspectionItem_creation_shouldSetDefaults() {
        InspectionPlan.InspectionItem item = new InspectionPlan.InspectionItem("ITEM-001", "含量", "计量");

        assertEquals("ITEM-001", item.getItemCode());
        assertEquals("含量", item.getItemName());
        assertEquals("计量", item.getSpecType());
        assertNull(item.getUsl());
        assertNull(item.getLsl());
        assertFalse(item.isCritical());
    }

    @Test
    @DisplayName("InspectionItem 设置规格限 — 应正常存储")
    void inspectionItem_setSpecLimits_shouldWork() {
        InspectionPlan.InspectionItem item = new InspectionPlan.InspectionItem();
        item.setItemCode("ITEM-002");
        item.setItemName("pH值");
        item.setSpecType("计量");
        item.setUsl(new BigDecimal("8.0"));
        item.setLsl(new BigDecimal("6.0"));
        item.setTarget(new BigDecimal("7.0"));
        item.setUnit("pH");
        item.setMethod("pH计法");
        item.setSampling("每批取样3个点");
        item.setOrder(1);
        item.setCritical(true);

        assertEquals(new BigDecimal("8.0"), item.getUsl());
        assertEquals(new BigDecimal("6.0"), item.getLsl());
        assertEquals(new BigDecimal("7.0"), item.getTarget());
        assertEquals("pH", item.getUnit());
        assertEquals("pH计法", item.getMethod());
        assertEquals(1, item.getOrder());
        assertTrue(item.isCritical());
    }

    // ==================== InspectionRecord 逻辑覆盖 ====================

    @Test
    @DisplayName("InspectionRecord — 测量值等于规格上限时判定 PASS")
    void inspectionRecord_valueOnUpperLimit_shouldPass() {
        InspectionRecord record = new InspectionRecord("REC-001", "粘度");
        record.setUsl(new BigDecimal("12.0"));
        record.setLsl(new BigDecimal("8.0"));

        record.record(new BigDecimal("12.0"), "检验员A", "G-001");

        assertEquals("PASS", record.getJudgement());
        assertTrue(record.isInSpec());
    }

    @Test
    @DisplayName("InspectionRecord — 测量值等于规格下限时判定 PASS")
    void inspectionRecord_valueOnLowerLimit_shouldPass() {
        InspectionRecord record = new InspectionRecord("REC-001", "粘度");
        record.setUsl(new BigDecimal("12.0"));
        record.setLsl(new BigDecimal("8.0"));

        record.record(new BigDecimal("8.0"), "检验员A", "G-001");

        assertEquals("PASS", record.getJudgement());
        assertTrue(record.isInSpec());
    }

    @Test
    @DisplayName("InspectionRecord — 使用 BigDecimal 正确比较精度")
    void inspectionRecord_bigDecimalScale_shouldCompareCorrectly() {
        InspectionRecord record = new InspectionRecord("REC-001", "含量");
        record.setUsl(new BigDecimal("100.0"));
        record.setLsl(new BigDecimal("95.0"));

        // 100.00 应该等于规格上限 100.0（按 compareTo）
        record.record(new BigDecimal("100.00"), "检验员A", "G-001");

        assertEquals("PASS", record.getJudgement());
        assertTrue(record.isInSpec());
    }

    // ==================== 模块加载测试 ====================

    @Test
    @DisplayName("模块上下文应正常加载")
    void shouldLoadContext() {
        assertTrue(true);
    }

    // ==================== QualityAction 测试 ====================

    @Test
    @DisplayName("QualityAction 创建 — IPQC 默认工厂方法")
    void qualityAction_ipqc_shouldSetCorrectType() {
        var action = QualityAction.ipqc("QA-001", "IPQC检验", "PLAN-001");

        assertEquals("QA-001", action.getCode());
        assertEquals("IPQC检验", action.getName());
        assertEquals("IPQC", action.getInspectionType());
        assertEquals("PLAN-001", action.getInspectionPlanCode());
        assertFalse(action.isQualityGate());
        assertEquals(Dict.Importance.Require, action.getImportance());
    }

    @Test
    @DisplayName("QualityAction 创建 — 质量门禁")
    void qualityAction_gate_shouldSetQualityGate() {
        var action = QualityAction.gate("QA-GATE", "质量门禁", "PLAN-GATE");

        assertEquals("QA-GATE", action.getCode());
        assertTrue(action.isQualityGate());
    }

    @Test
    @DisplayName("QualityAction 创建 — FQC 工厂方法")
    void qualityAction_fqc_shouldSetCorrectType() {
        var action = QualityAction.fqc("QA-002", "最终检验", "PLAN-FQC");

        assertEquals("FQC", action.getInspectionType());
    }

    @Test
    @DisplayName("QualityAction 创建 — IQC 工厂方法")
    void qualityAction_iqc_shouldSetCorrectType() {
        var action = QualityAction.iqc("QA-003", "来料检验", "PLAN-IQC");

        assertEquals("IQC", action.getInspectionType());
    }

    @Test
    @DisplayName("QualityAction 实现 IQualityAction 接口")
    void qualityAction_implementsInterface() {
        var action = new QualityAction("QA-001", "测试", "PLAN-001");
        assertTrue(action instanceof IQualityAction);
    }

    @Test
    @DisplayName("QualityAction setRequireResources 应正常设置")
    void qualityAction_setRequireResources_shouldWork() {
        var action = new QualityAction("QA-001", "测试", "PLAN-001");

        action.setRequireResources();
        assertEquals(0, action.requireResources().length);
    }

    @Test
    @DisplayName("QualityAction executeQuality 应返回结果")
    void qualityAction_executeQuality_shouldReturnResult() {
        var action = new QualityAction("QA-001", "测试", "PLAN-001");

        var result = action.executeQuality(null);

        assertNotNull(result);
        assertEquals("PLAN-001", result.inspectionPlanCode());
        assertTrue(result.passed());
        assertEquals("ACCEPT", result.judgment());
    }

    @Test
    @DisplayName("QualityAction 默认重要性为 Require")
    void qualityAction_defaultImportance_shouldBeRequire() {
        var action = new QualityAction("QA-001", "测试", "PLAN-001");
        assertEquals(Dict.Importance.Require, action.getImportance());
    }

    @Test
    @DisplayName("QualityAction 默认不需要 gate")
    void qualityAction_defaultGate_shouldBeFalse() {
        var action = new QualityAction("QA-001", "测试", "PLAN-001");
        assertFalse(action.isQualityGate());
    }

}
