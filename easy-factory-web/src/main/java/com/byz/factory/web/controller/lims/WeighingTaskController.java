package com.byz.factory.web.controller.lims;

import com.byz.factory.lims.model.WeighingItem;
import com.byz.factory.lims.model.WeighingTask;
import com.byz.factory.lims.service.WeighingTaskService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 称量任务 REST 控制器。
 * <p>
 * 管理称量任务全流程：创建 → 开始称量 → 记录结果 → 复核 → 完成。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/lims/weighing-tasks")
@Tag(name = "LIMS — 称量管理", description = "称量任务创建、执行、复核")
public class WeighingTaskController {

    @Autowired(required = false)
    private WeighingTaskService weighingTaskService;

    @PostMapping
    @Operation(summary = "创建称量任务")
    public Result<WeighingTask> create(@RequestParam String code,
                                        @RequestParam String name,
                                        @RequestParam String formulaCode,
                                        @RequestParam String workOrderId,
                                        @RequestParam String batchNo) {
        return Result.ok(weighingTaskService.create(code, name, formulaCode, workOrderId, batchNo));
    }

    @GetMapping("/{code}")
    @Operation(summary = "按编码查询")
    public Result<WeighingTask> getByCode(@PathVariable String code) {
        return Result.ok(weighingTaskService.getByCode(code));
    }

    @GetMapping("/by-work-order")
    @Operation(summary = "按工单号列出称量任务")
    public Result<List<WeighingTask>> listByWorkOrder(@RequestParam String workOrderId) {
        return Result.ok(weighingTaskService.listByWorkOrder(workOrderId));
    }

    @PutMapping("/{taskCode}/start")
    @Operation(summary = "开始称量")
    public Result<Void> startWeighing(@PathVariable String taskCode) {
        weighingTaskService.startWeighing(taskCode);
        return Result.ok();
    }

    @PutMapping("/{taskCode}/record-item")
    @Operation(summary = "记录称量结果")
    public Result<WeighingItem> recordWeighingItem(@PathVariable String taskCode,
                                                     @RequestParam String materialCode,
                                                     @RequestParam BigDecimal actualQty,
                                                     @RequestParam String operator,
                                                     @RequestParam String verifier,
                                                     @RequestParam String balance) {
        return Result.ok(weighingTaskService.recordWeighingItem(taskCode, materialCode, actualQty, operator, verifier, balance));
    }

    @PutMapping("/{taskCode}/verify")
    @Operation(summary = "复核通过")
    public Result<Void> verify(@PathVariable String taskCode) {
        weighingTaskService.verify(taskCode);
        return Result.ok();
    }

    @PutMapping("/{taskCode}/complete")
    @Operation(summary = "称量完成")
    public Result<Void> complete(@PathVariable String taskCode) {
        weighingTaskService.complete(taskCode);
        return Result.ok();
    }

}
