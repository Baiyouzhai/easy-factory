package com.byz.factory.wms.repository;

import com.byz.factory.wms.PickingTaskStatus;
import com.byz.factory.wms.model.PickingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 拣料任务 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface PickingTaskRepository extends JpaRepository<PickingTask, Long> {

    /** 按拣料单号查找 */
    PickingTask findByCode(String code);

    /** 按工单号查找 */
    List<PickingTask> findByWorkOrderNo(String workOrderNo);

    /** 按状态查找 */
    List<PickingTask> findByStatus(PickingTaskStatus status);

    /** 按工单号+状态查找 */
    List<PickingTask> findByWorkOrderNoAndStatus(String workOrderNo, PickingTaskStatus status);

}
