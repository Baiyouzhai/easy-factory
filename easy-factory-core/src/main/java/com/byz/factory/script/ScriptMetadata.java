package com.byz.factory.script;

/**
 * 脚本元数据 — 从脚本头部注释块解析的结构化信息。
 * <p>
 * 脚本必须包含以下格式的 JSDoc 注释：
 * <pre>
 *   /**
 *    * &#64;id          qms.quality-gate.v1
 *    * &#64;name        质量门禁判定
 *    * &#64;version     1.0.0
 *    * &#64;module      qms
 *    * &#64;description ...
 *    * &#64;param       {ProcessInfo} process - 当前工序
 *    * &#64;return      {JudgementResult} 判定结果
 *    * &#64;since       2026-07-12
 *    *&#47;
 * </pre>
 *
 * @author 苏政
 */
public class ScriptMetadata {

    /** 脚本唯一标识，格式: {module}.{name}.v{major} */
    private final String id;

    /** 脚本显示名称 */
    private final String name;

    /** 语义化版本 */
    private final String version;

    /** 所属模块: mes | qms | plm | equip | lims | common */
    private final String module;

    /** 功能描述 */
    private final String description;

    /** 作者 */
    private final String author;

    /** 生效日期 (ISO 8601) */
    private final String since;

    /** 状态: DRAFT | ACTIVE | DEPRECATED | DISABLED */
    private String status;

    /** 预期最大执行时间(ms) */
    private long timeoutMs;

    public ScriptMetadata(String id, String name, String version, String module,
                          String description, String author, String since) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.module = module;
        this.description = description;
        this.author = author;
        this.since = since;
        this.status = "DRAFT";
        this.timeoutMs = 30_000; // 默认30秒超时
    }

    /**
     * 从脚本源码的注释头解析元数据
     */
    public static ScriptMetadata parse(String source) {
        // 解析 /** ... */ 中的 @tag value 格式
        String id = extractTag(source, "@id");
        String name = extractTag(source, "@name");
        String version = extractTag(source, "@version");
        String module = extractTag(source, "@module");
        String description = extractTag(source, "@description");
        String author = extractTag(source, "@author");
        String since = extractTag(source, "@since");

        if (id == null || name == null || version == null || module == null) {
            throw new IllegalArgumentException(
                "脚本缺少必要元数据: @id, @name, @version, @module 为必填项");
        }

        return new ScriptMetadata(id, name, version, module,
            description, author, since);
    }

    private static String extractTag(String source, String tag) {
        int idx = source.indexOf(tag);
        if (idx < 0) return null;
        int start = idx + tag.length();
        int end = source.indexOf('\n', start);
        if (end < 0) end = source.length();
        String value = source.substring(start, end).trim();
        // 去掉行尾的 *​/ 等
        value = value.replaceAll("\\s*\\*?\\/\\s*$", "").trim();
        return value.isEmpty() ? null : value;
    }

    // ---- getters ----

    public String getId() { return id; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getModule() { return module; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public String getSince() { return since; }
    public String getStatus() { return status; }
    public long getTimeoutMs() { return timeoutMs; }

    public void setStatus(String status) { this.status = status; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }

    @Override
    public String toString() {
        return "ScriptMetadata{" +
            "id='" + id + '\'' +
            ", version='" + version + '\'' +
            ", module='" + module + '\'' +
            ", status='" + status + '\'' +
            '}';
    }

}
