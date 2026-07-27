package com.byz.factory.bi.model;

import com.byz.factory.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Map;

/**
 * KPI 快照 — 某个工厂在某个统计周期内的 KPI 指标汇总实体。
 * <p>
 * 继承 BaseEntity 获得 code/name/createdAt/updatedAt 基础字段。
 * KPI 快照是定期计算的数据快照（月度/季度/年度），不是实时数据。
 * 实时看板数据使用各个 Dashboard record 值对象。
 * <p>
 * KPI 计算来源（design-decisions.md §4.10、bi.md）：
 * <ul>
 *   <li>计划完成率 → MES 工单完成数 / MPS 计划数</li>
 *   <li>一次合格率 → QMS 一次合格批次 / 总检验批次</li>
 *   <li>OEE → Equip 可用率 × 性能率 × 质量率</li>
 *   <li>MTTR → EAM 总维修时间 / 维修次数</li>
 *   <li>MTBF → IoT+EAM 总运行时间 / 故障次数</li>
 *   <li>批次收率 → LIMS 实际产量 / 批量</li>
 *   <li>库存周转率 → WMS 月出库量 / 平均库存</li>
 *   <li>偏差关闭率 → QMS 已关闭偏差 / 总偏差</li>
 * </ul>
 * <p>
 * <h3>IExpand 约定</h3>
 * <pre>
 *   bi.kpi.period              — 统计周期
 *   bi.kpi.factoryCode         — 工厂编码
 *   bi.kpi.computedBy          — 计算人
 *   bi.kpi.computedAt          — 计算时间
 *   bi.kpi.remark              — 备注
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "kpi_snapshot")
public class KpiSnapshot extends BaseEntity {

    /** 统计周期（如 "2026-07", "2026-Q3", "2026"） */
    @Column(nullable = false, length = 20)
    private String period;

    /** 工厂编码 */
    @Column(name = "factory_code", nullable = false, length = 100)
    private String factoryCode;

    /** 计划完成率（%） */
    @Column(name = "plan_completion_rate", precision = 10, scale = 4)
    private BigDecimal planCompletionRate;

    /** 一次合格率（%） */
    @Column(name = "first_pass_rate", precision = 10, scale = 4)
    private BigDecimal firstPassRate;

    /** OEE 综合效率（%） */
    @Column(precision = 10, scale = 4)
    private BigDecimal oee;

    /** 平均维修时间（小时） */
    @Column(precision = 10, scale = 4)
    private BigDecimal mttr;

    /** 平均故障间隔（小时） */
    @Column(precision = 10, scale = 4)
    private BigDecimal mtbf;

    /** 批次收率（%） */
    @Column(name = "batch_yield", precision = 10, scale = 4)
    private BigDecimal batchYield;

    /** 库存周转率（次/月） */
    @Column(name = "inventory_turnover", precision = 10, scale = 4)
    private BigDecimal inventoryTurnover;

    /** 偏差关闭率（%） */
    @Column(name = "deviation_closure_rate", precision = 10, scale = 4)
    private BigDecimal deviationClosureRate;

    /** CAPA 关闭率（%） */
    @Column(name = "capa_closure_rate", precision = 10, scale = 4)
    private BigDecimal capaClosureRate;

    /** 安灯平均响应时间（分钟） */
    @Column(name = "andon_response_time", precision = 10, scale = 4)
    private BigDecimal andonResponseTime;

    /** 自定义 KPI 扩展（不持久化，使用 IExpand 存储） */
    @Transient
    private Map<String, BigDecimal> customKpis;

    /** 计算时间 */
    @Column(name = "computed_at", length = 50)
    private String computedAt;

    /** 计算人 */
    @Column(name = "computed_by", length = 100)
    private String computedBy;

    /** 备注 */
    @Column(length = 1000)
    private String remark;

    /** JPA 要求无参构造 */
    public KpiSnapshot() {
        super();
    }

    /**
     * @param code        快照编码（如 "KPI-2026-07-FACTORY-01"）
     * @param period      统计周期
     * @param factoryCode 工厂编码
     */
    public KpiSnapshot(String code, String period, String factoryCode) {
        super(code, "KPI-" + period + "-" + factoryCode);
        this.period = period;
        this.factoryCode = factoryCode;
        this.planCompletionRate = BigDecimal.ZERO;
        this.firstPassRate = BigDecimal.ZERO;
        this.oee = BigDecimal.ZERO;
        this.mttr = BigDecimal.ZERO;
        this.mtbf = BigDecimal.ZERO;
        this.batchYield = BigDecimal.ZERO;
        this.inventoryTurnover = BigDecimal.ZERO;
        this.deviationClosureRate = BigDecimal.ZERO;
        this.capaClosureRate = BigDecimal.ZERO;
        this.andonResponseTime = BigDecimal.ZERO;
    }

    // ==================== 业务便捷方法 ====================

    /**
     * 设置标准 KPI 值。
     *
     * @param type  KPI 类型
     * @param value KPI 值
     */
    public void setKpiValue(KpiType type, BigDecimal value) {
        switch (type) {
            case PLAN_COMPLETION_RATE -> this.planCompletionRate = value;
            case FIRST_PASS_RATE -> this.firstPassRate = value;
            case OEE -> this.oee = value;
            case MTTR -> this.mttr = value;
            case MTBF -> this.mtbf = value;
            case BATCH_YIELD -> this.batchYield = value;
            case INVENTORY_TURNOVER -> this.inventoryTurnover = value;
            case DEVIATION_CLOSURE_RATE -> this.deviationClosureRate = value;
            case CAPA_CLOSURE_RATE -> this.capaClosureRate = value;
            case ANDON_RESPONSE_TIME -> this.andonResponseTime = value;
        }
        markUpdated();
    }

    /**
     * 获取标准 KPI 值。
     *
     * @param type KPI 类型
     * @return KPI 值
     */
    public BigDecimal getKpiValue(KpiType type) {
        return switch (type) {
            case PLAN_COMPLETION_RATE -> planCompletionRate;
            case FIRST_PASS_RATE -> firstPassRate;
            case OEE -> oee;
            case MTTR -> mttr;
            case MTBF -> mtbf;
            case BATCH_YIELD -> batchYield;
            case INVENTORY_TURNOVER -> inventoryTurnover;
            case DEVIATION_CLOSURE_RATE -> deviationClosureRate;
            case CAPA_CLOSURE_RATE -> capaClosureRate;
            case ANDON_RESPONSE_TIME -> andonResponseTime;
        };
    }

    /**
     * 记录计算元数据。
     *
     * @param computedBy 计算人
     */
    public void markComputed(String computedBy) {
        this.computedBy = computedBy;
        this.computedAt = java.time.Instant.now().toString();
        markUpdated();
    }

    /**
     * 判断所有主要 KPI 是否均已计算（不为零）。
     * <p>
     * 注意：零值可能是"真的为 0"或"尚未计算"，调用方应根据业务场景判定。
     */
    public boolean isFullyComputed() {
        return planCompletionRate.compareTo(BigDecimal.ZERO) > 0
                || firstPassRate.compareTo(BigDecimal.ZERO) > 0
                || oee.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 获取所有已设置的 KPI 值（非零值）的数量。
     */
    public int getComputedKpiCount() {
        int count = 0;
        for (KpiType type : KpiType.values()) {
            BigDecimal v = getKpiValue(type);
            if (v != null && v.compareTo(BigDecimal.ZERO) != 0) count++;
        }
        return count;
    }

}
