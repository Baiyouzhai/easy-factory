package com.byz.factory.web.controller.qms;

import com.byz.factory.qms.model.Capa;
import com.byz.factory.qms.service.CapaService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CAPA 管理 REST 控制器。
 * <p>
 * 管理纠正与预防措施全流程：创建 → 根因分析 → 执行 → 效果验证 → 关闭。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/qms/capas")
@Tag(name = "QMS — CAPA 管理", description = "CAPA 创建、根因分析、执行、验证、关闭")
public class CapaController {

    @Autowired(required = false)
    private CapaService capaService;

    @PostMapping
    @Operation(summary = "创建 CAPA")
    public Result<Capa> createCapa(@RequestParam String code,
                                    @RequestParam String name,
                                    @RequestParam String deviationCode,
                                    @RequestParam String problemDescription,
                                    @RequestParam String assignTo) {
        return Result.ok(capaService.createCapa(code, name, deviationCode, problemDescription, assignTo));
    }

    @PutMapping("/{capaCode}/analyze-root-cause")
    @Operation(summary = "完成根因分析")
    public Result<Capa> analyzeRootCause(@PathVariable String capaCode,
                                          @RequestParam String rootCause) {
        return Result.ok(capaService.analyzeRootCause(capaCode, rootCause));
    }

    @PutMapping("/{capaCode}/execute-actions")
    @Operation(summary = "开始执行纠正/预防措施")
    public Result<Capa> executeActions(@PathVariable String capaCode,
                                        @RequestParam String correctiveAction,
                                        @RequestParam String preventiveAction) {
        return Result.ok(capaService.executeActions(capaCode, correctiveAction, preventiveAction));
    }

    @PutMapping("/{capaCode}/verify")
    @Operation(summary = "完成效果验证")
    public Result<Capa> verify(@PathVariable String capaCode,
                                @RequestParam String verification,
                                @RequestParam String approvedBy) {
        return Result.ok(capaService.verify(capaCode, verification, approvedBy));
    }

    @PutMapping("/{capaCode}/close")
    @Operation(summary = "关闭 CAPA")
    public Result<Capa> close(@PathVariable String capaCode) {
        return Result.ok(capaService.close(capaCode));
    }

    @PutMapping("/{capaCode}/cancel")
    @Operation(summary = "取消 CAPA")
    public Result<Capa> cancel(@PathVariable String capaCode,
                                @RequestParam String reason) {
        return Result.ok(capaService.cancel(capaCode, reason));
    }

    // ── 查询 ──

    @GetMapping("/{capaCode}")
    @Operation(summary = "按编号查询 CAPA")
    public Result<Capa> findCapa(@PathVariable String capaCode) {
        return Result.ok(capaService.findCapa(capaCode));
    }

    @GetMapping("/by-deviation")
    @Operation(summary = "按偏差编号查询 CAPA")
    public Result<List<Capa>> findCapasByDeviation(@RequestParam String deviationCode) {
        return Result.ok(capaService.findCapasByDeviation(deviationCode));
    }

    @GetMapping("/overdue")
    @Operation(summary = "获取逾期 CAPA")
    public Result<List<Capa>> getOverdueCapas() {
        return Result.ok(capaService.getOverdueCapas());
    }

    @GetMapping("/active")
    @Operation(summary = "获取活跃 CAPA（未关闭）")
    public Result<List<Capa>> getActiveCapas() {
        return Result.ok(capaService.getActiveCapas());
    }

}
