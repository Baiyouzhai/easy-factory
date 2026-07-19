package com.byz.factory.dms;

import com.byz.factory.dms.DocumentCategory;
import com.byz.factory.dms.DocumentStatus;
import com.byz.factory.dms.model.ApprovalStep;
import com.byz.factory.dms.model.ApprovalStep.ApprovalDecision;
import com.byz.factory.dms.model.ApprovalWorkflow;
import com.byz.factory.dms.model.Document;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.types.DmsEventTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DMS 模块模型构造测试")
class DmsModuleTest {

    // ==================== Document 构造 ====================

    @Test
    @DisplayName("Document 创建 — 字段正确初始化")
    void document_creation_shouldSetFields() {
        Document doc = new Document("SOP-001", "原料取样标准操作规程", DocumentCategory.SOP);

        assertEquals("SOP-001", doc.getDocumentCode());
        assertEquals("原料取样标准操作规程", doc.getTitle());
        assertEquals(DocumentCategory.SOP, doc.getCategory());
        assertEquals(DocumentStatus.DRAFT, doc.getStatus());
        assertEquals("1", doc.getVersion());
        assertEquals(0, doc.getReviewCycleMonths());
        assertNotNull(doc.getCreatedAt());
        assertNotNull(doc.getVersionStrategy());
    }

    @Test
    @DisplayName("Document 创建 — 不同类别均可创建")
    void document_creation_allCategories() {
        for (DocumentCategory cat : DocumentCategory.values()) {
            Document doc = new Document("DOC-" + cat.name(), "测试文档", cat);
            assertEquals(cat, doc.getCategory());
            assertEquals(DocumentStatus.DRAFT, doc.getStatus());
        }
    }

    // ==================== Document 状态转换（正常路径） ====================

    @Test
    @DisplayName("Document — 提交审批：DRAFT → UNDER_REVIEW")
    void document_submitForReview_shouldTransitionToUnderReview() {
        Document doc = new Document("SOP-002", "清洁验证方案", DocumentCategory.VALIDATION);

        doc.submitForReview();

        assertEquals(DocumentStatus.UNDER_REVIEW, doc.getStatus());
        assertNotNull(doc.getUpdatedAt());
    }

    @Test
    @DisplayName("Document — 批准：UNDER_REVIEW → APPROVED")
    void document_approve_shouldTransitionToApproved() {
        Document doc = new Document("SOP-003", "偏差处理规程", DocumentCategory.SOP);
        doc.submitForReview();

        doc.approve("张工");

        assertEquals(DocumentStatus.APPROVED, doc.getStatus());
        assertEquals("张工", doc.getApprovedBy());
    }

    @Test
    @DisplayName("Document — 生效：APPROVED → EFFECTIVE")
    void document_makeEffective_shouldTransitionToEffective() {
        Document doc = new Document("SOP-004", "CAPA管理规程", DocumentCategory.CAPA);
        doc.setReviewCycleMonths(12);
        doc.submitForReview();
        doc.approve("李工");

        LocalDate today = LocalDate.now();
        doc.makeEffective(today);

        assertEquals(DocumentStatus.EFFECTIVE, doc.getStatus());
        assertEquals(today, doc.getEffectiveDate());
        assertEquals(today.plusMonths(12), doc.getNextReviewDate());
    }

    @Test
    @DisplayName("Document — 生效时 reviewCycleMonths=0 则不计算下次复审日期")
    void document_makeEffective_withoutReviewCycle_shouldNotSetNextReview() {
        Document doc = new Document("BATCH-001", "批记录模板", DocumentCategory.BATCH_RECORD);
        doc.submitForReview();
        doc.approve("王工");

        doc.makeEffective(LocalDate.now());

        assertEquals(DocumentStatus.EFFECTIVE, doc.getStatus());
        assertNull(doc.getNextReviewDate());
    }

