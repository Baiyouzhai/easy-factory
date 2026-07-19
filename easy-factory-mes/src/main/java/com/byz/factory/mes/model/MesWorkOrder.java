package com.byz.factory.mes.model;

import com.byz.factory.batch.IWorkOrder;
import com.byz.factory.batch.WorkOrderStatus;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MES 工单实现 — 继承 BaseLifecycleEntity 获得状态机 + 审计能力。
 *
 * <h3>设计裁定（design-decisions.md §2.1）</h3>
 * <ul>
 *   <li><b>运行时解析 + 冻结快照</b>：工单发布时冻结 blueprintVersion，
 *       运行时按 blueprintCode + blueprintVersion 从 PLM 获取 Blueprint</li>
 *   <li>蓝图后续变更不影响已发布工单，节省存储（不复制工序树）</li>
 * </ul>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   mes.workOrderNo          — 工单号
 *   mes.productCode          — 产品编码
 *   mes.blueprintVersion     — 冻结的蓝图版本号
 *   mes.formulaCode          — 关联配方编码
 *   mes.recipeCode           — 关联设备配方编码
 *   mes.operator             — 操作人
 *   mes.batchNo              — 关联批号
 * </pre>
 *
 * <h3>注意</h3>
 * 模型层不发布领域事件——事件发布由 Service 实现类统一负责（design-decisions.md §1.1）。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWorkOrder extends BaseLifecycleEntity<WorkOrderStatus> implements IWorkOrder {

    /** 工单号 */
    private String workOrderNo;

    /** 产品编码 */
    private String productCode;

    /** 产品蓝图（运行时按 blueprintVersion 解析） */
    private IBlueprint blueprint;

    /** 蓝图版本号（工单发布时冻结，design-decisions.md §2.1） */
    private String blueprintVersion;

    /** 关联配方编码（追溯合规关键） */
    private String formulaCode;

    /** 关联设备配方编码 */
    private String recipeCode;

    /** 批量 */
    private BigDecimal quantity;

    /** 关联批号 */
    private String batchNo;

    /** 执行工厂 */
    private String factoryCode;

    /** 操作人 */
    private String operator;

    /** 计划开始时间 */
    private Instant plannedStart;

    /** 计划结束时间 */
    private Instant plannedEnd;

    /** 实际开始时间 */
    private Instant actualStart;

    /** 实际结束时间 */
    private Instant actualEnd;

    public MesWorkOrder(String workOrderNo, String productCode, BigDecimal quantity) {
        super(workOrderNo, productCode, WorkOrderStatus.CREATED);
        this.workOrderNo = workOrderNo;
        this.productCode = productCode;
        this.quantity = quantity;
    }

    // ==================== 业务便捷方法（不发布事件，仅状态转换 + 审计） ====================

    /**
     * 下达工单 — 冻结蓝图版本号（design-decisions.md §2.1）。
     * <p>
     * CREATED → RELEASED。
     *
     * @param blueprint        产品蓝图
     * @param operator         操作人
     * @param plannedStart     计划开始时间
     * @param plannedEnd       计划结束时间
     * @param factoryCode      执行工厂
     */
    public void release(IBlueprint blueprint, String operator,
                        Instant plannedStart, Instant plannedEnd, String factoryCode) {
        this.blueprint = blueprint;
        this.blueprintVersion = blueprint.getVersion();  // 冻结蓝图版本号
        this.operator = operator;
        this.plannedStart = plannedStart;
        this.plannedEnd = plannedEnd;
        this.factoryCode = factoryCode;
        transition(WorkOrderStatus.RELEASED);
        markUpdated();
    }

    /**
     * 开工 — 记录实际开始时间。
     * <p>
     * RELEASED → IN_PROGRESS。
     */
    public void start() {
        this.actualStart = Instant.now();
        transition(WorkOrderStatus.IN_PROGRESS);
        markUpdated();
    }

    /**
     * 完工 — 记录实际结束时间。
     * <p>
     * IN_PROGRESS → COMPLETED。
     */
    public void complete() {
        this.actualEnd = Instant.now();
        transition(WorkOrderStatus.COMPLETED);
        markUpdated();
    }

    /**
     * 关闭工单 — 终态。
     * <p>
     * COMPLETED → CLOSED。
     */
    public void close() {
        transition(WorkOrderStatus.CLOSED);
        markUpdated();
    }

    /**
     * 取消工单 — 终态。
     * <p>
     * 可从 CREATED / RELEASED / IN_PROGRESS 取消。
     */
    public void cancel() {
        transition(WorkOrderStatus.CANCELLED);
        markUpdated();
    }

    // ==================== 查询方法 ====================

    /**
     * 是否可编辑（仅在 CREATED 状态可编辑）。
     */
    public boolean isEditable() {
        return getStatus() == WorkOrderStatus.CREATED;
    }

    /**
     * 是否已关闭（终态）。
     */
    public boolean isClosed() {
        return getStatus() == WorkOrderStatus.CLOSED || getStatus() == WorkOrderStatus.CANCELLED;
    }

    /**
     * 蓝图版本是否已冻结。
     */
    public boolean isBlueprintVersionFrozen() {
        return blueprintVersion != null;
    }

}
