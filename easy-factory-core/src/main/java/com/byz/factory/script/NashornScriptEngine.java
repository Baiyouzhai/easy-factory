package com.byz.factory.script;

import com.byz.factory.process.IProcess;
import com.byz.factory.resource.IResourceItem;

import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;

/**
 * Nashorn 脚本引擎实现 — 基于 JSR-223 (openjdk.nashorn)，无沙箱。
 * <p>
 * 注意：Nashorn 不允许限制 Java 类访问，因此沙箱能力有限。
 * 生产环境推荐使用 {@link GraalScriptEngine}。
 *
 * @author 苏政
 */
public class NashornScriptEngine implements IScriptEngine {

    private final ScriptRegistry registry;
    private final ScriptEngine engine;

    public NashornScriptEngine(ScriptRegistry registry) {
        this.registry = registry;
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("nashorn");
        if (this.engine == null) {
            throw new IllegalStateException("Nashorn 脚本引擎不可用，请检查 nashorn-core 依赖");
        }
    }

    @Override
    public com.byz.factory.script.CompiledScript compile(String scriptId, String source) {
        ScriptMetadata metadata = ScriptMetadata.parse(source);
        if (!scriptId.equals(metadata.getId())) {
            throw new IllegalArgumentException(
                "脚本ID不匹配: 期望 " + scriptId + ", 实际 " + metadata.getId());
        }

        javax.script.CompiledScript compiled;
        try {
            compiled = ((Compilable) engine).compile(source);
        } catch (Exception e) {
            throw new RuntimeException("Nashorn 编译失败: " + scriptId + " — " + e.getMessage(), e);
        }

        NashornCompiledScript result = new NashornCompiledScript(scriptId, System.currentTimeMillis(), compiled);
        registry.register(scriptId, metadata, result);
        metadata.setStatus("ACTIVE");
        return result;
    }

    @Override
    public Object execute(com.byz.factory.script.CompiledScript compiled, ScriptContext context) {
        if (!(compiled instanceof NashornCompiledScript nashorn)) {
            throw new IllegalArgumentException("不支持的编译产物类型: " + compiled.getClass());
        }

        try {
            // 执行编译产物，定义 execute() 函数到引擎绑定
            nashorn.getCompiled().eval();

            // 构造白名单 API 表面并调用 execute(context)
            Map<String, Object> apiSurface = createApiSurface(context);
            Object result = ((Invocable) engine).invokeFunction("execute", apiSurface);

            if (result == null) {
                return context.getProcess().getResourcePack();
            }
            return result;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                "脚本 [" + nashorn.getScriptId() + "] 未定义可调用的 execute(context) 函数", e);
        } catch (Exception e) {
            throw new RuntimeException(
                "Nashorn 执行失败: " + nashorn.getScriptId() + " — " + e.getMessage(), e);
        }
    }

    @Override
    public Object eval(String source, ScriptContext context) {
        ScriptMetadata metadata = ScriptMetadata.parse(source);
        String scriptId = metadata.getId();
        com.byz.factory.script.CompiledScript compiled = compile(scriptId, source);
        try {
            return execute(compiled, context);
        } finally {
            registry.deprecate(scriptId);
        }
    }

    @Override
    public String getName() {
        return "Nashorn";
    }

    @Override
    public boolean isSandboxed() {
        return false;
    }

    @Override
    public ScriptContext createContext(IProcess process, IResourceItem... inputResources) {
        return new DefaultScriptContext(process, inputResources);
    }

    /**
     * 创建白名单 API 表面 — 与 GraalScriptEngine 保持一致。
     */
    static Map<String, Object> createApiSurface(ScriptContext scriptCtx) {
        Map<String, Object> api = new LinkedHashMap<>();

        IProcess p = scriptCtx.getProcess();
        api.put("processCode", p.getCode());
        api.put("processName", p.getName());

        List<Map<String, Object>> inputs = new ArrayList<>();
        for (IResourceItem r : scriptCtx.getInputResources()) {
            Map<String, Object> ri = new LinkedHashMap<>();
            ri.put("name", r.getName());
            ri.put("group", r.getGroup().name());
            ri.put("type", r.getType().name());
            ri.put("number", r.getNumber());
            inputs.add(ri);
        }
        api.put("inputResources", Collections.unmodifiableList(inputs));

        if (scriptCtx.getServices() != null) {
            api.put("services", scriptCtx.getServices());
        }

        api.put("log", (ScriptLogger) (level, msg) -> scriptCtx.log(level, msg));

        return Collections.unmodifiableMap(api);
    }

    // ---- 函数式接口 ----

    @FunctionalInterface
    public interface ScriptLogger {
        void log(String level, String message);
    }

}
