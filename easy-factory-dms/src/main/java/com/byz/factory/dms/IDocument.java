package com.byz.factory.dms;

import java.time.LocalDate;

/**
 * 文档抽象 — DMS 内部契约接口。
 * <p>
 * DMS 模块内部使用此接口解耦模型与服务层。
 *
 * @author 苏政
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
