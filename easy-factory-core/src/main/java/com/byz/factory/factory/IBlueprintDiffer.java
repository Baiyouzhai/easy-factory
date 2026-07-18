package com.byz.factory.factory;

import java.util.List;

/**
 * 蓝图差异比较器 — 对比两个蓝图版本的差异。
 * <p>
 * 差异维度：工序变更、参数变更、资源变更。
 *
 * @author 苏政
 */
@FunctionalInterface
public interface IBlueprintDiffer {

    /**
     * 对比两个蓝图版本的差异
     *
     * @param versionA 版本 A（通常为旧版本）
     * @param versionB 版本 B（通常为新版本）
     * @return 差异报告：工序变更、参数变更、资源变更
     */
    BlueprintDiff compare(IBlueprint versionA, IBlueprint versionB);

}
