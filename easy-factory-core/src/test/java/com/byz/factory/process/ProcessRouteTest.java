package com.byz.factory.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProcessRoute 生产路线测试")
class ProcessRouteTest {

    private ProcessRoute route;

    @BeforeEach
    void setUp() {
        route = new ProcessRoute("R001", "主线");
    }

    @Test
    @DisplayName("构造器 — code/name 正确设置")
    void constructor_setsCodeAndName() {
        assertEquals("R001", route.getCode());
        assertEquals("主线", route.getName());
    }

    @Test
    @DisplayName("无参构造 — 字段为null")
    void defaultConstructor_fieldsNull() {
        ProcessRoute empty = new ProcessRoute();
        assertNull(empty.getCode());
        assertNull(empty.getName());
    }

    @Test
    @DisplayName("processes — 设置和获取工序列表")
    void processes_setAndGet() {
        Process p1 = new Process("P001", "工序一");
        Process p2 = new Process("P002", "工序二");
        route.setProcesses(List.of(p1, p2));

        assertEquals(2, route.getProcesses().size());
        assertEquals("P001", route.getProcesses().get(0).getCode());
    }

    @Test
    @DisplayName("setCode/setCode — 编码更新")
    void code_update() {
        route.setCode("R002");
        assertEquals("R002", route.getCode());
    }

    @Test
    @DisplayName("setName/setName — 名称更新")
    void name_update() {
        route.setName("支线");
        assertEquals("支线", route.getName());
    }

}
