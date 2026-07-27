package com.byz.factory.plm.service.impl;

import com.byz.factory.factory.*;
import com.byz.factory.operation.capability.ProcessRouteMatcher;
import com.byz.factory.plm.bom.BOMConversionRequest;
import com.byz.factory.plm.bom.BOMConversionResult;
import com.byz.factory.plm.model.Blueprint;
import com.byz.factory.plm.model.ChangeRequest;
import com.byz.factory.plm.model.ProcessParameter;
import com.byz.factory.plm.model.ProcessTemplate;
import com.byz.factory.plm.repository.*;
import com.byz.factory.plm.service.BlueprintDifferImpl;
import com.byz.factory.plm.service.BlueprintService;
import com.byz.factory.process.IProcessParameter;
import com.byz.factory.shared.BumpType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 蓝图服务实现 — PLM 模块核心服务。
 * <p>
 * 管理工艺路线的全生命周期，使用 Spring 事务管理确保操作原子性。
 * 蓝图状态变更时，模型自身通过 {@link com.byz.factory.event.DomainEventPublisher} 发布事件。
 *
 * <h3>依赖说明</h3>
 * <ul>
 *   <li>BOM 转化 — 委托 Common 模块的规则引擎（{@code IRuleEngine}），
 *       当前为存根实现，等待 Common 模块就绪</li>
 *   <li>可制造性检查 — 委托 core 的 {@link ProductChecker} / {@link ProcessRouteMatcher}，
 *       需要工厂数据（{@code IFactory} 实现）</li>
 * </ul>
 *
 * @author 苏政
 */
@Service
@Transactional
public class BlueprintServiceImpl implements BlueprintService {

    private final BlueprintRepository blueprintRepo;
    private final ProcessTemplateRepository templateRepo;
    private final ProcessParameterRepository parameterRepo;
    private final ChangeRequestRepository changeRequestRepo;
    private final BlueprintDifferImpl blueprintDiffer;

    public BlueprintServiceImpl(BlueprintRepository blueprintRepo,
                                 ProcessTemplateRepository templateRepo,
                                 ProcessParameterRepository parameterRepo,
                                 ChangeRequestRepository changeRequestRepo,
                                 BlueprintDifferImpl blueprintDiffer) {
        this.blueprintRepo = blueprintRepo;
        this.templateRepo = templateRepo;
        this.parameterRepo = parameterRepo;
        this.changeRequestRepo = changeRequestRepo;
        this.blueprintDiffer = blueprintDiffer;
    }

    // ==================== 蓝图生命周期 ====================

    @Override
    public Blueprint createBlueprint(ProcessTemplate template, String productCode) {
        if (template == null) throw new IllegalArgumentException("工艺模板不能为 null");

        String code = "BP-" + productCode + "-" + System.currentTimeMillis() % 100000;
        Blueprint bp = new Blueprint(code, template.getName() + "-蓝图", productCode);
        bp.setAuthor(template.getCategory());  // 记录模板类别便于追溯

        // 从模板复制工序和参数
        if (template.getProcesses() != null) {
            bp.setProcesses(new java.util.ArrayList<>(template.getProcesses()));
        }
        return blueprintRepo.save(bp);
    }

    @Override
    public Blueprint submitForReview(String blueprintCode) {
        Blueprint bp = findBlueprint(blueprintCode);
        bp.submitForReview();
        return blueprintRepo.save(bp);
    }

    @Override
    public Blueprint approve(String blueprintCode, String approvedBy) {
        Blueprint bp = findBlueprint(blueprintCode);
        bp.approve(approvedBy);
        return blueprintRepo.save(bp);
    }

    @Override
    public Blueprint reject(String blueprintCode) {
        Blueprint bp = findBlueprint(blueprintCode);
        bp.reject();
        return blueprintRepo.save(bp);
    }

