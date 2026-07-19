package com.byz.factory.iot;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * 设备指令抽象 — 供 Equip/MES 跨模块引用指令下发。
 * <p>
 * IoT 接收上层系统的指令请求，通过协议适配器下发至设备，
 * 跟踪指令的完整生命周期（排队→发送→确认→完成/失败）。
 * <p>
 * <b>谁来引用？</b>
 * <ul>
 *   <li><b>Equip</b> — 配方参数下发时创建 SET_PARAM 指令</li>
 *   <li><b>MES</b> — 工单开工/停机时创建 START/STOP 指令</li>
 *   <li><b>Andon</b> — 报警确认时创建 ACK_ALARM 指令</li>
 * </ul>
 *
 * @author 苏政
 */
public interface ICommand {

    /** 指令优先级 */
    enum CommandPriority {
        /** 高优先级 — 紧急停机等 */
        HIGH,
        /** 普通优先级 — 参数设定等 */
        NORMAL,
        /** 低优先级 — 批量读取等 */
        LOW
    }

    /** 指令类型 */
    enum CommandType {
        /** 设定参数 */
        SET_PARAM,
        /** 启动设备 */
        START,
        /** 停止设备 */
        STOP,
        /** 读取数据 */
        READ,
        /** 确认报警 */
        ACK_ALARM
    }

    /** 设备编码 */
    String getEquipmentCode();

    /** 指令类型 */
    CommandType getCommandType();

    /** 指令参数 */
    Map<String, Object> getParameters();

    /** 优先级 */
    CommandPriority getPriority();

    /** 超时时间 */
    Duration getTimeout();

    /** 执行状态 */
    CommandStatus getStatus();

    /** 设备返回码（null=未返回） */
    String getResultCode();

    /** 设备返回消息 */
    String getResultMessage();

    /** 创建时间 */
    Instant getSentAt();

    /** 确认时间 */
    Instant getAcknowledgedAt();

    /** 完成/失败时间 */
    Instant getCompletedAt();

}
