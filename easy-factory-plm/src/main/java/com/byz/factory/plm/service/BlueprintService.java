package com.byz.factory.plm.service;

import com.byz.factory.factory.BlueprintDiff;
import com.byz.factory.plm.model.Blueprint;
import com.byz.factory.plm.model.ProcessParameter;
import com.byz.factory.plm.model.ProcessTemplate;
import com.byz.factory.process.IProcessParameter;

/**
 * 蓝图服务 — PLM 模块核心。
 * <p>
 * 管理工艺路线的全生命周期：创建→评审→批准→发布→废弃，
 * 以及工艺参数管理、版本管理和 BOM 转化。
 * <p>
 * TODO 待实现
 *
 * @author 苏政
 */
public interface BlueprintService {

    // ==================== 蓝图生命周期 ====================

    /** 从工艺模板创建蓝图草稿 */
    Blueprint createBlueprint(ProcessTemplate template, String productCode);

    /** 提交评审（DRAFT → UNDER_REVIEW） */
    Blueprint submitForReview(String blueprintCode);

    /** 批准蓝图（UNDER_REVIEW → APPROVED） */
    Blueprint approve(String blueprintCode, String approvedBy);

    /** 发布到 MES（APPROVED → RELEASED） */
    Blueprint releaseVersion(String blueprintCode);

    /** 废弃蓝图（RELEASED → OBSOLETED） */
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
    Blueprint createNewVersion(String blueprintCode, String newVersion);

    /** 对比两个蓝图版本的差异 */
    BlueprintDiff diff(String blueprintCode, String versionA, String versionB);

    // ==================== BOM 管理 ====================

    /** EBOM（设计 BOM）→ PBOM（工艺 BOM）转化 */
    String convertEBOMtoPBOM(String productCode);

    /** PBOM（工艺 BOM）→ MBOM（制造 BOM）转化 */
    String convertPBOMtoMBOM(String productCode);

}
