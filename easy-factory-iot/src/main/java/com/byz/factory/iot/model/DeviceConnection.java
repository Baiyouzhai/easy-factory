package com.byz.factory.iot.model;

import com.byz.factory.iot.IDeviceConnection;
import com.byz.factory.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

/**
 * IoT 设备连接 — 继承 BaseEntity 获得 code/name/audit，实现 IDeviceConnection。
 * <p>
 * 设备连接以 equipmentCode 作为实体 code，管理通信生命周期：
 * 注册 → 连接 → 心跳维持 → 断开。
 * <p>
 * <h3>IExpand 约定</h3>
 * <pre>
 *   iot.connectionStatus   — 连接状态 (ONLINE / OFFLINE / ERROR)
 *   iot.lastHeartbeat      — 最后心跳时间
 *   iot.authType           — 认证方式 (CERT / USERNAME_PASSWORD / TOKEN / NONE)
 *   iot.tags               — 采集标签列表 (JSON)
 * </pre>
 *
 * @author 苏政
 */
@Entity
@Table(name = "iot_device_connection")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DeviceConnection extends BaseEntity implements IDeviceConnection {

    @Column(nullable = false, length = 50)
    private String protocol;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(name = "poll_interval_ms")
    private int pollIntervalMs;

    @Column(name = "last_connected")
    private Instant lastConnected;

    @Column(name = "auth_config", length = 500)
    private String authConfig;

    /**
     * @param equipmentCode 关联设备编码（同时作为实体 code）
     * @param protocol      通信协议
     * @param endpoint      连接端点
     */
    public DeviceConnection(String equipmentCode, String protocol, String endpoint) {
        super(equipmentCode, "连接-" + equipmentCode);
        this.protocol = protocol;
        this.endpoint = endpoint;
        this.pollIntervalMs = 1000;
        this.lastConnected = Instant.now();
    }

    // ==================== 业务便捷方法 ====================

    /** 获取设备编码（继承的 code 字段即 equipmentCode） */
    @Override
    public String getEquipmentCode() {
        return getCode();
    }

    /** 更新心跳时间 */
    public void heartbeat() {
        this.lastConnected = Instant.now();
        markUpdated();
    }

    /**
     * 判断连接是否超时。
     * 默认规则：最后心跳距今超过 3 倍采集间隔视为离线。
     */
    public boolean isTimedOut() {
        long timeoutMs = pollIntervalMs * 3L;
        return Duration.between(lastConnected, Instant.now()).toMillis() > timeoutMs;
    }

}
