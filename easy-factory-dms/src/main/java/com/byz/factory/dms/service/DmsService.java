package com.byz.factory.dms.service;

import com.byz.factory.dms.model.Document;

public interface DmsService {
    Document create(String code, String title, String category);
    void submitForReview(String documentCode);
    void approve(String documentCode, String approver, String comment);
    void obsolete(String documentCode);
    void recordAudit(String entityType, String entityId, String action, String operator, String before, String after);
}
