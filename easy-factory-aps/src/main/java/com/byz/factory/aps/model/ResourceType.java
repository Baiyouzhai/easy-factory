package com.byz.factory.aps.model;

/**
 * 排程资源类型 — 限定 APS 排程约束中涉及的可调度资源。
 * <p>
 * 映射自 core {@link com.byz.factory.Dict.SourceGroup} 的子集：
 * MACHINE → SourceGroup.Machine，PERSONNEL → SourceGroup.Personnel。
 *
 * @author 苏政
 */
public enum ResourceType {

    /** 设备/机器 */
    MACHINE,

    /** 操作人员 */
    PERSONNEL;

}
