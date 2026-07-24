package com.byz.factory.event.types;

/**
 * Andon 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>MES</b> — 订阅 {@link #ANDON_CALL_ESCALATED}，
 *       安灯升级后暂停工单/产线；订阅 {@link #ANDON_CALL_RESOLVED}，
 *       安灯解决后恢复生产</li>
 *   <li><b>EAM</b> — 订阅 {@link #ANDON_CALL_CREATED}（EQUIPMENT_FAULT 类型），
 *       设备故障时创建维护工单</li>
 *   <li><b>QMS</b> — 订阅 {@link #ANDON_CALL_CREATED}（QUALITY_ISSUE 类型），
 *       质量问题时创建偏差</li>
 *   <li><b>BI</b> — 订阅 {@link #ANDON_CALL_CREATED} / {@link #ANDON_CALL_RESOLVED} /
 *       {@link #ANDON_CALL_CLOSED}，汇总安灯看板数据</li>
 *   <li><b>IoT</b> — 订阅 {@link #ANDON_CALL_ACKNOWLEDGED}，
 *       确认后取消声光报警</li>
 * </ul>
 *
 * @author 苏政
 */
public final class AndonEventTypes {

    private AndonEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "andon";

    // ── 安灯呼叫生命周期 ──

    /** 安灯呼叫已创建 — 载荷: callCode + triggerType + severity + workOrderNo + triggeredBy */
    public static final String ANDON_CALL_CREATED = "andon.call.created";

    /** 安灯呼叫已确认 — 载荷: callCode + acknowledgedBy + acknowledgedAt */
    public static final String ANDON_CALL_ACKNOWLEDGED = "andon.call.acknowledged";

    /** 安灯呼叫已升级 — 载荷: callCode + escalationLevel + escalatedAt */
    public static final String ANDON_CALL_ESCALATED = "andon.call.escalated";

    /** 安灯呼叫已解决 — 载荷: callCode + resolution + resolvedAt */
    public static final String ANDON_CALL_RESOLVED = "andon.call.resolved";

    /** 安灯呼叫已关闭 — 载荷: callCode + closedAt */
    public static final String ANDON_CALL_CLOSED = "andon.call.closed";

    // ── 上报规则 ──

    /** 上报规则已创建 — 载荷: ruleCode + triggerType + severity */
    public static final String RULE_CREATED = "andon.rule.created";

    /** 上报规则已更新 — 载荷: ruleCode + levelCount */
    public static final String RULE_UPDATED = "andon.rule.updated";

}
