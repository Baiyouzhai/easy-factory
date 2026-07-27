package com.byz.factory.qms.service.impl;

import com.byz.factory.batch.CapaStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.QmsEventTypes;
import com.byz.factory.qms.model.Capa;
import com.byz.factory.qms.repository.CapaRepository;
import com.byz.factory.qms.service.CapaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CapaServiceImpl implements CapaService {

    private final CapaRepository capaRepo;

    public CapaServiceImpl(CapaRepository capaRepo) {
        this.capaRepo = capaRepo;
    }

    @Override @Transactional
    public Capa createCapa(String code, String name, String deviationCode,
                            String problemDescription, String assignTo) {
        var capa = new Capa(code, name, deviationCode, problemDescription, assignTo);
        var saved = capaRepo.save(capa);
        publish(QmsEventTypes.CAPA_CREATED, Map.of(
                "capaCode", code, "deviationCode", deviationCode,
                "assignTo", assignTo));
        return saved;
    }

    @Override @Transactional
    public Capa analyzeRootCause(String capaCode, String rootCause) {
        var capa = capaRepo.findByCode(capaCode);
        capa.analyzeRootCause(rootCause);
        return capaRepo.save(capa);
    }

    @Override @Transactional
    public Capa executeActions(String capaCode, String correctiveAction,
                                String preventiveAction) {
        var capa = capaRepo.findByCode(capaCode);
        capa.executeActions(correctiveAction, preventiveAction);
        return capaRepo.save(capa);
    }

    @Override @Transactional
    public Capa verify(String capaCode, String verification, String approvedBy) {
        var capa = capaRepo.findByCode(capaCode);
        capa.verify(verification, approvedBy);
        return capaRepo.save(capa);
    }

    @Override @Transactional
    public Capa close(String capaCode) {
        var capa = capaRepo.findByCode(capaCode);
        capa.close();
        return capaRepo.save(capa);
    }

    @Override @Transactional
    public Capa cancel(String capaCode, String reason) {
        var capa = capaRepo.findByCode(capaCode);
        capa.cancel(reason);
        return capaRepo.save(capa);
    }

    @Override @Transactional(readOnly = true)
    public Capa findCapa(String capaCode) {
        return capaRepo.findByCode(capaCode);
    }

    @Override @Transactional(readOnly = true)
    public List<Capa> findCapasByDeviation(String deviationCode) {
        return capaRepo.findByDeviationCode(deviationCode);
    }

    @Override @Transactional(readOnly = true)
    public List<Capa> getOverdueCapas() {
        var active = capaRepo.findByStatusNotIn(
                List.of(CapaStatus.CLOSED, CapaStatus.CANCELLED));
        return active.stream().filter(Capa::isOverdue).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<Capa> getActiveCapas() {
        return capaRepo.findByStatusNotIn(
                List.of(CapaStatus.CLOSED, CapaStatus.CANCELLED));
    }

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "qms", payload));
    }
}
