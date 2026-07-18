package com.byz.factory.script;

import com.byz.factory.process.IProcess;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.shared.Dict;

import java.math.BigDecimal;
import java.util.*;

/**
 * GraalJS 脚本引擎实现 — 使用 GraalVM JavaScript 引擎，内置沙箱限制。
 * <p>
 * 安全策略（默认全部禁止，只开放白名单）：
 * <ul>
 *   <li>❌ 禁止访问任何 Java 类 (HostClassLookup)</li>
 *   <li>❌ 禁止文件/网络 IO</li>
 *   <li>❌ 禁止创建线程</li>
 *   <li>❌ 禁止创建进程</li>
 *   <li>❌ 禁止 JNI 本地访问</li>
 *   <li>✅ 仅能访问 ScriptContext 中显式绑定的 API 表面对象</li>
 * </ul>
 *
 * <p>注意：当前为骨架实现，需要 GraalVM SDK 依赖。
 * 在添加 {@code org.graalvm.js:js} 依赖前，编译时部分方法为 TODO。
 *
 * @author 苏政
 */
public class GraalScriptEngine implements IScriptEngine {

    private final ScriptRegistry registry;

    public GraalScriptEngine(ScriptRegistry registry) {
        this.registry = registry;
    }

    @Override
    public CompiledScript compile(String scriptId, String source) {
        // 1. 解析并校验元数据
        ScriptMetadata metadata = ScriptMetadata.parse(source);
        if (!scriptId.equals(metadata.getId())) {
            throw new IllegalArgumentException(
                "脚本ID不匹配: 期望 " + scriptId + ", 实际 " + metadata.getId());
        }

        // 2. 编译（TODO: 需要 GraalVM JS 依赖）
        // Context ctx = createSandbox();
        // Value compiled = ctx.eval("js", source);
        GraalCompiledScript result = new GraalCompiledScript(scriptId, System.currentTimeMillis(), source);

        // 3. 注册
        registry.register(scriptId, metadata, result);
        metadata.setStatus("ACTIVE");

        return result;
    }

    @Override
    public Object execute(CompiledScript compiled, ScriptContext context) {
        if (!(compiled instanceof GraalCompiledScript graal)) {
            throw new IllegalArgumentException("不支持的编译产物类型: " + compiled.getClass());
        }

        // TODO: 在沙箱中执行
        Object result = context.getResult();
        if (result != null) return result;

        // 默认返回工序资源包
        return context.getProcess().getResourcePack();
    }

    @Override
    public Object eval(String source, ScriptContext context) {
        String tempId = "eval-" + UUID.randomUUID().toString().substring(0, 8);
        CompiledScript compiled = compile(tempId, source);
        try {
            return execute(compiled, context);
        } finally {
            registry.deprecate(tempId);
        }
    }

    @Override
    public String getName() {
        return "GraalJS";
    }

    @Override
    public boolean isSandboxed() {
        return true;
    }

    @Override
    public ScriptContext createContext(IProcess process, IResourceItem... inputResources) {
        return new DefaultScriptContext(process, inputResources);
    }

    // ---- 沙箱配置 ----

    /**
     * 创建沙箱 Context（TODO: GraalVM SDK 可用时实现）
     */
    static Object createSandbox() {
        return null; // 占位
    }

    /**
     * 绑定授权 API 到脚本上下文
     */
    static void bindContext(Object ctx, ScriptContext scriptCtx) {
        // Bindings bindings = ctx.getBindings("js");
        // bindings.putMember("context", createApiSurface(scriptCtx));
    }

    /**
     * 创建白名单 API 表面
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

        api.put("createResource", (ResourceFactory) (name, group, type, number) -> {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("name", name);
            r.put("group", group);
            r.put("type", type);
            r.put("number", number);
            return r;
        });

        api.put("log", (ScriptLogger) (level, msg) -> scriptCtx.log(level, msg));

        return Collections.unmodifiableMap(api);
    }

    // ---- 函数式接口 ----

    @FunctionalInterface
    public interface ResourceFactory {
        Map<String, Object> create(String name, String group, String type, BigDecimal number);
    }

    @FunctionalInterface
    public interface ScriptLogger {
        void log(String level, String message);
    }

}
