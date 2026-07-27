package com.byz.factory.lims.service.impl;

import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.LimsEventTypes;
import com.byz.factory.lims.model.BatchRecord;
import com.byz.factory.lims.repository.BatchRecordRepository;
import com.byz.factory.lims.service.BatchRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BatchRecordServiceImpl implements BatchRecordService {

    private final BatchRecordRepository batchRepo;

    public BatchRecordServiceImpl(BatchRecordRepository batchRepo) { this.batchRepo = batchRepo; }

    @Override @Transactional
    public BatchRecord create(String batchNo, String workOrderId, String formulaCode,
                               String formulaVersion, String productCode) {
        BatchRecord br = new BatchRecord(batchNo, workOrderId, formulaCode, formulaVersion, productCode);
        BatchRecord saved = batchRepo.save(br);
        publish(LimsEventTypes.BATCH_RECORD_CREATED, Map.of("batchNo", batchNo, "workOrderId", workOrderId));
        return saved;
    }

    @Override @Transactional(readOnly = true)
    public BatchRecord getByBatchNo(String batchNo) { return batchRepo.findByBatchNo(batchNo); }

    @Override @Transactional(readOnly = true)
    public List<BatchRecord> listByWorkOrder(String workOrderId) { return batchRepo.findByWorkOrderId(workOrderId); }

    @Override @Transactional
    public void submitForReview(String batchNo) {
        BatchRecord br = batchRepo.findByBatchNo(batchNo);
        br.submitForReview();
        batchRepo.save(br);
    }

    @Override @Transactional
    public void approve(String batchNo, String reviewedBy) {
        BatchRecord br = batchRepo.findByBatchNo(batchNo);
        br.approve(reviewedBy);
        batchRepo.save(br);
    }

    @Override @Transactional
    public void reject(String batchNo, String reason) {
        BatchRecord br = batchRepo.findByBatchNo(batchNo);
        br.reject(reason);
        batchRepo.save(br);
    }

    @Override @Transactional
    public void archive(String batchNo) {
        BatchRecord br = batchRepo.findByBatchNo(batchNo);
        br.archive();
        batchRepo.save(br);
    }

    @Override @Transactional
    public void addDeviation(String batchNo, String deviation) {
        BatchRecord br = batchRepo.findByBatchNo(batchNo);
        br.addDeviation(deviation);
        batchRepo.save(br);
    }

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "lims", payload));
    }
}
