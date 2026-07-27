package com.byz.factory.erp.repository;

import com.byz.factory.erp.model.InventorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 库存快照 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface InventorySnapshotRepository extends JpaRepository<InventorySnapshot, Long> {

    /** 按物料编码查询所有库位的库存快照 */
    List<InventorySnapshot> findByMaterialCode(String materialCode);

    /** 按物料+工厂查询 */
    List<InventorySnapshot> findByMaterialCodeAndPlantCode(String materialCode, String plantCode);

    /** 按物料+工厂+库位精确查找 */
    Optional<InventorySnapshot> findByMaterialCodeAndPlantCodeAndStorageLocation(
            String materialCode, String plantCode, String storageLocation);

}
