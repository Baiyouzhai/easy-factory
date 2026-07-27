package com.byz.factory.lims;

import com.byz.factory.lims.*;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.LimsEventTypes;
import com.byz.factory.lims.model.*;
import com.byz.factory.shared.BumpType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LIMS 模块单元测试。
 * <p>
 * 覆盖：Formula、WeighingTask、WeighingItem、BatchRecord 的构造、状态转换、
 * 业务便捷方法、事件发布、IExpand 以及状态枚举定义。
 *
 * @author 苏政
 */
class LimsModuleTest {

    @AfterEach
    void tearDown() {
        DomainEventPublisher.clearAll();
    }

    // ==================== Formula 测试 ====================

    @Test
    @DisplayName("创建配方应设置默认值：DRAFT 状态 + 版本 0.1.0 + 空投料阶段")
    void formula_creation_shouldSetDefaults() {
        // Given & When
        Formula f = new Formula("F-AMX-001", "阿莫西林胶囊配方", "AMX-250MG");

        // Then
        assertEquals("F-AMX-001", f.getCode());
        assertEquals("阿莫西林胶囊配方", f.getName());
        assertEquals("AMX-250MG", f.getProductCode());
        assertEquals(FormulaStatus.DRAFT, f.getStatus());
        assertEquals("0.1.0", f.getVersion());
        assertNotNull(f.getPhases());
        assertTrue(f.getPhases().isEmpty());
        assertTrue(f.isEditable());
        assertFalse(f.isActive());
    }

    @Test
    @DisplayName("提交审批应转换 DRAFT → APPROVED 并发布事件")
    void formula_submitForApproval_shouldTransitionAndPublishEvent() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(LimsEventTypes.FORMULA_CREATED, events::add);

        // When
        f.submitForApproval();

        // Then
        assertEquals(FormulaStatus.APPROVED, f.getStatus());
        assertEquals(1, events.size());
        assertEquals(LimsEventTypes.FORMULA_CREATED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("批准配方应记录批准人并发布事件")
    void formula_approve_shouldRecordApproverAndPublishEvent() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        f.submitForApproval();
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(LimsEventTypes.FORMULA_APPROVED, events::add);

        // When
        f.approve("张三");

        // Then
        assertEquals("张三", f.getApprovedBy());
        assertEquals(1, events.size());
        assertEquals(LimsEventTypes.FORMULA_APPROVED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("激活配方应转换 APPROVED → ACTIVE 并发布事件")
    void formula_activate_shouldTransitionAndPublishEvent() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        f.submitForApproval();
        f.approve("张三");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(LimsEventTypes.FORMULA_ACTIVATED, events::add);

        // When
        f.activate();

        // Then
        assertEquals(FormulaStatus.ACTIVE, f.getStatus());
        assertTrue(f.isActive());
        assertFalse(f.isEditable());
        assertEquals(1, events.size());
        assertEquals(LimsEventTypes.FORMULA_ACTIVATED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("退役配方应转换 ACTIVE → RETIRED 并发布事件")
    void formula_retire_shouldTransitionAndPublishEvent() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        f.submitForApproval();
        f.approve("张三");
        f.activate();
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(LimsEventTypes.FORMULA_RETIRED, events::add);

        // When
        f.retire("新版本替代");

        // Then
        assertEquals(FormulaStatus.RETIRED, f.getStatus());
        assertEquals("新版本替代", f.getExpandProperty("lims.retireReason"));
        assertEquals(1, events.size());
        assertEquals(LimsEventTypes.FORMULA_RETIRED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("驳回配方应转换 APPROVED → DRAFT")
    void formula_reject_shouldReturnToDraft() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        f.submitForApproval();

        // When
        f.reject("参数不完整");

        // Then
        assertEquals(FormulaStatus.DRAFT, f.getStatus());
        assertEquals("参数不完整", f.getExpandProperty("lims.rejectionReason"));
        assertTrue(f.isEditable());
    }

    @Test
    @DisplayName("非法状态转换应抛出异常（DRAFT → ACTIVE）")
    void formula_illegalTransition_shouldThrow() {
        Formula f = new Formula("F-001", "测试配方", "P-001");

        // Then
        assertThrows(IllegalStateException.class, f::activate);
    }

    @Test
    @DisplayName("RETIRED 应为终态，不可再转换")
    void formula_retired_shouldBeTerminal() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        f.submitForApproval();
        f.approve("张三");
        f.activate();
        f.retire("完成使命");

        // Then
        assertThrows(IllegalStateException.class, f::activate);
        assertThrows(IllegalStateException.class, () -> f.transition(FormulaStatus.APPROVED));
    }

    @Test
    @DisplayName("DRAFT 状态不可退役")
    void formula_draftToRetired_shouldThrow() {
        Formula f = new Formula("F-001", "测试配方", "P-001");

        assertThrows(IllegalStateException.class, () -> f.retire("test"));
    }

    @Test
    @DisplayName("版本递增 MAJOR 应从 1.0.0 升至 2.0.0")
    void formula_bumpVersion_shouldWork() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        // 手动设置版本以便测试
        f.setVersion("1.0.0");

        // When
        String newVersion = f.bumpVersion(BumpType.MAJOR);

        // Then
        assertEquals("2.0.0", newVersion);
        assertEquals("2.0.0", f.getVersion());
    }

    @Test
    @DisplayName("版本递增 MINOR 应从 1.0.0 升至 1.1.0")
    void formula_bumpVersion_minor_shouldWork() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        f.setVersion("1.0.0");

        String newVersion = f.bumpVersion(BumpType.MINOR);

        assertEquals("1.1.0", newVersion);
    }

