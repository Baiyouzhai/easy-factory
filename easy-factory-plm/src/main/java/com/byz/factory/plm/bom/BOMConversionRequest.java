package com.byz.factory.plm.bom;

import com.byz.factory.factory.IBillOfMaterial;
import com.byz.factory.factory.IBlueprint;

/**
 * BOM 转化请求 — 将一种 BOM 类型转化为另一种。
 * <p>
 * 转化规则由 Common 模块的规则引擎（{@code IRuleEngine}）执行，
 * PLM 只负责构造请求和消费结果。
 *
 * @param requestId    请求唯一标识
 * @param sourceType   源 BOM 类型
 * @param targetType   目标 BOM 类型
 * @param productCode  产品编码
 * @param sourceBOM    源 BOM（可为 null，表示从零开始构建）
 * @param blueprint    关联的蓝图（工序路线信息用于 MBOM 分组）
 * @param factoryCode  目标工厂编码（MBOM 转化时需要）
 * @author 苏政
 */
public record BOMConversionRequest(
        String requestId,
        BOMType sourceType,
        BOMType targetType,
        String productCode,
        IBillOfMaterial sourceBOM,
        IBlueprint blueprint,
        String factoryCode) {

    public BOMConversionRequest {
        if (sourceType == null || targetType == null) {
            throw new IllegalArgumentException("sourceType 和 targetType 不能为 null");
        }
        if (sourceType == targetType) {
            throw new IllegalArgumentException("源类型和目标类型不能相同: " + sourceType);
        }
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("productCode 不能为空");
        }
    }

    /** 创建 EBOM → PBOM 转化请求 */
    public static BOMConversionRequest ebomToPbom(String requestId, String productCode,
                                                   IBillOfMaterial ebom, IBlueprint blueprint) {
        return new BOMConversionRequest(requestId, BOMType.EBOM, BOMType.PBOM,
                productCode, ebom, blueprint, null);
    }

    /** 创建 PBOM → MBOM 转化请求 */
    public static BOMConversionRequest pbomToMbom(String requestId, String productCode,
                                                   IBillOfMaterial pbom, IBlueprint blueprint,
                                                   String factoryCode) {
        return new BOMConversionRequest(requestId, BOMType.PBOM, BOMType.MBOM,
                productCode, pbom, blueprint, factoryCode);
    }
}
