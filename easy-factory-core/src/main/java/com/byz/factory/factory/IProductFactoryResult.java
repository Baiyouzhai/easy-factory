package com.byz.factory.factory;

import java.util.List;

/**
 * 可制造性检查结果 — 逐工序的通过/失败判定。
 *
 * @author 苏政
 */
public interface IProductFactoryResult {

    /** 产品名称 */
    String getProductName();

    /** 工厂编码 */
    String getFactoryCode();

    /** 逐工序检查结果 */
    List<ProcessCheckResult> getResults();

    /** 所有工序是否全部通过 */
    boolean isPassed();

    /** 汇总信息 */
    String getSummary();

}
