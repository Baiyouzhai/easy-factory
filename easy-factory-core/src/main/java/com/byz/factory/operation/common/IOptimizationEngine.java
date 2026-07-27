package com.byz.factory.operation.common;

import java.util.Comparator;
import java.util.List;

/**
 * 优化引擎抽象 — APS 排程的算法可插拔接口。
 * <p>
 * Phase 3 的规则式排程（EDD/SPT/CR）通过 {@link #toComparator} 返回排序器，
 * 供 APS 的 {@code SchedulingRule} 实现。
 * Phase 5 可接入 OR-Tools / OptaPlanner 等约束求解引擎，实现
 * {@link #optimize} 直接返回优化后的完整任务序列。
 * <p>
 * 用法：
 * <pre>{@code
 * // Phase 3 — 规则式
 * IOptimizationEngine<Task> ruleEngine = IOptimizationEngine.ruleBased("EDD", eddComparator);
 * List<Task> sorted = ruleEngine.optimize(tasks, "EDD", Instant.now());
 *
 * // Phase 5 — 约束求解
 * IOptimizationEngine<Task> cpEngine = new OptaPlannerEngine<>(constraints);
 * List<Task> optimal = cpEngine.optimize(tasks, "MIN_MAKESPAN", Instant.now());
 * }</pre>
 *
 * @param <T> 待排程的任务类型
 * @author 苏政
 */
public interface IOptimizationEngine<T> {

    /**
     * 对任务列表执行优化排程。
     * <p>
     * 规则式实现调用 {@link #toComparator(String, java.time.Instant)} 排序；
     * 约束求解实现忽略 comparator，直接求解最优序列。
     *
     * @param tasks    待排程任务列表（原地排序或返回新列表）
     * @param strategy 策略名称（EDD/SPT/CR/MIN_MAKESPAN/…）
     * @param now      排程参考时刻（用于 CR 等时间相关策略）
     * @return 优化后的任务列表
     */
    List<T> optimize(List<T> tasks, String strategy, java.time.Instant now);

    /**
     * 返回指定策略对应的比较器。
     * <p>
     * 规则式引擎必须实现此方法；约束求解引擎可抛出
     * {@link UnsupportedOperationException}。
     *
     * @param strategy 策略名称
     * @param now      排程参考时刻
     * @return 比较器
     */
    Comparator<T> toComparator(String strategy, java.time.Instant now);

    /**
     * 引擎名称（如 "EDD-Rule", "OptaPlanner-CP"）。
     */
    String getEngineName();

    // ==================== 工厂方法 ====================

    /**
     * 创建规则式优化引擎。
     *
     * @param name        引擎名称
     * @param comparators 策略→比较器 的注册表（可变参数，key=策略名, value=比较器）
     * @param <T>         任务类型
     * @return 规则式引擎
     */
    @SafeVarargs
    static <T> IOptimizationEngine<T> ruleBased(String name,
                                                 java.util.Map.Entry<String, Comparator<T>>... comparators) {
        java.util.Map<String, Comparator<T>> registry = java.util.Map.ofEntries(comparators);
        return new IOptimizationEngine<>() {
            @Override
            public List<T> optimize(List<T> tasks, String strategy, java.time.Instant now) {
                if (tasks == null || tasks.isEmpty()) return tasks;
                Comparator<T> cmp = toComparator(strategy, now);
                if (cmp != null) tasks.sort(cmp);
                return tasks;
            }

            @Override
            public Comparator<T> toComparator(String strategy, java.time.Instant now) {
                Comparator<T> cmp = registry.get(strategy.toUpperCase());
                if (cmp == null) {
                    throw new IllegalArgumentException("未知排程策略: " + strategy
                            + " (可用: " + registry.keySet() + ")");
                }
                return cmp;
            }

            @Override
            public String getEngineName() {
                return name;
            }
        };
    }

}
