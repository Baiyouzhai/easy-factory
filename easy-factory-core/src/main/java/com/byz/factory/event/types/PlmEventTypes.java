package com.byz.factory.event.types;

/**
 * PLM 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>MES</b> — 订阅 {@link #BLUEPRINT_RELEASED}，获取可执行的工艺路线</li>
 *   <li><b>DMS</b> — 订阅 {@link #BLUEPRINT_SUBMITTED} / {@link #BLUEPRINT_APPROVED}，触发审批流文档</li>
 *   <li><b>APS</b> — 订阅 {@link #BLUEPRINT_RELEASED} / {@link #BLUEPRINT_OBSOLETED}，更新排程可用蓝图列表</li>
 *   <li><b>ERP</b> — 订阅 {@link #BOM_TRANSFORMED}，同步物料需求到采购计划</li>
 *   <li><b>WMS</b> — 订阅 {@link #BLUEPRINT_RELEASED}，预置物料储位策略</li>
 * </ul>
 *
 * @author 苏政
 */
public final class PlmEventTypes {

    private PlmEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "plm";

    // ── 蓝图生命周期 ──

    /** 蓝图已提交评审 — 载荷: 蓝图编码 */
    public static final String BLUEPRINT_SUBMITTED = "plm.blueprint.submitted";

    /** 蓝图已批准 — 载荷: 蓝图编码 + 审批人 */
    public static final String BLUEPRINT_APPROVED = "plm.blueprint.approved";

    /** 蓝图已驳回 — 载荷: 蓝图编码 */
    public static final String BLUEPRINT_REJECTED = "plm.blueprint.rejected";

    /** 蓝图已发布 — 载荷: 蓝图编码 + 版本号 + 产品编码（MES 订阅此事件获取可执行蓝图） */
    public static final String BLUEPRINT_RELEASED = "plm.blueprint.released";

    /** 蓝图已废弃 — 载荷: 蓝图编码 + 版本号 */
    public static final String BLUEPRINT_OBSOLETED = "plm.blueprint.obsoleted";

    // ── 变更管理 ──

    /** 蓝图变更请求已提交 — 载荷: 蓝图编码 + 变更原因 */
    public static final String CHANGE_REQUESTED = "plm.change.requested";

    /** 蓝图变更请求已批准 — 载荷: 蓝图编码 + 新版本号 */
    public static final String CHANGE_APPROVED = "plm.change.approved";

    // ── BOM 转化 ──

    /** BOM 转化完成 — 载荷: 产品编码 + 源BOM类型 → 目标BOM类型 */
    public static final String BOM_TRANSFORMED = "plm.bom.transformed";

}
