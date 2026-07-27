package com.byz.factory.scm.repository;

import com.byz.factory.scm.PurchaseOrderStatus;
import com.byz.factory.scm.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 采购订单 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    /** 按采购单号查找 */
    Optional<PurchaseOrder> findByPoNo(String poNo);

    /** 按供应商编码查找 */
    List<PurchaseOrder> findBySupplierCode(String supplierCode);

    /** 按状态查找 */
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    /** 按审批人查找 */
    List<PurchaseOrder> findByApprovedBy(String approvedBy);

}
