package com.byz.factory.script;

import com.byz.factory.process.IProcess;
import com.byz.factory.process.Process;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.Dict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultScriptContext 脚本上下文测试")
class DefaultScriptContextTest {

    private Process process;
    private ResourceItem material;

    @BeforeEach
    void setUp() {
        process = new Process("P001", "测试工序");
        material = new ResourceItem("原料", Dict.SourceGroup.Material,
            Dict.SourceType.Other, BigDecimal.TEN);
    }

    @Test
    @DisplayName("getProcess — 返回构造时传入的工序")
    void getProcess_returnsProvidedProcess() {
        DefaultScriptContext ctx = new DefaultScriptContext(process, material);
        assertEquals("P001", ctx.getProcess().getCode());
    }

    @Test
    @DisplayName("getInputResources — 返回构造时传入的资源")
    void getInputResources_returnsProvidedResources() {
        DefaultScriptContext ctx = new DefaultScriptContext(process, material);
        assertEquals(1, ctx.getInputResources().length);
        assertEquals("原料", ctx.getInputResources()[0].getName());
    }

    @Test
    @DisplayName("null 资源变为空数组")
    void nullResources_becomesEmptyArray() {
        DefaultScriptContext ctx = new DefaultScriptContext(process, (IResourceItem[]) null);
        assertEquals(0, ctx.getInputResources().length);
    }

    @Test
    @DisplayName("addService — 添加白名单服务")
    void addService_storesService() {
        DefaultScriptContext ctx = new DefaultScriptContext(process);
        ctx.addService("mes", "mesService");
        assertEquals("mesService", ctx.getServices().get("mes"));
    }

    @Test
    @DisplayName("setParameter — 设置参数")
    void setParameter_storesParameter() {
        DefaultScriptContext ctx = new DefaultScriptContext(process);
        ctx.setParameter("key", "value");
        assertEquals("value", ctx.getParameters().get("key"));
    }

    @Test
    @DisplayName("log — 记录日志条目")
    void log_recordsEntry() {
        DefaultScriptContext ctx = new DefaultScriptContext(process);
        ctx.log("INFO", "test message");
        assertEquals(1, ctx.getLogEntries().size());
        assertTrue(ctx.getLogEntries().get(0).contains("test message"));
    }

    @Test
    @DisplayName("setResult/getResult — 存取结果")
    void result_setAndGet() {
        DefaultScriptContext ctx = new DefaultScriptContext(process);
        ctx.setResult("done");
        assertEquals("done", ctx.getResult());
    }

    @Test
    @DisplayName("getResult — 未设置返回null")
    void result_defaultIsNull() {
        DefaultScriptContext ctx = new DefaultScriptContext(process);
        assertNull(ctx.getResult());
    }

}
