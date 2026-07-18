package com.byz.factory.process.action;

import com.byz.factory.process.IActionModel;
import com.byz.factory.resource.IResourcePack;

/**
 * 合规动作 — IAction 在 DMS/GMP 子系统的扩展。
 * <p>
 * 执行时：清场检查→双人复核→电子签名→审计追踪记录。
 * 伴生结果：{@link ComplianceActionResult}
 *
 * @author 苏政
 */
public interface IComplianceAction extends IActionModel {

    /** 是否需要执行前清场检查 */
    default boolean requiresLineClearance() { return false; }

    /** 清场检查表编码 */
    default String getClearanceChecklistCode() { return null; }

    /** 是否需要双人复核 */
    default boolean requiresSecondPersonReview() { return false; }

    /** 是否需要见证签名 */
    default boolean requiresWitnessSignature() { return false; }

    /** 洁净区级别：A/B/C/D */
    default String getCleanroomClass() { return null; }

    /** 执行：清场+复核+签名 */
    ComplianceActionResult executeCompliance(IResourcePack input);

    /** 合规动作执行结果 */
    record ComplianceActionResult(
        boolean clearancePassed,       // 清场是否通过
        String clearanceBy,            // 清场确认人
        String reviewedBy,             // 复核人
        String witnessedBy,            // 见证人
        java.util.List<String> signatures,  // 电子签名ID列表
        String auditTrailId            // 审计追踪ID
    ) {}
}
