package com.byz.factory.iot;

import com.byz.factory.event.types.IotEventTypes;
import com.byz.factory.iot.*;
import com.byz.factory.iot.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IoT 模块测试")
class IotModuleTest {

    // ==================== DeviceConnection ====================

    @Test
    @DisplayName("DeviceConnection 创建 — code 自动设为 equipmentCode")
    void deviceConnection_creation_shouldUseEquipmentCodeAsEntityCode() {
        // Given
        // When
        DeviceConnection conn = new DeviceConnection("EQ-001", "OPC_UA", "opc.tcp://192.168.1.100:4840");

        // Then
        assertEquals("EQ-001", conn.getCode());
        assertEquals("EQ-001", conn.getEquipmentCode());
        assertEquals("连接-EQ-001", conn.getName());
        assertEquals("OPC_UA", conn.getProtocol());
        assertEquals("opc.tcp://192.168.1.100:4840", conn.getEndpoint());
        assertEquals(1000, conn.getPollIntervalMs());
        assertNotNull(conn.getLastConnected());
        assertNotNull(conn.getCreatedAt());
    }

    @Test
    @DisplayName("DeviceConnection 创建 — MODBUS 协议")
    void deviceConnection_creation_modbus_shouldSetCorrectly() {
        // Given
        // When
        DeviceConnection conn = new DeviceConnection("EQ-002", "MODBUS_TCP", "192.168.1.101:502");

        // Then
        assertEquals("MODBUS_TCP", conn.getProtocol());
        assertEquals("192.168.1.101:502", conn.getEndpoint());
    }

    @Test
    @DisplayName("DeviceConnection 创建 — MQTT 协议")
    void deviceConnection_creation_mqtt_shouldSetCorrectly() {
        // Given
        // When
        DeviceConnection conn = new DeviceConnection("EQ-003", "MQTT", "tcp://broker.emqx.io:1883");

        // Then
        assertEquals("MQTT", conn.getProtocol());
    }

    @Test
    @DisplayName("DeviceConnection isOnline — 刚创建时在线")
    void deviceConnection_isOnline_whenJustCreated_shouldReturnTrue() {
        // Given
        DeviceConnection conn = new DeviceConnection("EQ-004", "OPC_UA", "opc.tcp://localhost:4840");

        // When & Then
        assertTrue(conn.isOnline());
    }

    @Test
    @DisplayName("DeviceConnection isOnline — isTimedOut 检查超时")
    void deviceConnection_isTimedOut_withOldHeartbeat_shouldReturnTrue() {
        // Given
        DeviceConnection conn = new DeviceConnection("EQ-005", "OPC_UA", "opc.tcp://localhost:4840");
        // 模拟 4 秒前的心跳（超过 3 倍 pollIntervalMs=3000ms）
        conn.setLastConnected(Instant.now().minusSeconds(4));

        // When & Then
        assertTrue(conn.isTimedOut());
        assertFalse(conn.isOnline());
    }

    @Test
    @DisplayName("DeviceConnection heartbeat — 更新心跳时间")
    void deviceConnection_heartbeat_shouldUpdateLastConnected() throws Exception {
        // Given
        DeviceConnection conn = new DeviceConnection("EQ-006", "OPC_UA", "opc.tcp://localhost:4840");
        Instant before = conn.getLastConnected();
        // 短暂等待确保时间戳不同（Instant 精度为微秒级）
        Thread.sleep(1);

        // When
        conn.heartbeat();

        // Then
        assertTrue(conn.getLastConnected().isAfter(before));
        assertNotNull(conn.getUpdatedAt());
    }

    @Test
    @DisplayName("DeviceConnection authConfig — 可设置认证配置")
    void deviceConnection_authConfig_shouldBeSettable() {
        // Given
        DeviceConnection conn = new DeviceConnection("EQ-007", "OPC_UA", "opc.tcp://localhost:4840");

        // When
        conn.setAuthConfig("cert: /etc/ssl/client.pfx");

        // Then
        assertEquals("cert: /etc/ssl/client.pfx", conn.getAuthConfig());
    }

    @Test
    @DisplayName("DeviceConnection IExpand — 支持动态属性")
    void deviceConnection_expand_shouldSupportDynamicProperties() {
        // Given
        DeviceConnection conn = new DeviceConnection("EQ-008", "OPC_UA", "opc.tcp://localhost:4840");

        // When
        conn.setProperty("iot.authType", "CERT");
        conn.setProperty("iot.tags", "[\"TEMP_001\", \"PRESS_001\"]");

        // Then
        assertEquals("CERT", conn.getProperty("iot.authType"));
        assertEquals("[\"TEMP_001\", \"PRESS_001\"]", conn.getProperty("iot.tags"));
    }

