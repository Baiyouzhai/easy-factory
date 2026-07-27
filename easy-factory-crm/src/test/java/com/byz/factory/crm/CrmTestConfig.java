package com.byz.factory.crm;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * CRM 模块测试配置 — 提供 @DataJpaTest 所需的 @SpringBootConfiguration。
 *
 * @author easy-factory
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaAuditing
@EntityScan(basePackages = "com.byz.factory.crm.model")
@EnableJpaRepositories(basePackages = "com.byz.factory.crm.repository")
public class CrmTestConfig {
}
