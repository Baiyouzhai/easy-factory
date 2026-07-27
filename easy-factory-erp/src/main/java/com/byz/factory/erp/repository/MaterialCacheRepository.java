package com.byz.factory.erp.repository;

import com.byz.factory.erp.model.MaterialCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 物料主数据缓存 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface MaterialCacheRepository extends JpaRepository<MaterialCache, Long> {

    /** 按物料编码查找（code 即 ERP 物料编码） */
    Optional<MaterialCache> findByCode(String code);

    /** 按来源系统统计 */
    long countBySourceSystem(String sourceSystem);

}
