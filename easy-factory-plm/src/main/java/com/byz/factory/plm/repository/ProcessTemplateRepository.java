package com.byz.factory.plm.repository;

import com.byz.factory.plm.model.ProcessTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工艺模板 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface ProcessTemplateRepository extends JpaRepository<ProcessTemplate, Long> {

    /** 按编码查找 */
    Optional<ProcessTemplate> findByCode(String code);

    /** 按类别查找 */
    List<ProcessTemplate> findByCategory(String category);

    /** 按状态查找 */
    List<ProcessTemplate> findByStatus(String status);

    /** 按类别和状态查找 */
    List<ProcessTemplate> findByCategoryAndStatus(String category, String status);

    /** 按状态计数 */
    long countByStatus(String status);
}
