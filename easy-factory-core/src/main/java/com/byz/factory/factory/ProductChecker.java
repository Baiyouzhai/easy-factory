package com.byz.factory.factory;

import com.byz.factory.process.IAction;
import com.byz.factory.process.IProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 可制造性检查器实现。
 * <p>
 * 检查逻辑：
 * <ol>
 *   <li>遍历产品蓝图的每个工序</li>
 *   <li>检查工厂是否具备该工序（按 processCode 匹配）</li>
 *   <li>检查工厂工位是否能执行该工序的所有必要动作</li>
 *   <li>返回逐工序的通过/失败结果</li>
 * </ol>
 *
 * @author 苏政
 */
public class ProductChecker implements IProductChecker {

    @Override
    public ProductFactoryResult check(IProductInfo product, IFactory factory) {
        if (product == null || factory == null) {
            throw new IllegalArgumentException("product 和 factory 不能为 null");
        }

        IBlueprint blueprint = product.getBlueprint();
        if (blueprint == null) {
            return ProductFactoryResult.of(
                product.getName(), factory.getCode(),
                List.of(ProcessCheckResult.fail("N/A", "产品未关联蓝图")));
        }

        List<IProcess> requiredProcesses = blueprint.getProductionProcessList();
        if (requiredProcesses == null || requiredProcesses.isEmpty()) {
            return ProductFactoryResult.of(
                product.getName(), factory.getCode(),
                List.of(ProcessCheckResult.fail("N/A", "蓝图为空工序")));
        }

        // 构建工厂能力索引
        Set<String> factoryProcessCodes = factory.getProcesses().stream()
            .map(IProcess::getCode)
            .collect(Collectors.toSet());
        Set<String> allSupportedActions = factory.getWorkstationRegistry().values().stream()
            .flatMap(w -> w.getEquipmentBindings().stream())
            .flatMap(eb -> eb.getSupportedActionCodes().stream())
            .collect(Collectors.toSet());

        List<ProcessCheckResult> results = new ArrayList<>();
        for (IProcess process : requiredProcesses) {
            String processCode = process.getCode();

            // 检查工序是否存在
            if (!factoryProcessCodes.contains(processCode)) {
                results.add(ProcessCheckResult.fail(
                    processCode, "工厂缺少工序: " + processCode));
                continue;
            }

            // 检查工序的所有必要动作是否可执行
            List<IAction> actions = process.getActions();
            if (actions == null || actions.isEmpty()) {
                results.add(ProcessCheckResult.pass(processCode));
                continue;
            }

            List<String> missingActions = new ArrayList<>();
            for (IAction action : actions) {
                if (!allSupportedActions.contains(action.getCode())) {
                    missingActions.add(action.getCode());
                }
            }

            if (missingActions.isEmpty()) {
                results.add(ProcessCheckResult.pass(processCode));
            } else {
                results.add(ProcessCheckResult.fail(
                    processCode, "无法执行的动作: " + String.join(", ", missingActions)));
            }
        }

        return ProductFactoryResult.of(product.getName(), factory.getCode(), results);
    }

}
