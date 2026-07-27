package com.byz.factory.erp;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ERP 模块测试配置 — 提供 @DataJpaTest 所需的 @SpringBootConfiguration。
 *
 * @author easy-factory
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaAuditing
@EntityScan(basePackages = "com.byz.factory.erp.model")
@EnableJpaRepositories(basePackages = "com.byz.factory.erp.repository")
public class ErpTestConfig {
}
