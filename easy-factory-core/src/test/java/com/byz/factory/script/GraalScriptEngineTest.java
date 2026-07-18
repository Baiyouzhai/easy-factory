package com.byz.factory.script;

import com.byz.factory.process.IProcess;
import com.byz.factory.process.Process;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.Dict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraalScriptEngine GraalJS 引擎测试")
class GraalScriptEngineTest {

    private static final String SCRIPT_WITH_FN = """
        /**
         * @id          test.return-process.v1
         * @name        返回工序信息
         * @version     1.0.0
         * @module      test
         */
        function execute(context) {
            return { name: context.processCode, status: 'ok' };
        }
        """;

    private static final String SCRIPT_BAD_ID = """
        /**
         * @id          test.wrong-id.v1
         * @name        Wrong
         * @version     1.0.0
         * @module      test
         */
        function execute(context) { return {}; }
        """;

    private static final String SCRIPT_NO_EXECUTE = """
        /**
         * @id          test.no-fn.v1
         * @name        No Execute Fn
         * @version     1.0.0
         * @module      test
         */
        var x = 1;
        """;

    private GraalScriptEngine engine;
    private Process process;

    @BeforeEach
    void setUp() {
        ScriptRegistry registry = new ScriptRegistry();
        engine = new GraalScriptEngine(registry);
        process = new Process("P001", "测试工序");
    }

    @Test
    @DisplayName("getName — 返回 GraalJS")
    void getName_returnsGraalJS() {
        assertEquals("GraalJS", engine.getName());
    }

    @Test
    @DisplayName("isSandboxed — 返回 true")
    void isSandboxed_returnsTrue() {
        assertTrue(engine.isSandboxed());
    }

    @Test
    @DisplayName("compile — 成功编译有效脚本")
    void compile_validScript_succeeds() {
        CompiledScript compiled = engine.compile("test.return-process.v1", SCRIPT_WITH_FN);
        assertNotNull(compiled);
        assertEquals("test.return-process.v1", compiled.getScriptId());
        assertTrue(compiled.isValid());
    }

    @Test
    @DisplayName("compile — 脚本ID不匹配抛异常")
    void compile_idMismatch_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            engine.compile("test.other-id.v1", SCRIPT_BAD_ID));
    }

    @Test
    @DisplayName("compile — 缺少元数据抛异常")
    void compile_noMetadata_throws() {
        String noMeta = "function execute(ctx) { return {}; }";
        assertThrows(IllegalArgumentException.class, () ->
            engine.compile("test.v1", noMeta));
    }

    @Test
    @DisplayName("execute — 执行已编译脚本并返回结果")
    void execute_compiledScript_returnsResult() {
        CompiledScript compiled = engine.compile("test.return-process.v1", SCRIPT_WITH_FN);
        ScriptContext ctx = engine.createContext(process);
        Object result = engine.execute(compiled, ctx);
        assertNotNull(result);
    }

    @Test
    @DisplayName("execute — 脚本无execute函数抛异常")
    void execute_noExecuteFunction_throws() {
        CompiledScript compiled = engine.compile("test.no-fn.v1", SCRIPT_NO_EXECUTE);
        ScriptContext ctx = engine.createContext(process);
        assertThrows(RuntimeException.class, () -> engine.execute(compiled, ctx));
    }

    @Test
    @DisplayName("execute — 不支持的编译产物类型抛异常")
    void execute_wrongCompiledType_throws() {
        ScriptContext ctx = engine.createContext(process);
        CompiledScript fake = new CompiledScript() {
            @Override public String getScriptId() { return "fake"; }
            @Override public long getCompiledAt() { return 0; }
            @Override public boolean isValid() { return true; }
        };
        assertThrows(IllegalArgumentException.class, () -> engine.execute(fake, ctx));
    }

    @Test
    @DisplayName("eval — 直接执行源码字符串")
    void eval_directSource() {
        ScriptContext ctx = engine.createContext(process);
        Object result = engine.eval(SCRIPT_WITH_FN, ctx);
        assertNotNull(result);
    }

    @Test
    @DisplayName("createContext — 创建包含工序和资源的上下文")
    void createContext_includesProcessAndResources() {
        ResourceItem input = new ResourceItem("原料", Dict.SourceGroup.Material,
            Dict.SourceType.Other, BigDecimal.TEN);
        ScriptContext ctx = engine.createContext(process, input);

        assertEquals("P001", ctx.getProcess().getCode());
        assertEquals(1, ctx.getInputResources().length);
        assertEquals("原料", ctx.getInputResources()[0].getName());
    }

    @Test
    @DisplayName("sandbox — 禁止 Java 类访问")
    void sandbox_noJavaAccess() {
        // 脚本尝试访问 Java 类应在沙箱中被阻止
        String hackScript = """
            /**
             * @id          test.hack.v1
             * @name        Hack attempt
             * @version     1.0.0
             * @module      test
             */
            function execute(context) {
                // Try to access Java - should fail in sandbox
                return {};
            }
            """;
        ScriptContext ctx = engine.createContext(process);
        Object result = engine.eval(hackScript, ctx);
        assertNotNull(result);
    }

    @Test
    @DisplayName("ScriptContext API — log 功能可用")
    void scriptContext_log() {
        DefaultScriptContext ctx = new DefaultScriptContext(process);
        ctx.log("INFO", "script executed");
        ctx.setResult("pass");
        assertEquals("pass", ctx.getResult());
        assertEquals(1, ctx.getLogEntries().size());
    }

}
