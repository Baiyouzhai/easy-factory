package com.byz.factory.iot.repository;

import com.byz.factory.iot.model.AlarmEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 报警事件 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface AlarmEventRepository extends JpaRepository<AlarmEvent, Long> {

    /** 按报警编码查找 */
    AlarmEvent findByCode(String code);

    /** 按设备编码查找 */
    List<AlarmEvent> findByEquipmentCode(String equipmentCode);

    /** 查找未恢复的报警（resolvedAt IS NULL） */
    @Query("SELECT a FROM AlarmEvent a WHERE a.resolvedAt IS NULL")
    List<AlarmEvent> findActiveAlarms();

    /** 按设备查找未恢复的报警 */
    @Query("SELECT a FROM AlarmEvent a WHERE a.equipmentCode = :equipmentCode AND a.resolvedAt IS NULL")
    List<AlarmEvent> findActiveAlarmsByEquipment(@Param("equipmentCode") String equipmentCode);

}
