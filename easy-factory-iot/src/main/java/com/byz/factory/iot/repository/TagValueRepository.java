package com.byz.factory.iot.repository;

import com.byz.factory.iot.model.TagValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 标签值 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface TagValueRepository extends JpaRepository<TagValue, Long> {

    /** 按设备编码 + 标签名查找 */
    List<TagValue> findByEquipmentCodeAndTagName(String equipmentCode, String tagName);

    /** 按设备编码查找所有标签值 */
    List<TagValue> findByEquipmentCode(String equipmentCode);

    /** 按批次 ID 查找 */
    List<TagValue> findByBatchId(String batchId);

}