    @Test
    @DisplayName("Document — 作废：EFFECTIVE → OBSOLETE")
    void document_obsolete_shouldTransitionToObsolete() {
        Document doc = new Document("SOP-005", "待作废文档", DocumentCategory.SOP);
        doc.submitForReview();
        doc.approve("赵工");
        doc.makeEffective(LocalDate.now());

        doc.obsolete("新版已发布");

        assertEquals(DocumentStatus.OBSOLETE, doc.getStatus());
        assertEquals("新版已发布", doc.getExpandProperty("dms.obsoleteReason"));
    }

    @Test
    @DisplayName("Document — 直接作废：DRAFT → OBSOLETE")
    void document_obsolete_fromDraft_shouldTransitionToObsolete() {
        Document doc = new Document("SOP-006", "不再需要的草稿", DocumentCategory.SOP);

        doc.obsolete("需求取消");

        assertEquals(DocumentStatus.OBSOLETE, doc.getStatus());
    }

    // ==================== Document 驳回与重新提交 ====================

    @Test
    @DisplayName("Document — 驳回：UNDER_REVIEW → REJECTED → DRAFT")
    void document_rejectAndResubmit_shouldCycleCorrectly() {
        Document doc = new Document("SOP-007", "需修改的文档", DocumentCategory.SOP);
        doc.submitForReview();

        doc.reject("内容需补充风险评估章节");

        assertEquals(DocumentStatus.REJECTED, doc.getStatus());
        assertEquals("内容需补充风险评估章节", doc.getExpandProperty("dms.rejectionReason"));

        // 修改后重新提交
        doc.resubmit();
        assertEquals(DocumentStatus.DRAFT, doc.getStatus());

        // 再次提交审批
        doc.submitForReview();
        assertEquals(DocumentStatus.UNDER_REVIEW, doc.getStatus());
    }

    // ==================== Document 版本管理 ====================

    @Test
    @DisplayName("Document — 版本递增：1 → 2 → 3")
    void document_bumpVersion_shouldIncrement() {
        Document doc = new Document("SOP-008", "版本测试文档", DocumentCategory.SOP);

        assertEquals("1", doc.getVersion());

        doc.bumpVersion();
        assertEquals("2", doc.getVersion());

        doc.bumpVersion();
        assertEquals("3", doc.getVersion());
    }

    @Test
    @DisplayName("Document — 创建新版本草稿保留原始信息")
    void document_createNewVersion_shouldPreserveContent() {
        Document doc = new Document("SOP-009", "原始文档", DocumentCategory.SOP);
        doc.setAuthor("陈工");
        doc.setContent("这是原始内容");
        doc.setReviewCycleMonths(6);
        doc.bumpVersion(); // version: 1 → 2

        Document next = doc.createNewVersion("3");

        assertEquals("SOP-009", next.getDocumentCode());
        assertEquals("原始文档", next.getTitle());
        assertEquals(DocumentCategory.SOP, next.getCategory());
        assertEquals(DocumentStatus.DRAFT, next.getStatus());
        assertEquals("3", next.getVersion());
        assertEquals("陈工", next.getAuthor());
        assertEquals("这是原始内容", next.getContent());
        assertEquals(6, next.getReviewCycleMonths());
        assertEquals("2", next.getExpandProperty("dms.previousVersion"));
    }

    @Test
    @DisplayName("Document — supersede 作废旧版并创建新版")
    void document_supersede_shouldObsoleteAndCreateNewVersion() {
        Document doc = new Document("SOP-010", "待升版文档", DocumentCategory.SOP);
        doc.submitForReview();
        doc.approve("周工");
        doc.makeEffective(LocalDate.now());
        // version 当前为 "1"

        Document next = doc.supersede("内容需要大修");

        // 旧版已作废
        assertEquals(DocumentStatus.OBSOLETE, doc.getStatus());
        // 新版为草稿
        assertEquals(DocumentStatus.DRAFT, next.getStatus());
        assertEquals("2", next.getVersion());
        assertEquals("SOP-010", next.getDocumentCode());
    }

    // ==================== Document 查询方法 ====================

