package com.byz.factory.lims.model;

import com.byz.factory.lims.FormulaStatus;
import com.byz.factory.lims.IFormula;
import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.LimsEventTypes;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.shared.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配方 — LIMS 核心实体，管理产品配方的完整生命周期。
 * <p>
 * 继承 BaseLifecycleEntity 获得状态机（DRAFT→APPROVED→ACTIVE→RETIRED），
 * 实现 IFormula 供 MES/QMS/ERP 等下游模块编译期引用，
 * 实现 HasVersion 提供语义版本管理。
 * <p>
 * 状态变更时自动通过 {@link DomainEventPublisher} 发布领域事件，
 * 通知 MES/QMS/ERP/IoT 等订阅模块。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   lims.formulaCode       — 配方编码
 *   lims.formulaVersion    — 版本号
 *   lims.batchSize         — 批量规模
 *   lims.status            — DRAFT / APPROVED / ACTIVE / RETIRED
 *   lims.effectiveDate     — 生效日期
 *   lims.expiryDate        — 失效日期
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Formula extends BaseLifecycleEntity<FormulaStatus> implements IFormula, HasVersion {

    /** 对应产品编码 */
    private String productCode;

    /** 版本号（语义版本，如 "1.0.0"） */
    private String version;

    /** 标准批量规模 */
    private BigDecimal batchSize;

    /** 配方组分（来自 core 的 IResourcePack） */
    private IResourcePack resourcePack;

    /** 批准人 */
    private String approvedBy;

    /** 收率范围（如 "95-102%"） */
    private String yield;

    /** 投料阶段列表 */
    private List<FormulaPhase> phases;

    /** 版本递增策略（默认 MINOR 递增） */
    private IVersionStrategy versionStrategy;

    /**
     * @param code        配方编码
     * @param name        配方名称
     * @param productCode 对应产品编码
     */
    public Formula(String code, String name, String productCode) {
        super(code, name, FormulaStatus.DRAFT);
        this.productCode = productCode;
        this.versionStrategy = new SemanticVersionStrategy(BumpType.MINOR);
        this.version = versionStrategy.initialVersion();
        this.phases = new ArrayList<>();
    }

    // ==================== 版本管理 ====================

    /**
     * 递增版本号（使用当前策略）。
     *
     * @return 新版本号
     */
    public String bumpVersion(BumpType bumpType) {
        IVersionStrategy strategy = new SemanticVersionStrategy(bumpType);
        this.version = strategy.nextVersion(this.version);
        markUpdated();
        return this.version;
    }

    /**
     * 基于当前配方创建新版本草稿。
     * <p>
     * 当前配方退役后，通过此方法创建新版本配方（保留原始编码，从 DRAFT 重新开始）。
     *
     * @param newVersion 新版本号
     * @return 新版本草稿
     */
    public Formula createNewVersion(String newVersion) {
        Formula next = new Formula(this.getCode(), this.getName(), this.productCode);
        next.setVersion(newVersion);
        next.setBatchSize(this.batchSize);
        next.setYield(this.yield);
        next.setResourcePack(this.resourcePack != null ? this.resourcePack.copy() : null);
        next.setVersionStrategy(this.versionStrategy);
        // 复制投料阶段
        List<FormulaPhase> copiedPhases = new ArrayList<>();
        if (this.phases != null) {
            for (FormulaPhase p : this.phases) {
                copiedPhases.add(new FormulaPhase(p.getPhaseNo(), p.getPhaseName()));
            }
        }
        next.setPhases(copiedPhases);
        next.setExpandProperty("lims.previousVersion", this.version);
        return next;
    }

    // ==================== 业务便捷方法（含事件发布） ====================

    /**
     * 提交审批（DRAFT → APPROVED）。
     */
    public void submitForApproval() {
        transition(FormulaStatus.APPROVED);
        markUpdated();
        publishEvent(LimsEventTypes.FORMULA_CREATED, Map.of(
                "formulaCode", getCode(),
                "productCode", productCode,
                "version", version));
    }

    /**
     * 批准配方（APPROVED → APPROVED，记录批准人）。
     * <p>
     * 注意：此方法仅记录批准人，状态不变（保持 APPROVED），
     * 供审批流中审批人确认后调用。
     *
     * @param approvedBy 批准人
     */
    public void approve(String approvedBy) {
        this.approvedBy = approvedBy;
        markUpdated();
        publishEvent(LimsEventTypes.FORMULA_APPROVED, Map.of(
                "formulaCode", getCode(),
                "version", version,
                "approvedBy", approvedBy));
    }

    /**
     * 激活配方（APPROVED → ACTIVE）。
     * <p>
     * 激活后配方可用于生产，MES 可引用此版本创建工单。
     */
    public void activate() {
        transition(FormulaStatus.ACTIVE);
        markUpdated();
        publishEvent(LimsEventTypes.FORMULA_ACTIVATED, Map.of(
                "formulaCode", getCode(),
                "version", version));
    }

    /**
     * 驳回配方（APPROVED → DRAFT）。
     * <p>
     * 审批不通过时退回草稿状态供修改。
     *
     * @param reason 驳回原因
     */
    public void reject(String reason) {
        transition(FormulaStatus.DRAFT);
        markUpdated();
        setExpandProperty("lims.rejectionReason", reason);
    }

    /**
     * 退役配方（ACTIVE → RETIRED）。
     * <p>
     * 退役后不再用于新生产，但历史批记录保留对旧版本的引用。
     *
     * @param reason 退役原因
     */
    public void retire(String reason) {
        transition(FormulaStatus.RETIRED);
        markUpdated();
        setExpandProperty("lims.retireReason", reason);
        publishEvent(LimsEventTypes.FORMULA_RETIRED, Map.of(
                "formulaCode", getCode(),
                "version", version,
                "reason", reason));
    }

    /**
     * 添加投料阶段。
     *
     * @param phase 投料阶段
     */
    public void addPhase(FormulaPhase phase) {
        if (this.phases == null) {
            this.phases = new ArrayList<>();
        }
        this.phases.add(phase);
        markUpdated();
    }

    // ==================== 查询方法 ====================

    /**
     * 判断配方是否处于可编辑状态。
     */
    public boolean isEditable() {
        FormulaStatus s = getStatus();
        return s == FormulaStatus.DRAFT;
    }

    /**
     * 判断配方是否可用于生产。
     */
    public boolean isActive() {
        return getStatus() == FormulaStatus.ACTIVE;
    }

    // ==================== 内部类 ====================

    /**
     * 投料阶段 — 配方中按工艺阶段划分的物料投放分组。
     * <p>
     * 参照 EquipmentRecipe.RecipePhase 内部类模式。
     */
    @Data
    public static class FormulaPhase implements IFormulaPhase {

        /** 阶段号（从 1 开始） */
        private int phaseNo;

        /** 阶段名称（如 "预处理"、"主反应"、"后处理"） */
        private String phaseName;

        /** 阶段内的投料动作列表 */
        private List<String> actions;

        /** 阶段条件（如温度/压力/搅拌要求，自由文本或 JSON） */
        private String conditions;

        public FormulaPhase(int phaseNo, String phaseName) {
            this.phaseNo = phaseNo;
            this.phaseName = phaseName;
            this.actions = new ArrayList<>();
        }

    }

    // ==================== 内部 ====================

    private void publishEvent(String eventType, Object payload) {
        IDomainEvent event = IDomainEvent.of(eventType, "lims", payload);
        DomainEventPublisher.publish(event);
    }

}
