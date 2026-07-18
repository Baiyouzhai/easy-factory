package com.byz.factory.script;

/**
 * 脚本引擎注册中心 — 提供默认引擎的获取与设置。
 * <p>
 * 当前默认引擎为 J2V8ScriptEngine（将在 Phase 2 实现）。在 J2V8 就绪前，
 * 可以通过 {@link #setDefault(IScriptEngine)} 注入任意实现。
 *
 * @author 苏政
 */
public final class ScriptEngines {

    private static volatile IScriptEngine defaultEngine;

    static {
        // 待 J2V8 依赖就绪后启用:
        // defaultEngine = new J2V8ScriptEngine(new ScriptRegistry());
        defaultEngine = null;
    }

    private ScriptEngines() {
    }

    /**
     * 获取默认脚本引擎
     *
     * @return 默认引擎，可能为 null（未配置）
     */
    public static IScriptEngine getDefault() {
        return defaultEngine;
    }

    /**
     * 设置默认脚本引擎
     *
     * @param engine 引擎实例
     */
    public static void setDefault(IScriptEngine engine) {
        defaultEngine = engine;
    }

}
