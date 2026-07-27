package com.byz.factory.bi.repository;

import com.byz.factory.bi.model.KpiSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * KPI 快照 Repository。
 * <p>
 * 提供 KPI 快照的持久化查询能力。
 * BI 通过此 Repository 管理定期计算的 KPI 指标快照。
 *
 * @author 苏政
 */
@Repository
public interface KpiSnapshotRepository extends JpaRepository<KpiSnapshot, Long> {

    /** 按快照编码（code）查询 */
    KpiSnapshot findByCode(String code);

    /** 按工厂编码和周期查询唯一快照 */
    KpiSnapshot findByFactoryCodeAndPeriod(String factoryCode, String period);

    /** 按工厂编码查询所有快照，按周期降序排列 */
    List<KpiSnapshot> findByFactoryCodeOrderByPeriodDesc(String factoryCode);

    /** 查询指定工厂的最新 N 条快照（原生 SQL 保证按周期正确排序） */
    @Query(value = "SELECT * FROM kpi_snapshot WHERE factory_code = :factoryCode ORDER BY period DESC LIMIT :limit",
            nativeQuery = true)
    List<KpiSnapshot> findLatestByFactoryCode(@Param("factoryCode") String factoryCode,
                                               @Param("limit") int limit);

    /** 查询指定工厂的最新快照 */
    Optional<KpiSnapshot> findTopByFactoryCodeOrderByPeriodDesc(String factoryCode);

    /** 删除指定工厂和周期的快照（用于重新计算） */
    void deleteByFactoryCodeAndPeriod(String factoryCode, String period);

}
