package com.byz.factory.qms.repository;

import com.byz.factory.batch.CapaStatus;
import com.byz.factory.qms.model.Capa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapaRepository extends JpaRepository<Capa, Long> {
    Capa findByCode(String code);
    List<Capa> findByDeviationCode(String deviationCode);
    List<Capa> findByStatusNotIn(List<CapaStatus> statuses);
    List<Capa> findByStatus(CapaStatus status);
    List<Capa> findByAssignTo(String assignTo);
}
