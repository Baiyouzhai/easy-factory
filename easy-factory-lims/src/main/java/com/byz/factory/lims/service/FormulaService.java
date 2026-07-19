package com.byz.factory.lims.service;

import com.byz.factory.lims.model.Formula;
import com.byz.factory.resource.IResourcePack;

import java.util.List;

/**
 * 配方服务 — LIMS 模块核心。
 * <p>
 * TODO 待实现：配方版本管理、称量任务、批记录生成。
 * 实现类：{@code FormulaServiceImpl}
 *
 * @author 苏政
 */
public interface FormulaService {

    /** 创建配方 */
    Formula create(String code, String name, String productCode, IResourcePack components);

    /** 按编码查询配方 */
    Formula getByCode(String code);

    /** 列出所有配方 */
    List<Formula> list();

    /** 提交审批 */
    void submitForApproval(String formulaCode);

    /** 批准配方 */
    void approve(String formulaCode, String approvedBy);

    /** 激活配方（可用于生产） */
    void activate(String formulaCode);

    /** 退役配方 */
    void retire(String formulaCode, String reason);

    /** 发布新版本（退役当前版本并创建新版本） */
    Formula releaseVersion(String formulaCode, String newVersion);

    /** 创建称量任务（关联工单） */
    String createWeighingTask(String formulaCode, String batchNo, String workOrderNo);

    /** 按产品和版本查询配方 */
    Formula getFormulaByProductAndVersion(String productCode, String version);

}
