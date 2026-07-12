package com.byz.factory.script;

/**
 * GraalJS 编译产物 — 封装编译后的 JavaScript 源码及其沙箱上下文。
 *
 * @author 苏政
 */
public class GraalCompiledScript implements CompiledScript {

    private final String scriptId;
    private final long compiledAt;
    private final String source;  // TODO: 替换为 GraalVM Value 对象
    private boolean valid;

    public GraalCompiledScript(String scriptId, long compiledAt, String source) {
        this.scriptId = scriptId;
        this.compiledAt = compiledAt;
        this.source = source;
        this.valid = true;
    }

    @Override
    public String getScriptId() {
        return scriptId;
    }

    @Override
    public long getCompiledAt() {
        return compiledAt;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        this.valid = false;
    }

    public String getSource() {
        return source;
    }

}
