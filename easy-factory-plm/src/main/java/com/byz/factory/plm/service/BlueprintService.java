package com.byz.factory.plm.service;

import com.byz.factory.factory.*;
import com.byz.factory.plm.bom.BOMConversionRequest;
import com.byz.factory.plm.bom.BOMConversionResult;
import com.byz.factory.plm.model.Blueprint;
import com.byz.factory.plm.model.ChangeRequest;
import com.byz.factory.plm.model.ProcessParameter;
import com.byz.factory.plm.model.ProcessTemplate;
import com.byz.factory.process.IProcessParameter;
import com.byz.factory.shared.BumpType;

/**
 * 蓝图服务 — PLM 模块核心。
 * <p>
 * 管理工艺路线的全生命周期：创建→评审→批准→发布→废弃，
 * 以及工艺参数管理、版本管理和 BOM 转化。
 *
 * <h3>跨模块协作（通过领域事件 {@link com.byz.factory.event.DomainEventPublisher}）</h3>
 * <table>
 *   <tr><th>方向</th><th>事件</th><th>订阅方</th><th>用途</th></tr>
 *   <tr><td>PLM → MES</td><td>{@code plm.blueprint.released}</td><td>MES</td><td>获取可执行的工艺路线，创建工单</td></tr>
 *   <tr><td>PLM → DMS</td><td>{@code plm.blueprint.submitted}</td><td>DMS</td><td>触发审批流文档创建</td></tr>
 *   <tr><td>PLM → DMS</td><td>{@code plm.blueprint.approved}</td><td>DMS</td><td>审批完成，归档</td></tr>
 *   <tr><td>PLM → APS</td><td>{@code plm.blueprint.released}</td><td>APS</td><td>更新排程可用蓝图列表</td></tr>
 *   <tr><td>PLM → APS</td><td>{@code plm.blueprint.obsoleted}</td><td>APS</td><td>移除已废弃蓝图</td></tr>
 *   <tr><td>PLM → ERP</td><td>{@code plm.bom.transformed}</td><td>ERP</td><td>同步物料需求到采购计划</td></tr>
 *   <tr><td>PLM ← ERP</td><td>—</td><td>—</td><td>获取物料主数据供 BOM 引用（同步调用）</td></tr>
 * </table>
 *
 * @author 苏政
 * @see PlmEventTypes
 */
public interface BlueprintService {

    // ==================== 蓝图生命周期 ====================

    /** 从工艺模板创建蓝图草稿 */
    Blueprint createBlueprint(ProcessTemplate template, String productCode);

    /** 提交评审（DRAFT → UNDER_REVIEW）。发布 {@code plm.blueprint.submitted} 事件 */
    Blueprint submitForReview(String blueprintCode);

    /** 批准蓝图（UNDER_REVIEW → APPROVED）。发布 {@code plm.blueprint.approved} 事件 */
    Blueprint approve(String blueprintCode, String approvedBy);

    /** 驳回评审（UNDER_REVIEW/APPROVED → DRAFT）。发布 {@code plm.blueprint.rejected} 事件 */
    Blueprint reject(String blueprintCode);

    /** 发布到 MES（APPROVED → RELEASED）。发布 {@code plm.blueprint.released} 事件 */
    Blueprint releaseVersion(String blueprintCode);

    /** 废弃蓝图（→ OBSOLETED）。发布 {@code plm.blueprint.obsoleted} 事件 */
    Blueprint obsolete(String blueprintCode);

    // ==================== 工艺参数管理 ====================

    /** 为蓝图中的工序添加工艺参数 */
    ProcessParameter addProcessParameter(String blueprintCode, String processCode,
                                         ProcessParameter parameter);

    /** 更新工艺参数 */
    ProcessParameter updateProcessParameter(String blueprintCode, String processCode,
                                            String parameterCode, IProcessParameter newValue);

    // ==================== 版本管理 ====================

    /** 基于当前已发布版本创建新版本草稿 */
    Blueprint createNewVersion(String blueprintCode, String newVersion, String changeReason);

    /** 递增版本号（使用当前策略） */
    Blueprint bumpVersion(String blueprintCode, BumpType bumpType);

    /** 对比两个蓝图版本的差异 */
    BlueprintDiff diff(String blueprintCode, String versionA, String versionB);

    // ==================== BOM 管理 ====================

    /**
     * 执行 BOM 转化（委托 Common 模块的规则引擎）。
     * <p>
     * EBOM（设计 BOM）→ PBOM（工艺 BOM）→ MBOM（制造 BOM）。
     * 转化完成后发布 {@code plm.bom.transformed} 事件，ERP 可订阅同步物料需求。
     */
    BOMConversionResult convertBOM(BOMConversionRequest request);

    /** EBOM（设计 BOM）→ PBOM（工艺 BOM）转化（便捷方法） */
    BOMConversionResult convertEBOMtoPBOM(String productCode);

    /** PBOM（工艺 BOM）→ MBOM（制造 BOM）转化（便捷方法） */
    BOMConversionResult convertPBOMtoMBOM(String productCode, String factoryCode);

    // ==================== 工厂可制造性检查 ====================

    /**
     * 检查蓝图在指定工厂的可制造性。
     * <p>
     * 委托 core 的 {@link IProductChecker} 执行工序-工厂匹配。
     *
     * @param blueprintCode 蓝图编码
     * @param factoryCode   工厂编码
     * @return 逐工序检查结果
     */
    ProductFactoryResult checkFeasibility(String blueprintCode, String factoryCode);

    /**
     * 将蓝图动作匹配到工厂设备（细粒度）。
     * <p>
     * 委托 core 的 {@link com.byz.factory.operation.capability.ProcessRouteMatcher}。
     */
    com.byz.factory.operation.capability.ProcessRouteMatcher.RouteMatchResult
        matchEquipment(String blueprintCode, String factoryCode);

    // ==================== 变更管理 ====================

    /** 创建变更请求 */
    ChangeRequest createChangeRequest(String code, String blueprintCode,
                                       String fromVersion, String changeReason, String requestedBy);

    /** 提交变更请求评审 */
    ChangeRequest submitChangeRequest(String changeRequestCode);

    /** 批准变更请求 */
    ChangeRequest approveChangeRequest(String changeRequestCode, String approvedBy);

    /** 实施变更（创建蓝图新版本 + 关闭变更请求） */
    Blueprint implementChange(String changeRequestCode, String newVersion);

}
