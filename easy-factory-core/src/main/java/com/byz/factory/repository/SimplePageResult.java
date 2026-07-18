package com.byz.factory.repository;

import java.util.Collections;
import java.util.List;

/**
 * 简单分页结果实现
 *
 * @param <T> 实体类型
 * @author 苏政
 */
class SimplePageResult<T> implements IPageResult<T> {

    private final List<T> content;
    private final long totalElements;
    private final int pageNumber;
    private final int pageSize;

    SimplePageResult(List<T> content, long totalElements, IPageRequest pageRequest) {
        this.content = Collections.unmodifiableList(content);
        this.totalElements = totalElements;
        this.pageNumber = pageRequest.getPageNumber();
        this.pageSize = pageRequest.getPageSize();
    }

    @Override
    public List<T> getContent() { return content; }

    @Override
    public long getTotalElements() { return totalElements; }

    @Override
    public int getPageNumber() { return pageNumber; }

    @Override
    public int getPageSize() { return pageSize; }

}
