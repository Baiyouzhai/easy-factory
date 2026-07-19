package com.byz.factory.aps.model;

import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 资源日历 — 记录单个资源（设备/人员）在特定日期的可用时间段和产能。
 * <p>
 * APS 排程时通过资源日历来校验产能约束（硬约束）：
 * 排程任务的起止时间必须在资源可用窗口内，且累计工时不超过 capacityHours。
 * <p>
 * 用法：
 * <pre>{@code
 * ResourceCalendar cal = new ResourceCalendar("RC-001", "设备PT-001",
 *     ResourceType.MACHINE, LocalDate.now(),
 *     Instant.parse("2026-07-19T08:00:00Z"),
 *     Instant.parse("2026-07-19T20:00:00Z"),
 *     BigDecimal.valueOf(12));
 * boolean canFit = cal.canFit(taskStart, taskEnd); // 是否在可用窗口内？
 * long availableMinutes = cal.getAvailableMinutes(); // 可用分钟数
 * }</pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceCalendar extends BaseEntity {

    /** 资源编码（设备编码或人员编码） */
    private String resourceCode;

    /** 资源类型 */
    private ResourceType resourceType;

    /** 日期 */
    private LocalDate date;

    /** 可用时段开始 */
    private Instant availableFrom;

    /** 可用时段结束 */
    private Instant availableTo;

    /** 可用产能（小时） */
    private java.math.BigDecimal capacityHours;

    // ==================== 构造器 ====================

    protected ResourceCalendar() {
        super();
    }

    public ResourceCalendar(String code, String resourceCode, ResourceType resourceType,
                            LocalDate date, Instant availableFrom, Instant availableTo,
                            java.math.BigDecimal capacityHours) {
        super(code, "资源日历-" + resourceCode + "-" + date);
        this.resourceCode = resourceCode;
        this.resourceType = resourceType;
        this.date = date;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
        this.capacityHours = capacityHours;
    }

    // ==================== 业务方法 ====================

    /**
     * 获取可用分钟数。
     */
    public long getAvailableMinutes() {
        return Duration.between(availableFrom, availableTo).toMinutes();
    }

    /**
     * 获取产能分钟数。
     */
    public long getCapacityMinutes() {
        if (capacityHours == null) return 0;
        return capacityHours.multiply(java.math.BigDecimal.valueOf(60)).longValue();
    }

    /**
     * 判断给定的时间段是否完全在此资源的可用窗口内（时刻范围约束，不等同于产能约束）。
     *
     * @param start 任务开始时间
     * @param end   任务结束时间
     * @return true 如果任务时间在可用窗口内
     */
    public boolean canFit(Instant start, Instant end) {
        if (start == null || end == null) return false;
        return !start.isBefore(availableFrom) && !end.isAfter(availableTo);
    }

    /**
     * 计算指定时间段与可用窗口的重叠分钟数。
     *
     * @param start 时间段开始
     * @param end   时间段结束
     * @return 重叠分钟数（无重叠时返回 0）
     */
    public long overlapMinutes(Instant start, Instant end) {
        if (start == null || end == null) return 0;
        Instant overlapStart = start.isAfter(availableFrom) ? start : availableFrom;
        Instant overlapEnd = end.isBefore(availableTo) ? end : availableTo;
        if (overlapStart.isBefore(overlapEnd)) {
            return Duration.between(overlapStart, overlapEnd).toMinutes();
        }
        return 0;
    }

}
