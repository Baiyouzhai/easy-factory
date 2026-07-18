package com.byz.factory.operation.common;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 报告打印工具 — 提供缩进、表格、树形、分隔线等格式化输出方法。
 *
 * @author 苏政
 */
public final class ReportPrinter {

    private ReportPrinter() {}

    // ---- 常量 ----

    private static final String THIN_SEP  = "────────────────────────────────────────";
    private static final String THICK_SEP = "════════════════════════════════════════";
    private static final String DASH_SEP  = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";

    // ---- 标题/分隔 ----

    public static String header(String title) {
        return "\n" + DASH_SEP + "\n  " + title + "\n" + DASH_SEP;
    }

    public static String thickHeader(String title) {
        return "\n" + THICK_SEP + "\n  " + title + "\n" + THICK_SEP;
    }

    public static String separator() {
        return THIN_SEP;
    }

    // ---- 缩进 ----

    public static String indent(int level, String text) {
        String prefix = "  ".repeat(Math.max(0, level));
        return prefix + text;
    }

    // ---- 键值对 ----

    public static String kv(String key, Object value) {
        return String.format("  %-12s: %s", key, value);
    }

    // ---- 表格 ----

    /**
     * 生成对齐的表格。
     *
     * @param headers 列标题
     * @param rows    数据行（每行是 String 列表）
     * @return 格式化表格
     */
    public static String table(List<String> headers, List<List<String>> rows) {
        int colCount = headers.size();
        int[] colWidths = new int[colCount];

        // 计算列宽
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = headers.get(i).length();
        }
        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(colCount, row.size()); i++) {
                // 去掉 emoji 等宽字符的干扰
                int len = displayWidth(row.get(i));
                if (len > colWidths[i]) colWidths[i] = len;
            }
        }

        StringBuilder sb = new StringBuilder();
        String sep = "┌" + IntStream.range(0, colCount)
            .mapToObj(i -> "─".repeat(colWidths[i] + 2))
            .collect(Collectors.joining("┬")) + "┐\n";

        sb.append(sep);
        sb.append(formatRow(headers, colWidths, "│"));
        sb.append("├").append(IntStream.range(0, colCount)
            .mapToObj(i -> "─".repeat(colWidths[i] + 2))
            .collect(Collectors.joining("┼"))).append("┤\n");

        for (List<String> row : rows) {
            sb.append(formatRow(row, colWidths, "│"));
        }

        sb.append("└").append(IntStream.range(0, colCount)
            .mapToObj(i -> "─".repeat(colWidths[i] + 2))
            .collect(Collectors.joining("┴"))).append("┘\n");

        return sb.toString();
    }

    private static String formatRow(List<String> cells, int[] widths, String border) {
        StringBuilder sb = new StringBuilder(border);
        for (int i = 0; i < widths.length; i++) {
            String cell = i < cells.size() ? cells.get(i) : "";
            int pad = widths[i] - displayWidth(cell);
            sb.append(" ").append(cell).append(" ".repeat(Math.max(0, pad + 1))).append(border);
        }
        sb.append("\n");
        return sb.toString();
    }

    // ---- 树形 ----

    /**
     * 生成树形结构。
     *
     * @param root      根节点标签
     * @param children  子节点列表
     * @param labelFn   子节点→标签的函数
     */
    public static <T> String tree(String root, List<T> children, Function<T, String> labelFn) {
        StringBuilder sb = new StringBuilder();
        sb.append(root).append("\n");
        for (int i = 0; i < children.size(); i++) {
            boolean last = i == children.size() - 1;
            String prefix = last ? "  └─ " : "  ├─ ";
            sb.append(prefix).append(labelFn.apply(children.get(i))).append("\n");
        }
        return sb.toString();
    }

    // ---- 颜色（ANSI，可选） ----

    public static String green(String text)  { return "\033[32m" + text + "\033[0m"; }
    public static String red(String text)    { return "\033[31m" + text + "\033[0m"; }
    public static String yellow(String text) { return "\033[33m" + text + "\033[0m"; }
    public static String bold(String text)   { return "\033[1m" + text + "\033[0m"; }

    public static String check(boolean ok) {
        return ok ? green("✅ 通过") : red("❌ 失败");
    }

    // ---- 时间格式化 ----

    /**
     * 格式化时间段为人类可读字符串。
     * <p>例如: 2h 30m, 45m 20s, 500ms</p>
     */
    public static String formatDuration(Duration d) {
        if (d == null || d.isZero()) return "0s";
        long h = d.toHours();
        long m = d.toMinutesPart();
        long s = d.toSecondsPart();
        long ms = d.toMillisPart();
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        if (s > 0) return s + "s";
        return ms + "ms";
    }

    // ---- 辅助 ----

    /** 计算显示宽度（中文字符占2个字符宽度，emoji 不参与宽度计算） */
    private static int displayWidth(String s) {
        if (s == null) return 0;
        int width = 0;
        for (char c : s.toCharArray()) {
            if (c == '') break;  // ANSI 转义序列不计宽度
            if (Character.isIdeographic(c) || c > 0x2000) {
                width += 2;
            } else {
                width += 1;
            }
        }
        return width;
    }

}
