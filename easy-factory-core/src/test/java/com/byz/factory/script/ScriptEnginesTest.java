package com.byz.factory.script;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScriptEngines 多引擎注册中心测试")
class ScriptEnginesTest {

    @AfterEach
    void tearDown() {
        ScriptEngines.reset();
        // 恢复默认引擎
        ScriptRegistry registry = new ScriptRegistry();
        ScriptEngines.register(new GraalScriptEngine(registry));
        ScriptEngines.register(new NashornScriptEngine(new ScriptRegistry()));
    }

    @Test
    @DisplayName("getDefault — 默认引擎为 GraalJS（优先注册）")
    void getDefault_returnsGraalJS() {
        ScriptEngines.reset();
        ScriptRegistry registry = new ScriptRegistry();
        ScriptEngines.register(new GraalScriptEngine(registry));
        ScriptEngines.register(new NashornScriptEngine(new ScriptRegistry()));

        IScriptEngine engine = ScriptEngines.getDefault();
        assertNotNull(engine);
        assertEquals("GraalJS", engine.getName());
        assertTrue(engine.isSandboxed());
    }

    @Test
    @DisplayName("getEngine — 按名称获取 Nashorn")
    void getEngine_nashorn() {
        IScriptEngine engine = ScriptEngines.getEngine("Nashorn");
        assertNotNull(engine);
        assertEquals("Nashorn", engine.getName());
        assertFalse(engine.isSandboxed());
    }

    @Test
    @DisplayName("getEngine — 按名称获取 GraalJS")
    void getEngine_graalJS() {
        IScriptEngine engine = ScriptEngines.getEngine("GraalJS");
        assertNotNull(engine);
        assertEquals("GraalJS", engine.getName());
    }

    @Test
    @DisplayName("getEngine — 未知名称返回 null")
    void getEngine_unknown_returnsNull() {
        assertNull(ScriptEngines.getEngine("UnknownEngine"));
    }

    @Test
    @DisplayName("setDefault — 切换到 Nashorn")
    void setDefault_switchToNashorn() {
        ScriptEngines.setDefault("Nashorn");
        assertEquals("Nashorn", ScriptEngines.getDefault().getName());
        ScriptEngines.setDefault("GraalJS"); // 恢复
    }

    @Test
    @DisplayName("setDefault — 未知引擎抛异常")
    void setDefault_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> ScriptEngines.setDefault("UnknownEngine"));
    }

    @Test
    @DisplayName("listEngines — 返回所有已注册引擎")
    void listEngines_returnsAll() {
        Map<String, IScriptEngine> all = ScriptEngines.listEngines();
        assertTrue(all.size() >= 2);
        assertTrue(all.containsKey("GraalJS"));
        assertTrue(all.containsKey("Nashorn"));
    }

    @Test
    @DisplayName("无引擎时 getDefault 抛异常")
    void getDefault_noEngines_throws() {
        ScriptEngines.reset();
        assertThrows(IllegalStateException.class, () -> ScriptEngines.getDefault());
    }

}
