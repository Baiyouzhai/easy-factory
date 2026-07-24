package com.byz.factory.web.controller.qms;

import com.byz.factory.qms.model.InspectionOrder;
import com.byz.factory.qms.model.InspectionPlan;
import com.byz.factory.qms.model.InspectionRecord;
import com.byz.factory.qms.service.InspectionService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检验管理 REST 控制器。
 * <p>
 * 提供检验方案管理、检验指令全生命周期和检验记录查询 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/qms/inspections")
@Tag(name = "QMS — 质量管理", description = "检验方案、检验指令、检验记录")
public class InspectionController {

    @Autowired(required = false)
    private InspectionService inspectionService;

    // ── 检验方案管理 ──

    @PostMapping("/plans")
    @Operation(summary = "创建检验方案")
    public Result<InspectionPlan> createPlan(@RequestParam String code,
                                              @RequestParam String name,
                                              @RequestParam String productCode,
                                              @RequestParam String processCode,
                                              @RequestParam String inspectionType) {
        return Result.ok(inspectionService.createPlan(code, name, productCode, processCode, inspectionType));
    }

    @GetMapping("/plans/by-product")
    @Operation(summary = "按产品查询检验方案")
    public Result<List<InspectionPlan>> findPlansByProduct(@RequestParam String productCode) {
        return Result.ok(inspectionService.findPlansByProduct(productCode));
    }

    @GetMapping("/plans/by-product-and-process")
    @Operation(summary = "按产品和工序查询检验方案")
    public Result<InspectionPlan> findPlanByProductAndProcess(@RequestParam String productCode,
                                                                @RequestParam String processCode) {
        return Result.ok(inspectionService.findPlanByProductAndProcess(productCode, processCode));
    }

    @GetMapping("/plans/{planCode}")
    @Operation(summary = "获取检验方案")
    public Result<InspectionPlan> getPlan(@PathVariable String planCode) {
        return Result.ok(inspectionService.getPlan(planCode));
    }

    // ── 检验指令管理 ──

    @PostMapping("/orders")
    @Operation(summary = "创建检验指令")
    public Result<InspectionOrder> createOrder(@RequestParam String workOrderNo,
                                                @RequestParam String batchNo,
                                                @RequestParam String processCode,
                                                @RequestParam String planCode) {
        return Result.ok(inspectionService.createOrder(workOrderNo, batchNo, processCode, planCode));
    }

    @PutMapping("/orders/{inspectionNo}/start")
    @Operation(summary = "开始检验")
    public Result<InspectionOrder> startInspection(@PathVariable String inspectionNo,
                                                     @RequestParam String inspector) {
        return Result.ok(inspectionService.startInspection(inspectionNo, inspector));
    }

    @PutMapping("/orders/{inspectionNo}/submit-result")
    @Operation(summary = "提交检验记录")
    public Result<InspectionOrder> submitResult(@PathVariable String inspectionNo,
                                                 @RequestBody InspectionRecord record) {
        return Result.ok(inspectionService.submitResult(inspectionNo, record));
    }

    @PutMapping("/orders/{inspectionNo}/complete")
    @Operation(summary = "完成检验", description = "自动判定 PASS/FAIL")
    public Result<InspectionOrder> completeInspection(@PathVariable String inspectionNo) {
        return Result.ok(inspectionService.completeInspection(inspectionNo));
    }

    @PutMapping("/orders/{inspectionNo}/close")
    @Operation(summary = "关闭检验指令")
    public Result<InspectionOrder> closeOrder(@PathVariable String inspectionNo) {
        return Result.ok(inspectionService.closeOrder(inspectionNo));
    }

    @GetMapping("/orders/{inspectionNo}")
    @Operation(summary = "按检验编号查询")
    public Result<InspectionOrder> findOrder(@PathVariable String inspectionNo) {
        return Result.ok(inspectionService.findOrder(inspectionNo));
    }

    @GetMapping("/orders/by-work-order")
    @Operation(summary = "按工单号查询检验列表")
    public Result<List<InspectionOrder>> findOrdersByWorkOrder(@RequestParam String workOrderNo) {
        return Result.ok(inspectionService.findOrdersByWorkOrder(workOrderNo));
    }

    // ── 检验记录查询 ──

    @GetMapping("/orders/{inspectionNo}/records")
    @Operation(summary = "获取检验记录")
    public Result<List<InspectionRecord>> getRecords(@PathVariable String inspectionNo) {
        return Result.ok(inspectionService.getRecords(inspectionNo));
    }

}
