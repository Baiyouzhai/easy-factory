package com.byz.factory.shared;

/**
 * 版本号递增策略 — 定义版本号的生成规则。
 * <p>
 * 各模块按需选择策略：
 * <ul>
 *   <li>PLM 蓝图 — 优先使用 {@link SemanticVersionStrategy}（语义化版本）</li>
 *   <li>DMS 文档 — 可使用 {@link IncrementalVersionStrategy}（简单递增）</li>
 *   <li>配方/工艺模板 — 可使用任意策略</li>
 * </ul>
 * <p>
 * 用法：
 * <pre>{@code
 * IVersionStrategy strategy = new SemanticVersionStrategy(BumpType.MINOR);
 * String next = strategy.nextVersion("1.0.0");  // → "1.1.0"
 * }</pre>
 *
 * @author 苏政
 * @see SemanticVersion
 * @see HasVersion
 */
@FunctionalInterface
public interface IVersionStrategy {

    /**
     * 根据当前版本号计算下一个版本号。
     *
     * @param currentVersion 当前版本号字符串
     * @return 下一个版本号字符串
     * @throws IllegalArgumentException 版本号格式无效
     */
    String nextVersion(String currentVersion);

    /**
     * 获取初始版本号。
     *
     * @return 初始版本号字符串
     */
    default String initialVersion() {
        return SemanticVersion.INITIAL.toString();
    }

}
