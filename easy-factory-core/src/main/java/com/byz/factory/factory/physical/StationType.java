package com.byz.factory.factory.physical;

/**
 * 工位编组类型 — 描述工位内多台设备之间的协作关系。
 *
 * @author 苏政
 */
public enum StationType {

    /** 单设备 — 该工位只有一台设备（最常见） */
    SINGLE,

    /** 并行 — 多台设备同时执行相同动作，产能叠加 */
    PARALLEL,

    /** 串联 — 多台设备依次完成不同动作，前一设备输出是后一设备输入 */
    SERIES,

    /** 主备 — 主设备优先，备设备在主设备不可用时启用 */
    PRIMARY_BACKUP;

    /** 是否为多设备工位 */
    public boolean isMultiEquipment() {
        return this != SINGLE;
    }

    /** 是否需要检查设备可用性（PARALLEL 可降级，PRIMARY_BACKUP 可切换） */
    public boolean canDegrade() {
        return this == PARALLEL || this == PRIMARY_BACKUP;
    }
}