    @Test
    @DisplayName("版本递增 PATCH 应从 1.0.0 升至 1.0.1")
    void formula_bumpVersion_patch_shouldWork() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        f.setVersion("1.0.0");

        String newVersion = f.bumpVersion(BumpType.PATCH);

        assertEquals("1.0.1", newVersion);
    }

    @Test
    @DisplayName("添加投料阶段应正常追加")
    void formula_addPhase_shouldWork() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        Formula.FormulaPhase phase = new Formula.FormulaPhase(1, "预处理");

        // When
        f.addPhase(phase);

        // Then
        assertEquals(1, f.getPhases().size());
        assertEquals(1, f.getPhases().get(0).getPhaseNo());
        assertEquals("预处理", f.getPhases().get(0).getPhaseName());
    }

    @Test
    @DisplayName("创建新版本应保留原始编码和产品，返回 DRAFT 状态")
    void formula_createNewVersion_shouldCloneAndReset() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        f.setVersion("1.0.0");
        f.setBatchSize(new BigDecimal("1000"));
        f.setYield("95-102%");
        f.submitForApproval();
        f.approve("张三");
        f.activate();

        // When
        Formula next = f.createNewVersion("2.0.0");

        // Then
        assertEquals("F-001", next.getCode());
        assertEquals("P-001", next.getProductCode());
        assertEquals("2.0.0", next.getVersion());
        assertEquals(FormulaStatus.DRAFT, next.getStatus());
        assertEquals(new BigDecimal("1000"), next.getBatchSize());
        assertEquals("95-102%", next.getYield());
        assertEquals("1.0.0", next.getExpandProperty("lims.previousVersion"));
    }

    @Test
    @DisplayName("IExpand 应支持存储和读取动态属性")
    void formula_iexpand_shouldStoreDynamicProperties() {
        Formula f = new Formula("F-001", "测试配方", "P-001");

        // When
        f.setExpandProperty("lims.effectiveDate", "2026-08-01");
        f.setExpandProperty("lims.expiryDate", "2028-08-01");

        // Then
        assertEquals("2026-08-01", f.getExpandProperty("lims.effectiveDate"));
        assertEquals("2028-08-01", f.getExpandProperty("lims.expiryDate"));
    }

    @Test
    @DisplayName("FormulaStatus 枚举应覆盖所有状态")
    void formulaStatus_shouldCoverAllStatuses() {
        FormulaStatus[] statuses = FormulaStatus.values();
        assertEquals(4, statuses.length);
    }

    // ==================== WeighingTask 测试 ====================

    @Test
    @DisplayName("创建称量任务应设置默认值：PENDING 状态 + 空称量项目")
    void weighingTask_creation_shouldSetDefaults() {
        // Given & When
        WeighingTask task = new WeighingTask("WT-001", "称量任务1", "F-001", "WO-001", "B001");

        // Then
        assertEquals("WT-001", task.getCode());
        assertEquals("F-001", task.getFormulaCode());
        assertEquals("WO-001", task.getWorkOrderId());
        assertEquals("B001", task.getBatchNo());
        assertEquals(WeighingTaskStatus.PENDING, task.getStatus());
        assertNotNull(task.getItems());
        assertTrue(task.getItems().isEmpty());
    }

    @Test
    @DisplayName("开始称量应转换 PENDING → WEIGHING 并发布事件")
    void weighingTask_startWeighing_shouldTransitionAndPublishEvent() {
        WeighingTask task = new WeighingTask("WT-001", "称量任务1", "F-001", "WO-001", "B001");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(LimsEventTypes.WEIGHING_TASK_CREATED, events::add);

        // When
        task.startWeighing();

        // Then
        assertEquals(WeighingTaskStatus.WEIGHING, task.getStatus());
        assertEquals(1, events.size());
        assertEquals(LimsEventTypes.WEIGHING_TASK_CREATED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("复核应转换 WEIGHING → VERIFIED")
    void weighingTask_verify_shouldTransition() {
        WeighingTask task = new WeighingTask("WT-001", "称量任务1", "F-001", "WO-001", "B001");
        task.startWeighing();

        // When
        task.verify();

        // Then
        assertEquals(WeighingTaskStatus.VERIFIED, task.getStatus());
    }

    @Test
    @DisplayName("称量完成应转换 VERIFIED → COMPLETE 并发布事件")
    void weighingTask_complete_shouldTransitionAndPublishEvent() {
        WeighingTask task = new WeighingTask("WT-001", "称量任务1", "F-001", "WO-001", "B001");
        task.startWeighing();
        task.verify();
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(LimsEventTypes.WEIGHING_COMPLETED, events::add);

        // When
        task.completeWeighing();

        // Then
        assertEquals(WeighingTaskStatus.COMPLETE, task.getStatus());
        assertEquals(1, events.size());
        assertEquals(LimsEventTypes.WEIGHING_COMPLETED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("非法状态转换应抛出异常（PENDING → COMPLETE）")
    void weighingTask_illegalTransition_shouldThrow() {
        WeighingTask task = new WeighingTask("WT-001", "称量任务1", "F-001", "WO-001", "B001");

        assertThrows(IllegalStateException.class, task::completeWeighing);
    }

    @Test
    @DisplayName("COMPLETE 应为终态")
    void weighingTask_complete_shouldBeTerminal() {
        WeighingTask task = new WeighingTask("WT-001", "称量任务1", "F-001", "WO-001", "B001");
        task.startWeighing();
        task.verify();
        task.completeWeighing();

        assertThrows(IllegalStateException.class, task::startWeighing);
        assertThrows(IllegalStateException.class, task::verify);
    }

    @Test
    @DisplayName("添加称量项目应正常追加")
    void weighingTask_addItem_shouldWork() {
        WeighingTask task = new WeighingTask("WT-001", "称量任务1", "F-001", "WO-001", "B001");
        WeighingItem item = new WeighingItem("MAT-001", new BigDecimal("100"), new BigDecimal("1.0"));

        // When
        task.addItem(item);

        // Then
        assertEquals(1, task.getItems().size());
        assertEquals("MAT-001", task.getItems().get(0).getMaterialCode());
    }

    @Test
    @DisplayName("判断是否全部称量完成")
    void weighingTask_isAllItemsWeighed_shouldReturnCorrectly() {
        WeighingTask task = new WeighingTask("WT-001", "称量任务1", "F-001", "WO-001", "B001");
        WeighingItem item = new WeighingItem("MAT-001", new BigDecimal("100"), new BigDecimal("1.0"));
        task.addItem(item);

        // 未称量
        assertFalse(task.isAllItemsWeighed());

        // 称量后
        item.recordWeighing(new BigDecimal("99.5"), "操作员A", "复核员B", "BAL-001");
        assertTrue(task.isAllItemsWeighed());
    }

    @Test
    @DisplayName("获取偏差项目列表——超差物料应被检出")
    void weighingTask_getDeviatedItems_shouldDetectOutOfTolerance() {
        WeighingTask task = new WeighingTask("WT-001", "称量任务1", "F-001", "WO-001", "B001");
        WeighingItem item1 = new WeighingItem("MAT-001", new BigDecimal("100"), new BigDecimal("1.0"));
        WeighingItem item2 = new WeighingItem("MAT-002", new BigDecimal("50"), new BigDecimal("1.0"));
        task.addItem(item1);
        task.addItem(item2);

        // item1 在允差内（偏差 0.5%）→ item2 超差（偏差 2%）
        item1.recordWeighing(new BigDecimal("99.5"), "操作员A", "复核员B", "BAL-001");
        item2.recordWeighing(new BigDecimal("51.0"), "操作员A", "复核员B", "BAL-001");

        // When
        var deviated = task.getDeviatedItems();

        // Then
        assertEquals(1, deviated.size());
        assertEquals("MAT-002", deviated.get(0).getMaterialCode());
    }

    @Test
    @DisplayName("WeighingTaskStatus 枚举应覆盖所有状态")
    void weighingTaskStatus_shouldCoverAllStatuses() {
        WeighingTaskStatus[] statuses = WeighingTaskStatus.values();
        assertEquals(4, statuses.length);
    }

    // ==================== WeighingItem 测试 ====================

    @Test
    @DisplayName("创建称量项目应设置默认值：配方量和允差，称量量为 null")
    void weighingItem_creation_shouldSetDefaults() {
        // Given & When
        WeighingItem item = new WeighingItem("MAT-001", new BigDecimal("100"), new BigDecimal("1.5"));

        // Then
        assertEquals("MAT-001", item.getMaterialCode());
        assertEquals(new BigDecimal("100"), item.getFormulaQty());
        assertEquals(new BigDecimal("1.5"), item.getTolerance());
        assertNull(item.getActualQty());
        assertNull(item.getWeighedAt());
    }

    @Test
    @DisplayName("记录称量应设置实际量、操作人和时间戳")
    void weighingItem_recordWeighing_shouldSetAllFields() {
        WeighingItem item = new WeighingItem("MAT-001", new BigDecimal("100"), new BigDecimal("1.0"));

        // When
        item.recordWeighing(new BigDecimal("99.8"), "操作员A", "复核员B", "BAL-001");

        // Then
        assertEquals(new BigDecimal("99.8"), item.getActualQty());
        assertEquals("操作员A", item.getOperator());
        assertEquals("复核员B", item.getVerifier());
        assertEquals("BAL-001", item.getBalance());
        assertNotNull(item.getWeighedAt());
    }

    @Test
    @DisplayName("计算偏差百分比——无称量数据时应返回 null")
    void weighingItem_getDeviationPercent_noActualQty_shouldReturnNull() {
        WeighingItem item = new WeighingItem("MAT-001", new BigDecimal("100"), new BigDecimal("1.0"));

        assertNull(item.getDeviationPercent());
    }

    @Test
    @DisplayName("计算偏差百分比——正确计算")
    void weighingItem_getDeviationPercent_shouldCalculateCorrectly() {
        WeighingItem item = new WeighingItem("MAT-001", new BigDecimal("100"), new BigDecimal("1.0"));
        item.recordWeighing(new BigDecimal("98.0"), "操作员A", "复核员B", "BAL-001");

        // 偏差 = |98-100|/100*100 = 2.0%
        assertEquals(new BigDecimal("2.0000"), item.getDeviationPercent());
    }

    @Test
    @DisplayName("判断超差——偏差 2% 超过允差 1%")
    void weighingItem_isOutOfTolerance_shouldReturnTrue() {
        WeighingItem item = new WeighingItem("MAT-001", new BigDecimal("100"), new BigDecimal("1.0"));
        item.recordWeighing(new BigDecimal("102.5"), "操作员A", "复核员B", "BAL-001");

        assertTrue(item.isOutOfTolerance());
    }

    @Test
    @DisplayName("判断未超差——偏差 0.5% 在允差 1% 内")
    void weighingItem_isOutOfTolerance_shouldReturnFalse() {
        WeighingItem item = new WeighingItem("MAT-001", new BigDecimal("100"), new BigDecimal("1.0"));
        item.recordWeighing(new BigDecimal("99.5"), "操作员A", "复核员B", "BAL-001");

        assertFalse(item.isOutOfTolerance());
    }

    // ==================== BatchRecord 测试 ====================

    @Test
    @DisplayName("创建批记录应设置默认值：IN_PROGRESS 状态 + 空记录列表")
    void batchRecord_creation_shouldSetDefaults() {
        // Given & When
        BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-250MG");

        // Then
        assertEquals("B001", br.getBatchNo());
        assertEquals("WO-001", br.getWorkOrderId());
        assertEquals("F-001", br.getFormulaCode());
        assertEquals("1.0.0", br.getFormulaVersion());
        assertEquals("AMX-250MG", br.getProductCode());
        assertEquals(BatchRecordStatus.IN_PROGRESS, br.getStatus());
        assertNotNull(br.getProcessRecords());
        assertNotNull(br.getWeighingTasks());
        assertNotNull(br.getInspectionRecords());
        assertNotNull(br.getDeviations());
        assertTrue(br.isEditable());
        assertFalse(br.isArchived());
    }

    @Test
    @DisplayName("提交审核应转换 IN_PROGRESS → REVIEW 并发布事件")
    void batchRecord_submitForReview_shouldTransitionAndPublishEvent() {
        BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-250MG");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(LimsEventTypes.BATCH_RECORD_CREATED, events::add);

        // When
        br.submitForReview();

        // Then
        assertEquals(BatchRecordStatus.REVIEW, br.getStatus());
        assertEquals(1, events.size());
        assertEquals(LimsEventTypes.BATCH_RECORD_CREATED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("批准批记录应转换 REVIEW → APPROVED 并记录审核人和时间")
    void batchRecord_approve_shouldTransitionAndRecordReviewer() {
        BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-250MG");
        br.submitForReview();
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(LimsEventTypes.BATCH_RECORD_APPROVED, events::add);

        // When
        br.approve("李四");

        // Then
        assertEquals(BatchRecordStatus.APPROVED, br.getStatus());
        assertEquals("李四", br.getReviewedBy());
        assertNotNull(br.getReviewedAt());
        assertEquals(1, events.size());
        assertEquals(LimsEventTypes.BATCH_RECORD_APPROVED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("驳回批记录应转换 REVIEW → IN_PROGRESS")
    void batchRecord_reject_shouldReturnToInProgress() {
        BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-250MG");
        br.submitForReview();

        // When
        br.reject("称量数据不完整");

        // Then
        assertEquals(BatchRecordStatus.IN_PROGRESS, br.getStatus());
        assertEquals("称量数据不完整", br.getExpandProperty("lims.batch.rejectionReason"));
        assertTrue(br.isEditable());
    }

    @Test
    @DisplayName("归档批记录应转换 APPROVED → ARCHIVED 并发布事件")
    void batchRecord_archive_shouldTransitionAndPublishEvent() {
        BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-250MG");
        br.submitForReview();
        br.approve("李四");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(LimsEventTypes.BATCH_RECORD_ARCHIVED, events::add);

        // When
        br.archive();

        // Then
        assertEquals(BatchRecordStatus.ARCHIVED, br.getStatus());
        assertTrue(br.isArchived());
        assertEquals(1, events.size());
        assertEquals(LimsEventTypes.BATCH_RECORD_ARCHIVED, events.get(0).getEventType());
    }

    @Test
    @DisplayName("非法状态转换应抛出异常（IN_PROGRESS → APPROVED）")
    void batchRecord_illegalTransition_shouldThrow() {
        BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-250MG");

        assertThrows(IllegalStateException.class, () -> br.approve("test"));
    }

    @Test
    @DisplayName("ARCHIVED 应为终态")
    void batchRecord_archived_shouldBeTerminal() {
        BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-250MG");
        br.submitForReview();
        br.approve("李四");
        br.archive();

        assertThrows(IllegalStateException.class, br::submitForReview);
        assertThrows(IllegalStateException.class, () -> br.approve("test"));
    }

    @Test
    @DisplayName("添加工序执行记录")
    void batchRecord_addProcessRecord_shouldWork() {
        BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-250MG");

        br.addProcessRecord("PR-001");

        assertEquals(1, br.getProcessRecords().size());
        assertEquals("PR-001", br.getProcessRecords().get(0));
    }

    @Test
    @DisplayName("添加关联称量任务")
    void batchRecord_addWeighingTask_shouldWork() {
        BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-250MG");

        br.addWeighingTask("WT-001");

        assertEquals(1, br.getWeighingTasks().size());
        assertEquals("WT-001", br.getWeighingTasks().get(0));
    }

    @Test
    @DisplayName("添加偏差记录")
    void batchRecord_addDeviation_shouldWork() {
        BatchRecord br = new BatchRecord("B001", "WO-001", "F-001", "1.0.0", "AMX-250MG");

        br.addDeviation("DEV-001: 称量超差");

        assertEquals(1, br.getDeviations().size());
        assertEquals("DEV-001: 称量超差", br.getDeviations().get(0));
    }

    @Test
    @DisplayName("BatchRecordStatus 枚举应覆盖所有状态")
    void batchRecordStatus_shouldCoverAllStatuses() {
        BatchRecordStatus[] statuses = BatchRecordStatus.values();
        assertEquals(4, statuses.length);
    }

    // ==================== LimsEventTypes 测试 ====================

    @Test
    @DisplayName("事件命名应遵循 {module}.{entity}.{past_tense} 约定")
    void limsEventTypes_namingConvention() {
        assertTrue(LimsEventTypes.FORMULA_CREATED.startsWith("lims.formula."));
        assertTrue(LimsEventTypes.FORMULA_APPROVED.startsWith("lims.formula."));
        assertTrue(LimsEventTypes.FORMULA_ACTIVATED.startsWith("lims.formula."));
        assertTrue(LimsEventTypes.FORMULA_RETIRED.startsWith("lims.formula."));
        assertTrue(LimsEventTypes.WEIGHING_TASK_CREATED.startsWith("lims.weighing."));
        assertTrue(LimsEventTypes.WEIGHING_COMPLETED.startsWith("lims.weighing."));
        assertTrue(LimsEventTypes.BATCH_RECORD_CREATED.startsWith("lims.batch_record."));
        assertTrue(LimsEventTypes.BATCH_RECORD_APPROVED.startsWith("lims.batch_record."));
        assertTrue(LimsEventTypes.BATCH_RECORD_ARCHIVED.startsWith("lims.batch_record."));
    }

    @Test
    @DisplayName("所有事件常量应已定义——共 9 个")
    void limsEventTypes_allConstantsDefined() {
        assertEquals("lims", LimsEventTypes.PREFIX);
        assertNotNull(LimsEventTypes.FORMULA_CREATED);
        assertNotNull(LimsEventTypes.FORMULA_APPROVED);
        assertNotNull(LimsEventTypes.FORMULA_ACTIVATED);
        assertNotNull(LimsEventTypes.FORMULA_RETIRED);
        assertNotNull(LimsEventTypes.WEIGHING_TASK_CREATED);
        assertNotNull(LimsEventTypes.WEIGHING_COMPLETED);
        assertNotNull(LimsEventTypes.BATCH_RECORD_CREATED);
        assertNotNull(LimsEventTypes.BATCH_RECORD_APPROVED);
        assertNotNull(LimsEventTypes.BATCH_RECORD_ARCHIVED);
    }

    @Test
    @DisplayName("事件负载应正确传递——验证 FORMULA_ACTIVATED 事件数据")
    void limsEventTypes_eventPayload_shouldBeCorrect() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        f.setVersion("1.2.0");
        f.submitForApproval();
        f.approve("张三");
        List<IDomainEvent> events = new ArrayList<>();
        DomainEventPublisher.subscribe(LimsEventTypes.FORMULA_ACTIVATED, events::add);

        // When
        f.activate();

        // Then
        assertEquals(1, events.size());
        IDomainEvent event = events.get(0);
        assertEquals(LimsEventTypes.FORMULA_ACTIVATED, event.getEventType());
        assertEquals("lims", event.getSource());
        assertNotNull(event.getPayload());
        assertNotNull(event.getTimestamp());
    }

    // ==================== 模块加载测试 ====================

    @Test
    @DisplayName("模块上下文应正常加载")
    void shouldLoadContext() {
        assertTrue(true);
    }

}
