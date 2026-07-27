package com.byz.factory.dms.service.impl;

import com.byz.factory.dms.DocumentCategory;
import com.byz.factory.dms.DocumentStatus;
import com.byz.factory.dms.model.ApprovalStep;
import com.byz.factory.dms.model.ApprovalStep.ApprovalDecision;
import com.byz.factory.dms.model.ApprovalWorkflow;
import com.byz.factory.dms.model.Document;
import com.byz.factory.dms.repository.ApprovalWorkflowRepository;
import com.byz.factory.dms.repository.DocumentRepository;
import com.byz.factory.dms.service.DmsService;
import com.byz.factory.event.AuditTrail;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.DmsEventTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DMS 文档管理服务实现。
 * <p>
 * 约定：
 * <ul>
 *   <li>写操作 @Transactional</li>
 *   <li>读操作 @Transactional(readOnly = true)</li>
 *   <li>Model 层内嵌事件发布（历史遗留），ServiceImpl 不重复发布</li>
 *   <li>构造器注入 Repository</li>
 * </ul>
 *
 * @author easy-factory
 */
@Service
public class DmsServiceImpl implements DmsService {

    private final DocumentRepository documentRepo;
    private final ApprovalWorkflowRepository workflowRepo;

    // 内存审计日志（生产环境应持久化到数据库）
    private final List<AuditTrail> auditTrails = new ArrayList<>();

    public DmsServiceImpl(DocumentRepository documentRepo,
                           ApprovalWorkflowRepository workflowRepo) {
        this.documentRepo = documentRepo;
        this.workflowRepo = workflowRepo;
    }

    // ── 文档 CRUD ──

