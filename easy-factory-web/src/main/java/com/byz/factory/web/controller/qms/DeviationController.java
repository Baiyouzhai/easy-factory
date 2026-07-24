package com.byz.factory.web.controller.qms;

import com.byz.factory.batch.DeviationDisposition;
import com.byz.factory.batch.DeviationSeverity;
import com.byz.factory.qms.model.Deviation;
import com.byz.factory.qms.service.DeviationService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 偏差管理 REST 控制器。
 * <p>
 * 管理偏差全流程：创建 → 调查 → 处置 → 解决 → 关闭。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/qms/deviations")
@Tag(name = "QMS — 偏差管理", description = "偏差创建、调查、处置、关闭")
public class DeviationController {

    @Autowired(required = false)
    private DeviationService deviationService;

    @PostMapping
    @Operation(summary = "创建偏差")
    public Result<Deviation> createDeviation(@RequestParam String code,
                                              @RequestParam String name,
                                              @RequestParam String source,
                                              @RequestParam String inspectionNo,
                                              @RequestParam DeviationSeverity severity) {
        return Result.ok(deviationService.createDeviation(code, name, source, inspectionNo, severity));
    }

    @PutMapping("/{deviationCode}/start-investigation")
    @Operation(summary = "开始调查")
    public Result<Deviation> startInvestigation(@PathVariable String deviationCode,
                                                  @RequestParam String investigator) {
        return Result.ok(deviationService.startInvestigation(deviationCode, investigator));
    }

    @PutMapping("/{deviationCode}/complete-investigation")
    @Operation(summary = "完成调查", description = "记录根因和产品影响评估")
    public Result<Deviation> completeInvestigation(@PathVariable String deviationCode,
                                                     @RequestParam String rootCause,
                                                     @RequestParam String productImpact) {
        return Result.ok(deviationService.completeInvestigation(deviationCode, rootCause, productImpact));
    }

    @PutMapping("/{deviationCode}/dispose")
    @Operation(summary = "QA 处置判定", description = "决定返工/让步/拒收")
    public Result<Deviation> dispose(@PathVariable String deviationCode,
                                      @RequestParam DeviationDisposition disposition,
                                      @RequestParam String dispositionNote,
                                      @RequestParam String disposedBy) {
        return Result.ok(deviationService.dispose(deviationCode, disposition, dispositionNote, disposedBy));
    }

    @PutMapping("/{deviationCode}/link-capa")
    @Operation(summary = "关联 CAPA")
    public Result<Deviation> linkCapa(@PathVariable String deviationCode,
                                       @RequestParam String capaCode) {
        return Result.ok(deviationService.linkCapa(deviationCode, capaCode));
    }

    @PutMapping("/{deviationCode}/resolve")
    @Operation(summary = "标记已解决")
    public Result<Deviation> resolve(@PathVariable String deviationCode) {
        return Result.ok(deviationService.resolve(deviationCode));
    }

    @PutMapping("/{deviationCode}/close")
    @Operation(summary = "关闭偏差")
    public Result<Deviation> close(@PathVariable String deviationCode) {
        return Result.ok(deviationService.close(deviationCode));
    }

    @PutMapping("/{deviationCode}/cancel")
    @Operation(summary = "取消偏差")
    public Result<Deviation> cancel(@PathVariable String deviationCode,
                                     @RequestParam String reason) {
        return Result.ok(deviationService.cancel(deviationCode, reason));
    }

    // ── 查询 ──

    @GetMapping("/{deviationCode}")
    @Operation(summary = "按编号查询偏差")
    public Result<Deviation> findDeviation(@PathVariable String deviationCode) {
        return Result.ok(deviationService.findDeviation(deviationCode));
    }

    @GetMapping("/by-inspection")
    @Operation(summary = "按检验单号查询偏差")
    public Result<List<Deviation>> findDeviationsByInspection(@RequestParam String inspectionNo) {
        return Result.ok(deviationService.findDeviationsByInspection(inspectionNo));
    }

    @GetMapping("/by-work-order")
    @Operation(summary = "按工单号查询偏差")
    public Result<List<Deviation>> findDeviationsByWorkOrder(@RequestParam String workOrderNo) {
        return Result.ok(deviationService.findDeviationsByWorkOrder(workOrderNo));
    }

    @GetMapping("/active")
    @Operation(summary = "获取活跃偏差（未关闭）")
    public Result<List<Deviation>> getActiveDeviations() {
        return Result.ok(deviationService.getActiveDeviations());
    }

}
