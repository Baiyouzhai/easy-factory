package com.byz.factory.dms.service;

import com.byz.factory.dms.DocumentCategory;
import com.byz.factory.dms.model.ApprovalStep;
import com.byz.factory.dms.model.ApprovalWorkflow;
import com.byz.factory.dms.model.Document;
import com.byz.factory.event.AuditTrail;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DMS 文档管理服务 — 文档全生命周期、审批流、审计追踪的统一切入点。
 *
 * @author 苏政
 */
public interface DmsService {

    // ── 文档 CRUD ──

    /** 创建草稿文档 */
    Document createDraft(String code, String title, DocumentCategory category, String author);

    /** 根据编码查询文档 */
    Optional<Document> getDocument(String documentCode);

    /** 根据类别列出文档 */
    List<Document> listDocuments(DocumentCategory category);

    /** 搜索文档（按标题或编码模糊匹配） */
    List<Document> searchDocuments(String keyword);

    /** 更新文档内容（仅 DRAFT/REJECTED 状态可编辑） */
    void updateContent(String documentCode, String content);

    // ── 审批生命周期 ──

    /** 提交审批 */
    void submitForReview(String documentCode);

    /** 批准文档 */
    void approve(String documentCode, String approvedBy, String comment);

    /** 驳回文档（退回 DRAFT 供修改） */
    void reject(String documentCode, String reason);

    /** 使文档生效 */
    void makeEffective(String documentCode, LocalDate effectiveDate);

    /** 作废文档 */
    void obsolete(String documentCode, String reason);

    /** 升级版本（作废旧版 + 创建新版草稿） */
    Document supersede(String documentCode, String reason);

    // ── 审批流管理 ──

    /** 为文档创建审批流 */
    ApprovalWorkflow createApprovalWorkflow(String documentCode, String initiator);

    /** 向审批流添加审批步骤 */
    ApprovalStep addApprovalStep(String workflowId, String approverRole);

    /** 执行当前待审批步骤 */
    Optional<ApprovalStep> executeApprovalStep(String workflowId, String approver,
                                                ApprovalStep.ApprovalDecision decision, String comment);

    /** 查询文档的审批流 */
    Optional<ApprovalWorkflow> getApprovalWorkflow(String documentCode);

    // ── 审计追踪 ──

    /** 记录审计追踪 */
    AuditTrail recordAudit(String entityType, String entityId, String action, String operator,
                           String before, String after, String reason);

    /** 查询实体的审计追踪历史 */
    List<AuditTrail> getAuditTrails(String entityType, String entityId);

    // ── 合规 ──

    /** 记录电子签名 */
    void signDocument(String documentCode, String signerId, String meaning, String reason);

}
