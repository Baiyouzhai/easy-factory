package com.byz.factory.equip.repository;

import com.byz.factory.equip.EquipTestConfig;
import com.byz.factory.equip.model.OEMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = EquipTestConfig.class)
@DisplayName("OEMetricsRepository 集成测试")
class OEMetricsRepositoryTest {

    @Autowired
    private OEMetricsRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Test
    @DisplayName("保存并查询 OEE — 按设备+周期范围")
    void findByEquipmentAndPeriod() {
        // Given
        OEMetrics m1 = OEMetrics.of("EQ-001",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 7),
                new BigDecimal("0.90"), new BigDecimal("0.95"), new BigDecimal("0.98"));
        OEMetrics m2 = OEMetrics.of("EQ-001",
                LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 14),
                new BigDecimal("0.85"), new BigDecimal("0.92"), new BigDecimal("0.97"));
        repo.saveAll(List.of(m1, m2));

        // When
        List<OEMetrics> found = repo.findByEquipmentCodeAndPeriodStartBetweenOrderByPeriodStartDesc(
                "EQ-001", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 10));

        // Then
        assertFalse(found.isEmpty());
    }

    @Test
    @DisplayName("查最近一次 OEE — findFirstByEquipmentCodeOrderByPeriodEndDesc")
    void findLatestOee() {
        // Given
        OEMetrics m1 = OEMetrics.of("EQ-001",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 7),
                new BigDecimal("0.90"), new BigDecimal("0.95"), new BigDecimal("0.98"));
        OEMetrics m2 = OEMetrics.of("EQ-001",
                LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 14),
                new BigDecimal("0.85"), new BigDecimal("0.92"), new BigDecimal("0.97"));
        repo.saveAll(List.of(m1, m2));

        // When
        OEMetrics latest = repo.findFirstByEquipmentCodeOrderByPeriodEndDesc("EQ-001").orElse(null);

        // Then
        assertNotNull(latest);
        assertEquals(LocalDate.of(2026, 7, 8), latest.getPeriodStart());
    }

}
