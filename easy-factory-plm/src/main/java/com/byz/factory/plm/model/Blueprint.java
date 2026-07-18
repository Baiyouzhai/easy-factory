package com.byz.factory.plm.model;

import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.factory.BlueprintStatus;
import com.byz.factory.factory.IBlueprint;
import com.byz.factory.factory.PlmEventTypes;
import com.byz.factory.process.IProcess;
import com.byz.factory.shared.BaseLifecycleEntity;
import com.byz.factory.shared.BumpType;
import com.byz.factory.shared.IVersionStrategy;
import com.byz.factory.shared.SemanticVersionStrategy;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 蓝图 — PLM 核心实体，产品的工艺路线定义。
 * <p>
 * 继承 BaseLifecycleEntity 获得状态机（DRAFT→UNDER_REVIEW→APPROVED→RELEASED→OBSOLETED），
 * 实现 IBlueprint 供 MES/LIMS/MPS 等下游模块编译期引用。
 * <p>
 * 状态变更时自动通过 {@link DomainEventPublisher} 发布领域事件，
 * 通知 MES/DMS/APS 等订阅模块。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   plm.version        — 版本号
 *   plm.status         — 审批状态
 *   plm.author         — 设计人
 *   plm.approver       — 审批人
 *   plm.releaseDate    — 发布日期
 *   plm.effectiveDate  — 生效日期
 *   plm.changeReason   — 变更原因
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Blueprint extends BaseLifecycleEntity<BlueprintStatus> implements IBlueprint {

    /** 产品编码 */
    private String productCode;

    /** 版本号（语义版本） */
    private String version;

    /** 蓝图描述 */
    private String description;

    /** 工序清单 */
    private List<IProcess> processes;

    /** 设计人 */
    private String author;

    /** 审批人 */
    private String approvedBy;

    /** 版本递增策略（可注入，默认 MINOR 递增） */
    private IVersionStrategy versionStrategy;

    /**
     * @param code        蓝图编码
     * @param name        蓝图名称
     * @param productCode 产品编码
     */
    public Blueprint(String code, String name, String productCode) {
        super(code, name, BlueprintStatus.DRAFT);
        this.productCode = productCode;
        this.version = "0.1.0";
        this.processes = new ArrayList<>();
        this.versionStrategy = new SemanticVersionStrategy(BumpType.MINOR);
    }

    /** 获取生产工序清单（实现 IBlueprint 接口） */
    @Override
    public List<IProcess> getProductionProcessList() {
        return processes;
    }

    // ==================== 版本管理 ====================

    /**
     * 递增版本号（使用当前策略）。
     *
     * @param bumpType 递增类型
     * @return 新版本号
     */
    public String bumpVersion(BumpType bumpType) {
        IVersionStrategy strategy = bumpType == BumpType.MINOR
                ? this.versionStrategy
                : new SemanticVersionStrategy(bumpType);
        this.version = strategy.nextVersion(this.version);
        markUpdated();
        return this.version;
    }

    /**
     * 基于当前蓝图创建新版本草稿。
     * <p>
     * 通常由 {@link ChangeRequest} 实施时调用。
     *
     * @param newVersion  新版本号
     * @param changeReason 变更原因
     * @return 新版本蓝图（草稿状态）
     */
    public Blueprint createNewVersion(String newVersion, String changeReason) {
        Blueprint next = new Blueprint(this.getCode(), this.getName(), this.productCode);
        next.setVersion(newVersion);
        next.setDescription(this.description);
        next.setAuthor(this.author);
        next.setProcesses(new ArrayList<>(this.processes));
        next.setVersionStrategy(this.versionStrategy);
        // 在扩展属性中记录变更原因
        next.setExpandProperty("plm.changeReason", changeReason);
        next.setExpandProperty("plm.previousVersion", this.version);
        return next;
    }

    // ==================== 业务便捷方法（含事件发布） ====================

    /** 提交评审（DRAFT → UNDER_REVIEW） */
    public void submitForReview() {
        transition(BlueprintStatus.UNDER_REVIEW);
        markUpdated();
        publishEvent(PlmEventTypes.BLUEPRINT_SUBMITTED, Map.of(
                "blueprintCode", getCode(),
                "productCode", productCode,
                "version", version));
    }

    /** 批准蓝图（UNDER_REVIEW → APPROVED） */
    public void approve(String approvedBy) {
        this.approvedBy = approvedBy;
        transition(BlueprintStatus.APPROVED);
        markUpdated();
        publishEvent(PlmEventTypes.BLUEPRINT_APPROVED, Map.of(
                "blueprintCode", getCode(),
                "productCode", productCode,
                "version", version,
                "approvedBy", approvedBy));
    }

    /** 驳回评审（UNDER_REVIEW/APPROVED → DRAFT） */
    public void reject() {
        transition(BlueprintStatus.DRAFT);
        markUpdated();
        publishEvent(PlmEventTypes.BLUEPRINT_REJECTED, Map.of(
                "blueprintCode", getCode(),
                "version", version));
    }

    /** 发布到 MES（APPROVED → RELEASED） */
    public void release() {
        transition(BlueprintStatus.RELEASED);
        markUpdated();
        publishEvent(PlmEventTypes.BLUEPRINT_RELEASED, Map.of(
                "blueprintCode", getCode(),
                "productCode", productCode,
                "version", version,
                "processCount", processes != null ? processes.size() : 0));
    }

    /** 废弃蓝图（→ OBSOLETED） */
    public void obsolete() {
        transition(BlueprintStatus.OBSOLETED);
        markUpdated();
        publishEvent(PlmEventTypes.BLUEPRINT_OBSOLETED, Map.of(
                "blueprintCode", getCode(),
                "version", version));
    }

    // ==================== 内部 ====================

    private void publishEvent(String eventType, Object payload) {
        IDomainEvent event = IDomainEvent.of(eventType, "plm", payload);
        DomainEventPublisher.publish(event);
    }

}
