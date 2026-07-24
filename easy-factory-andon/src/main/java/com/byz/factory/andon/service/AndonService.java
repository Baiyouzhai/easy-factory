package com.byz.factory.andon.service;

import com.byz.factory.andon.model.*;

import java.util.List;

/**
 * 安灯服务接口 — Andon 模块核心。
 * <p>
 * 提供异常呼叫全流程管理：触发 → 确认 → 逐级上报 → 解决 → 关闭，
 * 以及上报规则的配置管理。
 * <p>
 * <b>跨模块协作方向：</b>
 * <ul>
 *   <li>订阅 {@code mes.process.interrupted} — 工序中断时自动创建安灯呼叫</li>
 *   <li>订阅 {@code equip.fault.reported} — 设备故障时自动创建安灯呼叫</li>
 *   <li>订阅 {@code qms.spc.out_of_control} — SPC 失控时自动创建安灯呼叫</li>
 *   <li>订阅 {@code qms.deviation.created} — 偏差创建时自动创建安灯呼叫</li>
 *   <li>订阅 {@code iot.alarm.triggered} — IoT 报警时自动创建安灯呼叫</li>
 *   <li>订阅 {@code aps.task.delayed} — 排程延误时异常预警</li>
 *   <li>发布 {@code andon.call.escalated} — 通知 EAM 创建维护工单</li>
 *   <li>发布 {@code andon.call.escalated} (EMERGENCY) — 通知 MES 暂停产线</li>
 *   <li>发布 {@code andon.call.created} (QUALITY_ISSUE) — 通知 QMS 创建偏差</li>
 * </ul>
 *
 * @author 苏政
 */
public interface AndonService {

    // ── 呼叫管理 ──

    /**
     * 触发安灯呼叫。
     *
     * @param triggerType   触发类型
     * @param severity      严重程度
     * @param source        呼叫来源
     * @param triggeredBy   触发人
     * @param description   呼叫描述
     * @return 创建的安灯呼叫
     */
    AndonCall trigger(TriggerType triggerType, AndonSeverity severity,
                      AndonSource source, String triggeredBy, String description);

    /**
     * 确认呼叫。
     *
     * @param callCode       呼叫编号
     * @param acknowledgedBy 确认人
     * @return 确认后的安灯呼叫
     */
    AndonCall acknowledge(String callCode, String acknowledgedBy);

    /**
     * 逐级上报。
     *
     * @param callCode 呼叫编号
     * @param reason   上报原因
     * @return 上报后的安灯呼叫
     */
    AndonCall escalate(String callCode, String reason);

    /**
     * 解决呼叫。
     *
     * @param callCode   呼叫编号
     * @param resolution 解决方案
     * @return 解决后的安灯呼叫
     */
    AndonCall resolve(String callCode, String resolution);

    /**
     * 关闭呼叫。
     *
     * @param callCode 呼叫编号
     * @return 关闭后的安灯呼叫
     */
    AndonCall close(String callCode);

    // ── 呼叫查询 ──

    /**
     * 按编号查询呼叫。
     */
    AndonCall findCall(String callCode);

    /**
     * 获取活跃呼叫列表（未关闭的呼叫）。
     */
    List<AndonCall> getActiveCalls();

    /**
     * 按工单号查询呼叫列表。
     */
    List<AndonCall> findCallsByWorkOrder(String workOrderNo);

    /**
     * 按设备编码查询呼叫列表。
     */
    List<AndonCall> findCallsByEquipment(String equipmentCode);

    /**
     * 获取紧急呼叫列表（CRITICAL/EMERGENCY 且未关闭）。
     */
    List<AndonCall> getUrgentCalls();

    /**
     * 获取看板数据（活跃呼叫统计）。
     *
     * @return 按严重程度分组的呼叫数量统计
     */
    AndonDashboard getDashboard();

    // ── 上报规则管理 ──

    /**
     * 创建上报规则。
     *
     * @param triggerType 触发类型
     * @param severity    严重程度
     * @return 创建的上报规则
     */
    EscalationRule createRule(TriggerType triggerType, AndonSeverity severity);

    /**
     * 为规则添加上报级别。
     *
     * @param ruleCode 规则编码
     * @param level    上报级别
     */
    void addEscalationLevel(String ruleCode, EscalationRule.EscalationLevel level);

    /**
     * 查询上报规则（按触发类型和严重程度）。
     *
     * @param triggerType 触发类型
     * @param severity    严重程度
     * @return 上报规则，不存在返回 null
     */
    EscalationRule findRule(TriggerType triggerType, AndonSeverity severity);

    /**
     * 获取所有启用的上报规则。
     */
    List<EscalationRule> getActiveRules();

    /**
     * 禁用上报规则。
     */
    void disableRule(String ruleCode);

    /**
     * 启用上报规则。
     */
    void enableRule(String ruleCode);

}
