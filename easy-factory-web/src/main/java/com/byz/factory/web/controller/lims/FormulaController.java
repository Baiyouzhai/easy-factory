package com.byz.factory.web.controller.lims;

import com.byz.factory.lims.model.Formula;
import com.byz.factory.lims.service.FormulaService;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配方管理 REST 控制器。
 * <p>
 * 管理产品配方的全生命周期：创建→审批→激活→退役，以及版本管理和称量任务创建。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/lims/formulas")
@Tag(name = "LIMS — 配方管理", description = "配方全生命周期、版本管理")
public class FormulaController {

    @Autowired(required = false)
    private FormulaService formulaService;

    @PostMapping
    @Operation(summary = "创建配方")
    public Result<Formula> create(@RequestParam String code,
                                   @RequestParam String name,
                                   @RequestParam String productCode,
                                   @RequestBody IResourcePack components) {
        return Result.ok(formulaService.create(code, name, productCode, components));
    }

    @GetMapping("/{code}")
    @Operation(summary = "按编码查询配方")
    public Result<Formula> getByCode(@PathVariable String code) {
        return Result.ok(formulaService.getByCode(code));
    }

    @GetMapping
    @Operation(summary = "列出所有配方")
    public Result<List<Formula>> list() {
        return Result.ok(formulaService.list());
    }

    @PutMapping("/{formulaCode}/submit")
    @Operation(summary = "提交审批")
    public Result<Void> submitForApproval(@PathVariable String formulaCode) {
        formulaService.submitForApproval(formulaCode);
        return Result.ok();
    }

    @PutMapping("/{formulaCode}/approve")
    @Operation(summary = "批准配方")
    public Result<Void> approve(@PathVariable String formulaCode,
                                 @RequestParam String approvedBy) {
        formulaService.approve(formulaCode, approvedBy);
        return Result.ok();
    }

    @PutMapping("/{formulaCode}/activate")
    @Operation(summary = "激活配方", description = "标记为可用于生产")
    public Result<Void> activate(@PathVariable String formulaCode) {
        formulaService.activate(formulaCode);
        return Result.ok();
    }

    @PutMapping("/{formulaCode}/retire")
    @Operation(summary = "退役配方")
    public Result<Void> retire(@PathVariable String formulaCode,
                                @RequestParam String reason) {
        formulaService.retire(formulaCode, reason);
        return Result.ok();
    }

    @PostMapping("/{formulaCode}/release-version")
    @Operation(summary = "发布新版本", description = "退役当前版本并创建新版本")
    public Result<Formula> releaseVersion(@PathVariable String formulaCode,
                                           @RequestParam String newVersion) {
        return Result.ok(formulaService.releaseVersion(formulaCode, newVersion));
    }

    @PostMapping("/{formulaCode}/create-weighing-task")
    @Operation(summary = "创建称量任务", description = "关联工单创建称量任务")
    public Result<String> createWeighingTask(@PathVariable String formulaCode,
                                              @RequestParam String batchNo,
                                              @RequestParam String workOrderNo) {
        return Result.ok(formulaService.createWeighingTask(formulaCode, batchNo, workOrderNo));
    }

    @GetMapping("/by-product-and-version")
    @Operation(summary = "按产品和版本查询配方")
    public Result<Formula> getFormulaByProductAndVersion(@RequestParam String productCode,
                                                           @RequestParam String version) {
        return Result.ok(formulaService.getFormulaByProductAndVersion(productCode, version));
    }

}
