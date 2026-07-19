package com.byz.factory.mps.service;

import com.byz.factory.mps.model.CapacityCheck;
import com.byz.factory.mps.model.DemandSource;
import com.byz.factory.mps.model.DemandSourceType;
import com.byz.factory.mps.model.ProductionPlan;
import com.byz.factory.mps.model.ProductionPlan.PlanItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 主生产计划服务 — MPS 模块核心业务接口。
 * <p>
 * 提供计划 CRUD、生命周期管理、需求管理和粗产能检查（RCCP）能力。
 * 其中 {@link #release} 是 MPS→MES 的关键契约——发布后 MES 订阅事件生成工单。
 * <p>
 * TODO 待实现：{@code MpsServiceImpl}
 *
 * @author 苏政
 */
public interface MpsService {

    // ── 计划 CRUD ──

    /**
     * 创建生产计划。
     *
     * @param periodType  周期类型 (WEEKLY / MONTHLY / QUARTERLY)
     * @param periodStart 周期起始
     * @param periodEnd   周期结束
     * @return 新建的生产计划（DRAFT 状态）
     */
    ProductionPlan create(String periodType, LocalDate periodStart, LocalDate periodEnd);

    /**
     * 根据计划编号查询。
     *
     * @param planNo 计划编号
     * @return 生产计划（可能为空）
     */
    Optional<ProductionPlan> getPlan(String planNo);

    /**
     * 按周期类型和状态列出计划。
     *
     * @param periodType 周期类型（null 表示不过滤）
     * @param status     状态（null 表示不过滤）
     * @return 计划列表
     */
    List<ProductionPlan> listPlans(String periodType, String status);

    /**
     * 向计划添加明细项。
     *
     * @param planNo 计划编号
     * @param item   计划明细
     */
    void addPlanItem(String planNo, PlanItem item);

    // ── 计划生命周期 ──

    /**
     * 审批通过（DRAFT → APPROVED）。
     *
     * @param planNo     计划编号
     * @param approvedBy 审批人
     */
    void approve(String planNo, String approvedBy);

    /**
     * 驳回（APPROVED → DRAFT）。
     *
     * @param planNo 计划编号
     */
    void reject(String planNo);

    /**
     * 发布到下游（APPROVED → RELEASED）。
     * <p>
     * MES 订阅 {@code mps.plan.released} 事件后为每个 PlanItem 生成工单。
     *
     * @param planNo 计划编号
     */
    void release(String planNo);

    /**
     * 开始执行（RELEASED → IN_PROGRESS）。
     *
     * @param planNo 计划编号
     */
    void start(String planNo);

    /**
     * 标记完成（IN_PROGRESS → COMPLETED）。
     *
     * @param planNo 计划编号
     */
    void complete(String planNo);

    /**
     * 关闭已完成计划（COMPLETED → CLOSED）。
     *
     * @param planNo 计划编号
     */
    void close(String planNo);

    // ── 需求管理 ──

    /**
     * 注册需求来源。
     *
     * @param sourceType  来源类型
     * @param referenceNo 来源单据号
     * @param productCode 产品编码
     * @param quantity    需求数量
     * @param dueDate     需求日期
     * @return 注册的需求来源
     */
    DemandSource registerDemand(DemandSourceType sourceType, String referenceNo,
                                String productCode, BigDecimal quantity, LocalDate dueDate);

    /**
     * 查询指定产品的需求列表。
     *
     * @param productCode 产品编码
     * @return 需求来源列表
     */
    List<DemandSource> getDemands(String productCode);

    // ── 粗产能检查 ──

    /**
     * 执行粗产能检查（RCCP）。
     * <p>
     * 底层使用 core 的 {@link com.byz.factory.operation.capacity.FactoryCapacityProfile}
     * 和 {@link com.byz.factory.operation.capacity.BottleneckDetector} 进行分析。
     *
     * @param planNo      计划编号
     * @param factoryCode 工厂编码
     * @return 粗产能检查结果
     */
    CapacityCheck checkCapacity(String planNo, String factoryCode);

}
