package com.byz.factory.andon.model;

/**
 * 安灯触发类型 — 描述什么事件触发了安灯呼叫。
 * <p>
 * 不同触发类型决定通知对象和后续联动模块：
 * <ul>
 *   <li>{@link #EQUIPMENT_FAULT} — 设备故障 → 通知 EAM 创建维护工单</li>
 *   <li>{@link #QUALITY_ISSUE} — 质量问题 → 通知 QMS 创建偏差</li>
 *   <li>{@link #MATERIAL_SHORTAGE} — 物料短缺 → 通知 WMS/SCM</li>
 *   <li>{@link #SAFETY_INCIDENT} — 安全事件 → 全厂广播</li>
 *   <li>{@link #PROCESS_DELAY} — 工序超时 → 通知 MES</li>
 *   <li>{@link #OTHER} — 其它异常</li>
 * </ul>
 *
 * @author 苏政
 */
public enum TriggerType {

    /** 设备故障 */
    EQUIPMENT_FAULT,
    /** 质量问题（连续不良/SPC 超限） */
    QUALITY_ISSUE,
    /** 物料短缺 */
    MATERIAL_SHORTAGE,
    /** 安全事件 */
    SAFETY_INCIDENT,
    /** 工序超时 */
    PROCESS_DELAY,
    /** 其它 */
    OTHER;

}
