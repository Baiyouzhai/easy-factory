package com.byz.factory.shared;

/**
 * 语义版本递增类型 — 配合 {@link SemanticVersionStrategy} 使用。
 *
 * @author 苏政
 */
public enum BumpType {
    /** 主版本号递增 — 破坏性变更（如删除关键工序） */
    MAJOR,
    /** 次版本号递增 — 功能新增（如添加工序） */
    MINOR,
    /** 修订号递增 — 问题修正（如参数微调） */
    PATCH
}
