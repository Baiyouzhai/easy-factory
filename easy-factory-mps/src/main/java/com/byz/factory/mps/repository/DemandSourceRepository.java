package com.byz.factory.mps.repository;

import com.byz.factory.mps.model.DemandSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 需求来源 Repository — JPA 数据访问层。
 *
 * @author 苏政
 */
@Repository
public interface DemandSourceRepository extends JpaRepository<DemandSource, Long> {

    /** 按来源单据号查询 */
    DemandSource findByReferenceNo(String referenceNo);

    /** 按产品编码查询 */
    List<DemandSource> findByProductCode(String productCode);

    /** 按来源类型查询 */
    List<DemandSource> findBySourceType(String sourceType);

    /** 按产品编码和来源类型查询 */
    List<DemandSource> findByProductCodeAndSourceType(String productCode, String sourceType);

}
