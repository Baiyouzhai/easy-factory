package com.byz.factory.lims.repository;

import com.byz.factory.lims.FormulaStatus;
import com.byz.factory.lims.LimsTestConfig;
import com.byz.factory.lims.model.Formula;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = LimsTestConfig.class)
@DisplayName("LIMS Repository 集成测试")
class LimsRepositoryTest {

    @Autowired
    private FormulaRepository formulaRepo;

    @BeforeEach
    void setUp() {
        formulaRepo.deleteAll();
    }

    @Test
    @DisplayName("保存并查询配方 — findByCode")
    void saveAndFindByCode() {
        Formula f = new Formula("F-001", "测试配方", "P-001");
        f.setBatchSize(new BigDecimal("1000"));
        formulaRepo.save(f);

        Formula found = formulaRepo.findByCode("F-001");

        assertNotNull(found);
        assertEquals("测试配方", found.getName());
        assertEquals(FormulaStatus.DRAFT, found.getStatus());
        assertEquals(new BigDecimal("1000"), found.getBatchSize());
    }

    @Test
    @DisplayName("按产品编码查找配方")
    void findByProductCode() {
        formulaRepo.save(new Formula("F-001", "配方A", "P-001"));
        formulaRepo.save(new Formula("F-002", "配方B", "P-001"));
        formulaRepo.save(new Formula("F-003", "配方C", "P-002"));

        var results = formulaRepo.findByProductCode("P-001");
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("按状态查找配方")
    void findByStatus() {
        Formula f1 = new Formula("F-001", "配方A", "P-001");
        f1.submitForApproval();
        formulaRepo.save(f1);
        Formula f2 = new Formula("F-002", "配方B", "P-002");
        formulaRepo.save(f2);

        var approved = formulaRepo.findByStatus(FormulaStatus.APPROVED);
        var drafts = formulaRepo.findByStatus(FormulaStatus.DRAFT);

        assertEquals(1, approved.size());
        assertEquals(1, drafts.size());
    }

    @Test
    @DisplayName("按产品和版本查找配方")
    void findByProductCodeAndVersion() {
        Formula f = new Formula("F-001", "配方A", "P-001");
        f.setVersion("1.0.0");
        formulaRepo.save(f);

        Formula found = formulaRepo.findByProductCodeAndVersion("P-001", "1.0.0");
        assertNotNull(found);
        assertEquals("F-001", found.getCode());
    }

    @Test
    @DisplayName("查询不存在的配方返回 null")
    void findNonExistent() {
        assertNull(formulaRepo.findByCode("NONEXISTENT"));
    }
}