    @Test
    @DisplayName("DeviceConnection 实现 IDeviceConnection 接口")
    void deviceConnection_shouldImplementIDeviceConnection() {
        // Given
        DeviceConnection conn = new DeviceConnection("EQ-009", "HTTP", "http://192.168.1.200/api");

        // Then
        assertInstanceOf(IDeviceConnection.class, conn);
    }

    // ==================== TagValue ====================

    @Test
    @DisplayName("TagValue 创建 — 基本字段设置正确")
    void tagValue_creation_shouldSetFields() {
        // Given
        // When
        TagValue tv = new TagValue("EQ-001", "TEMP_001", new BigDecimal("82.5"));

        // Then
        assertEquals("EQ-001", tv.getEquipmentCode());
        assertEquals("TEMP_001", tv.getTagName());
        assertEquals(new BigDecimal("82.5"), tv.getValue());
        assertEquals(TagValue.TagQuality.GOOD, tv.getQuality());
        assertNotNull(tv.getTimestamp());
        assertNotNull(tv.getServerTimestamp());
        assertEquals(BigDecimal.ONE, tv.getMultiplier());
        assertEquals(BigDecimal.ZERO, tv.getOffset());
    }

    @Test
    @DisplayName("TagValue of — 工厂方法创建 GOOD 质量标签值")
    void tagValue_of_shouldCreateGoodQuality() {
        // Given
        // When
        TagValue tv = TagValue.of("EQ-002", "SPEED", new BigDecimal("1450"));

        // Then
        assertEquals("EQ-002", tv.getEquipmentCode());
        assertEquals("SPEED", tv.getTagName());
        assertEquals(new BigDecimal("1450"), tv.getScaledValue());
        assertEquals(TagValue.TagQuality.GOOD, tv.getQuality());
    }

    @Test
    @DisplayName("TagValue bad — 工厂方法创建 BAD 质量标签值")
    void tagValue_bad_shouldCreateBadQuality() {
        // Given
        // When
        TagValue tv = TagValue.bad("EQ-003", "TEMP_002");

        // Then
        assertEquals(TagValue.TagQuality.BAD, tv.getQuality());
        assertNull(tv.getValue());
    }

    @Test
    @DisplayName("TagValue uncertain — 工厂方法创建 UNCERTAIN 质量标签值")
    void tagValue_uncertain_shouldCreateUncertainQuality() {
        // Given
        // When
        TagValue tv = TagValue.uncertain("EQ-004", "PRESS_001", 999.9);

        // Then
        assertEquals(TagValue.TagQuality.UNCERTAIN, tv.getQuality());
        assertEquals(999.9, tv.getValue());
    }

    @Test
    @DisplayName("TagValue setScaling — 换算系数和偏移量")
    void tagValue_setScaling_shouldApplyMultiplierAndOffset() {
        // Given
        TagValue tv = new TagValue("EQ-005", "TEMP_003", new BigDecimal("100"));
        // 4-20mA → 0-100°C: scaled = raw * 6.25 - 25

        // When
        tv.setScaling(new BigDecimal("6.25"), new BigDecimal("-25"));

        // Then
        assertEquals(new BigDecimal("6.25"), tv.getMultiplier());
        assertEquals(new BigDecimal("-25"), tv.getOffset());
        assertEquals(new BigDecimal("600.00"), tv.getScaledValue());
    }

    @Test
    @DisplayName("TagValue batchId — 可设置批次 ID")
    void tagValue_batchId_shouldBeSettable() {
        // Given
        TagValue tv = TagValue.of("EQ-006", "FLOW_001", new BigDecimal("50.0"));

        // When
        tv.setBatchId("BATCH-2026-001");

        // Then
        assertEquals("BATCH-2026-001", tv.getBatchId());
    }

    @Test
    @DisplayName("TagValue 实现 ITagValue 接口")
    void tagValue_shouldImplementITagValue() {
        // Given
        TagValue tv = TagValue.of("EQ-007", "TEMP", BigDecimal.TEN);

        // Then
        assertInstanceOf(ITagValue.class, tv);
    }

    // ==================== Command ====================

