package com.byz.factory.andon.model;

import com.byz.factory.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 安灯上报规则 — 定义异常逐级上报的静态配置。
 * <p>
 * 每条规则针对一种触发类型 × 严重程度的组合，配置多级上报链。
 * 超过指定超时时间无人响应时，自动升级到下一级别。
 * 上报规则为静态配置（数据库表），非脚本化（design-decisions.md §3.4）。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   andon.rule.createdBy      — 创建人
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "andon_escalation_rule")
public class EscalationRule extends BaseEntity {

    /** 触发类型 */
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 30)
    private TriggerType triggerType;

    /** 严重程度 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AndonSeverity severity;

    /** 自动停止策略 */
    @Enumerated(EnumType.STRING)
    @Column(name = "auto_stop", nullable = false, length = 20)
    private AutoStop autoStop;

    /** 是否启用 */
    @Column(nullable = false)
    private boolean enabled;

    /** 逐级上报链（暂存内存，不做 JPA 级联持久化） */
    @Transient
    private List<EscalationLevel> levels;

    /** JPA 要求无参构造 */
    public EscalationRule() {
        super();
    }

    /**
     * @param code        规则编码
     * @param triggerType 触发类型
     * @param severity    严重程度
     */
    public EscalationRule(String code, TriggerType triggerType, AndonSeverity severity) {
        super(code, "上报规则-" + triggerType.name() + "-" + severity.name());
        this.triggerType = triggerType;
        this.severity = severity;
        this.autoStop = AutoStop.NONE;
        this.enabled = true;
        this.levels = new ArrayList<>();
    }

    // ==================== 业务方法 ====================

    /** 添加上报级别。 */
    public void addLevel(EscalationLevel level) {
        if (this.levels == null) {
            this.levels = new ArrayList<>();
        }
        this.levels.add(level);
        markUpdated();
    }

    /** 按级别序号获取上报级别（1-based），不存在返回 null。 */
    public EscalationLevel getLevel(int level) {
        if (levels == null) return null;
        return levels.stream()
                .filter(l -> l.getLevel() == level)
                .findFirst()
                .orElse(null);
    }

    /** 获取最大上报级别数。 */
    public int getMaxLevel() {
        return levels != null ? levels.size() : 0;
    }

    /** 启用规则。 */
    public void enable() {
        this.enabled = true;
        markUpdated();
    }

    /** 禁用规则。 */
    public void disable() {
        this.enabled = false;
        markUpdated();
    }

    /** 设置自动停止策略。 */
    public void setAutoStopPolicy(AutoStop autoStop) {
        this.autoStop = autoStop;
        markUpdated();
    }

    // ==================== 内部类 ====================

    /**
     * 上报级别 — 每个级别定义超时时间、通知角色和上报条件。
     */
    @Data
    public static class EscalationLevel {

        /** 级别序号（1-based） */
        private int level;

        /** 超时时间（分钟），0 表示立即通知 */
        private int timeoutMinutes;

        /** 通知角色列表 */
        private List<String> notifyRoles;

        /** 上报条件 */
        private EscalateCondition escalateOn;

        public EscalationLevel() {
            this.notifyRoles = new ArrayList<>();
        }

        public EscalationLevel(int level, int timeoutMinutes, EscalateCondition escalateOn) {
            this.level = level;
            this.timeoutMinutes = timeoutMinutes;
            this.escalateOn = escalateOn;
            this.notifyRoles = new ArrayList<>();
        }

        /** 添加通知角色。 */
        public void addNotifyRole(String role) {
            if (this.notifyRoles == null) {
                this.notifyRoles = new ArrayList<>();
            }
            this.notifyRoles.add(role);
        }

    }

    // ==================== 值枚举 ====================

    /** 自动停止策略。 */
    public enum AutoStop {
        NONE, PAUSE_PROCESS, STOP_LINE, STOP_FACTORY;
    }

    /** 上报条件。 */
    public enum EscalateCondition {
        TIMEOUT, NO_RESPONSE;
    }

}
