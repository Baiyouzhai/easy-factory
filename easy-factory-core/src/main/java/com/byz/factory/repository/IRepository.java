package com.byz.factory.repository;

import java.util.List;
import java.util.Optional;

/**
 * 通用仓储端口 — 六边形架构中的持久化抽象。
 * <p>
 * Core 只定义端口（接口），业务模块/基础设施提供适配器（MyBatis/JPA/内存实现）。
 *
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @author 苏政
 */
public interface IRepository<T, ID> {

    /**
     * 按主键查找
     *
     * @param id 主键
     * @return 实体
     */
    Optional<T> findById(ID id);

    /**
     * 查找全部
     *
     * @return 实体列表
     */
    List<T> findAll();

    /**
     * 按分页条件查找
     *
     * @param pageRequest 分页参数
     * @return 分页结果
     */
    default IPageResult<T> findAll(IPageRequest pageRequest) {
        List<T> all = findAll();
        int from = (int) pageRequest.getOffset();
        int to = Math.min(from + pageRequest.getPageSize(), all.size());
        if (from >= all.size()) {
            return new SimplePageResult<>(List.of(), all.size(), pageRequest);
        }
        return new SimplePageResult<>(all.subList(from, to), all.size(), pageRequest);
    }

    /**
     * 保存（新增或更新）
     *
     * @param entity 实体
     * @return 持久化后的实体
     */
    T save(T entity);

    /**
     * 批量保存
     *
     * @param entities 实体集合
     * @return 持久化后的实体列表
     */
    List<T> saveAll(Iterable<T> entities);

    /**
     * 删除
     *
     * @param entity 实体
     */
    void delete(T entity);

    /**
     * 按主键删除
     *
     * @param id 主键
     */
    void deleteById(ID id);

    /**
     * 判断是否存在
     *
     * @param id 主键
     * @return true 如果存在
     */
    boolean existsById(ID id);

    /**
     * 总数
     *
     * @return 实体总数
     */
    long count();

}
