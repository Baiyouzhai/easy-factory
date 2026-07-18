package com.byz.factory.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 蓝图版本差异报告 — {@link IBlueprintDiffer#compare} 的返回值。
 * <p>
 * 记录两个蓝图版本之间在工序、参数、资源三个维度的所有差异项。
 *
 * @param versionA   版本 A 的版本号
 * @param versionB   版本 B 的版本号
 * @param diffItems  差异项列表
 * @author 苏政
 */
public record BlueprintDiff(
        String versionA,
        String versionB,
        List<BlueprintDiff.DiffItem> diffItems) {

    public BlueprintDiff {
        diffItems = Collections.unmodifiableList(new ArrayList<>(diffItems));
    }

    /** 差异类型 */
    public enum DiffType {
        /** 新增 */
        ADDED,
        /** 删除 */
        REMOVED,
        /** 修改 */
        MODIFIED,
        /** 重排序 */
        REORDERED
    }

    /** 差异维度 */
    public enum DiffDimension {
        /** 工序变更 */
        PROCESS,
        /** 参数变更 */
        PARAMETER,
        /** 资源变更 */
        RESOURCE
    }

    /**
     * 单个差异项
     *
     * @param dimension  差异维度
     * @param diffType   差异类型
     * @param targetCode 变更对象编码（工序编码/参数编码/资源编码）
     * @param detail     差异详情描述
     */
    public record DiffItem(DiffDimension dimension, DiffType diffType,
                           String targetCode, String detail) {}

    /** 是否有差异 */
    public boolean hasDiff() {
        return !diffItems.isEmpty();
    }
}
