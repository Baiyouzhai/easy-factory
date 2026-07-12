package com.byz.factory.mps.service;

import com.byz.factory.mps.model.ProductionPlan;

public interface MpsService {
    ProductionPlan create(String periodType, String periodStart, String periodEnd);
    void approve(String planNo, String approvedBy);
    void release(String planNo);       // 发布到 MES
    String checkCapacity(String planNo, String factoryCode);
}