    @Test
    @DisplayName("Document — isExpired 判断是否过期")
    void document_isExpired_shouldReturnCorrectly() {
        Document doc = new Document("SOP-011", "复审测试", DocumentCategory.SOP);
        doc.setReviewCycleMonths(12);
        doc.submitForReview();
        doc.approve("刘工");
        doc.makeEffective(LocalDate.now().minusMonths(13)); // 13个月前生效，已过期

        assertTrue(doc.isExpired());
    }

    @Test
    @DisplayName("Document — 未到复审日期时 isExpired 返回 false")
    void document_isExpired_withFutureReviewDate_shouldReturnFalse() {
        Document doc = new Document("SOP-012", "未过期文档", DocumentCategory.SOP);
        doc.setReviewCycleMonths(12);
        doc.submitForReview();
        doc.approve("吴工");
        doc.makeEffective(LocalDate.now()); // 今天生效

        assertFalse(doc.isExpired());
    }

    @Test
    @DisplayName("Document — isEditable 仅 DRAFT/REJECTED 返回 true")
    void document_isEditable_shouldReturnCorrectly() {
        Document doc = new Document("SOP-013", "可编辑性测试", DocumentCategory.SOP);

        assertTrue(doc.isEditable()); // DRAFT

        doc.submitForReview();
        assertFalse(doc.isEditable()); // UNDER_REVIEW

        doc.approve("郑工");
        assertFalse(doc.isEditable()); // APPROVED

        doc.makeEffective(LocalDate.now());
        assertFalse(doc.isEditable()); // EFFECTIVE

        doc.obsolete("测试");
        assertFalse(doc.isEditable()); // OBSOLETE
    }

    // ==================== Document 事件发布 ====================

