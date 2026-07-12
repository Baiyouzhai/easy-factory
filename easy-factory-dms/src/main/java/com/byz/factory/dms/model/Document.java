package com.byz.factory.dms.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class Document extends DataExpand {
    private String code;
    private String title;
    private String category;         // SOP/BATCH_RECORD/INSPECTION/DEVIATION/CAPA/VALIDATION/CALIBRATION
    private String version;
    private String status;           // DRAFT/UNDER_REVIEW/APPROVED/EFFECTIVE/OBSOLETED
    private String author;
    private LocalDate effectiveDate;
    private int reviewCycleMonths;
    private LocalDate nextReviewDate;
    private String content;          // 文档内容或附件ID

    public Document(String code, String title, String category) {
        this.code = code; this.title = title; this.category = category;
        this.version = "1.0"; this.status = "DRAFT";
    }
}
