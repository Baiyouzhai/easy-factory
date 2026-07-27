package com.byz.factory.iot.service.impl;

import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.IotEventTypes;
import com.byz.factory.iot.ICommand;
import com.byz.factory.iot.model.AlarmEvent;
import com.byz.factory.iot.model.Command;
import com.byz.factory.iot.model.DeviceConnection;
import com.byz.factory.iot.model.TagValue;
import com.byz.factory.iot.repository.AlarmEventRepository;
import com.byz.factory.iot.repository.CommandRepository;
import com.byz.factory.iot.repository.DeviceConnectionRepository;
import com.byz.factory.iot.repository.TagValueRepository;
import com.byz.factory.iot.service.IProtocolAdapter;
import com.byz.factory.iot.service.IotGatewayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 网关服务实现 — 基于本地 Repository 的设备数据采集与指令管理。
 * <p>
 * 约定：
 * <ul>
 *   <li>写操作 @Transactional + publish 领域事件</li>
 *   <li>状态变更后 publish 对应 IotEventTypes 事件常量</li>
 *   <li>读操作 @Transactional(readOnly = true)</li>
 *   <li>轮询状态由内存 Map 管理（pending: 迁移到数据库）</li>
 * </ul>
 *
 * @author easy-factory
 */
@Service
public class IotGatewayServiceImpl implements IotGatewayService {

    private final DeviceConnectionRepository connRepo;
    private final TagValueRepository tagRepo;
    private final CommandRepository cmdRepo;
    private final AlarmEventRepository alarmRepo;

    /** 协议适配器注册表（protocol → adapter） */
    private final Map<String, IProtocolAdapter> adapters = new ConcurrentHashMap<>();

    /** 轮询状态（equipmentCode → isPolling） */
    private final Map<String, Boolean> pollingState = new ConcurrentHashMap<>();

    public IotGatewayServiceImpl(DeviceConnectionRepository connRepo,
                                  TagValueRepository tagRepo,
                                  CommandRepository cmdRepo,
                                  AlarmEventRepository alarmRepo) {
        this.connRepo = connRepo;
        this.tagRepo = tagRepo;
        this.cmdRepo = cmdRepo;
        this.alarmRepo = alarmRepo;
    }

    // ==================== 设备连接管理 ====================

    @Override
    @Transactional
    public void register(DeviceConnection connection) {
        connRepo.save(connection);
        DomainEventPublisher.publish(IDomainEvent.of(
                IotEventTypes.DEVICE_CONNECTED, "iot",
                Map.of("equipmentCode", connection.getEquipmentCode(),
                       "protocol", connection.getProtocol())));
    }

