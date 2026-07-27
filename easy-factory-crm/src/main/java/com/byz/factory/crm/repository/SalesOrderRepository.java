package com.byz.factory.crm.repository;

import com.byz.factory.crm.SalesOrderStatus;
import com.byz.factory.crm.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 销售订单 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    /** 按订单号查找 */
    Optional<SalesOrder> findByOrderNo(String orderNo);

    /** 按客户编码查找 */
    List<SalesOrder> findByCustomerCode(String customerCode);

    /** 按状态查找 */
    List<SalesOrder> findByStatus(SalesOrderStatus status);

    /** 按客户编码和状态查找 */
    List<SalesOrder> findByCustomerCodeAndStatus(String customerCode, SalesOrderStatus status);

}
