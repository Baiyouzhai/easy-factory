package com.byz.factory.web.common;

import java.util.List;

/**
 * 分页响应体。
 * <p>
 * 继承 {@link Result}，增加分页元数据字段。
 * 适用于需要分页查询的 REST 接口。
 *
 * @param <T> 数据列表元素类型
 * @author 苏政
 */
public class PageResult<T> extends Result<List<T>> {

    /** 当前页码（从 1 开始） */
    private int page;

    /** 每页条数 */
    private int size;

    /** 总记录数 */
    private long total;

    /** 总页数 */
    private int pages;

    protected PageResult(int code, String message, List<T> data,
                         int page, int size, long total, int pages) {
        super(code, message, data);
        this.page = page;
        this.size = size;
        this.total = total;
        this.pages = pages;
    }

    // ── 工厂方法 ──

    /**
     * 创建分页成功响应。
     *
     * @param data  当前页数据
     * @param total 总记录数
     * @param page  当前页码
     * @param size  每页条数
     * @param <T>   数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> data, long total, int page, int size) {
        int pages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResult<>(CODE_OK, "success", data, page, size, total, pages);
    }

    // ── getter / setter ──

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

}
