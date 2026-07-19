package com.byz.factory.iot.model;

import com.byz.factory.iot.CommandStatus;
import com.byz.factory.iot.ICommand;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@Data
@EqualsAndHashCode(callSuper = true)
public class Command extends BaseLifecycleEntity<CommandStatus> implements ICommand {

    private String equipmentCode;
    private CommandType commandType;
    private Map<String, Object> parameters;
    private CommandPriority priority;
    private Duration timeout;
    private String resultCode;
    private String resultMessage;
    private Instant sentAt;
    private Instant acknowledgedAt;
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
        this.timeout = Duration.ofSeconds(30);
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
