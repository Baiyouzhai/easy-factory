package com.byz.factory.web.controller.plm;

import com.byz.factory.factory.BlueprintDiff;
import com.byz.factory.factory.ProductFactoryResult;
import com.byz.factory.operation.capability.ProcessRouteMatcher;
import com.byz.factory.plm.bom.BOMConversionRequest;
import com.byz.factory.plm.bom.BOMConversionResult;
import com.byz.factory.plm.model.Blueprint;
import com.byz.factory.plm.model.ChangeRequest;
import com.byz.factory.plm.model.ProcessParameter;
import com.byz.factory.plm.model.ProcessTemplate;
import com.byz.factory.plm.service.BlueprintService;
import com.byz.factory.process.IProcessParameter;
import com.byz.factory.shared.BumpType;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 蓝图管理 REST 控制器。
 * <p>
 * 管理工艺路线的全生命周期：创建→评审→批准→发布→废弃，
 * 以及工艺参数管理、版本管理和 BOM 转化。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/plm/blueprints")
@Tag(name = "PLM — 研发工艺", description = "蓝图生命周期、工艺参数、版本管理、BOM 转化")
public class BlueprintController {

    @Autowired(required = false)
    private BlueprintService blueprintService;

    // ==================== 蓝图生命周期 ====================

    @PostMapping
    @Operation(summary = "创建蓝图草稿", description = "从工艺模板创建蓝图（初始状态 DRAFT）")
    public Result<Blueprint> createBlueprint(@RequestBody ProcessTemplate template,
                                              @RequestParam String productCode) {
        return Result.ok(blueprintService.createBlueprint(template, productCode));
    }

    @PutMapping("/{blueprintCode}/submit")
    @Operation(summary = "提交评审", description = "DRAFT → UNDER_REVIEW")
    public Result<Blueprint> submitForReview(@PathVariable String blueprintCode) {
        return Result.ok(blueprintService.submitForReview(blueprintCode));
    }

    @PutMapping("/{blueprintCode}/approve")
    @Operation(summary = "批准蓝图", description = "UNDER_REVIEW → APPROVED")
    public Result<Blueprint> approve(@PathVariable String blueprintCode,
                                      @RequestParam String approvedBy) {
        return Result.ok(blueprintService.approve(blueprintCode, approvedBy));
    }

    @PutMapping("/{blueprintCode}/reject")
    @Operation(summary = "驳回评审", description = "UNDER_REVIEW/APPROVED → DRAFT")
    public Result<Blueprint> reject(@PathVariable String blueprintCode) {
        return Result.ok(blueprintService.reject(blueprintCode));
    }

    @PutMapping("/{blueprintCode}/release")
    @Operation(summary = "发布到 MES", description = "APPROVED → RELEASED")
    public Result<Blueprint> releaseVersion(@PathVariable String blueprintCode) {
        return Result.ok(blueprintService.releaseVersion(blueprintCode));
    }

    @PutMapping("/{blueprintCode}/obsolete")
    @Operation(summary = "废弃蓝图", description = "→ OBSOLETED")
    public Result<Blueprint> obsolete(@PathVariable String blueprintCode) {
        return Result.ok(blueprintService.obsolete(blueprintCode));
    }

    // ==================== 工艺参数管理 ====================

    @PostMapping("/{blueprintCode}/processes/{processCode}/parameters")
    @Operation(summary = "添加工艺参数", description = "为蓝图中指定工序添加工艺参数")
    public Result<ProcessParameter> addProcessParameter(@PathVariable String blueprintCode,
                                                          @PathVariable String processCode,
                                                          @RequestBody ProcessParameter parameter) {
        return Result.ok(blueprintService.addProcessParameter(blueprintCode, processCode, parameter));
    }

    @PutMapping("/{blueprintCode}/processes/{processCode}/parameters/{parameterCode}")
    @Operation(summary = "更新工艺参数")
    public Result<ProcessParameter> updateProcessParameter(@PathVariable String blueprintCode,
                                                             @PathVariable String processCode,
                                                             @PathVariable String parameterCode,
                                                             @RequestBody IProcessParameter newValue) {
        return Result.ok(blueprintService.updateProcessParameter(blueprintCode, processCode, parameterCode, newValue));
    }

    // ==================== 版本管理 ====================

    @PostMapping("/{blueprintCode}/versions")
    @Operation(summary = "创建新版本", description = "基于当前已发布版本创建新版本草稿")
    public Result<Blueprint> createNewVersion(@PathVariable String blueprintCode,
                                               @RequestParam String newVersion,
                                               @RequestParam String changeReason) {
        return Result.ok(blueprintService.createNewVersion(blueprintCode, newVersion, changeReason));
    }

