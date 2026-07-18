package com.byz.factory.lims.service;

import com.byz.factory.lims.model.Formula;
import com.byz.factory.resource.IResourcePack;

/**
 * 配方服务 — LIMS 模块核心。
 * <p>
 * TODO 待实现：配方版本管理、称量任务、批记录生成
 *
 * @author 苏政
 */
public interface FormulaService {

    /** 创建配方 */
    Formula create(String code, String name, String productCode, IResourcePack components);

    /** 发布新版本 */
    Formula releaseVersion(String formulaCode, String newVersion);

    /** 创建称量任务（关联工单） */
    String createWeighingTask(String formulaCode, String batchNo, String workOrderNo);

}
