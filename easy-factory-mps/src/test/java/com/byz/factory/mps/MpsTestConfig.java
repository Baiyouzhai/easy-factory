package com.byz.factory.mps;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * MPS 模块测试配置 — 为 @DataJpaTest 提供 Spring Boot 上下文。
 *
 * @author 苏政
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaAuditing
@EntityScan(basePackages = "com.byz.factory.mps.model")
@EnableJpaRepositories(basePackages = "com.byz.factory.mps.repository")
public class MpsTestConfig {
}
