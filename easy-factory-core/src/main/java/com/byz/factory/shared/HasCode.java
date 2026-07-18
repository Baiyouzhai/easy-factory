package com.byz.factory.shared;

/**
 * 编码标识 — 为实体提供统一的编码访问契约。
 * <p>
 * 实现此接口的类可以通过 {@code getCode()} 获取唯一业务编码，
 * 使泛型 Repository 和工具类无需知道具体类型。
 *
 * @author 苏政
 */
public interface HasCode {

    /**
     * 业务编码（唯一标识）
     *
     * @return 编码
     */
    String getCode();

}
