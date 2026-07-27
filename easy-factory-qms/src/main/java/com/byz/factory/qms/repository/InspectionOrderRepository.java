package com.byz.factory.qms.repository;

import com.byz.factory.batch.InspectionStatus;
import com.byz.factory.qms.model.InspectionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionOrderRepository extends JpaRepository<InspectionOrder, Long> {
    InspectionOrder findByInspectionNo(String inspectionNo);
    List<InspectionOrder> findByWorkOrderNo(String workOrderNo);
    List<InspectionOrder> findByStatus(InspectionStatus status);
    List<InspectionOrder> findByBatchNo(String batchNo);
}
