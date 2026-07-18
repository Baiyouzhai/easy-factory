package com.byz.factory.operation.capacity;

import com.byz.factory.factory.IBlueprint;
import com.byz.factory.operation.common.ReportPrinter;
import com.byz.factory.operation.time.ITimed;
import com.byz.factory.operation.time.ProcessCycleTime;
import com.byz.factory.process.IAction;
import com.byz.factory.process.IProcess;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

/**
 * 瓶颈识别器 — 找到约束产能的工序/设备。
 * <p>
 * 算法：对每道工序计算"可用时间 ÷ 单件时间 = 最大产能"，产能最小的即为瓶颈。
 *
 * @author 苏政
 */
public class BottleneckDetector {

    /**
     * 检测瓶颈—基于工序周期时间。
     *
     * @param blueprint     产品蓝图
     * @param durationMap   动作时长映射
     * @param profile       工厂产能画像
     * @return 瓶颈分析结果
     */
    public BottleneckResult detect(IBlueprint blueprint,
                                    Map<String, ? extends ITimed> durationMap,
                                    FactoryCapacityProfile profile) {
        List<IProcess> processes = blueprint.getProductionProcessList();
        if (processes == null || processes.isEmpty()) {
            return BottleneckResult.empty();
        }

        List<ProcessCapacity> capacities = new ArrayList<>();
        String bottleneckCode = null;
        BigDecimal minThroughput = null;
        Duration minDailyCapacity = null;

        // 单件（取 batchSize=1 计算周期时间）
        BigDecimal unitBatch = BigDecimal.ONE;

        for (IProcess process : processes) {
            // 计算工序周期时间（单件）
            ProcessCycleTime pct = new ProcessCycleTime();
            Duration cycleTime = pct.calculate(process, durationMap, unitBatch);

            // 找到该工序依赖的设备
            String equipmentCode = findEquipmentCode(process);

            // 获取该设备每日有效可用时间
            Duration effectiveDaily = profile.getEffectiveMachineTime(equipmentCode);

            // 最大日产能 = 日可用时间 ÷ 单件周期
            BigDecimal dailyCapacity;
            if (cycleTime.isZero()) {
                dailyCapacity = BigDecimal.valueOf(Long.MAX_VALUE);
            } else {
                dailyCapacity = BigDecimal.valueOf(effectiveDaily.toMillis())
                    .divide(BigDecimal.valueOf(cycleTime.toMillis()), 2, RoundingMode.HALF_UP);
            }

            // 最大年产能
            BigDecimal annualCapacity = dailyCapacity.multiply(
                BigDecimal.valueOf(profile.getWorkingDaysPerYear()));

            capacities.add(new ProcessCapacity(
                process.getCode(), process.getName(),
                equipmentCode, cycleTime, effectiveDaily,
                dailyCapacity, annualCapacity));

            // 更新瓶颈
            if (minThroughput == null || dailyCapacity.compareTo(minThroughput) < 0) {
                minThroughput = dailyCapacity;
                bottleneckCode = process.getCode();
                minDailyCapacity = effectiveDaily;
            }
        }

        return new BottleneckResult(bottleneckCode, minThroughput,
            minDailyCapacity, capacities);
    }

    /**
     * 从工序的动作中提取设备编码。
     * 查找 requireResources 中 group=Machine 的资源。
     */
    private String findEquipmentCode(IProcess process) {
        for (IAction action : process.getActions()) {
            if (action instanceof com.byz.factory.process.IActionModel model) {
                for (var r : model.requireResources()) {
                    if (r.getGroup() == com.byz.factory.shared.Dict.SourceGroup.Machine) {
                        return r.getName();
                    }
                }
            }
        }
        return "通用设备";  // 未指定设备时使用通用标识
    }

    // ---- 结果类型 ----

    public record ProcessCapacity(
        String processCode,
        String processName,
        String equipmentCode,
        Duration cycleTimePerUnit,
        Duration effectiveDailyTime,
        BigDecimal maxDailyOutput,
        BigDecimal maxAnnualOutput
    ) {}

    public record BottleneckResult(
        String bottleneckProcessCode,
        BigDecimal maxDailyThroughput,
        Duration bottleneckDailyTime,
        List<ProcessCapacity> processCapacities
    ) {
        public static BottleneckResult empty() {
            return new BottleneckResult(null, BigDecimal.ZERO, Duration.ZERO, Collections.emptyList());
        }

        public boolean hasBottleneck() {
            return bottleneckProcessCode != null;
        }

        /** 生成瓶颈分析报告 */
        public String toReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 瓶颈分析 ===\n\n");
            sb.append("约束工序: ").append(bottleneckProcessCode != null ? bottleneckProcessCode : "无").append("\n");
            sb.append("最大日产能: ").append(maxDailyThroughput).append(" 件/天\n");
            sb.append("瓶颈日可用时间: ").append(ReportPrinter.formatDuration(bottleneckDailyTime)).append("\n\n");
            sb.append("【各工序产能明细】\n");
            for (ProcessCapacity pc : processCapacities) {
                String marker = pc.processCode().equals(bottleneckProcessCode) ? " ★瓶颈" : "";
                sb.append("  ").append(pc.processCode()).append(" [").append(pc.equipmentCode()).append("]")
                  .append(marker).append("\n");
                sb.append("    周期: ").append(ReportPrinter.formatDuration(pc.cycleTimePerUnit()))
                  .append("  日产能: ").append(pc.maxDailyOutput()).append(" 件")
                  .append("  年产能: ").append(pc.maxAnnualOutput()).append(" 件\n");
            }
            return sb.toString();
        }
    }

}