    @Override
    public Blueprint releaseVersion(String blueprintCode) {
        Blueprint bp = findBlueprint(blueprintCode);
        bp.release();
        return blueprintRepo.save(bp);
    }

    @Override
    public Blueprint obsolete(String blueprintCode) {
        Blueprint bp = findBlueprint(blueprintCode);
        bp.obsolete();
        return blueprintRepo.save(bp);
    }

    // ==================== 工艺参数管理 ====================

    @Override
    public ProcessParameter addProcessParameter(String blueprintCode, String processCode,
                                                 ProcessParameter parameter) {
        // 验证蓝图存在
        findBlueprint(blueprintCode);
        // 工艺参数持久化（与蓝图的关联通过 IExpand 或后续的关联表管理）
        return parameterRepo.save(parameter);
    }

    @Override
    public ProcessParameter updateProcessParameter(String blueprintCode, String processCode,
                                                    String parameterCode, IProcessParameter newValue) {
        findBlueprint(blueprintCode);
        ProcessParameter param = parameterRepo.findByCode(parameterCode)
                .orElseThrow(() -> new IllegalArgumentException("工艺参数不存在: " + parameterCode));
        // 更新参数值（保留原始编码和名称）
        param.setTargetValue(newValue.getTargetValue());
        param.setLowerLimit(newValue.getLowerLimit());
        param.setUpperLimit(newValue.getUpperLimit());
        param.markUpdated();
        return parameterRepo.save(param);
    }

    // ==================== 版本管理 ====================

    @Override
    public Blueprint createNewVersion(String blueprintCode, String newVersion, String changeReason) {
        Blueprint current = findBlueprint(blueprintCode);
        Blueprint next = current.createNewVersion(newVersion, changeReason);
        return blueprintRepo.save(next);
    }

    @Override
    public Blueprint bumpVersion(String blueprintCode, BumpType bumpType) {
        Blueprint bp = findBlueprint(blueprintCode);
        bp.bumpVersion(bumpType);
        return blueprintRepo.save(bp);
    }

    @Override
    public BlueprintDiff diff(String blueprintCode, String versionA, String versionB) {
        Blueprint bpA = blueprintRepo.findByProductCodeAndVersion(
                        findBlueprint(blueprintCode).getProductCode(), versionA)
                .orElseThrow(() -> new IllegalArgumentException("版本不存在: " + versionA));
        Blueprint bpB = blueprintRepo.findByProductCodeAndVersion(
                        findBlueprint(blueprintCode).getProductCode(), versionB)
                .orElseThrow(() -> new IllegalArgumentException("版本不存在: " + versionB));
        return blueprintDiffer.compare(bpA, bpB);
    }

    // ==================== BOM 管理 ====================

    @Override
    public BOMConversionResult convertBOM(BOMConversionRequest request) {
        // ⚠️ 存根：BOM 转化委托给 Common 模块的规则引擎
        // 参见: docs/architecture/modules/common-rule-engine-requirements.md
        throw new UnsupportedOperationException(
                "BOM 转化依赖 Common 模块的规则引擎（IRuleEngine）。"
                + "请先实现 Common 模块的 BOM 转化规则引擎。"
                + "需求规格见: docs/architecture/modules/common-rule-engine-requirements.md");
    }

    @Override
    public BOMConversionResult convertEBOMtoPBOM(String productCode) {
        throw new UnsupportedOperationException(
                "EBOM→PBOM 转化依赖 Common 模块规则引擎，尚未实现。");
    }

    @Override
    public BOMConversionResult convertPBOMtoMBOM(String productCode, String factoryCode) {
        throw new UnsupportedOperationException(
                "PBOM→MBOM 转化依赖 Common 模块规则引擎，尚未实现。");
    }

    // ==================== 工厂可制造性检查 ====================

