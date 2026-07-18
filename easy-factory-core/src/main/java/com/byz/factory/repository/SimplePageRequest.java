package com.byz.factory.repository;

import java.util.Optional;

/**
 * 简单分页请求实现
 *
 * @author 苏政
 */
class SimplePageRequest implements IPageRequest {

    private final int pageNumber;
    private final int pageSize;
    private final Sort sort;

    SimplePageRequest(int pageNumber, int pageSize, Sort sort) {
        if (pageNumber < 0) throw new IllegalArgumentException("pageNumber must be >= 0");
        if (pageSize < 1) throw new IllegalArgumentException("pageSize must be >= 1");
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    @Override
    public int getPageNumber() { return pageNumber; }

    @Override
    public int getPageSize() { return pageSize; }

    @Override
    public Optional<Sort> getSort() { return Optional.ofNullable(sort); }

}
