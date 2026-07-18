package com.byz.factory.iot.service;

import com.byz.factory.iot.model.AlarmEvent;
import com.byz.factory.iot.model.Command;
import com.byz.factory.iot.model.DeviceConnection;
import com.byz.factory.iot.model.TagValue;

import java.util.List;
import java.util.Map;

/**
 * IoT 网关服务 — 设备数据采集与指令下发。
 * <p>
 * 网关协调多个协议适配器，向上层（Equip/MES/QMS/Andon）提供：
 * <ul>
 *   <li>设备注册与连接管理</li>
 *   <li>实时数据采集（轮询/订阅）</li>
 *   <li>指令下发与生命周期跟踪</li>
 *   <li>报警检测与事件发布</li>
 * </ul>
 *
 * @author 苏政
 */
public interface IotGatewayService {

    // ── 设备连接管理 ──

    /** 注册设备连接 */
    void register(DeviceConnection connection);

    /** 注销设备连接 */
    void unregister(String equipmentCode);

    /** 获取设备连接 */
    DeviceConnection getConnection(String equipmentCode);

    /** 获取所有在线设备 */
    List<DeviceConnection> getOnlineDevices();

    // ── 数据采集 ──

    /** 读取实时标签值 */
    Map<String, TagValue> readTags(String equipmentCode, String... tagNames);

    /** 启动轮询采集 */
    void startPolling(String equipmentCode);

    /** 停止轮询采集 */
    void stopPolling(String equipmentCode);

    /** 是否正在采集 */
    boolean isPolling(String equipmentCode);

    // ── 指令下发 ──

    /** 创建并下发指令（返回指令对象供跟踪） */
    Command sendCommand(String equipmentCode, Command.CommandType commandType,
                        Map<String, Object> parameters);

    /** 查询指令状态 */
    Command getCommandStatus(String commandCode);

    // ── 报警管理 ──

    /** 获取未恢复的报警列表 */
    List<AlarmEvent> getActiveAlarms();

    /** 获取设备未恢复的报警列表 */
    List<AlarmEvent> getActiveAlarms(String equipmentCode);

    /** 确认报警 */
    void acknowledgeAlarm(String alarmCode, String by);

}
