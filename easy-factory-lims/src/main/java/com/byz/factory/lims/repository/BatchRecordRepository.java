package com.byz.factory.lims.repository;

import com.byz.factory.lims.BatchRecordStatus;
import com.byz.factory.lims.model.BatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchRecordRepository extends JpaRepository<BatchRecord, Long> {
    BatchRecord findByBatchNo(String batchNo);
    List<BatchRecord> findByWorkOrderId(String workOrderId);
    List<BatchRecord> findByStatus(BatchRecordStatus status);
}
