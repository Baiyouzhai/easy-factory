package com.byz.factory.operation.common;

/**
 * 可报告 — 所有能生成格式化报告的实体都实现此接口。
 * <p>
 * 支持纯文本（人类可读）和 JSON（机器可读）两种格式。
 *
 * @author 苏政
 */
public interface IReportable {

    /**
     * 生成纯文本报告（含层级缩进，人类可读）。
     *
     * @return 格式化的报告文本
     */
    String toReport();

    /**
     * 生成 JSON 格式报告（机器可读）。
     *
     * @return JSON 字符串
     */
    default String toJson() {
        return "{}";
    }

    /**
     * 直接输出报告到 System.out。
     */
    default void printReport() {
        System.out.println(toReport());
    }

    /**
     * 输出报告到指定流。
     *
     * @param out 输出目标
     */
    default void printReport(java.io.PrintStream out) {
        out.println(toReport());
    }

}
