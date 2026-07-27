package com.byz.factory.qms.service.impl;

import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.QmsEventTypes;
import com.byz.factory.qms.model.InspectionOrder;
import com.byz.factory.qms.model.InspectionPlan;
import com.byz.factory.qms.model.InspectionRecord;
import com.byz.factory.qms.repository.InspectionOrderRepository;
import com.byz.factory.qms.repository.InspectionPlanRepository;
import com.byz.factory.qms.service.InspectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class InspectionServiceImpl implements InspectionService {

    private final InspectionPlanRepository planRepo;
    private final InspectionOrderRepository orderRepo;

    public InspectionServiceImpl(InspectionPlanRepository planRepo, InspectionOrderRepository orderRepo) {
        this.planRepo = planRepo;
        this.orderRepo = orderRepo;
    }

    // ── 检验方案管理 ──

    @Override @Transactional
    public InspectionPlan createPlan(String code, String name, String productCode,
                                      String processCode, String inspectionType) {
        var plan = new InspectionPlan(code, name, productCode, processCode,
                com.byz.factory.batch.InspectionType.valueOf(inspectionType));
        return planRepo.save(plan);
    }

    @Override @Transactional(readOnly = true)
    public List<InspectionPlan> findPlansByProduct(String productCode) {
        return planRepo.findByProductCode(productCode);
    }

    @Override @Transactional(readOnly = true)
    public InspectionPlan findPlanByProductAndProcess(String productCode, String processCode) {
        return planRepo.findByProductCodeAndProcessCode(productCode, processCode);
    }

    @Override @Transactional(readOnly = true)
    public InspectionPlan getPlan(String planCode) {
        return planRepo.findByCode(planCode);
    }

    // ── 检验指令管理 ──

    @Override @Transactional
    public InspectionOrder createOrder(String workOrderNo, String batchNo,
                                        String processCode, String planCode) {
        var order = new InspectionOrder("INSP-" + batchNo + "-" + processCode, batchNo, processCode);
        order.setWorkOrderNo(workOrderNo);
        order.setPlanCode(planCode);
        var saved = orderRepo.save(order);
        publish(QmsEventTypes.INSPECTION_ORDER_CREATED, Map.of(
                "inspectionNo", saved.getInspectionNo(), "workOrderNo", workOrderNo,
                "processCode", processCode, "planCode", planCode));
        return saved;
    }

    @Override @Transactional
    public InspectionOrder startInspection(String inspectionNo, String inspector) {
        var order = orderRepo.findByInspectionNo(inspectionNo);
        order.startInspection(inspector);
        return orderRepo.save(order);
    }

    @Override @Transactional
    public InspectionOrder submitResult(String inspectionNo, InspectionRecord record) {
        var order = orderRepo.findByInspectionNo(inspectionNo);
        order.submitResult(record);
        return orderRepo.save(order);
    }

    @Override @Transactional
    public InspectionOrder completeInspection(String inspectionNo) {
        var order = orderRepo.findByInspectionNo(inspectionNo);
        order.completeInspection();
        return orderRepo.save(order);
    }

    @Override @Transactional
    public InspectionOrder closeOrder(String inspectionNo) {
        var order = orderRepo.findByInspectionNo(inspectionNo);
        order.close();
        return orderRepo.save(order);
    }

    @Override @Transactional(readOnly = true)
    public InspectionOrder findOrder(String inspectionNo) {
        return orderRepo.findByInspectionNo(inspectionNo);
    }

    @Override @Transactional(readOnly = true)
    public List<InspectionOrder> findOrdersByWorkOrder(String workOrderNo) {
        return orderRepo.findByWorkOrderNo(workOrderNo);
    }

    @Override @Transactional(readOnly = true)
    public List<InspectionRecord> getRecords(String inspectionNo) {
        var order = orderRepo.findByInspectionNo(inspectionNo);
        return order != null ? order.getRecords() : List.of();
    }

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "qms", payload));
    }
}
