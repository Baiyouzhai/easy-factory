package com.byz.factory.plm.repository;

import com.byz.factory.plm.model.ProcessParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工艺参数 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface ProcessParameterRepository extends JpaRepository<ProcessParameter, Long> {

    /** 按编码查找 */
    Optional<ProcessParameter> findByCode(String code);

    /** 按重要性查找 */
    List<ProcessParameter> findByImportance(ProcessParameter.Importance importance);

    /** 按控制方式查找 */
    List<ProcessParameter> findByControlMethod(ProcessParameter.ControlMethod controlMethod);

    /** 按数据类型查找 */
    List<ProcessParameter> findByDataType(ProcessParameter.DataType dataType);

    /** 按名称模糊搜索 */
    List<ProcessParameter> findByNameContainingIgnoreCase(String name);
}
