package com.byz.factory.shared;

/**
 * 语义化版本策略 — MAJOR.MINOR.PATCH 递增。
 * <p>
 * 适用于 PLM 蓝图、工艺模板等需要精确表达变更规模的实体。
 * <p>
 * 用法：
 * <pre>{@code
 * IVersionStrategy strategy = new SemanticVersionStrategy(BumpType.MINOR);
 * strategy.nextVersion("1.0.0");  // → "1.1.0"
 * strategy.nextVersion("2.3.1");  // → "2.4.0"
 * }</pre>
 *
 * @author 苏政
 */
public class SemanticVersionStrategy implements IVersionStrategy {

    private final BumpType bumpType;

    public SemanticVersionStrategy(BumpType bumpType) {
        this.bumpType = bumpType;
    }

    @Override
    public String nextVersion(String currentVersion) {
        SemanticVersion v = SemanticVersion.parse(currentVersion);
        return switch (bumpType) {
            case MAJOR -> v.nextMajor().toString();
            case MINOR -> v.nextMinor().toString();
            case PATCH -> v.nextPatch().toString();
        };
    }

}