    @Test
    @DisplayName("Command 创建 — 初始状态为 QUEUED")
    void command_creation_shouldBeQueued() {
        // Given
        // When
        Command cmd = new Command("CMD-001", "EQ-001",
                ICommand.CommandType.SET_PARAM, Map.of("TEMP_Setpoint", 120.0));

        // Then
        assertEquals("CMD-001", cmd.getCode());
        assertEquals("EQ-001", cmd.getEquipmentCode());
        assertEquals(ICommand.CommandType.SET_PARAM, cmd.getCommandType());
        assertEquals(CommandStatus.QUEUED, cmd.getStatus());
        assertEquals(ICommand.CommandPriority.NORMAL, cmd.getPriority());
        assertNotNull(cmd.getParameters());
        assertEquals(120.0, cmd.getParameters().get("TEMP_Setpoint"));
    }

    @Test
    @DisplayName("Command 生命周期 — QUEUED → SENT → ACKNOWLEDGED → COMPLETED")
    void command_lifecycle_normalPath_shouldTransitionCorrectly() {
        // Given
        Command cmd = new Command("CMD-002", "EQ-002",
                ICommand.CommandType.START, Map.of());

        // When & Then: QUEUED → SENT
        cmd.markSent();
        assertEquals(CommandStatus.SENT, cmd.getStatus());
        assertNotNull(cmd.getSentAt());

        // When & Then: SENT → ACKNOWLEDGED
        cmd.markAcknowledged();
        assertEquals(CommandStatus.ACKNOWLEDGED, cmd.getStatus());
        assertNotNull(cmd.getAcknowledgedAt());

        // When & Then: ACKNOWLEDGED → COMPLETED
        cmd.markCompleted("0", "执行成功");
        assertEquals(CommandStatus.COMPLETED, cmd.getStatus());
        assertEquals("0", cmd.getResultCode());
        assertEquals("执行成功", cmd.getResultMessage());
        assertNotNull(cmd.getCompletedAt());
    }

    @Test
    @DisplayName("Command 生命周期 — SENT → FAILED（设备拒绝）")
    void command_lifecycle_sentToFailed_shouldTransitionCorrectly() {
        // Given
        Command cmd = new Command("CMD-003", "EQ-003",
                ICommand.CommandType.SET_PARAM, Map.of("SPEED", 99999));
        cmd.markSent();

        // When
        cmd.markFailed("E001", "参数值超出范围");

        // Then
        assertEquals(CommandStatus.FAILED, cmd.getStatus());
        assertEquals("E001", cmd.getResultCode());
        assertEquals("参数值超出范围", cmd.getResultMessage());
    }

    @Test
    @DisplayName("Command 生命周期 — ACKNOWLEDGED → FAILED（执行超时）")
    void command_lifecycle_acknowledgedToFailed_shouldTransitionCorrectly() {
        // Given
        Command cmd = new Command("CMD-004", "EQ-004",
                ICommand.CommandType.START, Map.of());
        cmd.markSent();
        cmd.markAcknowledged();

        // When
        cmd.markFailed("TIMEOUT", "设备执行超时");

        // Then
        assertEquals(CommandStatus.FAILED, cmd.getStatus());
    }

    @Test
    @DisplayName("Command cancel — QUEUED → CANCELLED")
    void command_cancel_shouldTransitionToCancelled() {
        // Given
        Command cmd = new Command("CMD-005", "EQ-005",
                ICommand.CommandType.READ, Map.of());

        // When
        cmd.cancel();

        // Then
        assertEquals(CommandStatus.CANCELLED, cmd.getStatus());
    }

    @Test
    @DisplayName("Command 非法转换 — COMPLETED 是终态，不能再转")
    void command_illegalTransition_fromCompleted_shouldThrow() {
        // Given
        Command cmd = new Command("CMD-006", "EQ-006",
                ICommand.CommandType.SET_PARAM, Map.of());
        cmd.markSent();
        cmd.markAcknowledged();
        cmd.markCompleted("0", "OK");

        // When & Then: COMPLETED → 任何状态都非法
        assertThrows(IllegalStateException.class, () -> cmd.markSent());
    }

    @Test
    @DisplayName("Command 非法转换 — FAILED 是终态，不能再转")
    void command_illegalTransition_fromFailed_shouldThrow() {
        // Given
        Command cmd = new Command("CMD-007", "EQ-007",
                ICommand.CommandType.START, Map.of());
        cmd.markSent();
        cmd.markFailed("E999", "致命错误");

        // When & Then: FAILED → 任何状态都非法
        assertThrows(IllegalStateException.class, () -> cmd.markCompleted("0", "OK"));
    }

