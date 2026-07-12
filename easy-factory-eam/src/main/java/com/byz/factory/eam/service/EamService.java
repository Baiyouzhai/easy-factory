package com.byz.factory.eam.service;

import com.byz.factory.eam.model.Asset;

public interface EamService {
    Asset register(Asset asset);
    void createMaintenanceOrder(String assetCode, String type, String description);
    void completeMaintenance(String orderCode, double downtime, double cost);
    void recordCalibration(String assetCode, String result, String nextDueDate);
    void scrap(String assetCode);
}
