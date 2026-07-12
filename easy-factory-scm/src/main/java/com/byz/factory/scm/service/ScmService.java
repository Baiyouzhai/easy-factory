package com.byz.factory.scm.service;

import com.byz.factory.scm.model.Supplier;

public interface ScmService {
    Supplier registerSupplier(String code, String name, String category);
    void qualifySupplier(String code);
    void disqualifySupplier(String code);
    void createPurchaseOrder(String supplierCode, String materialCode, double quantity);
}
