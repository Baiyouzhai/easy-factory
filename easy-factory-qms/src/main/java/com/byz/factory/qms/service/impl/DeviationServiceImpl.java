package com.byz.factory.qms.service.impl;

import com.byz.factory.batch.DeviationDisposition;
import com.byz.factory.batch.DeviationSeverity;
import com.byz.factory.batch.DeviationStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.QmsEventTypes;
import com.byz.factory.qms.model.Deviation;
import com.byz.factory.qms.repository.DeviationRepository;
import com.byz.factory.qms.service.DeviationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class DeviationServiceImpl implements DeviationService {

    private final DeviationRepository deviationRepo;

    public DeviationServiceImpl(DeviationRepository deviationRepo) {
        this.deviationRepo = deviationRepo;
    }

    @Override @Transactional
    public Deviation createDeviation(String code, String name, String source,
                                      String inspectionNo, DeviationSeverity severity) {
        var dev = new Deviation(code, name, source, inspectionNo, severity);
        var saved = deviationRepo.save(dev);
        publish(QmsEventTypes.DEVIATION_CREATED, Map.of(
                "deviationCode", code, "inspectionNo", inspectionNo,
                "severity", severity.name(), "source", source));
        return saved;
    }

    @Override @Transactional
    public Deviation startInvestigation(String deviationCode, String investigator) {
        var dev = deviationRepo.findByCode(deviationCode);
        dev.startInvestigation(investigator);
        return deviationRepo.save(dev);
    }

    @Override @Transactional
    public Deviation completeInvestigation(String deviationCode, String rootCause,
                                            String productImpact) {
        var dev = deviationRepo.findByCode(deviationCode);
        dev.completeInvestigation(rootCause, productImpact);
        return deviationRepo.save(dev);
    }

    @Override @Transactional
    public Deviation dispose(String deviationCode, DeviationDisposition disposition,
                              String dispositionNote, String disposedBy) {
        var dev = deviationRepo.findByCode(deviationCode);
        dev.dispose(disposition, dispositionNote, disposedBy);
        return deviationRepo.save(dev);
    }

    @Override @Transactional
    public Deviation linkCapa(String deviationCode, String capaCode) {
        var dev = deviationRepo.findByCode(deviationCode);
        dev.linkCapa(capaCode);
        return deviationRepo.save(dev);
    }

    @Override @Transactional
    public Deviation resolve(String deviationCode) {
        var dev = deviationRepo.findByCode(deviationCode);
        dev.resolve();
        return deviationRepo.save(dev);
    }

    @Override @Transactional
    public Deviation close(String deviationCode) {
        var dev = deviationRepo.findByCode(deviationCode);
        dev.close();
        return deviationRepo.save(dev);
    }

    @Override @Transactional
    public Deviation cancel(String deviationCode, String reason) {
        var dev = deviationRepo.findByCode(deviationCode);
        dev.cancel(reason);
        return deviationRepo.save(dev);
    }

    @Override @Transactional(readOnly = true)
    public Deviation findDeviation(String deviationCode) {
        return deviationRepo.findByCode(deviationCode);
    }

    @Override @Transactional(readOnly = true)
    public List<Deviation> findDeviationsByInspection(String inspectionNo) {
        return deviationRepo.findByInspectionNo(inspectionNo);
    }

    @Override @Transactional(readOnly = true)
    public List<Deviation> findDeviationsByWorkOrder(String workOrderNo) {
        return deviationRepo.findByWorkOrderNo(workOrderNo);
    }

    @Override @Transactional(readOnly = true)
    public List<Deviation> getActiveDeviations() {
        return deviationRepo.findByStatusNotIn(
                List.of(DeviationStatus.CLOSED, DeviationStatus.CANCELLED));
    }

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "qms", payload));
    }
}