    @Test
    @DisplayName("Document — submitForReview 发布 DOCUMENT_SUBMITTED 事件")
    void document_submitForReview_shouldPublishEvent() {
        AtomicInteger counter = new AtomicInteger(0);
        DomainEventPublisher.subscribe(DmsEventTypes.DOCUMENT_SUBMITTED, e -> counter.incrementAndGet());

        Document doc = new Document("SOP-014", "事件测试文档", DocumentCategory.SOP);
        doc.submitForReview();

        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Document — approve 发布 DOCUMENT_APPROVED 事件")
    void document_approve_shouldPublishEvent() {
        AtomicInteger counter = new AtomicInteger(0);
        DomainEventPublisher.subscribe(DmsEventTypes.DOCUMENT_APPROVED, e -> counter.incrementAndGet());

        Document doc = new Document("SOP-015", "审批事件测试", DocumentCategory.SOP);
        doc.submitForReview();
        doc.approve("孙工");

        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Document — 全生命周期事件链：submit→approve→effective→obsolete")
    void document_fullLifecycle_shouldPublishAllEvents() {
        AtomicInteger submitted = new AtomicInteger(0);
        AtomicInteger approved = new AtomicInteger(0);
        AtomicInteger effective = new AtomicInteger(0);
        AtomicInteger obsoleted = new AtomicInteger(0);

        DomainEventPublisher.subscribe(DmsEventTypes.DOCUMENT_SUBMITTED, e -> submitted.incrementAndGet());
        DomainEventPublisher.subscribe(DmsEventTypes.DOCUMENT_APPROVED, e -> approved.incrementAndGet());
        DomainEventPublisher.subscribe(DmsEventTypes.DOCUMENT_EFFECTIVE, e -> effective.incrementAndGet());
        DomainEventPublisher.subscribe(DmsEventTypes.DOCUMENT_OBSOLETED, e -> obsoleted.incrementAndGet());

        Document doc = new Document("SOP-016", "全生命周期文档", DocumentCategory.SOP);
        doc.setReviewCycleMonths(12);
        doc.submitForReview();
        doc.approve("马工");
        doc.makeEffective(LocalDate.now());
        doc.obsolete("已过期");

        assertEquals(1, submitted.get());
        assertEquals(1, approved.get());
        assertEquals(1, effective.get());
        assertEquals(1, obsoleted.get());
    }

    // ==================== ApprovalStep ====================

    @Test
    @DisplayName("ApprovalStep 创建 — 默认 PENDING")
    void approvalStep_creation_shouldBePending() {
        ApprovalStep step = new ApprovalStep(1, "QA经理");

        assertEquals(1, step.getStepNo());
        assertEquals("QA经理", step.getApproverRole());
        assertEquals(ApprovalDecision.PENDING, step.getDecision());
        assertNull(step.getApprover());
        assertNull(step.getTimestamp());
    }

    @Test
    @DisplayName("ApprovalStep — 执行审批")
    void approvalStep_execute_shouldSetFields() {
        ApprovalStep step = new ApprovalStep(1, "QA经理");

        step.execute("张经理", ApprovalDecision.APPROVED, "内容符合要求");

        assertEquals("张经理", step.getApprover());
        assertEquals(ApprovalDecision.APPROVED, step.getDecision());
        assertEquals("内容符合要求", step.getComment());
        assertNotNull(step.getTimestamp());
    }

    @Test
    @DisplayName("ApprovalStep — 拒绝")
    void approvalStep_execute_reject() {
        ApprovalStep step = new ApprovalStep(2, "质量总监");

        step.execute("李总监", ApprovalDecision.REJECTED, "缺少风险评估");

        assertEquals(ApprovalDecision.REJECTED, step.getDecision());
    }

    @Test
    @DisplayName("ApprovalStep — 需要修订")
    void approvalStep_execute_needsRevision() {
        ApprovalStep step = new ApprovalStep(1, "生产主管");

        step.execute("王主管", ApprovalDecision.NEEDS_REVISION, "参数需要调整");

        assertEquals(ApprovalDecision.NEEDS_REVISION, step.getDecision());
    }

    // ==================== ApprovalWorkflow ====================

    @Test
    @DisplayName("ApprovalWorkflow 创建 — 字段正确初始化")
    void approvalWorkflow_creation_shouldSetFields() {
        ApprovalWorkflow wf = new ApprovalWorkflow("SOP-020", "文档管理员");

        assertEquals("SOP-020", wf.getDocumentCode());
        assertEquals("文档管理员", wf.getInitiator());
        assertNotNull(wf.getSteps());
        assertTrue(wf.getSteps().isEmpty());
        assertNull(wf.getCompletedAt());
    }

    @Test
    @DisplayName("ApprovalWorkflow — 添加审批步骤")
    void approvalWorkflow_addStep_shouldIncrementStepNo() {
        ApprovalWorkflow wf = new ApprovalWorkflow("SOP-021", "文档管理员");

        wf.addStep("QA经理");
        wf.addStep("质量总监");

        assertEquals(2, wf.getSteps().size());
        assertEquals(1, wf.getSteps().get(0).getStepNo());
        assertEquals(2, wf.getSteps().get(1).getStepNo());
    }

    @Test
    @DisplayName("ApprovalWorkflow — 单步骤审批完成")
    void approvalWorkflow_singleStepApproval_shouldComplete() {
        ApprovalWorkflow wf = new ApprovalWorkflow("SOP-022", "文档管理员");
        wf.addStep("QA经理");

        wf.executeCurrentStep("张经理", ApprovalDecision.APPROVED, "通过");

        assertTrue(wf.isCompleted());
        assertTrue(wf.isApproved());
        assertFalse(wf.isRejected());
        assertNotNull(wf.getCompletedAt());
    }

    @Test
    @DisplayName("ApprovalWorkflow — 多步骤全部通过")
    void approvalWorkflow_multiStepApproval_shouldComplete() {
        ApprovalWorkflow wf = new ApprovalWorkflow("SOP-023", "文档管理员");
        wf.addStep("QA经理");
        wf.addStep("质量总监");
        wf.addStep("工厂厂长");

        wf.executeCurrentStep("张经理", ApprovalDecision.APPROVED, "第一步通过");
        assertEquals(2, wf.getCurrentStepNo());

        wf.executeCurrentStep("李总监", ApprovalDecision.APPROVED, "第二步通过");
        assertEquals(3, wf.getCurrentStepNo());

        wf.executeCurrentStep("王厂长", ApprovalDecision.APPROVED, "最终通过");

        assertTrue(wf.isCompleted());
        assertTrue(wf.isApproved());
        assertEquals(3, wf.getTotalSteps());
        assertEquals(-1, wf.getCurrentStepNo()); // 无待审批步骤
    }

    @Test
    @DisplayName("ApprovalWorkflow — 中间步骤拒绝")
    void approvalWorkflow_midStepRejected_shouldStop() {
        ApprovalWorkflow wf = new ApprovalWorkflow("SOP-024", "文档管理员");
        wf.addStep("QA经理");
        wf.addStep("质量总监");

        wf.executeCurrentStep("张经理", ApprovalDecision.REJECTED, "内容不合格");

        assertTrue(wf.isCompleted());
        assertFalse(wf.isApproved());
        assertTrue(wf.isRejected());
    }

    @Test
    @DisplayName("ApprovalWorkflow — 空步骤不视为完成")
    void approvalWorkflow_emptySteps_shouldNotBeCompleted() {
        ApprovalWorkflow wf = new ApprovalWorkflow("SOP-025", "文档管理员");

        assertFalse(wf.isCompleted());
        assertFalse(wf.isApproved());
        assertEquals(-1, wf.getCurrentStepNo());
    }

    // ==================== DocumentStatus 状态机 ====================

    @Test
    @DisplayName("DocumentStatus — DRAFT 允许转换到 UNDER_REVIEW 和 OBSOLETE")
    void documentStatus_draftTransitions() {
        var allowed = DocumentStatus.DRAFT.allowedTransitions();
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(DocumentStatus.UNDER_REVIEW));
        assertTrue(allowed.contains(DocumentStatus.OBSOLETE));
    }

    @Test
    @DisplayName("DocumentStatus — APPROVED 允许转换到 EFFECTIVE 和 OBSOLETE")
    void documentStatus_approvedTransitions() {
        var allowed = DocumentStatus.APPROVED.allowedTransitions();
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(DocumentStatus.EFFECTIVE));
        assertTrue(allowed.contains(DocumentStatus.OBSOLETE));
    }

