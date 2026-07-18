package com.byz.factory.resource;

import com.byz.factory.exception.ResourceException;
import com.byz.factory.operation.common.IScalable;
import com.byz.factory.shared.UOM;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 资源项 — 带数量管理能力的资源。
 * <p>
 * 在 {@link IResource} 基础之上增加数量操作：增加(add)、消耗(use)、
 * 合并(put)、拷贝(copy)，以及数量/必要性判断。
 * <p>
 * 原名 IResourceModel，DDD 重构时改名为 IResourceItem——"Item" 体现"可计数的资源条目"。
 *
 * @author 苏政
 * @see IResource
 * @see ResourceItem
 */
public interface IResourceItem extends IResource, IScalable<IResourceItem> {

    /** 空资源数组常量 */
    IResourceItem[] EMPTY_ARRAY = new IResourceItem[0];

    /**
     * 获取数量
     *
     * @return 数量，不为null
     */
    BigDecimal getNumber();

    /**
     * 设置数量
     *
     * @param number 数量 >=0
     * @return this
     */
    IResourceItem setNumber(Number number);

    /**
     * 是否为空(数量为零或null)
     *
     * @return 是否为空
     */
    default boolean isEmpty() {
        BigDecimal number = getNumber();
        if (null == number) return true;
        return BigDecimal.ZERO.compareTo(number) >= 0;
    }

    /**
     * 补充数量（增加已有数量）
     *
     * @param number 补充数量
     * @return this
     */
    default IResourceItem add(Number number) {
        if (null == number) return this;
        BigDecimal target = number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(number.toString());
        BigDecimal source = getNumber();
        if (null == source) return setNumber(target);
        return setNumber(source.add(target));
    }

    /**
     * 消耗数量（减少已有数量）
     *
     * @param number 消耗数量
     * @return this
     * @throws ResourceException 资源不足时抛出
     */
    default IResourceItem use(Number number) {
        if (null == number) return this;
        BigDecimal target = number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(number.toString());
        BigDecimal source = getNumber();
        if (null == source) {
            throw new ResourceException("资源数量不足: " + getName() + " (当前为空) 需要 " + number);
        }
        BigDecimal result = source.subtract(target);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResourceException("资源数量不能为负数: " + getName() + " -> " + source + " use " + number);
        }
        return setNumber(result);
    }

    /**
     * 合并数量 — 将指定数量加入已有数量（与 add 相同语义，用于压缩场景）
     *
     * @param number 合并数量
     * @return this
     */
    default IResourceItem put(Number number) {
        return add(number);
    }

    /**
     * 深拷贝当前资源
     *
     * @return 资源副本
     */
    IResourceItem copy();

    /**
     * 是否为必要资源。
     * 默认返回 false（非必要），子类可覆盖。
     *
     * @return 是否必要
     */
    default boolean require() {
        return false;
    }

    /**
     * 获取计量单位。
     * <p>
     * 默认返回 {@link UOM#NONE}，子类可覆盖。
     *
     * @return 计量单位
     */
    default UOM getUom() {
        return UOM.NONE;
    }

    // ---- IScalable ----

    @Override
    default IResourceItem scaleBy(BigDecimal factor) {
        IResourceItem scaled = copy();
        scaled.setNumber(getNumber().multiply(factor));
        return scaled;
    }

}
