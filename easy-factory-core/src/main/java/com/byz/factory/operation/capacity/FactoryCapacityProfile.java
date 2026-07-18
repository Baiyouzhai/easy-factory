package com.byz.factory.operation.capacity;

import com.byz.factory.operation.common.ReportPrinter;
import com.byz.factory.process.IProcess;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

/**
 * 工厂产能画像 — 刻画工厂的生产能力边界。
 * <p>
 * 封装工作日历、设备/人员产能、班次等信息，提供可用时间查询和理论最大产能估算。
 *
 * @author 苏政
 */
public class FactoryCapacityProfile {

    private final String factoryCode;

    /** 每日可用时间（分钟） */
    private final Duration dailyAvailableTime;

    /** 设备产能: equipmentCode → 每日可用机时 */
    private final Map<String, Duration> machineDailyHours;

    /** 设备OEE因子: equipmentCode → OEE (0~1) */
    private final Map<String, BigDecimal> machineOeeFactors;

    /** 人员产能: skillType → 每日可用人时 */
    private final Map<String, Duration> laborDailyHours;

    /** 工作天数/年（默认 250 天） */
    private final int workingDaysPerYear;

    public FactoryCapacityProfile(String factoryCode, Duration dailyAvailableTime) {
        this.factoryCode = factoryCode;
        this.dailyAvailableTime = dailyAvailableTime;
        this.machineDailyHours = new LinkedHashMap<>();
        this.machineOeeFactors = new LinkedHashMap<>();
        this.laborDailyHours = new LinkedHashMap<>();
        this.workingDaysPerYear = 250;
    }

    // ---- 注册产能 ----

    public FactoryCapacityProfile withMachine(String equipmentCode, Duration dailyHours,
                                               BigDecimal oeeFactor) {
        machineDailyHours.put(equipmentCode, dailyHours);
        machineOeeFactors.put(equipmentCode, oeeFactor);
        return this;
    }

    public FactoryCapacityProfile withMachine(String equipmentCode, long minutesPerDay,
                                               double oee) {
        return withMachine(equipmentCode, Duration.ofMinutes(minutesPerDay),
            BigDecimal.valueOf(oee));
    }

    public FactoryCapacityProfile withLabor(String skillType, Duration dailyHours) {
        laborDailyHours.put(skillType, dailyHours);
        return this;
    }

    // ---- 查询 ----

    /** 获取设备有效可用时间（扣除OEE损耗） */
    public Duration getEffectiveMachineTime(String equipmentCode) {
        Duration raw = machineDailyHours.getOrDefault(equipmentCode, Duration.ZERO);
        BigDecimal oee = machineOeeFactors.getOrDefault(equipmentCode, BigDecimal.ONE);
        long effectiveMillis = (long) (raw.toMillis() * oee.doubleValue());
        return Duration.ofMillis(effectiveMillis);
    }

    /** 获取人员可用时间 */
    public Duration getLaborTime(String skillType) {
        return laborDailyHours.getOrDefault(skillType, Duration.ZERO);
    }

    /** 获取设备OEE */
    public BigDecimal getOee(String equipmentCode) {
        return machineOeeFactors.getOrDefault(equipmentCode, BigDecimal.ONE);
    }

    // ---- 计算 ----

    /**
     * 估算理论最大年产能（基于该工厂所有可用资源）。
     *
     * @param hoursPerUnit 生产单件产品所需的工时
     * @return 理论最大年产量（件）
     */
    public BigDecimal getMaxTheoreticalAnnualOutput(BigDecimal hoursPerUnit) {
        if (hoursPerUnit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        // 取所有资源中可用时间最多的（理想情况）
        Duration maxDaily = machineDailyHours.values().stream()
            .max(Duration::compareTo)
            .orElse(dailyAvailableTime);

        BigDecimal dailyMinutes = BigDecimal.valueOf(maxDaily.toMinutes());
        BigDecimal unitsPerDay = dailyMinutes.divide(
            hoursPerUnit.multiply(BigDecimal.valueOf(60)), 2, java.math.RoundingMode.HALF_UP);
        return unitsPerDay.multiply(BigDecimal.valueOf(workingDaysPerYear));
    }

    // ---- Getters ----

    public String getFactoryCode() { return factoryCode; }
    public Duration getDailyAvailableTime() { return dailyAvailableTime; }
    public int getWorkingDaysPerYear() { return workingDaysPerYear; }
    public Map<String, Duration> getMachineDailyHours() { return Collections.unmodifiableMap(machineDailyHours); }
    public Map<String, Duration> getLaborDailyHours() { return Collections.unmodifiableMap(laborDailyHours); }

    /**
     * 生成工厂产能报告
     */
    public String toReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 工厂产能画像: ").append(factoryCode).append(" ===\n\n");
        sb.append("日可用时间: ").append(ReportPrinter.formatDuration(dailyAvailableTime)).append("\n");
        sb.append("年工作天数: ").append(workingDaysPerYear).append("\n\n");

        sb.append("【设备产能】\n");
        machineDailyHours.forEach((code, d) -> {
            BigDecimal oee = getOee(code);
            sb.append("  ").append(code).append(": 日").append(ReportPrinter.formatDuration(d))
              .append(" OEE=").append(oee).append("\n");
        });

        sb.append("\n【人员产能】\n");
        laborDailyHours.forEach((skill, d) ->
            sb.append("  ").append(skill).append(": 日").append(ReportPrinter.formatDuration(d)).append("\n"));

        return sb.toString();
    }
}
