package com.byz.factory.iot.model;

import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * IoT 设备连接 — 继承 BaseEntity 获得 code/name/audit。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceConnection extends BaseEntity {

    private String protocol;
    private String endpoint;
    private int pollIntervalMs;
    private Instant lastConnected;

    public DeviceConnection(String equipmentCode, String protocol, String endpoint) {
        super(equipmentCode, "连接-" + equipmentCode);
        this.protocol = protocol;
        this.endpoint = endpoint;
        this.pollIntervalMs = 1000;
        this.lastConnected = Instant.now();
    }

}
