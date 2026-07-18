package com.byz.factory.resource;

import java.math.BigDecimal;

/**
 * 使用资源 — 记录实际使用量
 *
 * @author 苏政
 */
public interface IUsedResource extends IResourceItem {

    /**
     * (实际)使用值
     *
     * @return (实际)使用值
     */
    BigDecimal getUsedValue();

}
