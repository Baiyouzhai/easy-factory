package com.byz.factory.batch;

import java.time.LocalDate;

/**
 * 文档抽象 — DMS 模块的核心实体接口，代表 GMP 受控文档。
 * <p>
 * 其他模块（LIMS/QMS/PLM/EAM）可通过此接口编译期引用文档，
 * 无需依赖 easy-factory-dms。
 * <p>
 * DMS 管理 SOP、批记录、检验报告、偏差报告、CAPA 报告、验证文件、校准证书
 * 等 GxP 文件的版本控制、审批流和审计追踪。
 *
 * @author 苏政
 * @see DocumentStatus
 * @see DocumentCategory
 */
public interface IDocument {

    /** 文档编号（业务标识） */
    String getDocumentCode();

    /** 文档标题 */
    String getTitle();

    /** 文档类别 */
    DocumentCategory getCategory();

    /** 文档状态 */
    DocumentStatus getStatus();

    /** 版本号 */
    String getVersion();

    /** 作者 */
    String getAuthor();

    /** 生效日期 */
    LocalDate getEffectiveDate();

    /** 复审周期（月） */
    int getReviewCycleMonths();

    /** 下次复审日期 */
    LocalDate getNextReviewDate();

    /** 文档内容/摘要 */
    String getContent();

}
