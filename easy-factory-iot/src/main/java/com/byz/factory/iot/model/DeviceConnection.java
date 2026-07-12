package com.byz.factory.iot.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * IoT 设备连接 — 管理设备通信协议和连接状态。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceConnection extends DataExpand {

    private String equipmentCode;
    private String protocol;            // OPC_UA / MODBUS_TCP / MQTT / S7
    private String endpoint;
    private int pollIntervalMs;
    private String status;              // ONLINE / OFFLINE / ERROR
    private Instant lastConnected;

    public DeviceConnection(String equipmentCode, String protocol, String endpoint) {
        this.equipmentCode = equipmentCode;
        this.protocol = protocol;
        this.endpoint = endpoint;
        this.pollIntervalMs = 1000;
        this.status = "OFFLINE";
    }

}
