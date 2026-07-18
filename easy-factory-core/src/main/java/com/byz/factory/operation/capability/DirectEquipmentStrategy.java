package com.byz.factory.operation.capability;

import com.byz.factory.factory.IFactory;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.factory.physical.*;
import com.byz.factory.process.IAction;
import com.byz.factory.process.IProcess;
import com.byz.factory.shared.Dict.Importance;

import java.util.*;

/**
 * 直接设备匹配 — 忽略产线，动作直接在全厂工位中查找设备。
 * <p>
 * 适用于：工厂产线概念模糊、设备可灵活调度、多品种小批量场景。
 *
 * @author 苏政
 */
public class DirectEquipmentStrategy implements IMatchingStrategy {

    @Override public String getName() { return "直接设备匹配"; }

    @Override
    public ProcessRouteMatcher.RouteMatchResult match(IBlueprint blueprint, IFactory factory) {
        List<IProcess> processes = blueprint.getProductionProcessList();
        Map<String, IWorkstation> registry = factory.getWorkstationRegistry();

        List<ProcessRouteMatcher.ProcessRouteResult> processResults = new ArrayList<>();
        int eq = 0, deg = 0, skip = 0, miss = 0;

        for (IProcess process : processes) {
            var pr = matchProcess(process, registry);
            processResults.add(pr);
            eq   += pr.equippedCount();
            deg  += pr.degradedCount();
            skip += pr.skippedCount();
            miss += pr.missingCount();
        }
        return new ProcessRouteMatcher.RouteMatchResult(blueprint.getCode(), miss == 0,
            processResults, eq, deg, skip, miss);
    }

    /** 包内可见：供 LineFirstStrategy 回退使用 */
    ProcessRouteMatcher.ProcessRouteResult matchProcess(IProcess process, Map<String, IWorkstation> registry) {
        List<ProcessRouteMatcher.ActionEquipmentResult> actions = new ArrayList<>();
        boolean feasible = true;
        for (IAction action : process.getActions()) {
            var r = findEquipment(action, registry);
            actions.add(r);
            if (r.status() == ProcessRouteMatcher.MatchStatus.MISSING) feasible = false;
        }
        return new ProcessRouteMatcher.ProcessRouteResult(process.getCode(), process.getName(),
            "全厂直接匹配", feasible, actions);
    }

    private ProcessRouteMatcher.ActionEquipmentResult findEquipment(IAction action, Map<String, IWorkstation> registry) {
        String code = action.getCode();
        boolean required = action.getImportance() == Importance.Require;
        List<ProcessRouteMatcher.EquipmentFound> found = new ArrayList<>();
        for (IWorkstation ws : registry.values()) {
            for (IEquipmentBinding b : ws.findCapableEquipment(code)) {
                found.add(ProcessRouteMatcher.EquipmentFound.direct(
                    b.getEquipmentCode(), ws.getCode(), ws.getStationType(), b.isPrimary()));
            }
        }
        if (!found.isEmpty()) {
            boolean hasPrimary = found.stream().anyMatch(ProcessRouteMatcher.EquipmentFound::primary);
            return ProcessRouteMatcher.ActionEquipmentResult.equipped(code, found,
                hasPrimary ? ProcessRouteMatcher.MatchStatus.EQUIPPED : ProcessRouteMatcher.MatchStatus.DEGRADED);
        }
        if (!required) return ProcessRouteMatcher.ActionEquipmentResult.skipped(code);
        return ProcessRouteMatcher.ActionEquipmentResult.missing(code, "无设备能执行此动作");
    }
}
