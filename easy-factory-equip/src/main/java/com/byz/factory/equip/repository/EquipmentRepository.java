package com.byz.factory.equip.repository;

import com.byz.factory.equip.MachineStatus;
import com.byz.factory.equip.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设备台账 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    /** 按设备编码查找 */
    Equipment findByCode(String code);

    /** 按运行状态查找 */
    List<Equipment> findByStatus(MachineStatus status);

    /** 按类别查找 */
    List<Equipment> findByCategory(String category);

    /** 按安装位置查找 */
    List<Equipment> findByLocation(String location);

}
