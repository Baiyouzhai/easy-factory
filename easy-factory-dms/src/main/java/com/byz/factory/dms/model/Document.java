package com.byz.factory.dms.model;

import com.byz.factory.batch.DocumentCategory;
import com.byz.factory.batch.DocumentStatus;
import com.byz.factory.batch.IDocument;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.DmsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import com.byz.factory.shared.IVersionStrategy;
import com.byz.factory.shared.IncrementalVersionStrategy;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Map;

/**
 * 文档 — DMS 核心实体，GMP 受控文档的完整生命周期管理。
 * <p>
 * 继承 BaseLifecycleEntity 获得状态机（DRAFT→UNDER_REVIEW→APPROVED/REJECTED→EFFECTIVE→OBSOLETED），
 * 实现 IDocument 供 LIMS/QMS/PLM/EAM 等下游模块编译期引用。
 * <p>
 * 状态变更时自动通过 {@link DomainEventPublisher} 发布领域事件，
 * 通知 LIMS/QMS/PLM/EAM 等订阅模块。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   dms.version           — 版本号
 *   dms.category          — 文档类别
 *   dms.author            — 作者
 *   dms.approvedBy        — 审批人
 *   dms.effectiveDate     — 生效日期
 *   dms.reviewCycle       — 复审周期（月）
 *   dms.nextReviewDate    — 下次复审日期
 *   dms.obsoleteReason    — 作废原因
 *   dms.rejectionReason   — 驳回原因
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Document extends BaseLifecycleEntity<DocumentStatus> implements IDocument {

    /** 文档编号（业务标识） */
    private String documentCode;

    /** 文档标题 */
    private String title;

    /** 文档类别 */
    private DocumentCategory category;

    /** 版本号（整数递增：1 → 2 → 3 …） */
    private String version;

    /** 作者 */
    private String author;

    /** 审批人 */
    private String approvedBy;

    /** 生效日期 */
    private LocalDate effectiveDate;

    /** 复审周期（月） */
    private int reviewCycleMonths;

    /** 下次复审日期 */
    private LocalDate nextReviewDate;

    /** 文档内容/摘要 */
    private String content;

    /** 版本递增策略（DMS 默认使用简单整数递增） */
    private IVersionStrategy versionStrategy;

    /**
     * @param documentCode 文档编号
     * @param title        文档标题
     * @param category     文档类别
     */
    public Document(String documentCode, String title, DocumentCategory category) {
        super(documentCode, title, DocumentStatus.DRAFT);
        this.documentCode = documentCode;
        this.title = title;
        this.category = category;
        this.versionStrategy = new IncrementalVersionStrategy();
        this.version = versionStrategy.initialVersion();
    }

    // ==================== 版本管理 ====================

    /**
     * 递增版本号（使用当前策略）。
     *
     * @return 新版本号
     */
    public String bumpVersion() {
        this.version = versionStrategy.nextVersion(this.version);
        markUpdated();
        return this.version;
    }

    /**
     * 基于当前文档创建新版本草稿。
     * <p>
     * 当前文档作废后，通过此方法创建新版本文档（保留原始文档编号，从 DRAFT 重新开始）。
     *
     * @param newVersion 新版本号
     * @return 新版本草稿
     */
    public Document createNewVersion(String newVersion) {
        Document next = new Document(this.documentCode, this.title, this.category);
        next.setVersion(newVersion);
        next.setAuthor(this.author);
        next.setContent(this.content);
        next.setReviewCycleMonths(this.reviewCycleMonths);
        next.setVersionStrategy(this.versionStrategy);
        // 在扩展属性中记录来源
        next.setExpandProperty("dms.previousVersion", this.version);
        return next;
    }

    // ==================== 业务便捷方法（含事件发布） ====================

    /**
     * 提交审批（DRAFT → UNDER_REVIEW）。
     */
    public void submitForReview() {
        transition(DocumentStatus.UNDER_REVIEW);
        markUpdated();
        publishEvent(DmsEventTypes.DOCUMENT_SUBMITTED, Map.of(
                "documentCode", documentCode,
                "category", category.name(),
                "version", version));
    }

    /**
     * 批准文档（UNDER_REVIEW → APPROVED）。
     *
     * @param approvedBy 审批人
     */
    public void approve(String approvedBy) {
        this.approvedBy = approvedBy;
        transition(DocumentStatus.APPROVED);
        markUpdated();
        publishEvent(DmsEventTypes.DOCUMENT_APPROVED, Map.of(
                "documentCode", documentCode,
                "version", version,
                "approvedBy", approvedBy));
    }

    /**
     * 驳回评审（UNDER_REVIEW → REJECTED）。
     *
     * @param reason 驳回原因
     */
    public void reject(String reason) {
        transition(DocumentStatus.REJECTED);
        markUpdated();
        setExpandProperty("dms.rejectionReason", reason);
        publishEvent(DmsEventTypes.DOCUMENT_REJECTED, Map.of(
                "documentCode", documentCode,
                "version", version,
                "reason", reason));
    }

    /**
     * 重新提交（REJECTED → DRAFT，供修改后再次提审）。
     */
    public void resubmit() {
        transition(DocumentStatus.DRAFT);
        markUpdated();
    }

    /**
     * 文档生效（APPROVED → EFFECTIVE）。
     * <p>
     * 设置生效日期并自动计算下次复审日期。
     *
     * @param effectiveDate 生效日期
     */
    public void makeEffective(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
        if (reviewCycleMonths > 0) {
            this.nextReviewDate = effectiveDate.plusMonths(reviewCycleMonths);
        }
        transition(DocumentStatus.EFFECTIVE);
        markUpdated();
        publishEvent(DmsEventTypes.DOCUMENT_EFFECTIVE, Map.of(
                "documentCode", documentCode,
                "version", version,
                "effectiveDate", effectiveDate.toString(),
                "nextReviewDate", nextReviewDate != null ? nextReviewDate.toString() : "N/A"));
    }

    /**
     * 作废文档（任意非终态 → OBSOLETED）。
     *
     * @param reason 作废原因
     */
    public void obsolete(String reason) {
        transition(DocumentStatus.OBSOLETE);
        markUpdated();
        setExpandProperty("dms.obsoleteReason", reason);
        publishEvent(DmsEventTypes.DOCUMENT_OBSOLETED, Map.of(
                "documentCode", documentCode,
                "version", version,
                "reason", reason));
    }

    /**
     * 升级版本——当前文档作废并创建新版本。
     * <p>
     * 适用场景：文档需要内容修订时，先作废旧版本再生成新版本草稿。
     *
     * @param reason 作废原因
     * @return 新版本草稿
     */
    public Document supersede(String reason) {
        obsolete(reason);
        String newVersion = bumpVersion();
        Document next = createNewVersion(newVersion);
        publishEvent(DmsEventTypes.DOCUMENT_VERSIONED, Map.of(
                "documentCode", documentCode,
                "oldVersion", this.version,
                "newVersion", newVersion,
                "reason", reason));
        return next;
    }

    // ==================== 查询方法 ====================

    /**
     * 判断文档是否已过期（超过复审日期）。
     */
    public boolean isExpired() {
        return nextReviewDate != null && LocalDate.now().isAfter(nextReviewDate);
    }

    /**
     * 判断文档是否处于可编辑状态。
     */
    public boolean isEditable() {
        DocumentStatus s = getStatus();
        return s == DocumentStatus.DRAFT || s == DocumentStatus.REJECTED;
    }

    // ==================== 内部 ====================

    private void publishEvent(String eventType, Object payload) {
        IDomainEvent event = IDomainEvent.of(eventType, "dms", payload);
        DomainEventPublisher.publish(event);
    }

}
