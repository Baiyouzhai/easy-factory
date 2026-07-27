package com.byz.factory.erp.repository;

import com.byz.factory.erp.TransactionStatus;
import com.byz.factory.erp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 事务回传记录 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** 按事务编号查找 */
    Transaction findByCode(String code);

    /** 按状态查找 */
    List<Transaction> findByStatus(TransactionStatus status);

    /** 按物料编码查找 */
    List<Transaction> findByMaterialCode(String materialCode);

    /** 按参考单据查找 */
    List<Transaction> findByReferenceDoc(String referenceDoc);

}
