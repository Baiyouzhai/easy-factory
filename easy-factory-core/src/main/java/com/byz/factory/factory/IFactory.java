package com.byz.factory.factory;

import com.byz.factory.factory.physical.IEquipmentBinding;
import com.byz.factory.factory.physical.IProductionLine;
import com.byz.factory.factory.physical.IWorkstation;
import com.byz.factory.process.IAction;
import com.byz.factory.process.IProcess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工厂 — 一个可执行的生产系统。
 * <p>
 * 两个维度描述：
 * <ul>
 *   <li><b>工艺层</b> — getProcesses() 声明的工序能力</li>
 *   <li><b>物理层</b> — getProductionLines() 实际的产线/工位/设备布局</li>
 * </ul>
 *
 * @author 苏政
 */
public interface IFactory {

    /** 工厂编码 */
    String getCode();

    /** 工厂名称 */
    String getName();

    // ── 工艺层 ──

    /** 工序清单（能力声明） */
    List<IProcess> getProcesses();

    /** 动作清单 — 汇总所有工序的动作 */
    default List<IAction> getActions() {
        List<IAction> actions = new ArrayList<>();
        List<IProcess> list = getProcesses();
        if (null != list) list.forEach(item -> actions.addAll(item.getActions()));
        return actions;
    }

    // ── 物理层 ──

    /** 产线清单 */
    default List<IProductionLine> getProductionLines() {
        return Collections.emptyList();
    }

    /** 工位注册表（工厂级共享，code→工位） */
    default Map<String, IWorkstation> getWorkstationRegistry() {
        return Collections.emptyMap();
    }

    /** 查找能执行指定动作的工位（遍历注册表） */
    default List<IWorkstation> findWorkstationsFor(String actionCode) {
        return getWorkstationRegistry().values().stream()
            .filter(w -> w.canExecute(actionCode))
            .toList();
    }

    /** 该工厂是否有设备能执行指定动作 */
    default boolean canExecute(String actionCode) {
        return getWorkstationRegistry().values().stream()
            .anyMatch(w -> w.canExecute(actionCode));
    }

    /** 设备池 — 全厂所有设备绑定（匹配引擎核心依据） */
    default Map<String, IEquipmentBinding> getEquipmentPool() {
        Map<String, IEquipmentBinding> pool = new LinkedHashMap<>();
        for (IWorkstation ws : getWorkstationRegistry().values()) {
            for (IEquipmentBinding b : ws.getEquipmentBindings()) {
                pool.put(b.getEquipmentCode(), b);
            }
        }
        return pool;
    }

    /** 查找引用指定工位的产线 */
    default List<IProductionLine> findLinesUsing(String workstationCode) {
        return getProductionLines().stream()
            .filter(line -> line.getNodes().stream()
                .anyMatch(n -> n.getWorkstationCode().equals(workstationCode)))
            .toList();
    }

}
