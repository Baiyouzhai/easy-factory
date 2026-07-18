package com.byz.factory.iot.service;

import com.byz.factory.iot.model.DeviceConnection;
import com.byz.factory.iot.model.TagValue;

import java.util.List;
import java.util.function.Consumer;

/**
 * 协议适配器 — 工业通信协议的统一抽象。
 * <p>
 * 每种工业协议（OPC UA / Modbus / MQTT / Siemens S7 / HTTP）提供各自的实现，
 * 通过此接口向上层屏蔽协议差异，提供统一的连接/读写/订阅能力。
 * <p>
 * <b>目标实现：</b>
 * <ul>
 *   <li>{@code OpcUaAdapter} — OPC UA 协议（Eclipse Milo）</li>
 *   <li>{@code ModbusAdapter} — Modbus TCP/RTU</li>
 *   <li>{@code MqttAdapter} — MQTT（IoT 网关）</li>
 *   <li>{@code S7Adapter} — Siemens S7（西门子 PLC）</li>
 *   <li>{@code HttpAdapter} — REST API（简易设备）</li>
 * </ul>
 *
 * @author 苏政
 */
public interface IProtocolAdapter {

    /**
     * 建立连接。
     * @param connection 设备连接配置
     * @return true=连接成功
     */
    boolean connect(DeviceConnection connection);

    /** 断开连接 */
    void disconnect();

    /** 是否已连接 */
    boolean isConnected();

    /**
     * 批量读取标签值。
     * @param tags 标签名列表
     * @return 标签值列表
     */
    List<TagValue> read(List<String> tags);

    /**
     * 写入单个标签值。
     * @param tag   标签名
     * @param value 写入值
     * @return true=写入成功
     */
    boolean write(String tag, Object value);

    /**
     * 订阅标签变化（推送模式）。
     * @param tags     订阅标签列表
     * @param callback 数据回调
     */
    void subscribe(List<String> tags, Consumer<TagValue> callback);

}
