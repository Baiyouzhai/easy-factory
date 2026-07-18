package com.byz.factory.repository;

import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 实体类型
 * @author 苏政
 */
public interface IPageResult<T> {

    /** 当前页数据 */
    List<T> getContent();

    /** 总记录数 */
    long getTotalElements();

    /** 总页数 */
    default int getTotalPages() {
        int pageSize = getPageSize();
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) getTotalElements() / pageSize);
    }

    /** 当前页码 */
    int getPageNumber();

    /** 每页大小 */
    int getPageSize();

    /** 是否有下一页 */
    default boolean hasNext() {
        return getPageNumber() + 1 < getTotalPages();
    }

    /** 是否有上一页 */
    default boolean hasPrevious() {
        return getPageNumber() > 0;
    }

}
