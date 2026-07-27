package com.byz.factory.iot;

import java.time.Duration;
import java.time.Instant;

/**
 * 设备连接抽象 — 供 Equip/MES 跨模块引用设备在线状态。
 * <p>
 * IoT 模块管理设备通信连接的生命周期（注册/连接/断开/心跳），
 * 其他模块通过此接口获取设备的连接状态而无需依赖 IoT 内部实现。
 * <p>
 * <b>谁来引用？</b>
 * <ul>
 *   <li><b>Equip</b> — 判断设备是否在线，决定是否可下发参数</li>
 *   <li><b>MES</b> — 工单执行前检查设备连接状态</li>
 *   <li><b>Andon</b> — 设备断连时触发报警</li>
 * </ul>
 *
 * @author 苏政
 */
public interface IDeviceConnection {

    /** 关联设备编码 */
    String getEquipmentCode();

    /** 通信协议（OPC_UA / MODBUS_TCP / MQTT / S7_Comm / HTTP） */
    String getProtocol();

    /** 连接端点（如 opc.tcp://192.168.1.100:4840） */
    String getEndpoint();

    /** 采集间隔（毫秒） */
    int getPollIntervalMs();

    /** 最后连接/心跳时间 */
    Instant getLastConnected();

    /**
     * 判断设备是否在线。
     * 默认规则：最后心跳在 3 倍采集间隔内视为在线。
     */
    default boolean isOnline() {
        Instant last = getLastConnected();
        if (last == null) {
            return false;
        }
        long timeoutMs = getPollIntervalMs() * 3L;
        return Duration.between(last, Instant.now()).toMillis() < timeoutMs;
    }

}
