package com.byz.factory.operation.common;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 操作结果基类 — 所有运营分析结果类型的公共父类。
 * <p>
 * 提供：操作名、成功标志、消息/警告列表、执行耗时、格式化报告输出。
 * 子类应通过 record 或 class 扩展此基类，添加领域特定的结果字段。
 *
 * @author 苏政
 */
public abstract class OperationResult implements IReportable {

    /** 执行的操作名称 */
    protected String operationName;

    /** 是否成功 */
    protected boolean success = true;

    /** 消息列表 */
    protected final List<String> messages = new ArrayList<>();

    /** 警告列表 */
    protected final List<String> warnings = new ArrayList<>();

    /** 执行耗时（毫秒） */
    protected long elapsedMs;

    protected OperationResult() {
    }

    protected OperationResult(String operationName) {
        this.operationName = operationName;
    }

    // ---- 消息管理 ----

    public OperationResult addMessage(String message) {
        messages.add(message);
        return this;
    }

    public OperationResult addWarning(String warning) {
        warnings.add(warning);
        return this;
    }

    public OperationResult markFailed(String reason) {
        this.success = false;
        addMessage(reason);
        return this;
    }

    // ---- Getters/Setters ----

    public String getOperationName() { return operationName; }
    public void setOperationName(String name) { this.operationName = name; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public List<String> getMessages() { return Collections.unmodifiableList(messages); }
    public List<String> getWarnings() { return Collections.unmodifiableList(warnings); }

    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }

    // ---- 报告 ----

    /** 状态图标 */
    protected String statusIcon() {
        return success ? "✅" : "❌";
    }

    @Override
    public String toReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(statusIcon()).append(" ").append(operationName != null ? operationName : "操作");
        if (elapsedMs > 0) {
            sb.append(" (").append(elapsedMs).append("ms)");
        }
        sb.append("\n");
        for (String msg : messages) {
            sb.append("  ").append(msg).append("\n");
        }
        for (String w : warnings) {
            sb.append("  ⚠️ ").append(w).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void printReport(PrintStream out) {
        out.print(toReport());
    }

}
