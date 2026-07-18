package com.byz.factory.factory.physical;

import java.util.*;

/**
 * 产线 — 由工位引用序列组成的一条生产路径。
 * <p>
 * 产线通过 {@link LineNode} 引用工厂级工位，定义工位在该产线上的顺序。
 * 同一个工位可以被多条产线共用。
 *
 * @author 苏政
 */
public interface IProductionLine {

    /** 产线编码 */
    String getCode();

    /** 产线名称 */
    String getName();

    /** 该产线能执行的工序编码集合 */
    Set<String> getSupportedProcessCodes();

    /** 工位引用序列（按加工顺序排列） */
    List<LineNode> getNodes();

    /**
     * 解析 LineNode → 实际工位对象。
     * @param workstationRegistry 工厂工位注册表 (code → workstation)
     */
    default List<IWorkstation> resolveWorkstations(Map<String, IWorkstation> workstationRegistry) {
        return getNodes().stream()
            .map(n -> workstationRegistry.get(n.getWorkstationCode()))
            .filter(Objects::nonNull)
            .toList();
    }

    /** 该产线是否能执行指定动作 */
    default boolean canExecute(String actionCode, Map<String, IWorkstation> registry) {
        return resolveWorkstations(registry).stream()
            .anyMatch(w -> w.canExecute(actionCode));
    }

    /** 工序衔接参数（替代LineNode序列表达工序间关系） */
    default List<LineConnection> getConnections() {
        return Collections.emptyList();
    }

    /** 设备-动作预设分配: actionCode → equipmentCode */
    default Map<String, String> getEquipmentAssignments() {
        return Collections.emptyMap();
    }

    /** 汇总所有引用工位的全部动作 */
    default Set<String> getAllSupportedActions(Map<String, IWorkstation> registry) {
        Set<String> all = new LinkedHashSet<>();
        for (IWorkstation w : resolveWorkstations(registry)) {
            for (IEquipmentBinding b : w.getEquipmentBindings()) {
                all.addAll(b.getSupportedActionCodes());
            }
        }
        return all;
    }

    /** 产线能力报告 */
    default String toReport(Map<String, IWorkstation> registry) {
        StringBuilder sb = new StringBuilder();
        sb.append("产线[").append(getCode()).append("] ").append(getName()).append("\n");
        sb.append("  工序: ").append(getSupportedProcessCodes()).append("\n");
        for (LineNode node : getNodes()) {
            IWorkstation w = registry.get(node.getWorkstationCode());
            sb.append("  ├─ ").append(node);
            if (w != null) {
                sb.append(" ").append(w.getName());
                for (IEquipmentBinding b : w.getEquipmentBindings()) {
                    sb.append("\n  │    └─ ").append(b);
                }
            } else {
                sb.append(" ⚠️ 未注册");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
