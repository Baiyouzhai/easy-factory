package com.byz.factory.web.controller.scm;

import com.byz.factory.scm.model.PurchaseOrder;
import com.byz.factory.scm.model.Supplier;
import com.byz.factory.scm.service.ScmService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应链管理 REST 控制器。
 * <p>
 * 提供供应商管理和采购订单全生命周期 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/scm")
@Tag(name = "SCM — 供应链管理", description = "供应商管理、采购订单")
public class ScmController {

    @Autowired(required = false)
    private ScmService scmService;

    // ==================== 供应商管理 ====================

    @PostMapping("/suppliers")
    @Operation(summary = "注册新供应商")
    public Result<Supplier> registerSupplier(@RequestParam String code,
                                              @RequestParam String name,
                                              @RequestParam String category) {
        return Result.ok(scmService.registerSupplier(code, name, category));
    }

    @GetMapping("/suppliers/{code}")
    @Operation(summary = "查询供应商")
    public Result<Supplier> findSupplier(@PathVariable String code) {
        return Result.ok(scmService.findSupplier(code));
    }

    @GetMapping("/suppliers/qualified")
    @Operation(summary = "获取所有合格供应商")
    public Result<List<Supplier>> getQualifiedSuppliers() {
        return Result.ok(scmService.getQualifiedSuppliers());
    }

    @PutMapping("/suppliers/{code}/qualify")
    @Operation(summary = "通过资质审核")
    public Result<Void> qualifySupplier(@PathVariable String code) {
        scmService.qualifySupplier(code);
        return Result.ok();
    }

    @PutMapping("/suppliers/{code}/disqualify")
    @Operation(summary = "取消资质")
    public Result<Void> disqualifySupplier(@PathVariable String code) {
        scmService.disqualifySupplier(code);
        return Result.ok();
    }

    @PutMapping("/suppliers/{code}/deactivate")
    @Operation(summary = "暂停合作")
    public Result<Void> deactivateSupplier(@PathVariable String code) {
        scmService.deactivateSupplier(code);
        return Result.ok();
    }

    @PutMapping("/suppliers/{code}/reactivate")
    @Operation(summary = "恢复合作")
    public Result<Void> reactivateSupplier(@PathVariable String code) {
        scmService.reactivateSupplier(code);
        return Result.ok();
    }

    // ==================== 采购订单管理 ====================

    @PostMapping("/purchase-orders")
    @Operation(summary = "创建采购订单", description = "初始状态 DRAFT")
    public Result<PurchaseOrder> createPurchaseOrder(@RequestParam String poNo,
                                                      @RequestParam String supplierCode) {
        return Result.ok(scmService.createPurchaseOrder(poNo, supplierCode));
    }

    @PutMapping("/purchase-orders/{poNo}/approve")
    @Operation(summary = "审批采购订单", description = "DRAFT → APPROVED")
    public Result<Void> approvePurchaseOrder(@PathVariable String poNo,
                                              @RequestParam String approvedBy) {
        scmService.approvePurchaseOrder(poNo, approvedBy);
        return Result.ok();
    }

    @PutMapping("/purchase-orders/{poNo}/send")
    @Operation(summary = "发送采购订单", description = "APPROVED → SENT")
    public Result<Void> sendPurchaseOrder(@PathVariable String poNo) {
        scmService.sendPurchaseOrder(poNo);
        return Result.ok();
    }

    @PutMapping("/purchase-orders/{poNo}/receive")
    @Operation(summary = "记录收货", description = "更新 PO 行已收数量")
    public Result<Void> receivePurchaseOrder(@PathVariable String poNo,
                                              @RequestParam String materialCode,
                                              @RequestParam BigDecimal receivedQty) {
        scmService.receivePurchaseOrder(poNo, materialCode, receivedQty);
        return Result.ok();
    }

    @PutMapping("/purchase-orders/{poNo}/cancel")
    @Operation(summary = "取消采购订单", description = "DRAFT/APPROVED → CANCELLED")
    public Result<Void> cancelPurchaseOrder(@PathVariable String poNo) {
        scmService.cancelPurchaseOrder(poNo);
        return Result.ok();
    }

    @GetMapping("/purchase-orders/{poNo}")
    @Operation(summary = "查询采购订单")
    public Result<PurchaseOrder> findPurchaseOrder(@PathVariable String poNo) {
        return Result.ok(scmService.findPurchaseOrder(poNo));
    }

    @GetMapping("/purchase-orders/by-supplier")
    @Operation(summary = "获取供应商的所有采购订单")
    public Result<List<PurchaseOrder>> getPurchaseOrdersBySupplier(@RequestParam String supplierCode) {
        return Result.ok(scmService.getPurchaseOrdersBySupplier(supplierCode));
    }

}
