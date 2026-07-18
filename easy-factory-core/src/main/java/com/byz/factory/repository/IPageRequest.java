package com.byz.factory.repository;

import java.util.Optional;

/**
 * 分页查询参数
 *
 * @author 苏政
 */
public interface IPageRequest {

    /** 页码（从0开始） */
    int getPageNumber();

    /** 每页大小 */
    int getPageSize();

    /** 偏移量 = pageNumber * pageSize */
    default long getOffset() {
        return (long) getPageNumber() * getPageSize();
    }

    /** 排序（可选） */
    Optional<Sort> getSort();

    /**
     * 工厂方法：创建简单分页请求
     */
    static IPageRequest of(int page, int size) {
        return new SimplePageRequest(page, size, null);
    }

    /**
     * 工厂方法：创建带排序的分页请求
     */
    static IPageRequest of(int page, int size, Sort sort) {
        return new SimplePageRequest(page, size, sort);
    }

}
