package com.byz.factory.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductFactoryResult 可制造性检查结果测试")
class ProductFactoryResultTest {

    @Test
    @DisplayName("全部通过 — isPassed 返回 true")
    void allPassed_returnsTrue() {
        ProductFactoryResult result = ProductFactoryResult.of("产品A", "工厂X", List.of(
            ProcessCheckResult.pass("P001"),
            ProcessCheckResult.pass("P002")
        ));

        assertTrue(result.isPassed());
        assertEquals("产品A", result.getProductName());
        assertEquals("工厂X", result.getFactoryCode());
    }

    @Test
    @DisplayName("部分失败 — isPassed 返回 false")
    void partialFail_returnsFalse() {
        ProductFactoryResult result = ProductFactoryResult.of("产品A", "工厂X", List.of(
            ProcessCheckResult.pass("P001"),
            ProcessCheckResult.fail("P002", "工序缺失")
        ));

        assertFalse(result.isPassed());
    }

    @Test
    @DisplayName("全部失败 — isPassed 返回 false")
    void allFail_returnsFalse() {
        ProductFactoryResult result = ProductFactoryResult.of("产品B", "工厂Y", List.of(
            ProcessCheckResult.fail("P001", "缺少工序"),
            ProcessCheckResult.fail("P002", "动作不匹配")
        ));

        assertFalse(result.isPassed());
    }

    @Test
    @DisplayName("getSummary — 全部通过摘要")
    void summary_allPassed() {
        ProductFactoryResult result = ProductFactoryResult.of("产品A", "工厂X", List.of(
            ProcessCheckResult.pass("P001"),
            ProcessCheckResult.pass("P002")
        ));

        String summary = result.getSummary();
        assertTrue(summary.contains("产品A"));
        assertTrue(summary.contains("工厂X"));
        assertTrue(summary.contains("2/2"));
        assertTrue(summary.contains("全部通过"));
    }

    @Test
    @DisplayName("getSummary — 部分通过摘要")
    void summary_partialPass() {
        ProductFactoryResult result = ProductFactoryResult.of("产品B", "工厂Y", List.of(
            ProcessCheckResult.pass("P001"),
            ProcessCheckResult.fail("P002", "缺失")
        ));

        String summary = result.getSummary();
        assertTrue(summary.contains("1/2"));
    }

    @Test
    @DisplayName("ProcessCheckResult.pass — 工厂方法")
    void processCheckResult_pass() {
        ProcessCheckResult r = ProcessCheckResult.pass("P001");
        assertEquals("P001", r.processCode());
        assertTrue(r.passed());
        assertEquals("OK", r.reason());
    }

    @Test
    @DisplayName("ProcessCheckResult.fail — 工厂方法")
    void processCheckResult_fail() {
        ProcessCheckResult r = ProcessCheckResult.fail("P002", "设备缺失");
        assertEquals("P002", r.processCode());
        assertFalse(r.passed());
        assertEquals("设备缺失", r.reason());
    }

    @Test
    @DisplayName("空结果列表 — isPassed 返回 true")
    void emptyResults_isPassed() {
        ProductFactoryResult result = ProductFactoryResult.of("产品C", "工厂Z", List.of());
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("结果列表不可修改")
    void results_unmodifiable() {
        ProductFactoryResult result = ProductFactoryResult.of("A", "X",
            List.of(ProcessCheckResult.pass("P001")));
        assertThrows(UnsupportedOperationException.class, () -> result.getResults().clear());
    }

}
