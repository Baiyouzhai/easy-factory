package com.byz.factory.equip.repository;

import com.byz.factory.equip.model.EquipmentParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设备工艺参数 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface EquipmentParameterRepository extends JpaRepository<EquipmentParameter, Long> {

    /** 按设备编码 + 参数编码查找 */
    EquipmentParameter findByEquipmentCodeAndParamCode(String equipmentCode, String paramCode);

    /** 按设备编码查所有参数 */
    List<EquipmentParameter> findByEquipmentCode(String equipmentCode);

}
