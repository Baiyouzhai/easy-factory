package com.byz.factory.plm.repository;

import com.byz.factory.factory.BlueprintStatus;
import com.byz.factory.plm.model.Blueprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 蓝图 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface BlueprintRepository extends JpaRepository<Blueprint, Long> {

    /** 按编码查找 */
    Optional<Blueprint> findByCode(String code);

    /** 按产品编码查找所有版本 */
    List<Blueprint> findByProductCodeOrderByVersionDesc(String productCode);

    /** 按产品编码和状态查找 */
    List<Blueprint> findByProductCodeAndStatus(String productCode, BlueprintStatus status);

    /** 按状态查找 */
    List<Blueprint> findByStatus(BlueprintStatus status);

    /** 按作者查找 */
    List<Blueprint> findByAuthor(String author);

    /** 按产品编码查找已发布的最新版本 */
    Optional<Blueprint> findTopByProductCodeAndStatusOrderByVersionDesc(
            String productCode, BlueprintStatus status);

    /** 按产品编码和版本精确查找 */
    Optional<Blueprint> findByProductCodeAndVersion(String productCode, String version);

    /** 按状态计数 */
    long countByStatus(BlueprintStatus status);

    /** 按产品编码和状态计数 */
    long countByProductCodeAndStatus(String productCode, BlueprintStatus status);

    /** 搜索（按名称或描述模糊匹配） */
    @Query("SELECT b FROM Blueprint b WHERE b.name ILIKE %:keyword% OR b.description ILIKE %:keyword%")
    List<Blueprint> search(@Param("keyword") String keyword);
}
