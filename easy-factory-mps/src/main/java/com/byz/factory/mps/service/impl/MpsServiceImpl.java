package com.byz.factory.mps.service.impl;

import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.MpsEventTypes;
import com.byz.factory.mps.ProductionPlanStatus;
import com.byz.factory.mps.model.CapacityCheck;
import com.byz.factory.mps.model.DemandSource;
import com.byz.factory.mps.model.DemandSourceType;
import com.byz.factory.mps.model.ProductionPlan;
import com.byz.factory.mps.model.ProductionPlan.PlanItem;
import com.byz.factory.mps.repository.DemandSourceRepository;
import com.byz.factory.mps.repository.ProductionPlanRepository;
import com.byz.factory.mps.service.MpsService;
import com.byz.factory.operation.capacity.FactoryCapacityProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 主生产计划服务实现 — MPS 模块核心业务逻辑。
 * <p>
 * 提供计划 CRUD、生命周期管理、需求管理和粗产能检查（RCCP）的完整实现。
 * 构造器注入 Repository，写操作加 {@link Transactional}，
 * 状态变更后通过 {@link DomainEventPublisher} 发布领域事件。
 *
 * @author 苏政
 */
@Service
public class MpsServiceImpl implements MpsService {

    private final ProductionPlanRepository planRepo;
    private final DemandSourceRepository demandRepo;

    public MpsServiceImpl(ProductionPlanRepository planRepo, DemandSourceRepository demandRepo) {
        this.planRepo = planRepo;
        this.demandRepo = demandRepo;
    }

    // ── 计划 CRUD ──

