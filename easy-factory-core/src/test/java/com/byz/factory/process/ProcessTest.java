package com.byz.factory.process;

import com.byz.factory.exception.ActionException;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.Dict;
import com.byz.factory.shared.Dict.Execute;
import com.byz.factory.shared.Dict.Importance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Process 工序执行测试")
class ProcessTest {

    @Test
    @DisplayName("单动作执行")
    void execute_singleAction() {
        Process process = new Process("P001", "单步工序");
        Action action = new Action("A01", "测试", Importance.Require, 1);
        action.setExecuteType(Execute.Nothing);
        process.setActions(List.of(action));

        ResourceItem input = new ResourceItem("原料", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.TEN);
        IResourcePack result = process.execute(input);
        assertNotNull(result);
    }

    @Test
    @DisplayName("多动作链式执行 — 前一个输出作为后一个输入")
    void execute_actionChain() {
        Process process = new Process("P002", "多步工序");
        Action a1 = new Action("A01", "第一步", Importance.Require, 1);
        a1.setExecuteType(Execute.Nothing);
        Action a2 = new Action("A02", "第二步", Importance.Require, 2);
        a2.setExecuteType(Execute.Nothing);
        process.setActions(List.of(a1, a2));

        ResourceItem input = new ResourceItem("原料", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.ONE);
        assertDoesNotThrow(() -> process.execute(input));
    }

    @Test
    @DisplayName("空动作清单抛异常")
    void execute_emptyActions_throws() {
        Process process = new Process("P003", "空工序");
        process.setActions(List.of());

        assertThrows(ActionException.class, () -> process.execute());
    }

    @Test
    @DisplayName("null动作清单抛异常")
    void execute_nullActions_throws() {
        Process process = new Process("P004", "Null工序");
        process.setActions(null);

        assertThrows(ActionException.class, () -> process.execute());
    }

    @Test
    @DisplayName("setRequireResources/getRequireResources 正确存储")
    void requireResources_setAndGet() {
        Process process = new Process("P005", "资源工序");
        ResourceItem required = new ResourceItem("必要资源", Dict.SourceGroup.Machine, Dict.SourceType.Machine, BigDecimal.ONE);
        process.setRequireResources(required);

        assertEquals(1, process.requireResources().length);
        assertEquals("必要资源", process.requireResources()[0].getName());
    }

    @Test
    @DisplayName("getOrder/setOrder 正确")
    void order_setAndGet() {
        Process process = new Process("P006", "排序工序");
        process.setOrder(42);
        assertEquals(42, process.getOrder());
    }
}
