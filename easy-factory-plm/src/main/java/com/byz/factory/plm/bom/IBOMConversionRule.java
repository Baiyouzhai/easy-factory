package com.byz.factory.plm.bom;

/**
 * BOM 转化规则 — PLM 对 Common 模块规则引擎的契约接口。
 * <p>
 * <b>此接口由 Common 模块实现。</b> PLM 只定义契约，不实现。
 * Common 模块需提供规则引擎（{@code IRuleEngine}）来执行转化。
 * <p>
 * 一条规则代表一次 BOM 转化操作：
 * <ul>
 *   <li>物料添加（如 PBOM 增加润滑剂）</li>
 *   <li>虚拟件展开（如将"混合料"展开为实际组分）</li>
 *   <li>损耗率应用（如原料损耗 2%）</li>
 *   <li>工序分组（如 MBOM 按制粒/压片/包衣分组）</li>
 * </ul>
 * <p>
 * <b>使用方式（PLM → Common）：</b>
 * <pre>{@code
 * // PLM 构造请求
 * BOMConversionRequest req = BOMConversionRequest.ebomToPbom(...);
 * // Common 的规则引擎执行
 * BOMConversionResult result = ruleEngine.execute(req, rules);
 * }</pre>
 *
 * @author 苏政
 * @see BOMConversionRequest
 * @see BOMConversionResult
 */
public interface IBOMConversionRule {

    /** 规则编码（唯一标识） */
    String getRuleCode();

    /** 规则名称（人可读） */
    String getRuleName();

    /** 规则适用的源 BOM 类型 */
    BOMType getSourceType();

    /** 规则适用的目标 BOM 类型 */
    BOMType getTargetType();

    /** 规则的优先级（数字越小越先执行） */
    int getPriority();

    /**
     * 对 BOM 行项执行转化规则。
     * <p>
     * 由 Common 模块的规则引擎调用。引擎负责遍历 BOM 行项并批量调用此方法。
     *
     * @param context 转化上下文（当前 BOM + 蓝图 + 工厂信息）
     * @return 转化后的结果
     */
    BOMConversionResult apply(BOMConversionRequest context);

}
