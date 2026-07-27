package com.byz.factory.wms.repository;

import com.byz.factory.wms.ReceiptStatus;
import com.byz.factory.wms.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 收货单 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    /** 按收货单号查找 */
    Receipt findByCode(String code);

    /** 按来源单号查找 */
    List<Receipt> findByReferenceNo(String referenceNo);

    /** 按供应商编码查找 */
    List<Receipt> findBySupplierCode(String supplierCode);

    /** 按状态查找 */
    List<Receipt> findByStatus(ReceiptStatus status);

}
