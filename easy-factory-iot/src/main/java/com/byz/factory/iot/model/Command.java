package com.byz.factory.iot.model;

import com.byz.factory.iot.CommandStatus;
import com.byz.factory.iot.ICommand;
import com.byz.factory.shared.BaseLifecycleEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * 设备指令 — 继承 BaseLifecycleEntity&lt;CommandStatus&gt; 获得状态机，实现 ICommand。
 * <p>
 * 指令的完整生命周期：
 * <pre>
 *   QUEUED → SENT → ACKNOWLEDGED → COMPLETED
 *                  ↘ FAILED
 *   QUEUED → CANCELLED
 * </pre>
 * <p>
 * <h3>IExpand 约定</h3>
 * <pre>
 *   iot.cmd.priority          — 指令优先级
 *   iot.cmd.retryCount        — 重试次数
 *   iot.cmd.source            — 指令来源 (EQUIP / MES / ANDON)
 * </pre>
 *
 * @author 苏政
 */
@Entity
@Table(name = "iot_command")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Command extends BaseLifecycleEntity<CommandStatus> implements ICommand {

    @Column(name = "equipment_code", nullable = false, length = 100)
    private String equipmentCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "command_type", nullable = false, length = 20)
    private CommandType commandType;

    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "text")
    private Map<String, Object> parameters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CommandPriority priority;

    @Column(name = "timeout_seconds")
    private Long timeoutSeconds;

    @Column(name = "result_code", length = 50)
    private String resultCode;

    @Column(name = "result_message", length = 500)
    private String resultMessage;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * @param commandCode   指令编码
     * @param equipmentCode 目标设备编码
     * @param commandType   指令类型
     * @param parameters    指令参数
     */
    public Command(String commandCode, String equipmentCode,
                   CommandType commandType, Map<String, Object> parameters) {
        super(commandCode, "指令-" + commandCode, CommandStatus.QUEUED);
        this.equipmentCode = equipmentCode;
        this.commandType = commandType;
        this.parameters = parameters != null ? parameters : Collections.emptyMap();
        this.priority = CommandPriority.NORMAL;
    }

    // ==================== ICommand 接口实现 ====================

    @Override
    public Duration getTimeout() {
        return timeoutSeconds != null ? Duration.ofSeconds(timeoutSeconds) : Duration.ofSeconds(30);
    }

    public void setTimeout(Duration timeout) {
        this.timeoutSeconds = timeout != null ? timeout.getSeconds() : null;
    }

    // ==================== 业务便捷方法 ====================

    /** 发送至设备 — QUEUED → SENT */
    public void markSent() {
        transition(CommandStatus.SENT);
        this.sentAt = Instant.now();
        markUpdated();
    }

    /** 设备已确认 — SENT → ACKNOWLEDGED */
    public void markAcknowledged() {
        transition(CommandStatus.ACKNOWLEDGED);
        this.acknowledgedAt = Instant.now();
        markUpdated();
    }

    /** 执行完成 — ACKNOWLEDGED → COMPLETED */
    public void markCompleted(String resultCode, String resultMessage) {
        transition(CommandStatus.COMPLETED);
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.completedAt = Instant.now();
        markUpdated();
    }

    /** 执行失败 — SENT/ACKNOWLEDGED → FAILED */
    public void markFailed(String resultCode, String resultMessage) {
        transition(CommandStatus.FAILED);
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.completedAt = Instant.now();
        markUpdated();
    }

    /** 取消指令 — QUEUED → CANCELLED */
    public void cancel() {
        transition(CommandStatus.CANCELLED);
        markUpdated();
    }

}
