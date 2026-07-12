package com.byz.factory.manufacture;

import com.byz.factory.model.IResourceModel;

import java.math.BigDecimal;

/**
 * 使用资源
 */
public interface IUsedResourceModel extends IResourceModel {

    /**
     * (实际)使用值
     *
     * @return (实际)使用值
     */
    BigDecimal getUsedValue();

}
