package com.byz.factory.iot;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * IoT 模块测试配置 — 提供 @DataJpaTest 所需的 @SpringBootConfiguration。
 *
 * @author easy-factory
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaAuditing
@EntityScan(basePackages = { "com.byz.factory.iot.model", "com.byz.factory.shared" })
@EnableJpaRepositories(basePackages = "com.byz.factory.iot.repository")
public class IotTestConfig {
}
