package com.byz.factory.web.controller.lims;

import com.byz.factory.lims.model.BatchRecord;
import com.byz.factory.lims.service.BatchRecordService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 批记录 REST 控制器。
 * <p>
 * 管理批记录全流程：创建 → 提交审核 → 批准/驳回 → 归档。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/lims/batch-records")
@Tag(name = "LIMS — 批记录管理", description = "批记录创建、审核、归档")
public class BatchRecordController {

    @Autowired(required = false)
    private BatchRecordService batchRecordService;

    @PostMapping
    @Operation(summary = "创建批记录")
    public Result<BatchRecord> create(@RequestParam String batchNo,
                                       @RequestParam String workOrderId,
                                       @RequestParam String formulaCode,
                                       @RequestParam String formulaVersion,
                                       @RequestParam String productCode) {
        return Result.ok(batchRecordService.create(batchNo, workOrderId, formulaCode, formulaVersion, productCode));
    }

    @GetMapping("/{batchNo}")
    @Operation(summary = "按批号查询")
    public Result<BatchRecord> getByBatchNo(@PathVariable String batchNo) {
        return Result.ok(batchRecordService.getByBatchNo(batchNo));
    }

    @GetMapping("/by-work-order")
    @Operation(summary = "按工单号列出批记录")
    public Result<List<BatchRecord>> listByWorkOrder(@RequestParam String workOrderId) {
        return Result.ok(batchRecordService.listByWorkOrder(workOrderId));
    }

    @PutMapping("/{batchNo}/submit")
    @Operation(summary = "提交审核")
    public Result<Void> submitForReview(@PathVariable String batchNo) {
        batchRecordService.submitForReview(batchNo);
        return Result.ok();
    }

    @PutMapping("/{batchNo}/approve")
    @Operation(summary = "批准")
    public Result<Void> approve(@PathVariable String batchNo,
                                 @RequestParam String reviewedBy) {
        batchRecordService.approve(batchNo, reviewedBy);
        return Result.ok();
    }

    @PutMapping("/{batchNo}/reject")
    @Operation(summary = "驳回重审")
    public Result<Void> reject(@PathVariable String batchNo,
                                @RequestParam String reason) {
        batchRecordService.reject(batchNo, reason);
        return Result.ok();
    }

    @PutMapping("/{batchNo}/archive")
    @Operation(summary = "归档")
    public Result<Void> archive(@PathVariable String batchNo) {
        batchRecordService.archive(batchNo);
        return Result.ok();
    }

    @PutMapping("/{batchNo}/add-deviation")
    @Operation(summary = "添加偏差记录")
    public Result<Void> addDeviation(@PathVariable String batchNo,
                                      @RequestParam String deviation) {
        batchRecordService.addDeviation(batchNo, deviation);
        return Result.ok();
    }

}
