package com.byz.factory.process.action;

import com.byz.factory.process.IActionModel;
import com.byz.factory.resource.IResourcePack;

import java.math.BigDecimal;

/**
 * 质检动作 — IAction 在 QMS 子系统的扩展。
 * <p>
 * 执行时：取样→检测→记录实测值→与标准比对→判定（放行/拒收/返工）。
 * 如果判定失败 + Control=Interrupt → 中断工序流转，创建偏差。
 * 伴生结果：{@link QualityActionResult}
 *
 * @author 苏政
 */
public interface IQualityAction extends IActionModel {

    /** 检验方案编码 */
    String getInspectionPlanCode();

    /** 检验类型：IQC / IPQC / FQC / OQC */
    default String getInspectionType() { return "IPQC"; }

    /** 是否质量门禁（失败则中断工序） */
    default boolean isQualityGate() { return false; }

    /** 执行：检测 + 判定 */
    QualityActionResult executeQuality(IResourcePack input);

    /** 质检动作执行结果 */
    record QualityActionResult(
        String inspectionPlanCode,
        boolean passed,
        java.util.List<ItemResult> items,
        String judgment,          // ACCEPT / REJECT / REWORK
        String deviationCode      // 不合格时创建的偏差编码
    ) {
        public record ItemResult(
            String itemCode, String itemName,
            BigDecimal measuredValue, String unit,
            BigDecimal lsl, BigDecimal usl,
            boolean passed
        ) {}
    }
}