    @Test
    @DisplayName("DocumentStatus — OBSOLETE 为终态，无允许转换")
    void documentStatus_obsoleteTransitions() {
        assertTrue(DocumentStatus.OBSOLETE.allowedTransitions().isEmpty());
    }

    // ==================== DocumentCategory ====================

    @Test
    @DisplayName("DocumentCategory — 7 种类别全部可创建")
    void documentCategory_allSevenCategories() {
        DocumentCategory[] cats = DocumentCategory.values();
        assertEquals(7, cats.length);
        assertTrue(DocumentCategory.valueOf("SOP") == DocumentCategory.SOP);
    }

    // ==================== 事件命名约定验证 ====================

    @Test
    @DisplayName("DmsEventTypes — 事件命名遵循 {module}.{entity}.{past_tense} 约定")
    void dmsEventTypes_namingConvention() {
        assertTrue(DmsEventTypes.DOCUMENT_SUBMITTED.startsWith("dms.document."));
        assertTrue(DmsEventTypes.DOCUMENT_APPROVED.startsWith("dms.document."));
        assertTrue(DmsEventTypes.DOCUMENT_REJECTED.startsWith("dms.document."));
        assertTrue(DmsEventTypes.DOCUMENT_EFFECTIVE.startsWith("dms.document."));
        assertTrue(DmsEventTypes.DOCUMENT_OBSOLETED.startsWith("dms.document."));
        assertTrue(DmsEventTypes.DOCUMENT_EXPIRED.startsWith("dms.document."));
        assertTrue(DmsEventTypes.DOCUMENT_VERSIONED.startsWith("dms.document."));
        assertTrue(DmsEventTypes.APPROVAL_STEP_COMPLETED.startsWith("dms.approval."));
    }

    // ==================== 模块可加载验证 ====================

    @Test
    @DisplayName("DMS 模块 — 基本可加载")
    void shouldLoad() {
        assertTrue(true);
    }

}
