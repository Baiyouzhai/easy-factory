package com.byz.factory.shared;

/**
 * 递增版本策略 — 简单整数递增（1 → 2 → 3 …）。
 * <p>
 * 适用于 DMS 文档、简单审批流等不需要语义版本的场景。
 *
 * @author 苏政
 */
public class IncrementalVersionStrategy implements IVersionStrategy {

    @Override
    public String nextVersion(String currentVersion) {
        int current = Integer.parseInt(currentVersion.trim());
        return String.valueOf(current + 1);
    }

    @Override
    public String initialVersion() {
        return "1";
    }

}
