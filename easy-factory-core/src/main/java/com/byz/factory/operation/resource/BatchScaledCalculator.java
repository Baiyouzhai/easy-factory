package com.byz.factory.operation.resource;

import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.resource.ResourcePack;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 批量缩放计算器 — 将标准用量的资源需求缩放到目标批量。
 *
 * @author 苏政
 */
public class BatchScaledCalculator {

    /**
     * 对资源包进行批量缩放。
     *
     * @param standardPack      标准用量资源包
     * @param standardBatchSize 标准批量
     * @param targetBatchSize   目标批量
     * @return 缩放后的资源包（新对象，不修改输入）
     */
    public IResourcePack scale(IResourcePack standardPack, BigDecimal standardBatchSize,
                                BigDecimal targetBatchSize) {
        if (standardBatchSize.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("standardBatchSize must be > 0");
        }

        BigDecimal scaleFactor = targetBatchSize.divide(standardBatchSize, 6, RoundingMode.HALF_UP);
        return scale(standardPack, scaleFactor);
    }

    /**
     * 对资源包按缩放因子进行缩放。
     *
     * @param pack        资源包
     * @param scaleFactor 缩放因子
     * @return 缩放后的资源包
     */
    public IResourcePack scale(IResourcePack pack, BigDecimal scaleFactor) {
        ResourcePack result = new ResourcePack();
        IResourceItem[] resources = pack.getResources();
        if (resources == null) return result;

        for (IResourceItem item : resources) {
            IResourceItem scaled = item.copy();
            BigDecimal newNumber = item.getNumber().multiply(scaleFactor);
            scaled.setNumber(newNumber);
            result.merge(scaled);
        }
        return result;
    }

    /**
     * 对单个资源项进行批量缩放。
     */
    public IResourceItem scaleItem(IResourceItem item, BigDecimal standardBatchSize,
                                    BigDecimal targetBatchSize) {
        BigDecimal scaleFactor = targetBatchSize.divide(standardBatchSize, 6, RoundingMode.HALF_UP);
        IResourceItem scaled = item.copy();
        scaled.setNumber(item.getNumber().multiply(scaleFactor));
        return scaled;
    }

}
