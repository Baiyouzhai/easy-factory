package com.byz.factory.shared;

/**
 * 版本标识 — 为实体提供统一的版本号访问契约。
 * <p>
 * 适用于需要版本管理的实体（工艺模板、配方、文档、计划等）。
 *
 * @author 苏政
 */
public interface HasVersion {

    /**
     * 版本号（语义化版本，如 "1.0.0"）
     *
     * @return 版本号
     */
    String getVersion();

}