    @Override
    @Transactional(readOnly = true)
    public ProductFactoryResult checkFeasibility(String blueprintCode, String factoryCode) {
        // ⚠️ 存根：可制造性检查需要工厂数据
        // core 已提供 ProductChecker / ProcessRouteMatcher，但需要 IFactory 实例
        // 工厂数据由 Equip/MES 模块提供
        throw new UnsupportedOperationException(
                "可制造性检查需要工厂 (IFactory) 数据。"
                + "Equip/MES 模块需提供工厂实现后，"
                + "通过 BlueprintService.checkFeasibility(blueprint, factory) 调用。");
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessRouteMatcher.RouteMatchResult matchEquipment(String blueprintCode, String factoryCode) {
        throw new UnsupportedOperationException(
                "设备匹配需要工厂 (IFactory) 数据，Equip/MES 模块实现后可用。");
    }

    // ==================== 变更管理 ====================

    @Override
    public ChangeRequest createChangeRequest(String code, String blueprintCode,
                                              String fromVersion, String changeReason,
                                              String requestedBy) {
        // 验证蓝图存在
        findBlueprint(blueprintCode);

        if (changeRequestRepo.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("变更请求编码已存在: " + code);
        }

        // 检查是否已有进行中的变更请求
        List<ChangeRequest> active = changeRequestRepo.findByBlueprintCodeAndStatusNotIn(
                blueprintCode, List.of(
                        ChangeRequest.ChangeRequestStatus.CLOSED,
                        ChangeRequest.ChangeRequestStatus.REJECTED));
        if (!active.isEmpty()) {
            throw new IllegalStateException(
                    "蓝图 " + blueprintCode + " 已有进行中的变更请求: " + active.get(0).getCode());
        }

        ChangeRequest cr = new ChangeRequest(code, blueprintCode, fromVersion, changeReason);
        cr.setRequestedBy(requestedBy);
        return changeRequestRepo.save(cr);
    }

    @Override
    public ChangeRequest submitChangeRequest(String changeRequestCode) {
        ChangeRequest cr = findChangeRequest(changeRequestCode);
        cr.submit();
        return changeRequestRepo.save(cr);
    }

    @Override
    public ChangeRequest approveChangeRequest(String changeRequestCode, String approvedBy) {
        ChangeRequest cr = findChangeRequest(changeRequestCode);
        cr.approve(approvedBy);
        return changeRequestRepo.save(cr);
    }

    @Override
    public Blueprint implementChange(String changeRequestCode, String newVersion) {
        ChangeRequest cr = findChangeRequest(changeRequestCode);
        cr.implement(newVersion);
        changeRequestRepo.save(cr);

        // 创建蓝图新版本
        Blueprint current = findBlueprint(cr.getBlueprintCode());
        Blueprint next = current.createNewVersion(newVersion, cr.getChangeReason());
        Blueprint saved = blueprintRepo.save(next);

        // 标记变更请求完成
        cr.close();
        changeRequestRepo.save(cr);

        return saved;
    }

    // ==================== 内部查询辅助 ====================

    @Transactional(readOnly = true)
    public Blueprint findBlueprint(String code) {
        return blueprintRepo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("蓝图不存在: " + code));
    }

    @Transactional(readOnly = true)
    public ChangeRequest findChangeRequest(String code) {
        return changeRequestRepo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("变更请求不存在: " + code));
    }

    @Transactional(readOnly = true)
    public List<Blueprint> getReleasedBlueprints(String productCode) {
        return blueprintRepo.findByProductCodeAndStatus(productCode, BlueprintStatus.RELEASED);
    }

    @Transactional(readOnly = true)
    public List<Blueprint> searchBlueprints(String keyword) {
        return blueprintRepo.search(keyword);
    }

    @Transactional(readOnly = true)
    public List<ChangeRequest> getPendingChangeRequests(String blueprintCode) {
        return changeRequestRepo.findByBlueprintCodeAndStatusNotIn(blueprintCode,
                List.of(ChangeRequest.ChangeRequestStatus.CLOSED,
                        ChangeRequest.ChangeRequestStatus.REJECTED));
    }

}
