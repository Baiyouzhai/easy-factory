package com.byz.factory.event.types;

/**
 * DMS 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>LIMS</b> — 订阅 {@link #DOCUMENT_EFFECTIVE}，
 *       检验报告生效后同步更新 LIMS 报告状态</li>
 *   <li><b>QMS</b> — 订阅 {@link #DOCUMENT_SUBMITTED} / {@link #DOCUMENT_APPROVED}，
 *       偏差报告/CAPA 报告的审批流状态同步</li>
 *   <li><b>PLM</b> — 订阅 {@link #DOCUMENT_EFFECTIVE} / {@link #DOCUMENT_OBSOLETED}，
 *       工艺路线/SOP 的版本生效与作废同步</li>
 *   <li><b>EAM</b> — 订阅 {@link #DOCUMENT_EFFECTIVE}，
 *       校准证书归档后更新设备下次校准日期</li>
 *   <li><b>Equip</b> — 订阅 {@link #DOCUMENT_APPROVED}，
 *       设备验证文件审批通过后更新设备验证状态</li>
 * </ul>
 *
 * @author 苏政
 */
public final class DmsEventTypes {

    private DmsEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "dms";

    // ── 文档生命周期 ──

    /** 文档已提交审批 — 载荷: 文档编码 + 类别 */
    public static final String DOCUMENT_SUBMITTED = "dms.document.submitted";

    /** 文档已批准 — 载荷: 文档编码 + 审批人 */
    public static final String DOCUMENT_APPROVED = "dms.document.approved";

    /** 文档已驳回 — 载荷: 文档编码 + 驳回原因 */
    public static final String DOCUMENT_REJECTED = "dms.document.rejected";

    /** 文档已生效 — 载荷: 文档编码 + 版本号 + 生效日期 */
    public static final String DOCUMENT_EFFECTIVE = "dms.document.effective";

    /** 文档已作废 — 载荷: 文档编码 + 版本号 + 作废原因 */
    public static final String DOCUMENT_OBSOLETED = "dms.document.obsoleted";

    /** 文档已过期（复审逾期） — 载荷: 文档编码 + 应复审日期 */
    public static final String DOCUMENT_EXPIRED = "dms.document.expired";

    // ── 版本管理 ──

    /** 文档新版本已创建 — 载荷: 文档编码 + 旧版本号 → 新版本号 */
    public static final String DOCUMENT_VERSIONED = "dms.document.versioned";

    // ── 审批流 ──

    /** 审批步骤完成 — 载荷: 文档编码 + 步骤序号 + 决定 */
    public static final String APPROVAL_STEP_COMPLETED = "dms.approval.step.completed";

}
