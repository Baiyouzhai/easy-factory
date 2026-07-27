package com.byz.factory.web.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 配置 — 跨模块实体扫描。
 * <p>
 * 所有模块的 JPA 实体和 Repository 统一由此配置管理。
 * entityBasePackages: 各模块 model 包路径（含 @Entity 注解的类）
 * repositoryBasePackages: 各模块 repository 包路径（含 @Repository 注解的接口）
 * <p>
 * <b>新增模块时在此处追加扫描路径。</b>
 *
 * @author easy-factory
 */
@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = {
        "com.byz.factory.mes.model",
        "com.byz.factory.qms.model",
        "com.byz.factory.plm.model",
        "com.byz.factory.equip.model",
        "com.byz.factory.lims.model",
        "com.byz.factory.erp.model",
        "com.byz.factory.iot.model",
        "com.byz.factory.eam.model",
        "com.byz.factory.mps.model",
        "com.byz.factory.aps.model",
        "com.byz.factory.wms.model",
        "com.byz.factory.andon.model",
        "com.byz.factory.bi.model",
        "com.byz.factory.scm.model",
        "com.byz.factory.crm.model",
        "com.byz.factory.dms.model"
})
@EnableJpaRepositories(basePackages = {
        "com.byz.factory.mes.repository",
        "com.byz.factory.qms.repository",
        "com.byz.factory.plm.repository",
        "com.byz.factory.equip.repository",
        "com.byz.factory.lims.repository",
        "com.byz.factory.erp.repository",
        "com.byz.factory.iot.repository",
        "com.byz.factory.eam.repository",
        "com.byz.factory.mps.repository",
        "com.byz.factory.aps.repository",
        "com.byz.factory.wms.repository",
        "com.byz.factory.andon.repository",
        "com.byz.factory.bi.repository",
        "com.byz.factory.scm.repository",
        "com.byz.factory.crm.repository",
        "com.byz.factory.dms.repository"
})
public class JpaConfig {
}
