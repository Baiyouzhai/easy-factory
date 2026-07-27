package com.byz.factory.crm.repository;

import com.byz.factory.crm.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** 按编码查找 */
    Optional<Customer> findByCode(String code);

    /** 按行业查找 */
    List<Customer> findByIndustry(String industry);

    /** 按 GMP 审计状态查找 */
    List<Customer> findByGmpAuditStatus(String gmpAuditStatus);

}
