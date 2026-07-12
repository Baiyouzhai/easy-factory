package com.byz.factory.manufacture;

import com.byz.factory.core.resource.IResource;

import java.math.BigDecimal;

/**
 * 使用资源
 */
public interface IUsedResource extends IResource {

    /**
     * (实际)使用值
     *
     * @return (实际)使用值
     */
    BigDecimal getUsedValue();

}
