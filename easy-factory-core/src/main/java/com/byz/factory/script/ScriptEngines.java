package com.byz.factory.script;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 脚本引擎注册中心 — 管理多个 IScriptEngine 实现，提供默认引擎获取与切换。
 * <p>
 * 引擎优先级：GraalJS（沙箱，推荐） > Nashorn（过渡，无沙箱）。
 * 各模块通过 {@link #getDefault()} 获取当前激活引擎。
 *
 * @author 苏政
 */
public final class ScriptEngines {

    private static final Map<String, IScriptEngine> engines = new LinkedHashMap<>();
    private static volatile String defaultEngineName;

    static {
        ScriptRegistry registry = new ScriptRegistry();

        // 优先级 1：GraalJS — 沙箱安全，推荐生产使用
        try {
            register(new GraalScriptEngine(registry));
        } catch (Exception e) {
            System.getLogger(ScriptEngines.class.getName())
                .log(System.Logger.Level.WARNING, "GraalJS 引擎初始化失败: " + e.getMessage());
        }

        // 优先级 2：Nashorn — 过渡引擎，无沙箱
        try {
            register(new NashornScriptEngine(new ScriptRegistry()));
        } catch (Exception e) {
            System.getLogger(ScriptEngines.class.getName())
                .log(System.Logger.Level.WARNING, "Nashorn 引擎初始化失败: " + e.getMessage());
        }
    }

    private ScriptEngines() {
    }

    /**
     * 注册脚本引擎。
     *
     * @param engine 引擎实例
     */
    public static void register(IScriptEngine engine) {
        engines.put(engine.getName(), engine);
        if (defaultEngineName == null) {
            defaultEngineName = engine.getName();
        }
    }

    /**
     * 获取默认脚本引擎。
     *
     * @return 默认引擎
     * @throws IllegalStateException 无可用引擎时抛出
     */
    public static IScriptEngine getDefault() {
        if (defaultEngineName == null) {
            throw new IllegalStateException("没有可用的脚本引擎，请检查依赖配置");
        }
        return engines.get(defaultEngineName);
    }

    /**
     * 按名称获取引擎。
     *
     * @param name 引擎名称（GraalJS / Nashorn）
     * @return 引擎实例，未找到返回 null
     */
    public static IScriptEngine getEngine(String name) {
        return engines.get(name);
    }

    /**
     * 切换默认引擎。
     *
     * @param name 引擎名称
     * @throws IllegalArgumentException 指定名称的引擎未注册时抛出
     */
    public static void setDefault(String name) {
        if (!engines.containsKey(name)) {
            throw new IllegalArgumentException("未知脚本引擎: " + name + "，可用: " + engines.keySet());
        }
        defaultEngineName = name;
    }

    /**
     * 列出所有已注册引擎。
     *
     * @return 不可修改的引擎映射
     */
    public static Map<String, IScriptEngine> listEngines() {
        return Collections.unmodifiableMap(engines);
    }

    /**
     * 重置引擎注册表（仅供测试使用）。
     */
    static void reset() {
        engines.clear();
        defaultEngineName = null;
    }

}
