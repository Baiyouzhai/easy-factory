package com.byz.factory.core.resource;

import com.byz.factory.data.Dict;
import com.byz.factory.data.IResource;
import com.byz.factory.exception.ResourceException;

import java.math.BigDecimal;

/**
 * 资源
 *
 * @author 苏政
 */
public interface IResourceModel extends IResource, Cloneable {

    /**
     * 设置数量
     *
     * @param number 数量>=0
     * @return this
     */
    default IResourceModel setNumber(Number number) {
        return this;
    }

    /**
     * 是否为空(没有数量)
     *
     * @return 是否为空
     */
    default boolean isEmpty() {
        Number number = getNumber();
        if (null == number) return true;
        BigDecimal num = number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(number.toString());
        return BigDecimal.ZERO.compareTo(num) < 1;
    }

    /**
     * 补充
     *
     * @param number 数量
     * @return this
     */
    default IResourceModel add(Number number) {
        if (null == number) return this;
        BigDecimal target = number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(number.toString());
        Number num1 = getNumber();
        if (null == num1) return setNumber(target);
        BigDecimal source = num1 instanceof BigDecimal ? (BigDecimal) num1 : new BigDecimal(num1.toString());
        BigDecimal num2 = source.subtract(target);
        if (num2.compareTo(BigDecimal.ZERO) < 0)
            throw new ResourceException("资源数量不能为负数: " + getName() + " -> " + num1 + " put " + number);
        return setNumber(num2);
    }

    /**
     * 使用
     *
     * @param number 数量
     * @return this
     */
    default IResourceModel use(Number number) {
        if (null == number) return this;
        BigDecimal target = number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(number.toString());
        Number num1 = getNumber();
        if (null == num1) return setNumber(target);
        BigDecimal source = num1 instanceof BigDecimal ? (BigDecimal) num1 : new BigDecimal(num1.toString());
        BigDecimal num2 = source.subtract(target);
        if (num2.compareTo(BigDecimal.ZERO) < 0)
            throw new ResourceException("资源数量不能为负数: " + getName() + " -> " + num1 + " use " + number);
        return setNumber(num2);
    }

}
