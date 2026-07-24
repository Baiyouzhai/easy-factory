package com.byz.factory.event.types;

/**
 * QMS 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>MES</b> — 订阅 {@link #INSPECTION_PASSED}，
 *       检验通过后恢复工序流转；订阅 {@link #INSPECTION_FAILED}，
 *       检验失败后暂停工序；订阅 {@link #DEVIATION_RESOLVED}，
 *       偏差解决后恢复生产</li>
 *   <li><b>Andon</b> — 订阅 {@link #DEVIATION_CREATED}，
 *       偏差创建时触发安灯呼叫；订阅 {@link #SPC_OUT_OF_CONTROL}，
 *       SPC 失控时触发异常报警</li>
 *   <li><b>DMS</b> — 订阅 {@link #CAPA_CREATED}，
 *       CAPA 创建后触发审批流文档；订阅 {@link #CAPA_VERIFIED}，
 *       CAPA 验证完成后归档</li>
 *   <li><b>LIMS</b> — 订阅 {@link #INSPECTION_COMPLETED}，
 *       检验完成后更新批记录中的检验章节</li>
 *   <li><b>IoT</b> — 订阅 {@link #INSPECTION_ORDER_CREATED}，
 *       激活检验用仪器数据采集</li>
 *   <li><b>BI</b> — 订阅 {@link #INSPECTION_COMPLETED} / {@link #DEVIATION_CREATED} /
 *       {@link #CAPA_CLOSED}，汇总质量看板数据</li>
 * </ul>
 *
 * @author 苏政
 */
public final class QmsEventTypes {

    private QmsEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "qms";

    // ── 检验生命周期 ──

    /** 检验指令已创建 — 载荷: inspectionNo + workOrderNo + processCode + planCode */
    public static final String INSPECTION_ORDER_CREATED = "qms.inspection.created";

    /** 检验已开始 — 载荷: inspectionNo + inspector + startedAt */
    public static final String INSPECTION_STARTED = "qms.inspection.started";

    /** 检验已完成（含判定） — 载荷: inspectionNo + result + defectCount */
    public static final String INSPECTION_COMPLETED = "qms.inspection.completed";

    /** 检验通过 — 载荷: inspectionNo + processCode + workOrderNo */
    public static final String INSPECTION_PASSED = "qms.inspection.passed";

    /** 检验不合格 — 载荷: inspectionNo + processCode + failedItems */
    public static final String INSPECTION_FAILED = "qms.inspection.failed";

    // ── 偏差管理 ──

    /** 偏差已创建 — 载荷: deviationCode + inspectionNo + severity + source */
    public static final String DEVIATION_CREATED = "qms.deviation.created";

    /** 偏差调查完成 — 载荷: deviationCode + rootCause + productImpact */
    public static final String DEVIATION_INVESTIGATED = "qms.deviation.investigated";

    /** 偏差已处置 — 载荷: deviationCode + disposition + disposedBy */
    public static final String DEVIATION_DISPOSITIONED = "qms.deviation.dispositioned";

    /** 偏差已解决 — 载荷: deviationCode + resolvedAt */
    public static final String DEVIATION_RESOLVED = "qms.deviation.resolved";

    /** 偏差已关闭 — 载荷: deviationCode + closedAt */
    public static final String DEVIATION_CLOSED = "qms.deviation.closed";

    // ── CAPA 管理 ──

    /** CAPA 已创建 — 载荷: capaCode + deviationCode + assignTo + dueDate */
    public static final String CAPA_CREATED = "qms.capa.created";

    /** CAPA 根因分析完成 — 载荷: capaCode + rootCause */
    public static final String CAPA_ROOT_CAUSE_DONE = "qms.capa.root_cause_done";

    /** CAPA 执行中 — 载荷: capaCode + correctiveAction + preventiveAction */
    public static final String CAPA_IN_PROGRESS = "qms.capa.in_progress";

    /** CAPA 已验证 — 载荷: capaCode + verification + verifiedAt */
    public static final String CAPA_VERIFIED = "qms.capa.verified";

    /** CAPA 已关闭 — 载荷: capaCode + closedAt */
    public static final String CAPA_CLOSED = "qms.capa.closed";

    // ── SPC 统计过程控制 ──

    /** SPC 数据点已采集 — 载荷: processCode + parameterCode + value + timestamp */
    public static final String SPC_DATA_COLLECTED = "qms.spc.data_collected";

    /** SPC 控制限警告 — 载荷: processCode + parameterCode + rule + value + limit */
    public static final String SPC_WARNING = "qms.spc.warning";

    /** SPC 失控 — 载荷: processCode + parameterCode + rule + value + controlLimit */
    public static final String SPC_OUT_OF_CONTROL = "qms.spc.out_of_control";

}