    @PutMapping("/{blueprintCode}/versions/bump")
    @Operation(summary = "递增版本号", description = "使用当前策略递增版本号（MAJOR/MINOR/PATCH）")
    public Result<Blueprint> bumpVersion(@PathVariable String blueprintCode,
                                          @RequestParam BumpType bumpType) {
        return Result.ok(blueprintService.bumpVersion(blueprintCode, bumpType));
    }

    @GetMapping("/{blueprintCode}/diff")
    @Operation(summary = "版本差异对比", description = "对比两个蓝图版本的差异")
    public Result<BlueprintDiff> diff(@PathVariable String blueprintCode,
                                       @RequestParam String versionA,
                                       @RequestParam String versionB) {
        return Result.ok(blueprintService.diff(blueprintCode, versionA, versionB));
    }

    // ==================== BOM 管理 ====================

    @PostMapping("/bom/convert")
    @Operation(summary = "BOM 转化", description = "执行 BOM 转化（EBOM→PBOM→MBOM）")
    public Result<BOMConversionResult> convertBOM(@RequestBody BOMConversionRequest request) {
        return Result.ok(blueprintService.convertBOM(request));
    }

    @PostMapping("/bom/convert/ebom-to-pbom")
    @Operation(summary = "EBOM → PBOM 转化")
    public Result<BOMConversionResult> convertEBOMtoPBOM(@RequestParam String productCode) {
        return Result.ok(blueprintService.convertEBOMtoPBOM(productCode));
    }

    @PostMapping("/bom/convert/pbom-to-mbom")
    @Operation(summary = "PBOM → MBOM 转化")
    public Result<BOMConversionResult> convertPBOMtoMBOM(@RequestParam String productCode,
                                                           @RequestParam String factoryCode) {
        return Result.ok(blueprintService.convertPBOMtoMBOM(productCode, factoryCode));
    }

    // ==================== 可制造性检查 ====================

    @GetMapping("/{blueprintCode}/feasibility")
    @Operation(summary = "可制造性检查", description = "检查蓝图在指定工厂的可制造性")
    public Result<ProductFactoryResult> checkFeasibility(@PathVariable String blueprintCode,
                                                           @RequestParam String factoryCode) {
        return Result.ok(blueprintService.checkFeasibility(blueprintCode, factoryCode));
    }

    @GetMapping("/{blueprintCode}/match-equipment")
    @Operation(summary = "设备匹配", description = "将蓝图动作匹配到工厂设备")
    public Result<ProcessRouteMatcher.RouteMatchResult> matchEquipment(
            @PathVariable String blueprintCode,
            @RequestParam String factoryCode) {
        return Result.ok(blueprintService.matchEquipment(blueprintCode, factoryCode));
    }

    // ==================== 变更管理 ====================

    @PostMapping("/change-requests")
    @Operation(summary = "创建变更请求")
    public Result<ChangeRequest> createChangeRequest(@RequestParam String code,
                                                       @RequestParam String blueprintCode,
                                                       @RequestParam String fromVersion,
                                                       @RequestParam String changeReason,
                                                       @RequestParam String requestedBy) {
        return Result.ok(blueprintService.createChangeRequest(code, blueprintCode, fromVersion, changeReason, requestedBy));
    }

    @PutMapping("/change-requests/{changeRequestCode}/submit")
    @Operation(summary = "提交变更请求评审")
    public Result<ChangeRequest> submitChangeRequest(@PathVariable String changeRequestCode) {
        return Result.ok(blueprintService.submitChangeRequest(changeRequestCode));
    }

    @PutMapping("/change-requests/{changeRequestCode}/approve")
    @Operation(summary = "批准变更请求")
    public Result<ChangeRequest> approveChangeRequest(@PathVariable String changeRequestCode,
                                                        @RequestParam String approvedBy) {
        return Result.ok(blueprintService.approveChangeRequest(changeRequestCode, approvedBy));
    }

    @PutMapping("/change-requests/{changeRequestCode}/implement")
    @Operation(summary = "实施变更", description = "创建蓝图新版本 + 关闭变更请求")
    public Result<Blueprint> implementChange(@PathVariable String changeRequestCode,
                                              @RequestParam String newVersion) {
        return Result.ok(blueprintService.implementChange(changeRequestCode, newVersion));
    }

}
