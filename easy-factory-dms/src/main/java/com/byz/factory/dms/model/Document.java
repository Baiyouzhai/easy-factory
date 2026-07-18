package com.byz.factory.dms.model;

import com.byz.factory.batch.DocumentStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 文档 — 继承 BaseLifecycleEntity 获得状态机（DRAFT→UNDER_REVIEW→APPROVED/REJECTED→OBSOLETE）。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Document extends BaseLifecycleEntity<DocumentStatus> {

    private String title;
    private String category;
    private String author;
    private LocalDate effectiveDate;
    private int reviewCycleMonths;
    private LocalDate nextReviewDate;
    private String content;

    public Document(String code, String title, String category) {
        super(code, title, DocumentStatus.DRAFT);
        this.title = title;
        this.category = category;
    }
}
