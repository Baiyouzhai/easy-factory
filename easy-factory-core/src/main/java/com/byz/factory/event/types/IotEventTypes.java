package com.byz.factory.event.types;

/**
 * IoT 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>Equip</b> — 订阅 {@link #DEVICE_CONNECTED} / {@link #DEVICE_DISCONNECTED}，
 *       同步设备在线状态；订阅 {@link #TAG_VALUE_COLLECTED}，更新设备参数实际值</li>
 *   <li><b>MES</b> — 订阅 {@link #DEVICE_DISCONNECTED}，
 *       设备断连时暂停关联工单；订阅 {@link #ALARM_TRIGGERED}，
 *       CRITICAL/EMERGENCY 级别报警时中断工序执行</li>
 *   <li><b>QMS</b> — 订阅 {@link #TAG_VALUE_COLLECTED}，
 *       采集值传入 SPC 控制图分析；订阅 {@link #ALARM_TRIGGERED}，
 *       自动发起偏差调查</li>
 *   <li><b>Andon</b> — 订阅 {@link #ALARM_TRIGGERED}，
 *       触发安灯呼叫（CRITICAL/EMERGENCY 级别）</li>
 *   <li><b>EAM</b> — 订阅 {@link #ALARM_TRIGGERED}，
 *       EMERGENCY 级别报警自动生成紧急维护工单</li>
 * </ul>
 *
 * @author 苏政
 */
public final class IotEventTypes {

    private IotEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "iot";

    // ── 设备连接 ──

    /** 设备已连接 — 载荷: 设备编码 + 协议 + 端点 */
    public static final String DEVICE_CONNECTED = "iot.device.connected";

    /** 设备已断开 — 载荷: 设备编码 + 断开原因 */
    public static final String DEVICE_DISCONNECTED = "iot.device.disconnected";

    // ── 数据采集 ──

    /** 标签值已采集 — 载荷: ITagValue（Equip 订阅更新参数实际值，QMS 订阅 SPC 分析） */
    public static final String TAG_VALUE_COLLECTED = "iot.tag.collected";

    // ── 指令下发 ──

    /** 指令已发送至设备 — 载荷: 设备编码 + 指令类型 + 参数 */
    public static final String COMMAND_SENT = "iot.command.sent";

    /** 指令执行完成 — 载荷: 设备编码 + 返回结果 */
    public static final String COMMAND_COMPLETED = "iot.command.completed";

    /** 指令执行失败 — 载荷: 设备编码 + 失败原因 */
    public static final String COMMAND_FAILED = "iot.command.failed";

    // ── 报警 ──

    /** 报警已触发 — 载荷: IAlarmEvent（QMS 订阅发起偏差，Andon 订阅触发呼叫，MES 订阅暂停工序） */
    public static final String ALARM_TRIGGERED = "iot.alarm.triggered";

    /** 报警已确认 — 载荷: 报警编码 + 确认人 */
    public static final String ALARM_ACKNOWLEDGED = "iot.alarm.acknowledged";

    /** 报警已恢复 — 载荷: 报警编码 + 恢复时间 */
    public static final String ALARM_RESOLVED = "iot.alarm.resolved";

}
