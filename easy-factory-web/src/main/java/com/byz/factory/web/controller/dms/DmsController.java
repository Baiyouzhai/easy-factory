package com.byz.factory.web.controller.dms;

import com.byz.factory.dms.DocumentCategory;
import com.byz.factory.dms.model.ApprovalStep;
import com.byz.factory.dms.model.ApprovalWorkflow;
import com.byz.factory.dms.model.Document;
import com.byz.factory.dms.service.DmsService;
import com.byz.factory.event.AuditTrail;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 文档管理 REST 控制器。
 * <p>
 * 提供文档 CRUD、审批生命周期、审批流管理和审计追踪 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/dms")
@Tag(name = "DMS — 文档管理", description = "文档 CRUD、审批流、审计追踪")
public class DmsController {

    @Autowired(required = false)
    private DmsService dmsService;

    // ── 文档 CRUD ──

    @PostMapping("/documents")
    @Operation(summary = "创建草稿文档")
    public Result<Document> createDraft(@RequestParam String code,
                                         @RequestParam String title,
                                         @RequestParam DocumentCategory category,
                                         @RequestParam String author) {
        return Result.ok(dmsService.createDraft(code, title, category, author));
    }

    @GetMapping("/documents/{documentCode}")
    @Operation(summary = "根据编码查询文档")
    public Result<Document> getDocument(@PathVariable String documentCode) {
        return Result.ok(dmsService.getDocument(documentCode).orElse(null));
    }

    @GetMapping("/documents/by-category")
    @Operation(summary = "根据类别列出文档")
    public Result<List<Document>> listDocuments(@RequestParam DocumentCategory category) {
        return Result.ok(dmsService.listDocuments(category));
    }

    @GetMapping("/documents/search")
    @Operation(summary = "搜索文档")
    public Result<List<Document>> searchDocuments(@RequestParam String keyword) {
        return Result.ok(dmsService.searchDocuments(keyword));
    }

    @PutMapping("/documents/{documentCode}/content")
    @Operation(summary = "更新文档内容")
    public Result<Void> updateContent(@PathVariable String documentCode,
                                       @RequestBody String content) {
        dmsService.updateContent(documentCode, content);
        return Result.ok();
    }

    // ── 审批生命周期 ──

    @PutMapping("/documents/{documentCode}/submit")
    @Operation(summary = "提交审批")
    public Result<Void> submitForReview(@PathVariable String documentCode) {
        dmsService.submitForReview(documentCode);
        return Result.ok();
    }

    @PutMapping("/documents/{documentCode}/approve")
    @Operation(summary = "批准文档")
    public Result<Void> approve(@PathVariable String documentCode,
                                 @RequestParam String approvedBy,
                                 @RequestParam(required = false) String comment) {
        dmsService.approve(documentCode, approvedBy, comment);
        return Result.ok();
    }

    @PutMapping("/documents/{documentCode}/reject")
    @Operation(summary = "驳回文档")
    public Result<Void> reject(@PathVariable String documentCode,
                                @RequestParam String reason) {
        dmsService.reject(documentCode, reason);
        return Result.ok();
    }

    @PutMapping("/documents/{documentCode}/make-effective")
    @Operation(summary = "使文档生效")
    public Result<Void> makeEffective(@PathVariable String documentCode,
                                       @RequestParam LocalDate effectiveDate) {
        dmsService.makeEffective(documentCode, effectiveDate);
        return Result.ok();
    }

    @PutMapping("/documents/{documentCode}/obsolete")
    @Operation(summary = "作废文档")
    public Result<Void> obsolete(@PathVariable String documentCode,
                                  @RequestParam String reason) {
        dmsService.obsolete(documentCode, reason);
        return Result.ok();
    }

    @PutMapping("/documents/{documentCode}/supersede")
    @Operation(summary = "升级版本", description = "作废旧版 + 创建新版草稿")
    public Result<Document> supersede(@PathVariable String documentCode,
                                       @RequestParam String reason) {
        return Result.ok(dmsService.supersede(documentCode, reason));
    }

    // ── 审批流管理 ──

    @PostMapping("/documents/{documentCode}/approval-workflow")
    @Operation(summary = "为文档创建审批流")
    public Result<ApprovalWorkflow> createApprovalWorkflow(@PathVariable String documentCode,
                                                             @RequestParam String initiator) {
        return Result.ok(dmsService.createApprovalWorkflow(documentCode, initiator));
    }

    @PostMapping("/approval-workflows/{workflowId}/steps")
    @Operation(summary = "添加审批步骤")
    public Result<ApprovalStep> addApprovalStep(@PathVariable String workflowId,
                                                  @RequestParam String approverRole) {
        return Result.ok(dmsService.addApprovalStep(workflowId, approverRole));
    }

    @PutMapping("/approval-workflows/{workflowId}/execute-step")
    @Operation(summary = "执行审批步骤")
    public Result<ApprovalStep> executeApprovalStep(@PathVariable String workflowId,
                                                      @RequestParam String approver,
                                                      @RequestParam ApprovalStep.ApprovalDecision decision,
                                                      @RequestParam(required = false) String comment) {
        return Result.ok(dmsService.executeApprovalStep(workflowId, approver, decision, comment).orElse(null));
    }

    @GetMapping("/documents/{documentCode}/approval-workflow")
    @Operation(summary = "查询文档的审批流")
    public Result<ApprovalWorkflow> getApprovalWorkflow(@PathVariable String documentCode) {
        return Result.ok(dmsService.getApprovalWorkflow(documentCode).orElse(null));
    }

    // ── 审计追踪 ──

    @PostMapping("/audit-trails")
    @Operation(summary = "记录审计追踪")
    public Result<AuditTrail> recordAudit(@RequestParam String entityType,
                                           @RequestParam String entityId,
                                           @RequestParam String action,
                                           @RequestParam String operator,
                                           @RequestParam(required = false) String before,
                                           @RequestParam(required = false) String after,
                                           @RequestParam(required = false) String reason) {
        return Result.ok(dmsService.recordAudit(entityType, entityId, action, operator, before, after, reason));
    }

    @GetMapping("/audit-trails")
    @Operation(summary = "查询实体的审计追踪历史")
    public Result<List<AuditTrail>> getAuditTrails(@RequestParam String entityType,
                                                     @RequestParam String entityId) {
        return Result.ok(dmsService.getAuditTrails(entityType, entityId));
    }

    // ── 合规 ──

    @PostMapping("/documents/{documentCode}/sign")
    @Operation(summary = "记录电子签名")
    public Result<Void> signDocument(@PathVariable String documentCode,
                                      @RequestParam String signerId,
                                      @RequestParam String meaning,
                                      @RequestParam String reason) {
        dmsService.signDocument(documentCode, signerId, meaning, reason);
        return Result.ok();
    }

}
