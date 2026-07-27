package com.byz.factory.equip.repository;

import com.byz.factory.equip.model.OEMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * OEE 指标 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface OEMetricsRepository extends JpaRepository<OEMetrics, Long> {

    /** 按设备编码 + 周期范围查 OEE 历史 */
    List<OEMetrics> findByEquipmentCodeAndPeriodStartBetweenOrderByPeriodStartDesc(
            String equipmentCode, LocalDate start, LocalDate end);

    /** 查某设备最近一次 OEE */
    Optional<OEMetrics> findFirstByEquipmentCodeOrderByPeriodEndDesc(String equipmentCode);

}
