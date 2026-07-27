package com.byz.factory.lims.repository;

import com.byz.factory.lims.WeighingTaskStatus;
import com.byz.factory.lims.model.WeighingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeighingTaskRepository extends JpaRepository<WeighingTask, Long> {
    WeighingTask findByCode(String code);
    List<WeighingTask> findByWorkOrderId(String workOrderId);
    List<WeighingTask> findByStatus(WeighingTaskStatus status);
    List<WeighingTask> findByBatchNo(String batchNo);
}
