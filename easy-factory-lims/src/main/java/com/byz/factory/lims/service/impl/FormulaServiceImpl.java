package com.byz.factory.lims.service.impl;

import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.LimsEventTypes;
import com.byz.factory.lims.model.Formula;
import com.byz.factory.lims.repository.FormulaRepository;
import com.byz.factory.lims.service.FormulaService;
import com.byz.factory.resource.IResourcePack;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class FormulaServiceImpl implements FormulaService {

    private final FormulaRepository formulaRepo;

    public FormulaServiceImpl(FormulaRepository formulaRepo) {
        this.formulaRepo = formulaRepo;
    }

    @Override @Transactional
    public Formula create(String code, String name, String productCode, IResourcePack components) {
        Formula f = new Formula(code, name, productCode);
        f.setResourcePack(components);
        Formula saved = formulaRepo.save(f);
        publish(LimsEventTypes.FORMULA_CREATED, Map.of("formulaCode", code, "productCode", productCode));
        return saved;
    }

    @Override @Transactional(readOnly = true)
    public Formula getByCode(String code) { return formulaRepo.findByCode(code); }

    @Override @Transactional(readOnly = true)
    public List<Formula> list() { return formulaRepo.findAll(); }

    @Override @Transactional
    public void submitForApproval(String formulaCode) {
        Formula f = formulaRepo.findByCode(formulaCode);
        f.submitForApproval();
        formulaRepo.save(f);
    }

    @Override @Transactional
    public void approve(String formulaCode, String approvedBy) {
        Formula f = formulaRepo.findByCode(formulaCode);
        f.approve(approvedBy);
        formulaRepo.save(f);
    }

    @Override @Transactional
    public void activate(String formulaCode) {
        Formula f = formulaRepo.findByCode(formulaCode);
        f.activate();
        formulaRepo.save(f);
    }

    @Override @Transactional
    public void retire(String formulaCode, String reason) {
        Formula f = formulaRepo.findByCode(formulaCode);
        f.retire(reason);
        formulaRepo.save(f);
    }

    @Override @Transactional
    public Formula releaseVersion(String formulaCode, String newVersion) {
        Formula f = formulaRepo.findByCode(formulaCode);
        f.retire("新版本 " + newVersion + " 替代");
        formulaRepo.save(f);
        Formula next = f.createNewVersion(newVersion);
        return formulaRepo.save(next);
    }

    @Override @Transactional
    public String createWeighingTask(String formulaCode, String batchNo, String workOrderNo) {
        // 委托至 WeighingTaskService；此处返回占位 task code
        return "WT-" + formulaCode + "-" + batchNo;
    }

    @Override @Transactional(readOnly = true)
    public Formula getFormulaByProductAndVersion(String productCode, String version) {
        return formulaRepo.findByProductCodeAndVersion(productCode, version);
    }

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "lims", payload));
    }
}
