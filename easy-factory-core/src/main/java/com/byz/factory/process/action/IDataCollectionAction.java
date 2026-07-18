package com.byz.factory.process.action;

import com.byz.factory.process.IActionModel;
import com.byz.factory.resource.IResourcePack;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * 数据采集动作 — IAction 在 IoT 子系统的扩展。
 * <p>
 * 执行时：按标签列表读取传感器数据、按频率采集、存储时序数据、触发报警。
 * 伴生结果：{@link DataCollectionResult}
 *
 * @author 苏政
 */
public interface IDataCollectionAction extends IActionModel {

    /** 采集标签列表 */
    java.util.List<DataTag> getDataTags();

    /** 采集间隔 */
    Duration getCollectionInterval();

    /** 是否连续采集（false=定时采集） */
    default boolean isContinuous() { return false; }

    /** 执行：开始/停止采集 */
    DataCollectionResult executeCollection(IResourcePack input);

    /** 采集标签定义 */
    record DataTag(String tagName, String equipmentCode, String unit, String dataType) {}

    /** 报警规则 */
    record AlarmRule(String tagName, BigDecimal highLimit, BigDecimal lowLimit, String severity) {}

    /** 数据采集结果 */
    record DataCollectionResult(
        boolean started,
        String sessionId,
        int tagCount,
        Duration duration
    ) {}
}
