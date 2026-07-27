package com.byz.factory.scm.repository;

import com.byz.factory.scm.InboundPlanStatus;
import com.byz.factory.scm.model.InboundPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 来料计划 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface InboundPlanRepository extends JpaRepository<InboundPlan, Long> {

    /** 按编码查找 */
    Optional<InboundPlan> findByCode(String code);

    /** 按关联采购订单号查找 */
    Optional<InboundPlan> findByPoNo(String poNo);

    /** 按供应商查找 */
    List<InboundPlan> findBySupplierCode(String supplierCode);

    /** 按状态查找 */
    List<InboundPlan> findByStatus(InboundPlanStatus status);

    /** 按是否已通知仓库查找 */
    List<InboundPlan> findByNotifyWarehouse(boolean notifyWarehouse);

}
