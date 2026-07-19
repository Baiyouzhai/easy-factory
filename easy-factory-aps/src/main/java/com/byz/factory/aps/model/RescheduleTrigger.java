package com.byz.factory.aps.model;

import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * 重排程触发记录 — 记录触发增量重排程的事件和处理状态。
 * <p>
 * 触发来源（design-decisions.md §2.6）：
 * <ul>
 *   <li>{@code mes.workorder.released} — 新工单插入</li>
 *   <li>{@code equip.fault.reported} — 设备不可用</li>
 *   <li>{@code scm.receipt.delayed} — 物料延迟</li>
 * </ul>
 * 全量重排程由计划员手动触发，通过 API 而非此记录。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RescheduleTrigger extends BaseEntity {

    /** 关联的排程编号 */
    private String scheduleCode;

    /** 触发事件（如 "mes.workorder.released"） */
    private String triggerEvent;

    /** 触发时间 */
    private Instant triggeredAt;

    /** 是否已处理 */
    private boolean processed;

    // ==================== 构造器 ====================

    protected RescheduleTrigger() {
        super();
    }

    public RescheduleTrigger(String code, String scheduleCode, String triggerEvent) {
        super(code, "重排程触发-" + scheduleCode);
        this.scheduleCode = scheduleCode;
        this.triggerEvent = triggerEvent;
        this.triggeredAt = Instant.now();
        this.processed = false;
    }

    // ==================== 业务方法 ====================

    /** 标记为已处理 */
    public void markProcessed() {
        this.processed = true;
        markUpdated();
    }

}
