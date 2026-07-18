package com.byz.factory.shared;

/**
 * 语义化版本号 — 遵循 MAJOR.MINOR.PATCH 规范。
 * <p>
 * 版本号比较规则：
 * <ul>
 *   <li>MAJOR — 不兼容的 API 修改（如工序大改）</li>
 *   <li>MINOR — 向下兼容的功能新增（如添加工序）</li>
 *   <li>PATCH — 向下兼容的问题修正（如参数微调）</li>
 * </ul>
 * <p>
 * 用法：
 * <pre>{@code
 * SemanticVersion v = SemanticVersion.parse("1.2.0");
 * SemanticVersion next = v.nextMajor();  // "2.0.0"
 * SemanticVersion minor = v.nextMinor(); // "1.3.0"
 * SemanticVersion patch = v.nextPatch(); // "1.2.1"
 * }</pre>
 *
 * @param major 主版本号
 * @param minor 次版本号
 * @param patch 修订号
 * @author 苏政
 */
public record SemanticVersion(int major, int minor, int patch) implements Comparable<SemanticVersion> {

    public SemanticVersion {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("版本号不允许为负数: " + major + "." + minor + "." + patch);
        }
    }

    /** 初始版本 */
    public static final SemanticVersion INITIAL = new SemanticVersion(0, 1, 0);

    /** 从字符串解析，支持 "1.0.0" 和 "1.0" 两种格式 */
    public static SemanticVersion parse(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("版本号不能为空");
        }
        String[] parts = version.trim().split("\\.");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("无效的语义版本号: " + version + "，期望 MAJOR.MINOR 或 MAJOR.MINOR.PATCH");
        }
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
        return new SemanticVersion(major, minor, patch);
    }

    /** 尝试解析，失败返回 null（不抛异常） */
    public static SemanticVersion tryParse(String version) {
        try {
            return parse(version);
        } catch (Exception e) {
            return null;
        }
    }

    /** 下一个主版本（破坏性变更） */
    public SemanticVersion nextMajor() {
        return new SemanticVersion(major + 1, 0, 0);
    }

    /** 下一个次版本（功能新增） */
    public SemanticVersion nextMinor() {
        return new SemanticVersion(major, minor + 1, 0);
    }

    /** 下一个修订版（问题修复） */
    public SemanticVersion nextPatch() {
        return new SemanticVersion(major, minor, patch + 1);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public int compareTo(SemanticVersion other) {
        int cmp = Integer.compare(this.major, other.major);
        if (cmp != 0) return cmp;
        cmp = Integer.compare(this.minor, other.minor);
        if (cmp != 0) return cmp;
        return Integer.compare(this.patch, other.patch);
    }
}
