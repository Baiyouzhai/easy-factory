package com.byz.factory.scm.repository;

import com.byz.factory.scm.SupplierStatus;
import com.byz.factory.scm.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 供应商 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /** 按编码查找 */
    Optional<Supplier> findByCode(String code);

    /** 按类别查找 */
    List<Supplier> findByCategory(String category);

    /** 按运营状态查找 */
    List<Supplier> findByStatus(SupplierStatus status);

    /** 按资质状态查找 */
    List<Supplier> findByQualification(String qualification);

    /** 按资质和运营状态组合查找 */
    List<Supplier> findByQualificationAndStatus(String qualification, SupplierStatus status);

}
