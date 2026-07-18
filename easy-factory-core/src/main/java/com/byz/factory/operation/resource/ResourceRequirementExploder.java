package com.byz.factory.operation.resource;

import com.byz.factory.factory.IBlueprint;
import com.byz.factory.process.IAction;
import com.byz.factory.process.IActionModel;
import com.byz.factory.process.IProcess;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.resource.ResourcePack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 资源需求展开器 — 按蓝图展开全部工序的资源需求，支持批量缩放和良率调整。
 * <p>
 * 典型用法：
 * <pre>{@code
 * ExploderResult result = new ResourceRequirementExploder()
 *     .withYield(processCode, new BigDecimal("0.95"))
 *     .explode(blueprint, batchSize);
 * IResourcePack total = result.getTotalResources();
 * }</pre>
 *
 * @author 苏政
 */
public class ResourceRequirementExploder {

    /** 每道工序的良率（默认 1.0 = 100%） */
    private final Map<String, BigDecimal> processYields = new LinkedHashMap<>();

    /**
     * 设置某道工序的良率。
     */
    public ResourceRequirementExploder withYield(String processCode, BigDecimal yield) {
        processYields.put(processCode, yield);
        return this;
    }

    /**
     * 展开资源需求。
     *
     * @param blueprint 产品蓝图
     * @param batchSize 目标批量
     * @return 展开结果
     */
    public ExploderResult explode(IBlueprint blueprint, BigDecimal batchSize) {
        List<IProcess> processes = blueprint.getProductionProcessList();
        if (processes == null || processes.isEmpty()) {
            return new ExploderResult(new ResourcePack(), Collections.emptyList());
        }

        ResourcePack totalResources = new ResourcePack();
        List<ProcessRequirement> breakdown = new ArrayList<>();
        BigDecimal cumulativeYield = BigDecimal.ONE;

        for (IProcess process : processes) {
            BigDecimal processYield = processYields.getOrDefault(
                process.getCode(), BigDecimal.ONE);
            ResourcePack processRequirement = new ResourcePack();

            for (IAction action : process.getActions()) {
                IResourceItem[] required = getRequiredResources(action);
                if (required == null) continue;

                for (IResourceItem item : required) {
                    // 按批量缩放
                    IResourceItem scaled = item.copy();
                    scaled.setNumber(item.getNumber().multiply(batchSize));
                    // 按累计良率放大
                    if (cumulativeYield.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal adjusted = scaled.getNumber()
                            .divide(cumulativeYield, 4, RoundingMode.HALF_UP);
                        scaled.setNumber(adjusted);
                    }
                    processRequirement.merge(scaled);
                    totalResources.merge(scaled);
                }
            }

            processRequirement.compress();
            breakdown.add(new ProcessRequirement(process.getCode(), process.getName(),
                processYield, processRequirement));

            // 更新累计良率
            cumulativeYield = cumulativeYield.multiply(processYield);
        }

        totalResources.compress();
        return new ExploderResult(totalResources, breakdown);
    }

    /**
     * 获取动作的资源需求（兼容 IAction 和 IActionModel）。
     */
    private IResourceItem[] getRequiredResources(IAction action) {
        if (action instanceof IActionModel model) {
            return model.requireResources();
        }
        return null;
    }

    // ---- 结果类型 ----

    /**
     * 单道工序的资源需求
     */
    public record ProcessRequirement(
        String processCode,
        String processName,
        BigDecimal yield,
        IResourcePack resources
    ) {}

    /**
     * 展开结果
     */
    public record ExploderResult(
        IResourcePack totalResources,
        List<ProcessRequirement> breakdown
    ) {
        /** 总资源种类数 */
        public int getResourceTypeCount() {
            IResourceItem[] items = totalResources.getResources();
            return items != null ? items.length : 0;
        }

        /** 生成需求报告 */
        public String toReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 资源需求展开 ===\n\n");
            for (ProcessRequirement pr : breakdown) {
                sb.append("工序[").append(pr.processCode()).append("] ")
                  .append(pr.processName())
                  .append(" (良率: ").append(pr.yield()).append(")\n");
                IResourceItem[] items = pr.resources().getResources();
                if (items != null) {
                    for (IResourceItem item : items) {
                        sb.append("  ├─ ").append(item.getName())
                          .append(" × ").append(item.getNumber())
                          .append(" [").append(item.getGroup()).append("]\n");
                    }
                }
            }
            sb.append("\n总计: ").append(getResourceTypeCount()).append(" 种资源\n");
            return sb.toString();
        }
    }

}
