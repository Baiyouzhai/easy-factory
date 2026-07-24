package com.byz.factory.web.controller.mes;

import com.byz.factory.mes.model.ActionRecord;
import com.byz.factory.mes.model.MesWorkOrder;
import com.byz.factory.mes.model.ProcessRecord;
import com.byz.factory.mes.service.WorkOrderService;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 工单管理 REST 控制器。
 * <p>
 * 负责工单生命周期管理（创建→下达→开工→完工→关闭）、
 * 工序流转执行和报工，以及与 QMS 的中断恢复协作。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/mes/work-orders")
@Tag(name = "MES — 制造执行", description = "工单管理、工序流转、报工追溯")
public class WorkOrderController {

    @Autowired(required = false)
    private WorkOrderService workOrderService;

    // ==================== 工单生命周期 ====================

    @PostMapping
    @Operation(summary = "创建工单", description = "从蓝图创建新工单（初始状态 CREATED）")
    public Result<MesWorkOrder> create(@RequestParam String blueprintCode,
                                       @RequestParam String productCode,
                                       @RequestParam BigDecimal quantity) {
        return Result.ok(workOrderService.create(blueprintCode, productCode, quantity));
    }

    @PutMapping("/{workOrderNo}/release")
    @Operation(summary = "下达工单", description = "冻结蓝图版本号，记录执行工厂和计划时间")
    public Result<MesWorkOrder> release(@PathVariable String workOrderNo,
                                        @RequestParam String operator,
                                        @RequestParam Instant plannedStart,
                                        @RequestParam Instant plannedEnd,
                                        @RequestParam String factoryCode) {
        return Result.ok(workOrderService.release(workOrderNo, operator, plannedStart, plannedEnd, factoryCode));
    }

    @PutMapping("/{workOrderNo}/start")
    @Operation(summary = "开工", description = "记录实际开始时间（RELEASED → IN_PROGRESS）")
    public Result<MesWorkOrder> start(@PathVariable String workOrderNo) {
        return Result.ok(workOrderService.start(workOrderNo));
    }

    @PutMapping("/{workOrderNo}/complete")
    @Operation(summary = "完工", description = "所有工序执行完毕（IN_PROGRESS → COMPLETED）")
    public Result<MesWorkOrder> complete(@PathVariable String workOrderNo) {
        return Result.ok(workOrderService.complete(workOrderNo));
    }

    @PutMapping("/{workOrderNo}/close")
    @Operation(summary = "关闭工单", description = "终态（COMPLETED → CLOSED）")
    public Result<MesWorkOrder> close(@PathVariable String workOrderNo) {
        return Result.ok(workOrderService.close(workOrderNo));
    }

    // ==================== 工序流转 ====================

    @PostMapping("/{workOrderNo}/processes/{processCode}/execute")
    @Operation(summary = "执行工序", description = "按 ExecutionMode 遍历动作链执行")
    public Result<IResourcePack> executeProcess(@PathVariable String workOrderNo,
                                                 @PathVariable String processCode,
                                                 @RequestBody IResourcePack input) {
        return Result.ok(workOrderService.executeProcess(workOrderNo, processCode, input));
    }

    @PostMapping("/{workOrderNo}/processes/{processCode}/actions/{actionCode}/report")
    @Operation(summary = "动作报工", description = "动作级报工，生成 ActionRecord 追溯记录")
    public Result<ActionRecord> reportAction(@PathVariable String workOrderNo,
                                              @PathVariable String processCode,
                                              @PathVariable String actionCode,
                                              @RequestParam String result,
                                              @RequestParam(required = false) String remark) {
        return Result.ok(workOrderService.reportAction(workOrderNo, processCode, actionCode, result, remark));
    }

    // ==================== QMS 中断恢复 ====================

    @PutMapping("/{workOrderNo}/processes/{processCode}/resume")
    @Operation(summary = "恢复工序", description = "从中断状态恢复执行（QMS 判定通过后回调）")
    public Result<ProcessRecord> resumeProcess(@PathVariable String workOrderNo,
                                                @PathVariable String processCode) {
        return Result.ok(workOrderService.resumeProcess(workOrderNo, processCode));
    }

    // ==================== 查询 ====================

    @GetMapping("/{workOrderNo}")
    @Operation(summary = "查询工单", description = "根据工单号查询工单详情")
    public Result<MesWorkOrder> findByWorkOrderNo(@PathVariable String workOrderNo) {
        return Result.ok(workOrderService.findByWorkOrderNo(workOrderNo));
    }

    @GetMapping("/{workOrderNo}/process-records")
    @Operation(summary = "查询工序记录", description = "查询工单下所有工序的执行记录")
    public Result<List<ProcessRecord>> findProcessRecords(@PathVariable String workOrderNo) {
        return Result.ok(workOrderService.findProcessRecords(workOrderNo));
    }

    @GetMapping("/process-records/{processRecordId}/action-records")
    @Operation(summary = "查询动作记录", description = "查询工序下所有动作的报工记录")
    public Result<List<ActionRecord>> findActionRecords(@PathVariable String processRecordId) {
        return Result.ok(workOrderService.findActionRecords(processRecordId));
    }

}
