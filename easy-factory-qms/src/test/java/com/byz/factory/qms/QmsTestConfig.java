package com.byz.factory.qms;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaAuditing
@EntityScan(basePackages = "com.byz.factory.qms.model")
@EnableJpaRepositories(basePackages = "com.byz.factory.qms.repository")
public class QmsTestConfig {
}
