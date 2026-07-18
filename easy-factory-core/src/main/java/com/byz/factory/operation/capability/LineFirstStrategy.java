package com.byz.factory.operation.capability;

import com.byz.factory.factory.IFactory;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.factory.physical.*;
import com.byz.factory.process.IAction;
import com.byz.factory.process.IProcess;
import com.byz.factory.shared.Dict.Importance;

import java.util.*;

/**
 * 线优先匹配策略 — 优先在一条产线内整线匹配，线不可用才降级到设备池。
 *
 * @author 苏政
 */
public class LineFirstStrategy implements IMatchingStrategy {

    @Override public String getName() { return "线优先匹配"; }

    @Override
    public ProcessRouteMatcher.RouteMatchResult match(IBlueprint blueprint, IFactory factory) {
        Map<String, IWorkstation> registry = factory.getWorkstationRegistry();
        List<IProductionLine> lines = factory.getProductionLines();
        List<IProcess> processes = blueprint.getProductionProcessList();

        List<ProcessRouteMatcher.ProcessRouteResult> processResults = new ArrayList<>();
        int eq = 0, deg = 0, skip = 0, miss = 0;

        for (IProcess process : processes) {
            var pr = matchProcessOnBestLine(process, lines, registry);
            processResults.add(pr);
            eq   += pr.equippedCount();
            deg  += pr.degradedCount();
            skip += pr.skippedCount();
            miss += pr.missingCount();
        }
        return new ProcessRouteMatcher.RouteMatchResult(blueprint.getCode(), miss == 0,
            processResults, eq, deg, skip, miss);
    }

    /** 找最佳产线 — 全覆盖 > 部分覆盖 > 设备池回退 */
    private ProcessRouteMatcher.ProcessRouteResult matchProcessOnBestLine(
            IProcess process, List<IProductionLine> lines, Map<String, IWorkstation> registry) {
        List<IProductionLine> matched = lines.stream()
            .filter(l -> l.getSupportedProcessCodes().contains(process.getCode())).toList();

        if (matched.isEmpty()) {
            return fallbackToPool(process, registry);
        }

        // 对每条产线独立匹配
        List<LineAttempt> attempts = new ArrayList<>();
        for (IProductionLine line : matched) {
            attempts.add(tryOnLine(process, line, registry));
        }

        LineAttempt best = attempts.stream()
            .min(Comparator.comparingInt(LineAttempt::missing)
                  .thenComparingInt(a -> -a.equipped))
            .orElse(attempts.get(0));

        if (best.missing == 0) {
            return buildResult(process, best.lineCode, best.results, true);
        }
        // 无线全覆盖 → 回退设备池
        return fallbackToPool(process, registry);
    }

    private LineAttempt tryOnLine(IProcess process, IProductionLine line, Map<String, IWorkstation> registry) {
        List<ProcessRouteMatcher.ActionEquipmentResult> results = new ArrayList<>();
        int missing = 0, equipped = 0;
        for (IAction action : process.getActions()) {
            var r = findOnLine(action, line, registry);
            results.add(r);
            if (r.status() == ProcessRouteMatcher.MatchStatus.MISSING) missing++;
            if (r.status() == ProcessRouteMatcher.MatchStatus.EQUIPPED) equipped++;
        }
        return new LineAttempt(line.getCode(), results, missing, equipped);
    }

    private ProcessRouteMatcher.ActionEquipmentResult findOnLine(IAction action, IProductionLine line,
                                                                  Map<String, IWorkstation> registry) {
        String code = action.getCode();
        boolean required = action.getImportance() == Importance.Require;
        List<ProcessRouteMatcher.EquipmentFound> found = new ArrayList<>();
        for (LineNode node : line.getNodes()) {
            IWorkstation ws = registry.get(node.getWorkstationCode());
            if (ws == null) continue;
            for (IEquipmentBinding b : ws.findCapableEquipment(code)) {
                found.add(new ProcessRouteMatcher.EquipmentFound(
                    b.getEquipmentCode(), ws.getCode(), line.getCode(), b.isPrimary(), node.getSequence()));
            }
        }
        if (!found.isEmpty()) {
            boolean hasPrimary = found.stream().anyMatch(ProcessRouteMatcher.EquipmentFound::primary);
            return ProcessRouteMatcher.ActionEquipmentResult.equipped(code, found,
                hasPrimary ? ProcessRouteMatcher.MatchStatus.EQUIPPED : ProcessRouteMatcher.MatchStatus.DEGRADED);
        }
        if (!required) return ProcessRouteMatcher.ActionEquipmentResult.skipped(code);
        return ProcessRouteMatcher.ActionEquipmentResult.missing(code, "产线" + line.getCode() + "无设备");
    }

    private ProcessRouteMatcher.ProcessRouteResult fallbackToPool(IProcess process, Map<String, IWorkstation> registry) {
        DirectEquipmentStrategy fallback = new DirectEquipmentStrategy();
        return fallback.matchProcess(process, registry);
    }

    private ProcessRouteMatcher.ProcessRouteResult buildResult(IProcess process, String line,
                                                                List<ProcessRouteMatcher.ActionEquipmentResult> actions,
                                                                boolean feasible) {
        return new ProcessRouteMatcher.ProcessRouteResult(process.getCode(), process.getName(),
            line, feasible, actions);
    }

    private record LineAttempt(String lineCode, List<ProcessRouteMatcher.ActionEquipmentResult> results,
                                int missing, int equipped) {}
}
