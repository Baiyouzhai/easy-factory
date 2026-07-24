package com.byz.factory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * easy-factory 统一门户启动类。
 * <p>
 * 聚合所有业务模块，提供统一的 REST API 入口。
 * 基于 Spring Boot 3.x 构建，采用模块化单体架构。
 *
 * @author 苏政
 */
@SpringBootApplication
public class EasyFactoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyFactoryApplication.class, args);
    }

}
