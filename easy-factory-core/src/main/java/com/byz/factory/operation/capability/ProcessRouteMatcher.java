package com.byz.factory.operation.capability;

import com.byz.factory.factory.IFactory;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.factory.physical.StationType;
import com.byz.factory.operation.common.IOperation;
import com.byz.factory.operation.common.OperationResult;
import com.byz.factory.operation.common.ReportPrinter;

import java.util.*;

/**
 * 工序路线匹配器 — 将蓝图的动作匹配到工厂的设备。
 * <p>
 * 通过 {@link IMatchingStrategy} 支持不同匹配策略：
 * <ul>
 *   <li>{@link DirectEquipmentStrategy} (默认) — 动作直接找设备，忽略产线</li>
 *   <li>LineFirstStrategy (自定义) — 线优先，整线匹配</li>
 * </ul>
 *
 * @author 苏政
 */
public class ProcessRouteMatcher implements IOperation<ProcessRouteMatcher.MatchInput, ProcessRouteMatcher.RouteMatchResult> {

    private final IMatchingStrategy strategy;

    public ProcessRouteMatcher() {
        this(new DirectEquipmentStrategy());
    }

    public ProcessRouteMatcher(IMatchingStrategy strategy) {
        this.strategy = strategy;
    }

    @Override public String getName() { return strategy.getName(); }
    @Override public String getCategory() { return "能力检查"; }

    @Override
    public RouteMatchResult execute(MatchInput input) {
        return strategy.match(input.blueprint(), input.factory());
    }

    public RouteMatchResult match(IBlueprint blueprint, IFactory factory) {
        return strategy.match(blueprint, factory);
    }

    // ── 输入/输出类型 ──

    public record MatchInput(IBlueprint blueprint, IFactory factory) {}

    public enum MatchStatus { EQUIPPED, DEGRADED, SKIPPED, MISSING }

    public record EquipmentFound(String equipmentCode, String workstationCode,
                                  String lineCode, boolean primary, int sequence) {
        public static EquipmentFound direct(String equipmentCode, String workstationCode,
                                             StationType stationType, boolean primary) {
            return new EquipmentFound(equipmentCode, workstationCode, stationType.name(), primary, 0);
        }
    }

    public record ActionEquipmentResult(String actionCode, MatchStatus status,
                                         List<EquipmentFound> equipment, String reason) {
        public static ActionEquipmentResult equipped(String code, List<EquipmentFound> eq, MatchStatus s) {
            return new ActionEquipmentResult(code, s, eq, null);
        }
        public static ActionEquipmentResult skipped(String code) {
            return new ActionEquipmentResult(code, MatchStatus.SKIPPED, List.of(), "Optional跳过");
        }
        public static ActionEquipmentResult missing(String code, String reason) {
            return new ActionEquipmentResult(code, MatchStatus.MISSING, List.of(), reason);
        }
        public String icon() { return switch (status) {
            case EQUIPPED -> "✅"; case DEGRADED -> "⚠️"; case SKIPPED -> "⏭️"; case MISSING -> "❌";
        };}
    }

    public record ProcessRouteResult(String processCode, String processName, String matchedLine,
                                      boolean feasible, List<ActionEquipmentResult> actionResults) {
        public int equippedCount() { return count(MatchStatus.EQUIPPED); }
        public int degradedCount() { return count(MatchStatus.DEGRADED); }
        public int skippedCount()  { return count(MatchStatus.SKIPPED);  }
        public int missingCount()  { return count(MatchStatus.MISSING);  }
        private int count(MatchStatus s) { return (int) actionResults.stream().filter(a -> a.status() == s).count(); }
    }

    public static class RouteMatchResult extends OperationResult {
        private final String blueprintCode;
        private final boolean feasible;
        private final List<ProcessRouteResult> processResults;
        private final int equippedActions, degradedActions, skippedActions, missingActions;

        public RouteMatchResult(String blueprintCode, boolean feasible, List<ProcessRouteResult> processResults,
                                 int eq, int deg, int skip, int miss) {
            super("动作-设备匹配");
            this.blueprintCode = blueprintCode;
            this.feasible = feasible;
            this.processResults = processResults;
            this.equippedActions = eq;
            this.degradedActions = deg;
            this.skippedActions = skip;
            this.missingActions = miss;
            setSuccess(feasible);
        }

        public static RouteMatchResult fail(String code, String reason) {
            RouteMatchResult r = new RouteMatchResult(code, false, List.of(), 0, 0, 0, 0);
            r.setSuccess(false);
            r.addMessage(reason);
            return r;
        }

        public boolean feasible() { return feasible; }
        public List<ProcessRouteResult> processResults() { return processResults; }
        public int equippedActions() { return equippedActions; }
        public int degradedActions() { return degradedActions; }
        public int skippedActions() { return skippedActions; }
        public int missingActions() { return missingActions; }

        @Override
        public String toReport() {
            StringBuilder sb = new StringBuilder();
            sb.append(feasible ? "✅" : "❌").append(" 蓝图[").append(blueprintCode)
              .append("] 动作-设备匹配: ").append(feasible ? "可行" : "不可行").append("\n");
            sb.append("  动作: ✅").append(equippedActions)
              .append(" ⚠️").append(degradedActions)
              .append(" ⏭️").append(skippedActions)
              .append(" ❌").append(missingActions).append("\n\n");
            for (ProcessRouteResult pr : processResults) {
                sb.append(ReportPrinter.indent(1, "工序[" + pr.processCode() + "] " + pr.processName()
                    + " → " + pr.matchedLine() + "\n"));
                for (ActionEquipmentResult ar : pr.actionResults()) {
                    sb.append(ReportPrinter.indent(2, ar.icon() + " " + ar.actionCode()));
                    if (!ar.equipment().isEmpty()) {
                        String eq = ar.equipment().stream()
                            .map(e -> e.equipmentCode() + "@" + e.workstationCode())
                            .reduce((a, b) -> a + ", " + b).orElse("");
                        sb.append(" → ").append(eq);
                    }
                    if (ar.reason() != null) sb.append(" (").append(ar.reason()).append(")");
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
    }

}
