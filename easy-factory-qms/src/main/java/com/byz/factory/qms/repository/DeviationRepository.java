package com.byz.factory.qms.repository;

import com.byz.factory.batch.DeviationStatus;
import com.byz.factory.qms.model.Deviation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviationRepository extends JpaRepository<Deviation, Long> {
    Deviation findByCode(String code);
    List<Deviation> findByInspectionNo(String inspectionNo);
    List<Deviation> findByWorkOrderNo(String workOrderNo);
    List<Deviation> findByStatusNotIn(List<DeviationStatus> statuses);
    List<Deviation> findByStatus(DeviationStatus status);
}
