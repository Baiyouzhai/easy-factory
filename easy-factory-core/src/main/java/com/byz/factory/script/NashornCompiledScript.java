package com.byz.factory.script;

import javax.script.CompiledScript;

/**
 * Nashorn 编译产物 — 封装 JSR-223 CompiledScript。
 *
 * @author 苏政
 */
public class NashornCompiledScript implements com.byz.factory.script.CompiledScript {

    private final String scriptId;
    private final long compiledAt;
    private final CompiledScript compiled;
    private boolean valid;

    public NashornCompiledScript(String scriptId, long compiledAt, CompiledScript compiled) {
        this.scriptId = scriptId;
        this.compiledAt = compiledAt;
        this.compiled = compiled;
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

    public CompiledScript getCompiled() {
        return compiled;
    }

}
