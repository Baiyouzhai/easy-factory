package com.byz.factory.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 可制造性检查结果实现。
 *
 * @author 苏政
 */
public class ProductFactoryResult implements IProductFactoryResult {

    private final String productName;
    private final String factoryCode;
    private final List<ProcessCheckResult> results;

    public ProductFactoryResult(String productName, String factoryCode,
                                List<ProcessCheckResult> results) {
        this.productName = productName;
        this.factoryCode = factoryCode;
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
    }

    public static ProductFactoryResult of(String productName, String factoryCode,
                                           List<ProcessCheckResult> results) {
        return new ProductFactoryResult(productName, factoryCode, results);
    }

    @Override
    public String getProductName() {
        return productName;
    }

    @Override
    public String getFactoryCode() {
        return factoryCode;
    }

    @Override
    public List<ProcessCheckResult> getResults() {
        return results;
    }

    @Override
    public boolean isPassed() {
        return results.stream().allMatch(ProcessCheckResult::passed);
    }

    @Override
    public String getSummary() {
        long passed = results.stream().filter(ProcessCheckResult::passed).count();
        return String.format("产品 [%s] 在工厂 [%s] 的可制造性: %d/%d 工序通过%s",
            productName, factoryCode, passed, results.size(),
            isPassed() ? " (全部通过)" : "");
    }

}
