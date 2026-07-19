package com.byz.factory.mps.model;

import com.byz.factory.batch.IProductionPlan;
import com.byz.factory.batch.ProductionPlanStatus;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.MpsEventTypes;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 生产计划 — MPS 核心实体，管理从草稿到关闭的完整生命周期。
 * <p>
 * 继承 BaseLifecycleEntity 获得状态机（DRAFT→APPROVED→RELEASED→IN_PROGRESS→COMPLETED→CLOSED），
 * 实现 IProductionPlan 供 MES/APS/ERP 等下游模块编译期引用。
 * <p>
 * 状态变更时自动通过 {@link DomainEventPublisher} 发布领域事件，
 * 通知 MES（生成工单）、APS（更新排程）、BI（执行看板）等订阅模块。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   mps.periodType       — 周期类型
 *   mps.periodStart      — 周期起始
 *   mps.periodEnd        — 周期结束
 *   mps.approvedBy       — 审批人
 *   mps.totalQuantity    — 计划总数量
 *   mps.itemCount        — 明细条目数
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductionPlan extends BaseLifecycleEntity<ProductionPlanStatus> implements IProductionPlan {

    /** 计划编号 */
    private String planNo;

    /** 周期类型 (WEEKLY / MONTHLY / QUARTERLY) */
    private String periodType;

    /** 周期起始日期 */
    private LocalDate periodStart;

    /** 周期结束日期 */
    private LocalDate periodEnd;

    /** 审批人 */
    private String approvedBy;

    /** 计划明细列表 */
    private List<PlanItem> items;

    /**
     * @param planNo      计划编号
     * @param periodType  周期类型
     * @param periodStart 周期起始
     * @param periodEnd   周期结束
     */
    public ProductionPlan(String planNo, String periodType, LocalDate periodStart, LocalDate periodEnd) {
        super(planNo, "计划-" + planNo, ProductionPlanStatus.DRAFT);
        this.planNo = planNo;
        this.periodType = periodType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.items = new ArrayList<>();
    }

    // ── IProductionPlan 接口方法 ──

    @Override
    public String getPlanNo() { return planNo; }

    @Override
    public ProductionPlanStatus getStatus() { return super.getStatus(); }

    @Override
    public List<IPlanItem> getItems() {
        if (items == null) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    // ── 业务便捷方法（含事件发布） ──

    /**
     * 审批通过（DRAFT → APPROVED）。
     *
     * @param approvedBy 审批人
     */
    public void approve(String approvedBy) {
        transition(ProductionPlanStatus.APPROVED);
        this.approvedBy = approvedBy;
        markUpdated();
        publishEvent(MpsEventTypes.PLAN_APPROVED, Map.of(
                "planNo", planNo,
                "approvedBy", approvedBy));
    }

    /**
     * 驳回（APPROVED → DRAFT）。
     * <p>
     * 审批不通过时退回草稿状态供修改。
     */
    public void reject() {
        transition(ProductionPlanStatus.DRAFT);
        markUpdated();
    }

    /**
     * 发布到下游（APPROVED → RELEASED）。
     * <p>
     * MES 订阅 {@code mps.plan.released} 事件后为每个 PlanItem 生成工单。
     */
    public void release() {
        transition(ProductionPlanStatus.RELEASED);
        markUpdated();
        publishEvent(MpsEventTypes.PLAN_RELEASED, Map.of(
                "planNo", planNo,
                "itemCount", items != null ? items.size() : 0));
    }

    /**
     * 开始执行（RELEASED → IN_PROGRESS）。
     */
    public void start() {
        transition(ProductionPlanStatus.IN_PROGRESS);
        markUpdated();
        publishEvent(MpsEventTypes.PLAN_STARTED, Map.of("planNo", planNo));
    }

    /**
     * 标记完成（IN_PROGRESS → COMPLETED）。
     */
    public void complete() {
        transition(ProductionPlanStatus.COMPLETED);
        markUpdated();
        publishEvent(MpsEventTypes.PLAN_COMPLETED, Map.of("planNo", planNo));
    }

    /**
     * 关闭已完成计划（COMPLETED → CLOSED）。
     * <p>
     * 关闭后不可再进行任何状态变更。
     */
    public void close() {
        transition(ProductionPlanStatus.CLOSED);
        markUpdated();
        publishEvent(MpsEventTypes.PLAN_CLOSED, Map.of("planNo", planNo));
    }

    // ── 明细管理 ──

    /**
     * 添加计划明细项。
     *
     * @param item 计划明细
     */
    public void addItem(PlanItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        markUpdated();
    }

    /**
     * 移除计划明细项。
     *
     * @param productCode 产品编码
     */
    public void removeItem(String productCode) {
        if (this.items != null) {
            this.items.removeIf(item -> item.getProductCode().equals(productCode));
            markUpdated();
        }
    }

    // ── 查询方法 ──

    /**
     * 判断计划是否处于可编辑状态。
     */
    public boolean isEditable() {
        ProductionPlanStatus s = getStatus();
        return s == ProductionPlanStatus.DRAFT;
    }

    /**
     * 判断计划是否已发布给下游。
     */
    public boolean isReleased() {
        ProductionPlanStatus s = getStatus();
        return s == ProductionPlanStatus.RELEASED
            || s == ProductionPlanStatus.IN_PROGRESS
            || s == ProductionPlanStatus.COMPLETED
            || s == ProductionPlanStatus.CLOSED;
    }

    /**
     * 计算计划总数量。
     */
    public BigDecimal getTotalQuantity() {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .map(PlanItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ── 内部：事件发布 ──

    private void publishEvent(String eventType, Object payload) {
        IDomainEvent event = IDomainEvent.of(eventType, "mps", payload);
        DomainEventPublisher.publish(event);
    }

    // ── PlanItem ──

    /**
     * 计划明细项 — 单个产品的一次生产计划条目。
     * <p>
     * 实现 {@link IProductionPlan.IPlanItem} 供 MES/APS 等消费模块编译期引用。
     */
    @Data
    public static class PlanItem implements IPlanItem {

        /** 产品编码 */
        private String productCode;

        /** 产品名称 */
        private String productName;

        /** 计划数量 */
        private BigDecimal quantity;

        /** 交付日期 */
        private LocalDate dueDate;

        /** 优先级（1=最高） */
        private int priority;

        /** 执行工厂编码 */
        private String factoryCode;

        public PlanItem(String productCode, String productName, BigDecimal quantity,
                        LocalDate dueDate, int priority, String factoryCode) {
            this.productCode = productCode;
            this.productName = productName;
            this.quantity = quantity;
            this.dueDate = dueDate;
            this.priority = priority;
            this.factoryCode = factoryCode;
        }

    }

}
