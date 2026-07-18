package com.byz.factory.script;

import com.byz.factory.process.IProcess;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.resource.ResourcePack;
import com.byz.factory.shared.Dict;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.math.BigDecimal;
import java.util.*;

/**
 * GraalJS 脚本引擎实现 — 使用 GraalVM polyglot API，内置沙箱限制。
 * <p>
 * 安全策略（默认全部禁止，只开放白名单）：
 * <ul>
 *   <li>禁止访问任何 Java 类 (HostAccess.NONE)</li>
 *   <li>禁止文件/网络 IO</li>
 *   <li>禁止创建线程</li>
 *   <li>禁止创建进程</li>
 *   <li>禁止 JNI 本地访问</li>
 *   <li>仅能访问 ScriptContext 中显式绑定的 API 表面对象</li>
 * </ul>
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

        // 2. 在沙箱中创建 Context 并加载脚本
        Context ctx = createSandbox();
        ctx.eval("js", source);
        GraalCompiledScript result = new GraalCompiledScript(scriptId, System.currentTimeMillis(), ctx);

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

        Context ctx = graal.getContext();
        Value bindings = ctx.getBindings("js");
        Value executeFn = bindings.getMember("execute");

        if (executeFn == null || !executeFn.canExecute()) {
            throw new RuntimeException(
                "脚本 [" + graal.getScriptId() + "] 未定义可调用的 execute(context) 函数");
        }

        // 构造白名单 API 表面并传入
        Map<String, Object> apiSurface = createApiSurface(context);
        try {
            Value result = executeFn.execute(apiSurface);
            if (result == null || result.isNull()) {
                return context.getProcess().getResourcePack();
            }
            if (result.hasMembers()) {
                return rawMapToResourcePack(result, context.getProcess());
            }
            return result.as(Object.class);
        } catch (Exception e) {
            throw new RuntimeException(
                "脚本执行失败: " + graal.getScriptId() + " — " + e.getMessage(), e);
        }
    }

    @Override
    public Object eval(String source, ScriptContext context) {
        // 先用脚本自身的 @id 编译，避免临时ID与元数据ID不匹配
        ScriptMetadata metadata = ScriptMetadata.parse(source);
        String scriptId = metadata.getId();
        CompiledScript compiled = compile(scriptId, source);
        try {
            return execute(compiled, context);
        } finally {
            registry.deprecate(scriptId);
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
     * 创建沙箱 Context — 默认全部禁止。
     */
    @SuppressWarnings("deprecation")
    static Context createSandbox() {
        return Context.newBuilder("js")
            .allowHostAccess(HostAccess.NONE)
            .allowIO(false)
            .allowCreateThread(false)
            .allowNativeAccess(false)
            .allowExperimentalOptions(false)
            .build();
    }

    /**
     * 创建白名单 API 表面 — 脚本只能通过此对象访问系统。
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

    /**
     * 将脚本返回的 Map 结构转换为 IResourcePack。
     */
    private static IResourcePack rawMapToResourcePack(Value value, IProcess process) {
        ResourcePack pack = new ResourcePack();
        try {
            if (value.hasMember("resources")) {
                Value resources = value.getMember("resources");
                if (resources.hasArrayElements()) {
                    for (long i = 0; i < resources.getArraySize(); i++) {
                        pack.merge(toResourceItem(resources.getArrayElement(i)));
                    }
                }
            } else {
                pack.merge(toResourceItem(value));
            }
            return pack;
        } catch (Exception e) {
            return process.getResourcePack();
        }
    }

    private static IResourceItem toResourceItem(Value r) {
        String name = r.hasMember("name") ? r.getMember("name").asString() : "unknown";
        String groupStr = r.hasMember("group") ? r.getMember("group").asString() : "Material";
        String typeStr = r.hasMember("type") ? r.getMember("type").asString() : "Other";
        BigDecimal number = r.hasMember("number")
            ? new BigDecimal(r.getMember("number").asString())
            : BigDecimal.ONE;

        Dict.SourceGroup group;
        Dict.SourceType type;
        try {
            group = Dict.SourceGroup.valueOf(groupStr);
        } catch (IllegalArgumentException e) {
            group = Dict.SourceGroup.Material;
        }
        try {
            type = Dict.SourceType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            type = Dict.SourceType.Other;
        }

        return new ResourceItem(name, group, type, number);
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
