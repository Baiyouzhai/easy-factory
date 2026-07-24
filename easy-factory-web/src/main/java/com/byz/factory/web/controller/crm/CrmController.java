package com.byz.factory.web.controller.crm;

import com.byz.factory.crm.model.Customer;
import com.byz.factory.crm.model.SalesOrder;
import com.byz.factory.crm.service.CrmService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 客户关系管理 REST 控制器。
 * <p>
 * 提供客户管理、销售订单和投诉管理 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/crm")
@Tag(name = "CRM — 客户关系", description = "客户管理、销售订单、投诉管理")
public class CrmController {

    @Autowired(required = false)
    private CrmService crmService;

    // ── 客户管理 ──

    @PostMapping("/customers")
    @Operation(summary = "注册客户")
    public Result<Customer> registerCustomer(@RequestParam String code,
                                              @RequestParam String name,
                                              @RequestParam String industry) {
        return Result.ok(crmService.registerCustomer(code, name, industry));
    }

    @GetMapping("/customers/{code}")
    @Operation(summary = "查询客户")
    public Result<Customer> getCustomer(@PathVariable String code) {
        return Result.ok(crmService.getCustomer(code));
    }

    @PutMapping("/customers/{customerCode}/audit-status")
    @Operation(summary = "更新 GMP 审计状态")
    public Result<Void> updateAuditStatus(@PathVariable String customerCode,
                                           @RequestParam String status,
                                           @RequestParam String date) {
        crmService.updateAuditStatus(customerCode, status, date);
        return Result.ok();
    }

    // ── 订单管理 ──

    @PostMapping("/orders")
    @Operation(summary = "创建销售订单")
    public Result<SalesOrder> createOrder(@RequestParam String customerCode,
                                           @RequestParam String productCode,
                                           @RequestParam BigDecimal quantity) {
        return Result.ok(crmService.createOrder(customerCode, productCode, quantity));
    }

    @PutMapping("/orders/{orderNo}/confirm")
    @Operation(summary = "确认订单")
    public Result<SalesOrder> confirmOrder(@PathVariable String orderNo) {
        return Result.ok(crmService.confirmOrder(orderNo));
    }

    @PutMapping("/orders/{orderNo}/link-plan")
    @Operation(summary = "关联生产计划")
    public Result<Void> linkToProductionPlan(@PathVariable String orderNo,
                                              @RequestParam String planNo) {
        crmService.linkToProductionPlan(orderNo, planNo);
        return Result.ok();
    }

    // ── 投诉管理 ──

    @PostMapping("/complaints")
    @Operation(summary = "创建客户投诉")
    public Result<String> createComplaint(@RequestParam String customerCode,
                                           @RequestParam String orderNo,
                                           @RequestParam String batchNo,
                                           @RequestParam String type,
                                           @RequestParam String description) {
        return Result.ok(crmService.createComplaint(customerCode, orderNo, batchNo, type, description));
    }

}
