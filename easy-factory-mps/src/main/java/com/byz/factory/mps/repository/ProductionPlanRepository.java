package com.byz.factory.mps.repository;

import com.byz.factory.mps.ProductionPlanStatus;
import com.byz.factory.mps.model.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 生产计划 Repository — JPA 数据访问层。
 *
 * @author 苏政
 */
@Repository
public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {

    /** 按计划编号查询 */
    Optional<ProductionPlan> findByPlanNo(String planNo);

    /** 按周期类型查询 */
    List<ProductionPlan> findByPeriodType(String periodType);

    /** 按状态查询 */
    List<ProductionPlan> findByStatus(ProductionPlanStatus status);

    /** 按周期类型和状态查询 */
    List<ProductionPlan> findByPeriodTypeAndStatus(String periodType, ProductionPlanStatus status);

    /** 按审批人查询 */
    List<ProductionPlan> findByApprovedBy(String approvedBy);

}
