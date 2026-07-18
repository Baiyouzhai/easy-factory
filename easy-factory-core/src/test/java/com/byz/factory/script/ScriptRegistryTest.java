package com.byz.factory.script;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScriptRegistry 脚本注册表测试")
class ScriptRegistryTest {

    private ScriptRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ScriptRegistry();
    }

    private ScriptMetadata makeMeta(String id) {
        return new ScriptMetadata(id, "Test-" + id, "1.0.0", "test",
            "test desc", "tester", "2026-07-18");
    }

    @Test
    @DisplayName("register — 注册后可通过ID查询")
    void register_thenGetById() {
        ScriptMetadata meta = makeMeta("test.v1");
        GraalCompiledScript compiled = new GraalCompiledScript("test.v1",
            System.currentTimeMillis(), null);
        registry.register("test.v1", meta, compiled);

        Optional<ScriptMetadata> found = registry.getMetadata("test.v1");
        assertTrue(found.isPresent());
        assertEquals("test.v1", found.get().getId());
    }

    @Test
    @DisplayName("register — 版本更新时旧版本入历史")
    void register_newVersion_oldGoesToHistory() {
        ScriptMetadata v1 = new ScriptMetadata("test.v1", "Test", "1.0.0", "test",
            "desc", "tester", "2026-07-18");
        ScriptMetadata v2 = new ScriptMetadata("test.v1", "Test", "2.0.0", "test",
            "desc v2", "tester", "2026-07-18");

        registry.register("test.v1", v1, null);
        registry.register("test.v1", v2, null);

        assertEquals("2.0.0", registry.getMetadata("test.v1").get().getVersion());
        List<ScriptMetadata> history = registry.getHistory("test.v1");
        assertEquals(1, history.size());
        assertEquals("1.0.0", history.get(0).getVersion());
    }

    @Test
    @DisplayName("listByModule — 按模块过滤")
    void listByModule_filtersCorrectly() {
        registry.register("mes.v1",
            new ScriptMetadata("mes.v1", "M", "1.0.0", "mes", "d", "a", "2026-01-01"), null);
        registry.register("qms.v1",
            new ScriptMetadata("qms.v1", "T", "1.0.0", "qms", "d", "a", "2026-01-01"), null);

        List<ScriptMetadata> mesScripts = registry.listByModule("mes");
        assertEquals(1, mesScripts.size());
        assertEquals("mes.v1", mesScripts.get(0).getId());

        List<ScriptMetadata> qmsScripts = registry.listByModule("qms");
        assertEquals(1, qmsScripts.size());
    }

    @Test
    @DisplayName("listByStatus — 按状态过滤")
    void listByStatus_filtersCorrectly() {
        ScriptMetadata meta = makeMeta("test.v1");
        meta.setStatus("ACTIVE");
        registry.register("test.v1", meta, null);

        List<ScriptMetadata> active = registry.listByStatus("ACTIVE");
        assertEquals(1, active.size());

        List<ScriptMetadata> deprecated = registry.listByStatus("DEPRECATED");
        assertTrue(deprecated.isEmpty());
    }

    @Test
    @DisplayName("listActive — 返回所有ACTIVE状态脚本")
    void listActive_returnsActiveScripts() {
        ScriptMetadata m1 = makeMeta("a.v1");
        m1.setStatus("ACTIVE");
        ScriptMetadata m2 = new ScriptMetadata("b.v1", "B", "1.0.0", "test", "d", "t", "2026-01-01");
        m2.setStatus("DRAFT");
        registry.register("a.v1", m1, null);
        registry.register("b.v1", m2, null);

        assertEquals(1, registry.listActive().size());
    }

    @Test
    @DisplayName("deprecate — 废弃后状态变为DEPRECATED")
    void deprecate_changesStatusToDeprecated() {
        registry.register("test.v1", makeMeta("test.v1"), null);
        registry.deprecate("test.v1");
        assertEquals("DEPRECATED", registry.getMetadata("test.v1").get().getStatus());
    }

    @Test
    @DisplayName("getMetadata — 不存在的ID返回empty")
    void getMetadata_unknown_returnsEmpty() {
        assertTrue(registry.getMetadata("nonexistent").isEmpty());
    }

    @Test
    @DisplayName("getCompiled — 注册后可查询编译产物")
    void getCompiled_thenGet() {
        GraalCompiledScript compiled = new GraalCompiledScript("test.v1",
            System.currentTimeMillis(), null);
        registry.register("test.v1", makeMeta("test.v1"), compiled);

        Optional<CompiledScript> found = registry.getCompiled("test.v1");
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("size — 返回注册数量")
    void size_returnsCount() {
        assertEquals(0, registry.size());
        registry.register("a.v1", makeMeta("a.v1"), null);
        registry.register("b.v1",
            new ScriptMetadata("b.v1", "B", "1.0.0", "test", "d", "t", "2026-01-01"), null);
        assertEquals(2, registry.size());
    }

}
