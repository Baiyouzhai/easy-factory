package com.byz.factory.lims.repository;

import com.byz.factory.lims.FormulaStatus;
import com.byz.factory.lims.model.Formula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormulaRepository extends JpaRepository<Formula, Long> {
    Formula findByCode(String code);
    List<Formula> findByProductCode(String productCode);
    List<Formula> findByStatus(FormulaStatus status);
    Formula findByProductCodeAndVersion(String productCode, String version);
}
