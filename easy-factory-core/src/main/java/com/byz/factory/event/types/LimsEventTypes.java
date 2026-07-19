package com.byz.factory.event.types;

/**
 * LIMS 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>MES</b> — 订阅 {@link #FORMULA_ACTIVATED}，
 *       配方激活后同步更新工单可用配方；订阅 {@link #WEIGHING_COMPLETED}，
 *       称量完成后接收物料信息</li>
 *   <li><b>QMS</b> — 订阅 {@link #WEIGHING_COMPLETED}，
 *       称量偏差触发偏差处理流程；订阅 {@link #BATCH_RECORD_APPROVED}，
 *       批记录批准后关闭关联偏差</li>
 *   <li><b>DMS</b> — 订阅 {@link #BATCH_RECORD_CREATED}，
 *       批记录生成后归档至文档管理系统</li>
 *   <li><b>ERP</b> — 订阅 {@link #WEIGHING_COMPLETED}，
 *       称量完成后同步物料消耗到 ERP 库存</li>
 *   <li><b>IoT</b> — 订阅 {@link #WEIGHING_TASK_CREATED}，
 *       称量任务创建后激活对应天平数据采集</li>
 * </ul>
 *
 * @author 苏政
 */
public final class LimsEventTypes {

    private LimsEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "lims";

    // ── 配方生命周期 ──

    /** 配方已创建 — 载荷: 配方编码 + 产品编码 + 版本 */
    public static final String FORMULA_CREATED = "lims.formula.created";

    /** 配方已批准 — 载荷: 配方编码 + 批准人 + 版本 */
    public static final String FORMULA_APPROVED = "lims.formula.approved";

    /** 配方已激活 — 载荷: 配方编码 + 版本 */
    public static final String FORMULA_ACTIVATED = "lims.formula.activated";

    /** 配方已退役 — 载荷: 配方编码 + 版本 */
    public static final String FORMULA_RETIRED = "lims.formula.retired";

    // ── 称量任务 ──

    /** 称量任务已创建 — 载荷: 任务编码 + 配方编码 + 工单号 + 批号 */
    public static final String WEIGHING_TASK_CREATED = "lims.weighing.task_created";

    /** 称量已完成 — 载荷: 任务编码 + 物料明细列表 + 偏差列表 */
    public static final String WEIGHING_COMPLETED = "lims.weighing.completed";

    // ── 批记录 ──

    /** 批记录已创建 — 载荷: 批号 + 工单号 + 配方编码 */
    public static final String BATCH_RECORD_CREATED = "lims.batch_record.created";

    /** 批记录已批准 — 载荷: 批号 + 审核人 + 审核时间 */
    public static final String BATCH_RECORD_APPROVED = "lims.batch_record.approved";

    /** 批记录已归档 — 载荷: 批号 */
    public static final String BATCH_RECORD_ARCHIVED = "lims.batch_record.archived";

}
