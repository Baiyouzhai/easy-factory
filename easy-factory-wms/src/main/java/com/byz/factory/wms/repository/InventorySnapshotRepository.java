package com.byz.factory.wms.repository;

import com.byz.factory.wms.MaterialStatus;
import com.byz.factory.wms.model.InventorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 库存快照 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface InventorySnapshotRepository extends JpaRepository<InventorySnapshot, Long> {

    /** 按编码查找 */
    InventorySnapshot findByCode(String code);

    /** 按物料+批次+库位查找（唯一维度组合） */
    InventorySnapshot findByMaterialCodeAndBatchNoAndLocationCode(
            String materialCode, String batchNo, String locationCode);

    /** 按物料+批次查找所有库位 */
    List<InventorySnapshot> findByMaterialCodeAndBatchNo(String materialCode, String batchNo);

    /** 按物料查找所有库位 */
    List<InventorySnapshot> findByMaterialCode(String materialCode);

    /** 按库位查找 */
    List<InventorySnapshot> findByLocationCode(String locationCode);

    /** 按质量状态查找 */
    List<InventorySnapshot> findByStatus(MaterialStatus status);

    /** 查询物料可用库存汇总（所有库位） */
    @Query("SELECT COALESCE(SUM(s.onHandQty - s.allocatedQty), 0) FROM InventorySnapshot s " +
           "WHERE s.materialCode = :materialCode AND s.batchNo = :batchNo AND s.status = 'RELEASED'")
    java.math.BigDecimal sumAvailableStock(String materialCode, String batchNo);

}
