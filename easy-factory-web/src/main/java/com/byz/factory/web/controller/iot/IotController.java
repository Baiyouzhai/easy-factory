package com.byz.factory.web.controller.iot;

import com.byz.factory.iot.model.AlarmEvent;
import com.byz.factory.iot.model.Command;
import com.byz.factory.iot.model.DeviceConnection;
import com.byz.factory.iot.model.TagValue;
import com.byz.factory.iot.service.IotGatewayService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * IoT 网关 REST 控制器。
 * <p>
 * 提供设备连接管理、数据采集、指令下发和报警管理 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/iot")
@Tag(name = "IoT — 设备互联", description = "设备连接、数据采集、指令下发、报警管理")
public class IotController {

    @Autowired(required = false)
    private IotGatewayService iotGatewayService;

    // ── 设备连接管理 ──

    @PostMapping("/devices")
    @Operation(summary = "注册设备连接")
    public Result<Void> register(@RequestBody DeviceConnection connection) {
        iotGatewayService.register(connection);
        return Result.ok();
    }

    @DeleteMapping("/devices/{equipmentCode}")
    @Operation(summary = "注销设备连接")
    public Result<Void> unregister(@PathVariable String equipmentCode) {
        iotGatewayService.unregister(equipmentCode);
        return Result.ok();
    }

    @GetMapping("/devices/{equipmentCode}")
    @Operation(summary = "获取设备连接")
    public Result<DeviceConnection> getConnection(@PathVariable String equipmentCode) {
        return Result.ok(iotGatewayService.getConnection(equipmentCode));
    }

    @GetMapping("/devices/online")
    @Operation(summary = "获取所有在线设备")
    public Result<List<DeviceConnection>> getOnlineDevices() {
        return Result.ok(iotGatewayService.getOnlineDevices());
    }

    // ── 数据采集 ──

    @PostMapping("/devices/{equipmentCode}/read-tags")
    @Operation(summary = "读取实时标签值")
    public Result<Map<String, TagValue>> readTags(@PathVariable String equipmentCode,
                                                    @RequestBody String[] tagNames) {
        return Result.ok(iotGatewayService.readTags(equipmentCode, tagNames));
    }

    @PutMapping("/devices/{equipmentCode}/start-polling")
    @Operation(summary = "启动轮询采集")
    public Result<Void> startPolling(@PathVariable String equipmentCode) {
        iotGatewayService.startPolling(equipmentCode);
        return Result.ok();
    }

    @PutMapping("/devices/{equipmentCode}/stop-polling")
    @Operation(summary = "停止轮询采集")
    public Result<Void> stopPolling(@PathVariable String equipmentCode) {
        iotGatewayService.stopPolling(equipmentCode);
        return Result.ok();
    }

    @GetMapping("/devices/{equipmentCode}/is-polling")
    @Operation(summary = "是否正在采集")
    public Result<Boolean> isPolling(@PathVariable String equipmentCode) {
        return Result.ok(iotGatewayService.isPolling(equipmentCode));
    }

    // ── 指令下发 ──

    @PostMapping("/devices/{equipmentCode}/commands")
    @Operation(summary = "创建并下发指令")
    public Result<Command> sendCommand(@PathVariable String equipmentCode,
                                        @RequestParam Command.CommandType commandType,
                                        @RequestBody Map<String, Object> parameters) {
        return Result.ok(iotGatewayService.sendCommand(equipmentCode, commandType, parameters));
    }

    @GetMapping("/commands/{commandCode}")
    @Operation(summary = "查询指令状态")
    public Result<Command> getCommandStatus(@PathVariable String commandCode) {
        return Result.ok(iotGatewayService.getCommandStatus(commandCode));
    }

    // ── 报警管理 ──

    @GetMapping("/alarms/active")
    @Operation(summary = "获取所有活跃报警")
    public Result<List<AlarmEvent>> getActiveAlarms() {
        return Result.ok(iotGatewayService.getActiveAlarms());
    }

    @GetMapping("/alarms/active/{equipmentCode}")
    @Operation(summary = "获取设备活跃报警")
    public Result<List<AlarmEvent>> getActiveAlarms(@PathVariable String equipmentCode) {
        return Result.ok(iotGatewayService.getActiveAlarms(equipmentCode));
    }

    @PutMapping("/alarms/{alarmCode}/acknowledge")
    @Operation(summary = "确认报警")
    public Result<Void> acknowledgeAlarm(@PathVariable String alarmCode,
                                          @RequestParam String by) {
        iotGatewayService.acknowledgeAlarm(alarmCode, by);
        return Result.ok();
    }

}
