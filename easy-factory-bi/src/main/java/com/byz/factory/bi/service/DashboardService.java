package com.byz.factory.bi.service;

import com.byz.factory.bi.model.KpiSnapshot;
import java.util.Map;

public interface DashboardService {
    Map<String, Object> getProductionDashboard(String factoryCode);
    Map<String, Object> getQualityDashboard(String factoryCode);
    Map<String, Object> getOeeDashboard(String factoryCode);
    KpiSnapshot getMonthlyKpi(String period);
}
