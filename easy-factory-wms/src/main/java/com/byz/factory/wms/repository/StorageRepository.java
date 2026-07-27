package com.byz.factory.wms.repository;

import com.byz.factory.wms.StorageType;
import com.byz.factory.wms.model.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 库位 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    /** 按库位编码查找 */
    Storage findByCode(String code);

    /** 按仓库查找 */
    List<Storage> findByWarehouse(String warehouse);

    /** 按仓库+区域查找 */
    List<Storage> findByWarehouseAndZone(String warehouse, String zone);

    /** 按存储类型查找 */
    List<Storage> findByStorageType(StorageType storageType);

}
