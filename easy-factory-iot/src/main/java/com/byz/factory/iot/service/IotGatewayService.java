package com.byz.factory.iot.service;

import com.byz.factory.iot.model.DeviceConnection;

import java.util.Map;

/**
 * IoT 网关服务 — 设备数据采集与指令下发。
 * <p>
 * TODO 待实现：协议适配器、标签采集、指令下发、报警联动
 *
 * @author 苏政
 */
public interface IotGatewayService {

    /** 注册设备连接 */
    void register(DeviceConnection connection);

    /** 读取实时数据 */
    Map<String, Object> readTags(String equipmentCode, String... tagNames);

    /** 下发指令 */
    boolean sendCommand(String equipmentCode, String commandType, Map<String, Object> parameters);

    /** 启动采集 */
    void startPolling(String equipmentCode);

    /** 停止采集 */
    void stopPolling(String equipmentCode);

}
