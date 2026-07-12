package com.byz.factory.design;

import com.byz.factory.model.IResourceModel;

import java.math.BigDecimal;

/**
 * 估算资源
 */
public interface IEstimateResourceModel extends IResourceModel {

    /**
     * 预估值
     *
     * @return 预估值
     */
    BigDecimal getEstimateValue();

    /**
     * 偏差值
     *
     * @return 偏差值
     */
    default BigDecimal getDeviationValue() {
        return BigDecimal.ZERO;
    }

    /**
     * 最小(估算)值
     *
     * @return 最小值
     */
    default BigDecimal getEstimateMinValue() {
        return getEstimateValue().subtract(getDeviationValue());
    }

    /**
     * 最大(估算)值
     *
     * @return 最大值
     */
    default BigDecimal getEstimateMaxValue() {
        return getEstimateValue().add(getDeviationValue());
    }

}
