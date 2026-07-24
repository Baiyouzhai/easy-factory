package com.byz.factory.web.controller.aps;

import com.byz.factory.aps.model.Schedule;
import com.byz.factory.aps.service.ApsService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 高级排程 REST 控制器。
 * <p>
 * 提供排程创建、优化、下发和重排程 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/aps")
@Tag(name = "APS — 高级排程", description = "排程创建、优化、下发、重排程")
public class ApsController {

    @Autowired(required = false)
    private ApsService apsService;

    @PostMapping("/schedules")
    @Operation(summary = "从 MPS 计划创建排程草稿")
    public Result<Schedule> createSchedule(@RequestParam String planNo,
                                            @RequestParam String factoryCode) {
        return Result.ok(apsService.createSchedule(planNo, factoryCode));
    }

    @PutMapping("/schedules/{scheduleCode}/optimize")
    @Operation(summary = "按指定策略优化排程", description = "支持 EDD/SPT/CR 三种规则")
    public Result<Schedule> optimize(@PathVariable String scheduleCode,
                                      @RequestParam String strategy) {
        return Result.ok(apsService.optimize(scheduleCode, strategy));
    }

    @PutMapping("/schedules/{scheduleCode}/dispatch")
    @Operation(summary = "下发排程到 MES")
    public Result<Void> dispatch(@PathVariable String scheduleCode) {
        apsService.dispatch(scheduleCode);
        return Result.ok();
    }

    @PutMapping("/schedules/{scheduleCode}/reschedule")
    @Operation(summary = "事件驱动增量重排程")
    public Result<Schedule> reschedule(@PathVariable String scheduleCode,
                                        @RequestParam String triggerEvent) {
        return Result.ok(apsService.reschedule(scheduleCode, triggerEvent));
    }

    @GetMapping("/schedules/{scheduleCode}")
    @Operation(summary = "查询排程方案")
    public Result<Schedule> getSchedule(@PathVariable String scheduleCode) {
        return Result.ok(apsService.getSchedule(scheduleCode));
    }

}
