package com.byz.factory.web.controller.mps;

import com.byz.factory.mps.model.CapacityCheck;
import com.byz.factory.mps.model.DemandSource;
import com.byz.factory.mps.model.DemandSourceType;
import com.byz.factory.mps.model.ProductionPlan;
import com.byz.factory.mps.model.ProductionPlan.PlanItem;
import com.byz.factory.mps.service.MpsService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 主生产计划 REST 控制器。
 * <p>
 * 提供计划 CRUD、生命周期管理、需求管理和粗产能检查（RCCP）API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/mps")
@Tag(name = "MPS — 主生产计划", description = "计划管理、需求管理、产能检查")
public class MpsController {

    @Autowired(required = false)
    private MpsService mpsService;

    // ── 计划 CRUD ──

    @PostMapping("/plans")
    @Operation(summary = "创建生产计划")
    public Result<ProductionPlan> create(@RequestParam String periodType,
                                          @RequestParam LocalDate periodStart,
                                          @RequestParam LocalDate periodEnd) {
        return Result.ok(mpsService.create(periodType, periodStart, periodEnd));
    }

    @GetMapping("/plans/{planNo}")
    @Operation(summary = "查询生产计划")
    public Result<ProductionPlan> getPlan(@PathVariable String planNo) {
        return Result.ok(mpsService.getPlan(planNo).orElse(null));
    }

    @GetMapping("/plans")
    @Operation(summary = "列出计划", description = "按周期类型和状态可选过滤")
    public Result<List<ProductionPlan>> listPlans(@RequestParam(required = false) String periodType,
                                                    @RequestParam(required = false) String status) {
        return Result.ok(mpsService.listPlans(periodType, status));
    }

    @PostMapping("/plans/{planNo}/items")
    @Operation(summary = "添加计划明细项")
    public Result<Void> addPlanItem(@PathVariable String planNo,
                                     @RequestBody PlanItem item) {
        mpsService.addPlanItem(planNo, item);
        return Result.ok();
    }

    // ── 计划生命周期 ──

    @PutMapping("/plans/{planNo}/approve")
    @Operation(summary = "审批通过", description = "DRAFT → APPROVED")
    public Result<Void> approve(@PathVariable String planNo,
                                 @RequestParam String approvedBy) {
        mpsService.approve(planNo, approvedBy);
        return Result.ok();
    }

    @PutMapping("/plans/{planNo}/reject")
    @Operation(summary = "驳回", description = "APPROVED → DRAFT")
    public Result<Void> reject(@PathVariable String planNo) {
        mpsService.reject(planNo);
        return Result.ok();
    }

    @PutMapping("/plans/{planNo}/release")
    @Operation(summary = "发布到下游", description = "APPROVED → RELEASED；MES 订阅后为每个 PlanItem 生成工单")
    public Result<Void> release(@PathVariable String planNo) {
        mpsService.release(planNo);
        return Result.ok();
    }

    @PutMapping("/plans/{planNo}/start")
    @Operation(summary = "开始执行", description = "RELEASED → IN_PROGRESS")
    public Result<Void> start(@PathVariable String planNo) {
        mpsService.start(planNo);
        return Result.ok();
    }

    @PutMapping("/plans/{planNo}/complete")
    @Operation(summary = "标记完成", description = "IN_PROGRESS → COMPLETED")
    public Result<Void> complete(@PathVariable String planNo) {
        mpsService.complete(planNo);
        return Result.ok();
    }

    @PutMapping("/plans/{planNo}/close")
    @Operation(summary = "关闭计划", description = "COMPLETED → CLOSED")
    public Result<Void> close(@PathVariable String planNo) {
        mpsService.close(planNo);
        return Result.ok();
    }

    // ── 需求管理 ──

    @PostMapping("/demands")
    @Operation(summary = "注册需求来源")
    public Result<DemandSource> registerDemand(@RequestParam DemandSourceType sourceType,
                                                 @RequestParam String referenceNo,
                                                 @RequestParam String productCode,
                                                 @RequestParam BigDecimal quantity,
                                                 @RequestParam LocalDate dueDate) {
        return Result.ok(mpsService.registerDemand(sourceType, referenceNo, productCode, quantity, dueDate));
    }

    @GetMapping("/demands")
    @Operation(summary = "查询产品的需求列表")
    public Result<List<DemandSource>> getDemands(@RequestParam String productCode) {
        return Result.ok(mpsService.getDemands(productCode));
    }

    // ── 粗产能检查 ──

    @GetMapping("/plans/{planNo}/capacity-check")
    @Operation(summary = "执行粗产能检查（RCCP）")
    public Result<CapacityCheck> checkCapacity(@PathVariable String planNo,
                                                 @RequestParam String factoryCode) {
        return Result.ok(mpsService.checkCapacity(planNo, factoryCode));
    }

}
