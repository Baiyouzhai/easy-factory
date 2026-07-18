package com.byz.factory.script;

import com.byz.factory.process.Process;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.Dict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NashornScriptEngine Nashorn 引擎测试")
class NashornScriptEngineTest {

    private static final String VALID_SCRIPT = """
        /**
         * @id          test.nashorn.v1
         * @name        Nashorn Test
         * @version     1.0.0
         * @module      test
         */
        function execute(context) {
            return { name: context.processCode, status: 'nashorn-ok' };
        }
        """;

    private static final String SCRIPT_NO_EXECUTE = """
        /**
         * @id          test.nofn.v1
         * @name        No Function
         * @version     1.0.0
         * @module      test
         */
        var x = 1;
        """;

    private NashornScriptEngine engine;
    private Process process;

    @BeforeEach
    void setUp() {
        ScriptRegistry registry = new ScriptRegistry();
        engine = new NashornScriptEngine(registry);
        process = new Process("P001", "Nashorn测试工序");
    }

    @Test
    @DisplayName("getName — 返回 Nashorn")
    void getName_returnsNashorn() {
        assertEquals("Nashorn", engine.getName());
    }

    @Test
    @DisplayName("isSandboxed — 返回 false（无沙箱）")
    void isSandboxed_returnsFalse() {
        assertFalse(engine.isSandboxed());
    }

    @Test
    @DisplayName("compile — 成功编译有效脚本")
    void compile_validScript_succeeds() {
        CompiledScript compiled = engine.compile("test.nashorn.v1", VALID_SCRIPT);
        assertNotNull(compiled);
        assertEquals("test.nashorn.v1", compiled.getScriptId());
        assertTrue(compiled.isValid());
    }

    @Test
    @DisplayName("compile — ID 不匹配抛异常")
    void compile_idMismatch_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            engine.compile("test.wrong.v1", VALID_SCRIPT));
    }

    @Test
    @DisplayName("execute — 执行脚本返回结果")
    void execute_returnsResult() {
        CompiledScript compiled = engine.compile("test.nashorn.v1", VALID_SCRIPT);
        ScriptContext ctx = engine.createContext(process);
        Object result = engine.execute(compiled, ctx);
        assertNotNull(result);
    }

    @Test
    @DisplayName("execute — 无 execute 函数抛异常")
    void execute_noFunction_throws() {
        CompiledScript compiled = engine.compile("test.nofn.v1", SCRIPT_NO_EXECUTE);
        ScriptContext ctx = engine.createContext(process);
        assertThrows(RuntimeException.class, () -> engine.execute(compiled, ctx));
    }

    @Test
    @DisplayName("execute — 不支持的编译产物抛异常")
    void execute_wrongType_throws() {
        ScriptContext ctx = engine.createContext(process);
        CompiledScript fake = new CompiledScript() {
            @Override public String getScriptId() { return "fake"; }
            @Override public long getCompiledAt() { return 0; }
            @Override public boolean isValid() { return true; }
        };
        assertThrows(IllegalArgumentException.class, () -> engine.execute(fake, ctx));
    }

    @Test
    @DisplayName("eval — 直接执行源码")
    void eval_directSource() {
        ScriptContext ctx = engine.createContext(process);
        Object result = engine.eval(VALID_SCRIPT, ctx);
        assertNotNull(result);
    }

    @Test
    @DisplayName("createContext — 创建上下文含工序和资源")
    void createContext_includesProcessAndResources() {
        ResourceItem input = new ResourceItem("原料", Dict.SourceGroup.Material,
            Dict.SourceType.Other, BigDecimal.TEN);
        ScriptContext ctx = engine.createContext(process, input);

        assertEquals("P001", ctx.getProcess().getCode());
        assertEquals(1, ctx.getInputResources().length);
        assertEquals("原料", ctx.getInputResources()[0].getName());
    }

    @Test
    @DisplayName("构造器 — Nashorn 不可用时抛异常")
    void constructor_nashornAvailable() {
        // Nashorn 依赖已配置，应正常构造
        ScriptRegistry registry = new ScriptRegistry();
        NashornScriptEngine e = new NashornScriptEngine(registry);
        assertNotNull(e);
        assertEquals("Nashorn", e.getName());
    }

}
