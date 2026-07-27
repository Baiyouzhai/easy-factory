package com.byz.factory.qms.repository;

import com.byz.factory.qms.model.InspectionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionPlanRepository extends JpaRepository<InspectionPlan, Long> {
    InspectionPlan findByCode(String code);
    List<InspectionPlan> findByProductCode(String productCode);
    InspectionPlan findByProductCodeAndProcessCode(String productCode, String processCode);
}
