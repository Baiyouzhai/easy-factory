package com.byz.factory.lims.service.impl;

import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.LimsEventTypes;
import com.byz.factory.lims.model.WeighingItem;
import com.byz.factory.lims.model.WeighingTask;
import com.byz.factory.lims.repository.WeighingTaskRepository;
import com.byz.factory.lims.service.WeighingTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class WeighingTaskServiceImpl implements WeighingTaskService {

    private final WeighingTaskRepository taskRepo;

    public WeighingTaskServiceImpl(WeighingTaskRepository taskRepo) { this.taskRepo = taskRepo; }

    @Override @Transactional
    public WeighingTask create(String code, String name, String formulaCode, String workOrderId, String batchNo) {
        WeighingTask t = new WeighingTask(code, name, formulaCode, workOrderId, batchNo);
        WeighingTask saved = taskRepo.save(t);
        publish(LimsEventTypes.WEIGHING_TASK_CREATED, Map.of("taskCode", code, "formulaCode", formulaCode));
        return saved;
    }

    @Override @Transactional(readOnly = true)
    public WeighingTask getByCode(String code) { return taskRepo.findByCode(code); }

    @Override @Transactional(readOnly = true)
    public List<WeighingTask> listByWorkOrder(String workOrderId) { return taskRepo.findByWorkOrderId(workOrderId); }

    @Override @Transactional
    public void startWeighing(String taskCode) {
        WeighingTask t = taskRepo.findByCode(taskCode);
        t.startWeighing();
        taskRepo.save(t);
    }

    @Override @Transactional
    public WeighingItem recordWeighingItem(String taskCode, String materialCode, BigDecimal actualQty,
                                            String operator, String verifier, String balance) {
        WeighingTask t = taskRepo.findByCode(taskCode);
        WeighingItem item = t.getItems().stream()
                .filter(i -> i.getMaterialCode().equals(materialCode))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("物料不存在: " + materialCode));
        item.recordWeighing(actualQty, operator, verifier, balance);
        taskRepo.save(t);
        return item;
    }

    @Override @Transactional
    public void verify(String taskCode) {
        WeighingTask t = taskRepo.findByCode(taskCode);
        t.verify();
        taskRepo.save(t);
    }

    @Override @Transactional
    public void complete(String taskCode) {
        WeighingTask t = taskRepo.findByCode(taskCode);
        t.completeWeighing();
        taskRepo.save(t);
    }

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "lims", payload));
    }
}
