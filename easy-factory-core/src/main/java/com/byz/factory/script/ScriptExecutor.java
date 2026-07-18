package com.byz.factory.script;

import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * 基于 Nashorn 的脚本执行器。
 *
 * @deprecated Nashorn 已在 JDK 15+ 移除，后续版本将迁移至 {@link IScriptEngine} (J2V8)。
 *             请使用 {@link ScriptEngines#getDefault()} 获取新的脚本引擎。
 *
 * @author 苏政
 */
@Deprecated
public class ScriptExecutor {

    @Deprecated
    public static final ScriptExecutor Instance = new ScriptExecutor();

    @Deprecated
    public static Object Declare(String script) {
        return Instance.declare(script);
    }

    @Deprecated
    public static Object Execute(String script, Object... args) {
        return Instance.execute(script, args);
    }

    protected ScriptEngine engine;

    public ScriptExecutor() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public Object declare(String script) {
        try {
            return ((Compilable) getEngine()).compile(script).eval();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object execute(String name, Object... args) {
        try {
            return ((Invocable) getEngine()).invokeFunction(name, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