    @Override
    @Transactional
    public ProductionPlan create(String periodType, LocalDate periodStart, LocalDate periodEnd) {
        String planNo = generatePlanNo();
        ProductionPlan plan = new ProductionPlan(planNo, periodType, periodStart, periodEnd);
        ProductionPlan saved = planRepo.save(plan);
        publish(MpsEventTypes.PLAN_CREATED, Map.of(
                "planNo", planNo,
                "periodType", periodType));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductionPlan> getPlan(String planNo) {
        return planRepo.findByPlanNo(planNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionPlan> listPlans(String periodType, String status) {
        if (periodType != null && status != null) {
            return planRepo.findByPeriodTypeAndStatus(periodType, ProductionPlanStatus.valueOf(status));
        }
        if (periodType != null) {
            return planRepo.findByPeriodType(periodType);
        }
        if (status != null) {
            return planRepo.findByStatus(ProductionPlanStatus.valueOf(status));
        }
        return planRepo.findAll();
    }

    @Override
    @Transactional
    public void addPlanItem(String planNo, PlanItem item) {
        ProductionPlan plan = planRepo.findByPlanNo(planNo)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在: " + planNo));
        plan.addItem(item);
        planRepo.save(plan);
    }

    // ── 计划生命周期 ──

    @Override
    @Transactional
    public void approve(String planNo, String approvedBy) {
        ProductionPlan plan = planRepo.findByPlanNo(planNo)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在: " + planNo));
        plan.approve(approvedBy);
        planRepo.save(plan);
    }

    @Override
    @Transactional
    public void reject(String planNo) {
        ProductionPlan plan = planRepo.findByPlanNo(planNo)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在: " + planNo));
        plan.reject();
        planRepo.save(plan);
    }

    @Override
    @Transactional
    public void release(String planNo) {
        ProductionPlan plan = planRepo.findByPlanNo(planNo)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在: " + planNo));
        plan.release();
        planRepo.save(plan);
    }

    @Override
    @Transactional
    public void start(String planNo) {
        ProductionPlan plan = planRepo.findByPlanNo(planNo)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在: " + planNo));
        plan.start();
        planRepo.save(plan);
    }

    @Override
    @Transactional
    public void complete(String planNo) {
        ProductionPlan plan = planRepo.findByPlanNo(planNo)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在: " + planNo));
        plan.complete();
        planRepo.save(plan);
    }

    @Override
    @Transactional
    public void close(String planNo) {
        ProductionPlan plan = planRepo.findByPlanNo(planNo)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在: " + planNo));
        plan.close();
        planRepo.save(plan);
    }

    // ── 需求管理 ──

    @Override
    @Transactional
    public DemandSource registerDemand(DemandSourceType sourceType, String referenceNo,
                                        String productCode, BigDecimal quantity, LocalDate dueDate) {
        DemandSource demand = new DemandSource(referenceNo, productCode, quantity);
        demand.setDueDate(dueDate);
        demand.register(sourceType);
        DemandSource saved = demandRepo.save(demand);
        publish(MpsEventTypes.DEMAND_REGISTERED, Map.of(
                "sourceType", sourceType.name(),
                "productCode", productCode,
                "quantity", quantity));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandSource> getDemands(String productCode) {
        return demandRepo.findByProductCode(productCode);
    }

    // ── 粗产能检查 ──

    @Override
    @Transactional(readOnly = true)
    public CapacityCheck checkCapacity(String planNo, String factoryCode) {
        ProductionPlan plan = planRepo.findByPlanNo(planNo)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在: " + planNo));

        if (plan.getItems() == null || plan.getItems().isEmpty()) {
            return CapacityCheck.pass(planNo, factoryCode, "NONE",
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "无生产明细，无需检查");
        }

        // 估算所需产能：基于计划总数量（简化估算，每件产品约 1 小时）
        BigDecimal totalQuantity = plan.getTotalQuantity();
        BigDecimal requiredHours = totalQuantity; // 简化：1 件 = 1 小时

        // 工厂产能画像（默认配置：2 班制 × 8 小时 = 960 分钟/天）
        FactoryCapacityProfile profile = new FactoryCapacityProfile(factoryCode,
                java.time.Duration.ofMinutes(960));
        profile.withMachine("通用设备", 960, 0.85); // OEE 85%

        // 计算可用产能
        BigDecimal dailyAvailableMinutes = BigDecimal.valueOf(
                profile.getEffectiveMachineTime("通用设备").toMinutes());
        // 按周期天数估算可用总产能（小时）
        long periodDays = plan.getPeriodEnd().toEpochDay() - plan.getPeriodStart().toEpochDay() + 1;
        BigDecimal availableHours = dailyAvailableMinutes
                .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(periodDays));

        // 利用率
        BigDecimal utilizationRate;
        if (availableHours.compareTo(BigDecimal.ZERO) <= 0) {
            utilizationRate = BigDecimal.valueOf(999);
        } else {
            utilizationRate = requiredHours.divide(availableHours, 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 判定
        CapacityCheck.CapacityResult result;
        String bottleneck = "通用设备（简化估算）";
        if (utilizationRate.compareTo(BigDecimal.valueOf(85)) >= 0) {
            result = utilizationRate.compareTo(BigDecimal.valueOf(100)) >= 0
                    ? CapacityCheck.CapacityResult.FAIL
                    : CapacityCheck.CapacityResult.WARNING;
        } else {
            result = CapacityCheck.CapacityResult.PASS;
        }

        CapacityCheck check = new CapacityCheck(planNo, factoryCode, "MACHINE",
                requiredHours, availableHours, utilizationRate, result, bottleneck);

        publish(MpsEventTypes.CAPACITY_CHECKED, Map.of(
                "planNo", planNo,
                "factoryCode", factoryCode,
                "result", result.name()));

        return check;
    }

    // ── 内部工具方法 ──

    /** 生成计划编号: MP-yyyyMMdd-序号 */
    private String generatePlanNo() {
        long count = planRepo.count() + 1;
        String datePart = LocalDate.now().toString().replace("-", "").substring(2); // yyMMdd
        return String.format("MP-%s-%03d", datePart, count);
    }

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, "mps", payload));
    }

}