    @Override
    @Transactional
    public void unregister(String equipmentCode) {
        DeviceConnection conn = connRepo.findByCode(equipmentCode);
        if (conn != null) {
            connRepo.delete(conn);
            stopPolling(equipmentCode);
            DomainEventPublisher.publish(IDomainEvent.of(
                    IotEventTypes.DEVICE_DISCONNECTED, "iot",
                    Map.of("equipmentCode", equipmentCode)));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceConnection getConnection(String equipmentCode) {
        return connRepo.findByCode(equipmentCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceConnection> getOnlineDevices() {
        List<DeviceConnection> all = connRepo.findAll();
        return all.stream().filter(DeviceConnection::isOnline).toList();
    }

    // ==================== 数据采集 ====================

    @Override
    @Transactional(readOnly = true)
    public Map<String, TagValue> readTags(String equipmentCode, String... tagNames) {
        if (tagNames == null || tagNames.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, TagValue> result = new LinkedHashMap<>();
        for (String tagName : tagNames) {
            List<TagValue> tags = tagRepo.findByEquipmentCodeAndTagName(equipmentCode, tagName);
            if (!tags.isEmpty()) {
                result.put(tagName, tags.get(tags.size() - 1)); // 取最新值
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void startPolling(String equipmentCode) {
        DeviceConnection conn = connRepo.findByCode(equipmentCode);
        if (conn == null) {
            throw new IllegalArgumentException("设备连接不存在: " + equipmentCode);
        }
        pollingState.put(equipmentCode, true);
    }

    @Override
    @Transactional
    public void stopPolling(String equipmentCode) {
        pollingState.remove(equipmentCode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPolling(String equipmentCode) {
        return Boolean.TRUE.equals(pollingState.get(equipmentCode));
    }

    // ==================== 指令下发 ====================

    @Override
    @Transactional
    public Command sendCommand(String equipmentCode, ICommand.CommandType commandType,
                                Map<String, Object> parameters) {
        requireConnection(equipmentCode); // 验证设备连接存在
        String commandCode = "CMD-" + equipmentCode + "-" + System.currentTimeMillis();

        Command cmd = new Command(commandCode, equipmentCode, commandType, parameters);
        cmd.markSent();
        Command saved = cmdRepo.save(cmd);

        DomainEventPublisher.publish(IDomainEvent.of(
                IotEventTypes.COMMAND_SENT, "iot",
                Map.of("commandCode", saved.getCode(),
                       "equipmentCode", equipmentCode,
                       "commandType", commandType.name())));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Command getCommandStatus(String commandCode) {
        return cmdRepo.findByCode(commandCode);
    }

    // ==================== 报警管理 ====================

    @Override
    @Transactional(readOnly = true)
    public List<AlarmEvent> getActiveAlarms() {
        return alarmRepo.findActiveAlarms();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlarmEvent> getActiveAlarms(String equipmentCode) {
        return alarmRepo.findActiveAlarmsByEquipment(equipmentCode);
    }

    @Override
    @Transactional
    public void acknowledgeAlarm(String alarmCode, String by) {
        AlarmEvent alarm = alarmRepo.findByCode(alarmCode);
        if (alarm == null) {
            throw new IllegalArgumentException("报警不存在: " + alarmCode);
        }
        alarm.acknowledge(by);
        alarmRepo.save(alarm);

        DomainEventPublisher.publish(IDomainEvent.of(
                IotEventTypes.ALARM_ACKNOWLEDGED, "iot",
                Map.of("alarmCode", alarmCode, "acknowledgedBy", by)));
    }

    // ==================== 协议适配器管理 ====================

    /** 注册协议适配器 */
    public void registerAdapter(String protocol, IProtocolAdapter adapter) {
        adapters.put(protocol.toUpperCase(), adapter);
    }

    /** 获取协议适配器 */
    public IProtocolAdapter getAdapter(String protocol) {
        return adapters.get(protocol.toUpperCase());
    }

    // ==================== 领域事件处理 ====================

    /**
     * 处理采集到的标签值 — 保存并发布 TAG_VALUE_COLLECTED 事件。
     * 由轮询线程或订阅回调调用。
     */
    @Transactional
    public void handleTagValue(TagValue tagValue) {
        TagValue saved = tagRepo.save(tagValue);
        DomainEventPublisher.publish(IDomainEvent.of(
                IotEventTypes.TAG_VALUE_COLLECTED, "iot",
                Map.of("equipmentCode", saved.getEquipmentCode(),
                       "tagName", saved.getTagName(),
                       "scaledValue", saved.getScaledValue(),
                       "quality", saved.getQuality().name())));
    }

    /**
     * 处理指令执行结果 — 更新 Command 状态并发布对应事件。
     */
    @Transactional
    public void handleCommandResult(String commandCode, boolean success,
                                     String resultCode, String resultMessage) {
        Command cmd = cmdRepo.findByCode(commandCode);
        if (cmd == null) {
            return;
        }
        if (success) {
            cmd.markCompleted(resultCode, resultMessage);
            cmdRepo.save(cmd);
            DomainEventPublisher.publish(IDomainEvent.of(
                    IotEventTypes.COMMAND_COMPLETED, "iot",
                    Map.of("commandCode", commandCode, "resultCode", resultCode)));
        } else {
            cmd.markFailed(resultCode, resultMessage);
            cmdRepo.save(cmd);
            DomainEventPublisher.publish(IDomainEvent.of(
                    IotEventTypes.COMMAND_FAILED, "iot",
                    Map.of("commandCode", commandCode, "resultCode", resultCode)));
        }
    }

    /**
     * 触发报警 — 创建 AlarmEvent 并发布 ALARM_TRIGGERED 事件。
     */
    @Transactional
    public AlarmEvent triggerAlarm(AlarmEvent alarm) {
        AlarmEvent saved = alarmRepo.save(alarm);
        DomainEventPublisher.publish(IDomainEvent.of(
                IotEventTypes.ALARM_TRIGGERED, "iot",
                Map.of("alarmCode", saved.getAlarmCode(),
                       "equipmentCode", saved.getEquipmentCode(),
                       "severity", saved.getSeverity().name(),
                       "triggerValue", saved.getTriggerValue())));
        return saved;
    }

    /**
     * 恢复报警 — 标记已恢复并发布 ALARM_RESOLVED 事件。
     */
    @Transactional
    public void resolveAlarm(String alarmCode) {
        AlarmEvent alarm = alarmRepo.findByCode(alarmCode);
        if (alarm == null) {
            return;
        }
        alarm.resolve();
        alarmRepo.save(alarm);
        DomainEventPublisher.publish(IDomainEvent.of(
                IotEventTypes.ALARM_RESOLVED, "iot",
                Map.of("alarmCode", alarmCode,
                       "equipmentCode", alarm.getEquipmentCode())));
    }

    // ==================== private ====================

    private DeviceConnection requireConnection(String equipmentCode) {
        DeviceConnection conn = connRepo.findByCode(equipmentCode);
        if (conn == null) {
            throw new IllegalArgumentException("设备连接不存在: " + equipmentCode);
        }
        return conn;
    }

}
