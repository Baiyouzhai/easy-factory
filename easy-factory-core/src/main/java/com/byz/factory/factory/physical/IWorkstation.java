package com.byz.factory.factory.physical;

import java.util.List;

/**
 * 工位 — 工厂级共享资源。工厂地板上的一个物理位置，安装了若干设备。
 * <p>
 * 同一个工位可以被多条产线引用（出现在不同产线的不同顺序位置）。
 *
 * @author 苏政
 */
public interface IWorkstation {

    /** 工位编码（唯一，工厂范围内） */
    String getCode();

    /** 工位名称 */
    String getName();

    /** 工位编组类型 */
    default StationType getStationType() { return StationType.SINGLE; }

    /** 安装在该工位的设备及其动作绑定 */
    List<IEquipmentBinding> getEquipmentBindings();

    /** 查找能执行指定动作的设备（主设备排前） */
    default List<IEquipmentBinding> findCapableEquipment(String actionCode) {
        return getEquipmentBindings().stream()
            .filter(b -> b.supports(actionCode))
            .sorted((a, b) -> Boolean.compare(b.isPrimary(), a.isPrimary()))
            .toList();
    }

    /** 该工位是否能执行指定动作 */
    default boolean canExecute(String actionCode) {
        return getEquipmentBindings().stream().anyMatch(b -> b.supports(actionCode));
    }
}
