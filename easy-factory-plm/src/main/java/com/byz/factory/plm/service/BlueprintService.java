package com.byz.factory.plm.service;

import com.byz.factory.factory.IBlueprint;
import com.byz.factory.plm.model.ProcessTemplate;

/**
 * 蓝图服务 — PLM 模块核心。
 * <p>
 * TODO 待实现：工艺路线版本管理、BOM转化、工艺参数管理、发布到MES
 *
 * @author 苏政
 */
public interface BlueprintService {

    /** 从模板创建蓝图 */
    IBlueprint createFromTemplate(ProcessTemplate template, String productCode);

    /** 发布新版本 */
    IBlueprint releaseVersion(String blueprintCode, String newVersion);

    /** 发布到 MES */
    void publishToMes(IBlueprint blueprint);

}
