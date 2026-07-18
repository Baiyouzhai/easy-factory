package com.byz.factory.event.types;

/**
 * Equip 模块领域事件类型常量。
 * <p>
 * 格式遵循 {@link com.byz.factory.event.IDomainEvent#getEventType()} 约定：
 * {@code {module}.{entity}.{past_tense}}。
 * <p>
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>MES</b> — 订阅 {@link #STATUS_CHANGED} / {@link #FAULT_REPORTED}，
 *       设备故障时中断工单执行；订阅 {@link #PRODUCTION_STARTED} / {@link #PRODUCTION_STOPPED}，
 *       跟踪工单实际开工/完工时间</li>
 *   <li><b>Andon</b> — 订阅 {@link #FAULT_REPORTED}，触发安灯报警</li>
 *   <li><b>EAM</b> — 订阅 {@link #MAINTENANCE_STARTED} / {@link #MAINTENANCE_COMPLETED}，
 *       同步维护工单状态</li>
 *   <li><b>IoT</b> — 订阅 {@link #RECIPE_DISPATCHED}，接收参数下发指令；
 *       订阅 {@link #PARAMETER_UPDATED}，同步参数变更</li>
 *   <li><b>BI</b> — 订阅 {@link #OEE_CALCULATED}，汇总设备效率看板</li>
 * </ul>
 *
 * @author 苏政
 */
public final class EquipEventTypes {

    private EquipEventTypes() { /* 常量类 */ }

    /** 模块前缀 */
    public static final String PREFIX = "equip";

    // ── 设备状态 ──

    /** 设备状态变更 — 载荷: 设备编码 + 原状态 + 新状态 */
    public static final String STATUS_CHANGED = "equip.status.changed";

    /** 设备故障 — 载荷: 设备编码 + 故障原因（MES 订阅中断工单，Andon 订阅触发报警） */
    public static final String FAULT_REPORTED = "equip.fault.reported";

    // ── 生产运行 ──

    /** 设备开始生产 — 载荷: 设备编码 + 工单号（MES 订阅跟踪开工时间） */
    public static final String PRODUCTION_STARTED = "equip.production.started";

    /** 设备停止生产 — 载荷: 设备编码 + 工单号 */
    public static final String PRODUCTION_STOPPED = "equip.production.stopped";

    // ── 维护保养 ──

    /** 维护开始 — 载荷: 设备编码 + 维护类型（EAM 订阅同步维护工单） */
    public static final String MAINTENANCE_STARTED = "equip.maintenance.started";

    /** 维护完成 — 载荷: 设备编码 + 维护结果 */
    public static final String MAINTENANCE_COMPLETED = "equip.maintenance.completed";

    // ── 配方与参数 ──

    /** 配方已下发 — 载荷: 配方编码 + 设备编码（IoT 订阅执行参数下发） */
    public static final String RECIPE_DISPATCHED = "equip.recipe.dispatched";

    /** 参数已更新 — 载荷: 设备编码 + 参数编码 + 新设定值 */
    public static final String PARAMETER_UPDATED = "equip.parameter.updated";

    // ── OEE ──

    /** OEE 已计算 — 载荷: 设备编码 + 周期 + OEE 值（BI 订阅汇总看板） */
    public static final String OEE_CALCULATED = "equip.oee.calculated";

}
