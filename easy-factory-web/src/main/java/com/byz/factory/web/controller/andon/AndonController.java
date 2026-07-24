package com.byz.factory.web.controller.andon;

import com.byz.factory.andon.model.*;
import com.byz.factory.andon.service.AndonService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 安灯系统 REST 控制器。
 * <p>
 * 提供异常呼叫管理（触发→确认→逐级上报→解决→关闭）和上报规则配置 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/andon")
@Tag(name = "Andon — 安灯系统", description = "异常呼叫、逐级上报、规则管理")
public class AndonController {

    @Autowired(required = false)
    private AndonService andonService;

    // ── 呼叫管理 ──

    @PostMapping("/calls")
    @Operation(summary = "触发安灯呼叫")
    public Result<AndonCall> trigger(@RequestParam TriggerType triggerType,
                                      @RequestParam AndonSeverity severity,
                                      @RequestParam AndonSource source,
                                      @RequestParam String triggeredBy,
                                      @RequestParam String description) {
        return Result.ok(andonService.trigger(triggerType, severity, source, triggeredBy, description));
    }

    @PutMapping("/calls/{callCode}/acknowledge")
    @Operation(summary = "确认呼叫")
    public Result<AndonCall> acknowledge(@PathVariable String callCode,
                                          @RequestParam String acknowledgedBy) {
        return Result.ok(andonService.acknowledge(callCode, acknowledgedBy));
    }

    @PutMapping("/calls/{callCode}/escalate")
    @Operation(summary = "逐级上报")
    public Result<AndonCall> escalate(@PathVariable String callCode,
                                       @RequestParam String reason) {
        return Result.ok(andonService.escalate(callCode, reason));
    }

    @PutMapping("/calls/{callCode}/resolve")
    @Operation(summary = "解决呼叫")
    public Result<AndonCall> resolve(@PathVariable String callCode,
                                      @RequestParam String resolution) {
        return Result.ok(andonService.resolve(callCode, resolution));
    }

    @PutMapping("/calls/{callCode}/close")
    @Operation(summary = "关闭呼叫")
    public Result<AndonCall> close(@PathVariable String callCode) {
        return Result.ok(andonService.close(callCode));
    }

    // ── 呼叫查询 ──

    @GetMapping("/calls/{callCode}")
    @Operation(summary = "按编号查询呼叫")
    public Result<AndonCall> findCall(@PathVariable String callCode) {
        return Result.ok(andonService.findCall(callCode));
    }

    @GetMapping("/calls/active")
    @Operation(summary = "获取活跃呼叫列表")
    public Result<List<AndonCall>> getActiveCalls() {
        return Result.ok(andonService.getActiveCalls());
    }

    @GetMapping("/calls/by-work-order")
    @Operation(summary = "按工单号查询呼叫")
    public Result<List<AndonCall>> findCallsByWorkOrder(@RequestParam String workOrderNo) {
        return Result.ok(andonService.findCallsByWorkOrder(workOrderNo));
    }

    @GetMapping("/calls/by-equipment")
    @Operation(summary = "按设备编码查询呼叫")
    public Result<List<AndonCall>> findCallsByEquipment(@RequestParam String equipmentCode) {
        return Result.ok(andonService.findCallsByEquipment(equipmentCode));
    }

    @GetMapping("/calls/urgent")
    @Operation(summary = "获取紧急呼叫列表")
    public Result<List<AndonCall>> getUrgentCalls() {
        return Result.ok(andonService.getUrgentCalls());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取看板数据", description = "活跃呼叫统计")
    public Result<AndonDashboard> getDashboard() {
        return Result.ok(andonService.getDashboard());
    }

    // ── 上报规则管理 ──

    @PostMapping("/rules")
    @Operation(summary = "创建上报规则")
    public Result<EscalationRule> createRule(@RequestParam TriggerType triggerType,
                                              @RequestParam AndonSeverity severity) {
        return Result.ok(andonService.createRule(triggerType, severity));
    }

    @PostMapping("/rules/{ruleCode}/levels")
    @Operation(summary = "添加上报级别")
    public Result<Void> addEscalationLevel(@PathVariable String ruleCode,
                                            @RequestBody EscalationRule.EscalationLevel level) {
        andonService.addEscalationLevel(ruleCode, level);
        return Result.ok();
    }

    @GetMapping("/rules/by-trigger")
    @Operation(summary = "查询上报规则")
    public Result<EscalationRule> findRule(@RequestParam TriggerType triggerType,
                                            @RequestParam AndonSeverity severity) {
        return Result.ok(andonService.findRule(triggerType, severity));
    }

    @GetMapping("/rules/active")
    @Operation(summary = "获取所有启用的上报规则")
    public Result<List<EscalationRule>> getActiveRules() {
        return Result.ok(andonService.getActiveRules());
    }

    @PutMapping("/rules/{ruleCode}/disable")
    @Operation(summary = "禁用上报规则")
    public Result<Void> disableRule(@PathVariable String ruleCode) {
        andonService.disableRule(ruleCode);
        return Result.ok();
    }

    @PutMapping("/rules/{ruleCode}/enable")
    @Operation(summary = "启用上报规则")
    public Result<Void> enableRule(@PathVariable String ruleCode) {
        andonService.enableRule(ruleCode);
        return Result.ok();
    }

}