    @Test
    @DisplayName("Command 非法转换 — QUEUED 不能直接 COMPLETED")
    void command_illegalTransition_queuedToCompleted_shouldThrow() {
        // Given
        Command cmd = new Command("CMD-008", "EQ-008",
                ICommand.CommandType.START, Map.of());

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> cmd.markCompleted("0", "OK"));
    }

    @Test
    @DisplayName("Command canTransition — 检查允许的目标状态")
    void command_canTransition_shouldCheckAllowedTargets() {
        // Given
        Command cmd = new Command("CMD-009", "EQ-009",
                ICommand.CommandType.SET_PARAM, Map.of());

        // Then: QUEUED 可以转 SENT 和 CANCELLED
        assertTrue(cmd.canTransition(CommandStatus.SENT));
        assertTrue(cmd.canTransition(CommandStatus.CANCELLED));
        assertFalse(cmd.canTransition(CommandStatus.COMPLETED));
        assertFalse(cmd.canTransition(CommandStatus.ACKNOWLEDGED));
    }

    @Test
    @DisplayName("Command 优先级 — 可设置为 HIGH")
    void command_priority_high_shouldBeSettable() {
        // Given
        Command cmd = new Command("CMD-010", "EQ-010",
                ICommand.CommandType.STOP, Map.of("reason", "紧急"));
        cmd.setPriority(ICommand.CommandPriority.HIGH);

        // Then
        assertEquals(ICommand.CommandPriority.HIGH, cmd.getPriority());
    }

    @Test
    @DisplayName("Command 超时 — 可自定义超时时间")
    void command_timeout_shouldBeCustomizable() {
        // Given
        Command cmd = new Command("CMD-011", "EQ-011",
                ICommand.CommandType.READ, Map.of());

        // When
        cmd.setTimeout(java.time.Duration.ofSeconds(10));

        // Then
        assertEquals(java.time.Duration.ofSeconds(10), cmd.getTimeout());
    }

    @Test
    @DisplayName("Command 实现 ICommand 接口")
    void command_shouldImplementICommand() {
        // Given
        Command cmd = new Command("CMD-012", "EQ-012",
                ICommand.CommandType.START, Map.of());

        // Then
        assertInstanceOf(ICommand.class, cmd);
    }

    // ==================== AlarmEvent ====================

    @Test
    @DisplayName("AlarmEvent 创建 — 基本字段设置正确")
    void alarmEvent_creation_shouldSetFields() {
        // Given
        // When
        AlarmEvent alarm = new AlarmEvent("TEMP_HIGH", "EQ-001",
                IAlarmEvent.AlarmSeverity.CRITICAL,
                new BigDecimal("135.0"), new BigDecimal("130.0"));

        // Then
        assertEquals("TEMP_HIGH", alarm.getAlarmCode());
        assertEquals("TEMP_HIGH", alarm.getCode()); // code = alarmCode
        assertEquals("EQ-001", alarm.getEquipmentCode());
        assertEquals(IAlarmEvent.AlarmSeverity.CRITICAL, alarm.getSeverity());
        assertEquals(new BigDecimal("135.0"), alarm.getTriggerValue());
        assertEquals(new BigDecimal("130.0"), alarm.getThreshold());
        assertNotNull(alarm.getTriggeredAt());
        assertTrue(alarm.getActions().isEmpty());
    }

    @Test
    @DisplayName("AlarmEvent of — 工厂方法创建含消息的报警")
    void alarmEvent_of_shouldSetMessage() {
        // Given
        // When
        AlarmEvent alarm = AlarmEvent.of("PRESS_LOW", "EQ-002",
                IAlarmEvent.AlarmSeverity.WARNING,
                "压力低于正常范围",
                new BigDecimal("3.5"), new BigDecimal("4.0"));

        // Then
        assertEquals("压力低于正常范围", alarm.getMessage());
        assertEquals(IAlarmEvent.AlarmSeverity.WARNING, alarm.getSeverity());
    }

    @Test
    @DisplayName("AlarmEvent acknowledge — 确认报警")
    void alarmEvent_acknowledge_shouldSetAcknowledgedInfo() {
        // Given
        AlarmEvent alarm = new AlarmEvent("ALM-001", "EQ-003",
                IAlarmEvent.AlarmSeverity.CRITICAL,
                new BigDecimal("150.0"), new BigDecimal("140.0"));

        // When
        alarm.acknowledge("操作员张三");

        // Then
        assertEquals("操作员张三", alarm.getAcknowledgedBy());
        assertNotNull(alarm.getAcknowledgedAt());
        assertTrue(alarm.isAcknowledged());
        assertFalse(alarm.isResolved());
    }

    @Test
    @DisplayName("AlarmEvent resolve — 恢复报警")
    void alarmEvent_resolve_shouldSetResolvedTime() {
        // Given
        AlarmEvent alarm = new AlarmEvent("ALM-002", "EQ-004",
                IAlarmEvent.AlarmSeverity.WARNING,
                new BigDecimal("85.0"), new BigDecimal("80.0"));
        alarm.acknowledge("操作员李四");

        // When
        alarm.resolve();

        // Then
        assertNotNull(alarm.getResolvedAt());
        assertTrue(alarm.isResolved());
    }

    @Test
    @DisplayName("AlarmEvent addAction — 添加联动动作")
    void alarmEvent_addAction_shouldAppendToActions() {
        // Given
        AlarmEvent alarm = new AlarmEvent("ALM-003", "EQ-005",
                IAlarmEvent.AlarmSeverity.EMERGENCY,
                new BigDecimal("200.0"), new BigDecimal("180.0"));

        // When
        alarm.addAction("STOP_MACHINE");
        alarm.addAction("NOTIFY_SUPERVISOR");
        alarm.addAction("CREATE_QMS_DEVIATION");

        // Then
        assertEquals(3, alarm.getActions().size());
        assertTrue(alarm.getActions().contains("STOP_MACHINE"));
        assertTrue(alarm.getActions().contains("NOTIFY_SUPERVISOR"));
        assertTrue(alarm.getActions().contains("CREATE_QMS_DEVIATION"));

        // 返回的列表不可修改
        assertThrows(UnsupportedOperationException.class,
                () -> alarm.getActions().add("EXTRA"));
    }

    @Test
    @DisplayName("AlarmEvent 严重程度 — 四种级别枚举")
    void alarmEvent_severity_shouldHaveFourLevels() {
        // Then
        assertEquals(4, IAlarmEvent.AlarmSeverity.values().length);
        assertEquals(IAlarmEvent.AlarmSeverity.INFO, IAlarmEvent.AlarmSeverity.valueOf("INFO"));
        assertEquals(IAlarmEvent.AlarmSeverity.WARNING, IAlarmEvent.AlarmSeverity.valueOf("WARNING"));
        assertEquals(IAlarmEvent.AlarmSeverity.CRITICAL, IAlarmEvent.AlarmSeverity.valueOf("CRITICAL"));
        assertEquals(IAlarmEvent.AlarmSeverity.EMERGENCY, IAlarmEvent.AlarmSeverity.valueOf("EMERGENCY"));
    }

    @Test
    @DisplayName("AlarmEvent 实现 IAlarmEvent 接口")
    void alarmEvent_shouldImplementIAlarmEvent() {
        // Given
        AlarmEvent alarm = new AlarmEvent("ALM-004", "EQ-006",
                IAlarmEvent.AlarmSeverity.INFO,
                BigDecimal.TEN, BigDecimal.ONE);

        // Then
        assertInstanceOf(IAlarmEvent.class, alarm);
    }

    // ==================== CommandStatus 状态机 ====================

    @Test
    @DisplayName("CommandStatus — QUEUED 的允许转换")
    void commandStatus_queued_allowedTransitions() {
        java.util.Set<CommandStatus> allowed = CommandStatus.QUEUED.allowedTransitions();
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(CommandStatus.SENT));
        assertTrue(allowed.contains(CommandStatus.CANCELLED));
    }

    @Test
    @DisplayName("CommandStatus — SENT 的允许转换")
    void commandStatus_sent_allowedTransitions() {
        java.util.Set<CommandStatus> allowed = CommandStatus.SENT.allowedTransitions();
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(CommandStatus.ACKNOWLEDGED));
        assertTrue(allowed.contains(CommandStatus.FAILED));
    }

    @Test
    @DisplayName("CommandStatus — 终态无允许转换")
    void commandStatus_terminalStates_shouldHaveNoTransitions() {
        assertTrue(CommandStatus.COMPLETED.allowedTransitions().isEmpty());
        assertTrue(CommandStatus.FAILED.allowedTransitions().isEmpty());
        assertTrue(CommandStatus.CANCELLED.allowedTransitions().isEmpty());
    }

    // ==================== IotEventTypes ====================

    @Test
    @DisplayName("IotEventTypes — 事件类型遵循 {module}.{entity}.{past_tense} 命名约定")
    void iotEventTypes_shouldFollowNamingConvention() {
        // Then: 所有事件常量以 "iot." 开头
        assertTrue(IotEventTypes.DEVICE_CONNECTED.startsWith("iot."));
        assertTrue(IotEventTypes.DEVICE_DISCONNECTED.startsWith("iot."));
        assertTrue(IotEventTypes.TAG_VALUE_COLLECTED.startsWith("iot."));
        assertTrue(IotEventTypes.COMMAND_SENT.startsWith("iot."));
        assertTrue(IotEventTypes.COMMAND_COMPLETED.startsWith("iot."));
        assertTrue(IotEventTypes.COMMAND_FAILED.startsWith("iot."));
        assertTrue(IotEventTypes.ALARM_TRIGGERED.startsWith("iot."));
        assertTrue(IotEventTypes.ALARM_ACKNOWLEDGED.startsWith("iot."));
        assertTrue(IotEventTypes.ALARM_RESOLVED.startsWith("iot."));
    }

    @Test
    @DisplayName("IotEventTypes — PREFIX 为 iot")
    void iotEventTypes_prefix_shouldBeIot() {
        assertEquals("iot", IotEventTypes.PREFIX);
    }

    @Test
    @DisplayName("IotEventTypes — 共 9 个事件类型")
    void iotEventTypes_count_shouldBeNine() {
        // 用反射简单验证常量数量
        assertEquals(9, IotEventTypes.class.getDeclaredFields().length - 1); // -1 排除 PREFIX
    }

    // ==================== ICommand.CommandType ====================

    @Test
    @DisplayName("CommandType — 五种指令类型")
    void commandType_shouldHaveFiveTypes() {
        // Then
        assertEquals(5, ICommand.CommandType.values().length);
        assertEquals(ICommand.CommandType.SET_PARAM, ICommand.CommandType.valueOf("SET_PARAM"));
        assertEquals(ICommand.CommandType.START, ICommand.CommandType.valueOf("START"));
        assertEquals(ICommand.CommandType.STOP, ICommand.CommandType.valueOf("STOP"));
        assertEquals(ICommand.CommandType.READ, ICommand.CommandType.valueOf("READ"));
        assertEquals(ICommand.CommandType.ACK_ALARM, ICommand.CommandType.valueOf("ACK_ALARM"));
    }

    // ==================== ICommand.CommandPriority ====================

    @Test
    @DisplayName("CommandPriority — 三级优先级")
    void commandPriority_shouldHaveThreeLevels() {
        // Then
        assertEquals(3, ICommand.CommandPriority.values().length);
        assertEquals(ICommand.CommandPriority.HIGH, ICommand.CommandPriority.valueOf("HIGH"));
        assertEquals(ICommand.CommandPriority.NORMAL, ICommand.CommandPriority.valueOf("NORMAL"));
        assertEquals(ICommand.CommandPriority.LOW, ICommand.CommandPriority.valueOf("LOW"));
    }

    // ==================== ITagValue.TagQuality ====================

    @Test
    @DisplayName("TagQuality — 三级数据质量")
    void tagQuality_shouldHaveThreeLevels() {
        // Then
        assertEquals(3, ITagValue.TagQuality.values().length);
        assertEquals(ITagValue.TagQuality.GOOD, ITagValue.TagQuality.valueOf("GOOD"));
        assertEquals(ITagValue.TagQuality.BAD, ITagValue.TagQuality.valueOf("BAD"));
        assertEquals(ITagValue.TagQuality.UNCERTAIN, ITagValue.TagQuality.valueOf("UNCERTAIN"));
    }

    // ==================== IotGatewayService 接口验证 ====================

    @Test
    @DisplayName("IotGatewayService — 定义了完整的网关服务契约")
    void iotGatewayService_shouldDefineCompleteContract() {
        // Then: 验证接口方法存在
        assertEquals(13, com.byz.factory.iot.service.IotGatewayService.class.getDeclaredMethods().length);
    }

    // ==================== IProtocolAdapter 接口验证 ====================

    @Test
    @DisplayName("IProtocolAdapter — 定义了协议适配器契约")
    void iProtocolAdapter_shouldDefineContract() {
        // Then: 验证接口方法存在
        assertEquals(6, com.byz.factory.iot.service.IProtocolAdapter.class.getDeclaredMethods().length);
    }

}