    @Override
    @Transactional
    public Document createDraft(String code, String title, DocumentCategory category, String author) {
        Document doc = new Document(code, title, category);
        doc.setAuthor(author);
        Document saved = documentRepo.save(doc);
        recordAudit("Document", code, "CREATE", author, null, "DRAFT", "新建文档草稿");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Document> getDocument(String documentCode) {
        return documentRepo.findByDocumentCode(documentCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> listDocuments(DocumentCategory category) {
        return documentRepo.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> searchDocuments(String keyword) {
        return documentRepo.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    @Transactional
    public void updateContent(String documentCode, String content) {
        Document doc = documentRepo.findByDocumentCode(documentCode)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentCode));
        if (!doc.isEditable()) {
            throw new IllegalStateException("文档 " + documentCode + " 当前状态不可编辑: " + doc.getStatus());
        }
        doc.setContent(content);
        doc.markUpdated();
        documentRepo.save(doc);
        recordAudit("Document", documentCode, "UPDATE", doc.getAuthor(),
                null, null, "更新文档内容");
    }

    // ── 审批生命周期 ──

    @Override
    @Transactional
    public void submitForReview(String documentCode) {
        Document doc = documentRepo.findByDocumentCode(documentCode)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentCode));
        doc.submitForReview();  // 内含 transition + event
        documentRepo.save(doc);
        recordAudit("Document", documentCode, "SUBMIT", doc.getAuthor(),
                DocumentStatus.DRAFT.name(), DocumentStatus.UNDER_REVIEW.name(), "提交审批");
    }

    @Override
    @Transactional
    public void approve(String documentCode, String approvedBy, String comment) {
        Document doc = documentRepo.findByDocumentCode(documentCode)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentCode));
        doc.approve(approvedBy);  // 内含 transition + event
        documentRepo.save(doc);
        recordAudit("Document", documentCode, "APPROVE", approvedBy,
                DocumentStatus.UNDER_REVIEW.name(), DocumentStatus.APPROVED.name(), comment);
    }

    @Override
    @Transactional
    public void reject(String documentCode, String reason) {
        Document doc = documentRepo.findByDocumentCode(documentCode)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentCode));
        doc.reject(reason);  // 内含 transition + event
        documentRepo.save(doc);
        recordAudit("Document", documentCode, "REJECT", doc.getAuthor(),
                DocumentStatus.UNDER_REVIEW.name(), DocumentStatus.REJECTED.name(), reason);
    }

    @Override
    @Transactional
    public void makeEffective(String documentCode, LocalDate effectiveDate) {
        Document doc = documentRepo.findByDocumentCode(documentCode)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentCode));
        doc.makeEffective(effectiveDate);  // 内含 transition + event
        documentRepo.save(doc);
        recordAudit("Document", documentCode, "EFFECTIVE", doc.getApprovedBy(),
                DocumentStatus.APPROVED.name(), DocumentStatus.EFFECTIVE.name(),
                "生效日期: " + effectiveDate);
    }

    @Override
    @Transactional
    public void obsolete(String documentCode, String reason) {
        Document doc = documentRepo.findByDocumentCode(documentCode)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentCode));
        doc.obsolete(reason);  // 内含 transition + event
        documentRepo.save(doc);
        recordAudit("Document", documentCode, "OBSOLETE", "system",
                null, DocumentStatus.OBSOLETE.name(), reason);
    }

    @Override
    @Transactional
    public Document supersede(String documentCode, String reason) {
        Document doc = documentRepo.findByDocumentCode(documentCode)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentCode));
        Document next = doc.supersede(reason);  // 内含 obsolete + version bump + event
        documentRepo.save(doc);      // 保存旧版（已作废）
        Document savedNext = documentRepo.save(next);  // 保存新版（草稿）
        return savedNext;
    }

    // ── 审批流管理 ──

    @Override
    @Transactional
    public ApprovalWorkflow createApprovalWorkflow(String documentCode, String initiator) {
        ApprovalWorkflow wf = new ApprovalWorkflow(documentCode, initiator);
        ApprovalWorkflow saved = workflowRepo.save(wf);
        recordAudit("ApprovalWorkflow", saved.getCode(), "CREATE", initiator,
                null, null, "创建审批流: " + documentCode);
        return saved;
    }

    @Override
    @Transactional
    public ApprovalStep addApprovalStep(String workflowCode, String approverRole) {
        ApprovalWorkflow wf = workflowRepo.findByDocumentCode(workflowCode)
                .orElseThrow(() -> new IllegalArgumentException("审批流不存在: " + workflowCode));
        ApprovalStep step = wf.addStep(approverRole);
        workflowRepo.save(wf);
        return step;
    }

    @Override
    @Transactional
    public Optional<ApprovalStep> executeApprovalStep(String workflowCode, String approver,
                                                       ApprovalDecision decision, String comment) {
        ApprovalWorkflow wf = workflowRepo.findByDocumentCode(workflowCode)
                .orElseThrow(() -> new IllegalArgumentException("审批流不存在: " + workflowCode));
        Optional<ApprovalStep> result = wf.executeCurrentStep(approver, decision, comment);
        workflowRepo.save(wf);
        result.ifPresent(step -> {
            DomainEventPublisher.publish(IDomainEvent.of(
                    DmsEventTypes.APPROVAL_STEP_COMPLETED, "dms",
                    Map.of("documentCode", workflowCode,
                            "stepNo", step.getStepNo(),
                            "decision", decision.name())));
            recordAudit("ApprovalWorkflow", workflowCode, "STEP_COMPLETED", approver,
                    null, decision.name(), comment);
        });
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApprovalWorkflow> getApprovalWorkflow(String documentCode) {
        return workflowRepo.findByDocumentCode(documentCode);
    }

    // ── 审计追踪 ──

    @Override
    public AuditTrail recordAudit(String entityType, String entityId, String action,
                                   String operator, String before, String after, String reason) {
        AuditTrail trail = new AuditTrail(entityType, entityId, action, operator, Instant.now(),
                before, after, reason);
        auditTrails.add(trail);
        return trail;
    }

    @Override
    public List<AuditTrail> getAuditTrails(String entityType, String entityId) {
        return auditTrails.stream()
                .filter(t -> t.entityType().equals(entityType) && t.entityId().equals(entityId))
                .toList();
    }

    // ── 合规 ──

    @Override
    public void signDocument(String documentCode, String signerId, String meaning, String reason) {
        Document doc = documentRepo.findByDocumentCode(documentCode)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentCode));
        recordAudit("Document", documentCode, "SIGN", signerId, null, meaning, reason);
    }

}
