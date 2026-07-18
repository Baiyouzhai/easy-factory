package com.byz.factory.process.action;

import com.byz.factory.process.IActionModel;
import com.byz.factory.resource.IResourcePack;

import java.math.BigDecimal;

/**
 * 物料动作 — IAction 在 WMS/LIMS 子系统的扩展。
 * <p>
 * 执行时：称量→投料→消耗记录→产出记录→物料追溯。
 * 伴生结果：{@link MaterialActionResult}
 *
 * @author 苏政
 */
public interface IMaterialAction extends IActionModel {

    /** 输入物料清单（消耗） */
    IResourcePack getInputMaterials();

    /** 输出物料清单（产出+废料） */
    IResourcePack getOutputMaterials();

    /** 称量精度要求 */
    default BigDecimal getDispensingPrecision() { return null; }

    /** 是否需要双人复核称量 */
    default boolean requiresDoubleCheck() { return false; }

    /** 执行：称量+投料+消耗/产出 */
    MaterialActionResult executeMaterial(IResourcePack input);

    /** 物料动作执行结果 */
    record MaterialActionResult(
        boolean success,
        IResourcePack consumed,        // 实际消耗
        IResourcePack produced,        // 实际产出（含废料）
        BigDecimal actualYield,        // 实际收率
        String weighingRecordId,       // 称量记录ID
        String batchNo                 // 关联批号
    ) {}
}
