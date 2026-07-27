package com.byz.factory.andon.repository;

import com.byz.factory.batch.AndonStatus;
import com.byz.factory.andon.model.AndonCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 安灯呼叫 Repository。
 *
 * @author 苏政
 */
@Repository
public interface AndonCallRepository extends JpaRepository<AndonCall, Long> {

    /** 按呼叫编号（code）查询 */
    AndonCall findByCode(String code);

    /** 按工单号查询 */
    List<AndonCall> findByWorkOrderNo(String workOrderNo);

    /** 按设备编码查询 */
    List<AndonCall> findByEquipmentCode(String equipmentCode);

    /** 按状态查询 */
    List<AndonCall> findByStatus(AndonStatus status);

    /** 按触发类型查询 */
    List<AndonCall> findByTriggerType(String triggerType);

}
