package com.byz.factory.plm.service;

import com.byz.factory.factory.BlueprintDiff;
import com.byz.factory.factory.BlueprintDiff.DiffDimension;
import com.byz.factory.factory.BlueprintDiff.DiffItem;
import com.byz.factory.factory.BlueprintDiff.DiffType;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.factory.IBlueprintDiffer;
import com.byz.factory.process.IProcess;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 蓝图版本差异比较器默认实现。
 * <p>
 * 对比两个蓝图版本在工序、参数、资源三个维度的差异：
 * <ul>
 *   <li>工序维度：新增/删除/修改/重排序</li>
 *   <li>参数维度：参数的增删改</li>
 *   <li>资源维度：资源需求的变更</li>
 * </ul>
 *
 * @author 苏政
 */
public class BlueprintDifferImpl implements IBlueprintDiffer {

    @Override
    public BlueprintDiff compare(IBlueprint versionA, IBlueprint versionB) {
        List<DiffItem> items = new ArrayList<>();

        List<IProcess> processesA = versionA.getProductionProcessList();
        List<IProcess> processesB = versionB.getProductionProcessList();

        Map<String, IProcess> mapA = toCodeMap(processesA);
        Map<String, IProcess> mapB = toCodeMap(processesB);

        // 工序维度：检测新增/删除
        for (String code : mapB.keySet()) {
            if (!mapA.containsKey(code)) {
                items.add(new DiffItem(DiffDimension.PROCESS, DiffType.ADDED,
                        code, "新增工序: " + mapB.get(code).getName()));
            }
        }
        for (String code : mapA.keySet()) {
            if (!mapB.containsKey(code)) {
                items.add(new DiffItem(DiffDimension.PROCESS, DiffType.REMOVED,
                        code, "删除工序: " + mapA.get(code).getName()));
            }
        }

        // 工序维度：检测修改（同名工序的动作数或参数变化）
        for (String code : mapA.keySet()) {
            if (mapB.containsKey(code)) {
                IProcess pA = mapA.get(code);
                IProcess pB = mapB.get(code);

                if (pA.getActions() != null && pB.getActions() != null
                        && pA.getActions().size() != pB.getActions().size()) {
                    items.add(new DiffItem(DiffDimension.PROCESS, DiffType.MODIFIED,
                            code, "工序动作数变更: " + pA.getActions().size()
                                  + " → " + pB.getActions().size()));
                }

                // 参数维度
                diffParameters(code, pA, pB, items);
            }
        }

        // 工序维度：检测重排序
        if (processesA.size() == processesB.size() && !sameOrder(processesA, processesB)) {
            items.add(new DiffItem(DiffDimension.PROCESS, DiffType.REORDERED,
                    "-", "工序顺序已变更"));
        }

        return new BlueprintDiff(
                versionA.getVersion(),
                versionB.getVersion(),
                items);
    }

    /** 对比同一工序的资源维度差异 */
    private void diffParameters(String processCode, IProcess pA, IProcess pB,
                                 List<DiffItem> items) {
        int sizeA = pA.requireResources() != null ? pA.requireResources().length : 0;
        int sizeB = pB.requireResources() != null ? pB.requireResources().length : 0;
        if (sizeA != sizeB) {
            items.add(new DiffItem(DiffDimension.RESOURCE, DiffType.MODIFIED,
                    processCode, "资源需求变更: " + sizeA + " → " + sizeB + " 项"));
        }
    }

    /** 将工序列表按 code 编入有序映射（保留插入顺序） */
    private Map<String, IProcess> toCodeMap(List<IProcess> processes) {
        Map<String, IProcess> map = new LinkedHashMap<>();
        if (processes != null) {
            for (IProcess p : processes) {
                map.put(p.getCode(), p);
            }
        }
        return map;
    }

    /** 判断两个工序列表是否顺序一致 */
    private boolean sameOrder(List<IProcess> listA, List<IProcess> listB) {
        if (listA.size() != listB.size()) return false;
        for (int i = 0; i < listA.size(); i++) {
            if (!listA.get(i).getCode().equals(listB.get(i).getCode())) {
                return false;
            }
        }
        return true;
    }

}
