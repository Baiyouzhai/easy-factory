package com.byz.factory.andon.model;

/**
 * 安灯呼叫严重程度 — 决定上报速度和自动联动动作。
 * <p>
 * 严重程度影响：
 * <ul>
 *   <li>{@link #INFO} — 仅记录，不触发上报</li>
 *   <li>{@link #WARNING} — 通知班组长，超时 5min 上报</li>
 *   <li>{@link #CRITICAL} — 通知车间主任，超时 3min 上报，可能暂停工序</li>
 *   <li>{@link #EMERGENCY} — 全厂广播，立即停止产线，逐级上报至厂长</li>
 * </ul>
 *
 * @author 苏政
 */
public enum AndonSeverity {

    /** 信息（仅记录） */
    INFO,
    /** 警告 */
    WARNING,
    /** 严重 */
    CRITICAL,
    /** 紧急 */
    EMERGENCY;

}
