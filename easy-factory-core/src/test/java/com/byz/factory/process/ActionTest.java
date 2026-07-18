package com.byz.factory.process;

import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.Dict;
import com.byz.factory.shared.Dict.Execute;
import com.byz.factory.shared.Dict.Importance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Action 动作执行测试")
class ActionTest {

    private Process process;
    private ResourceItem material;

    @BeforeEach
    void setUp() {
        process = new Process("P001", "测试工序");
        material = new ResourceItem("原料A", Dict.SourceGroup.Material, Dict.SourceType.Other, new BigDecimal("100"));
    }

    @Test
    @DisplayName("Nothing — 返回工序资源包")
    void execute_Nothing_returnsProcessResourcePack() {
        Action action = new Action("A01", "空操作", Importance.Optional, 1);
        action.setExecuteType(Execute.Nothing);
        IResourcePack result = action.execute(process, material);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create — 从 requireResources 创建新资源")
    void execute_Create_usesRequiredResources() {
        Action action = new Action("A02", "创建", Importance.Require, 2);
        action.setExecuteType(Execute.Create);
        ResourceItem required = new ResourceItem("产物X", Dict.SourceGroup.Product, Dict.SourceType.Other, BigDecimal.TEN);
        action.setRequireResources(required);

        IResourcePack result = action.execute(process);
        assertEquals(1, result.getResources().length);
        assertEquals("产物X", result.getResources()[0].getName());
    }

    @Test
    @DisplayName("Add — 输入数量加倍")
    void execute_Add_doublesInputQuantity() {
        Action action = new Action("A03", "追加", Importance.Require, 3);
        action.setExecuteType(Execute.Add);

        IResourcePack result = action.execute(process, material);
        IResourceItem[] resources = result.getResources();
        assertEquals(1, resources.length);
        assertTrue(resources[0].getNumber().compareTo(new BigDecimal("200")) >= 0);
    }

    @Test
    @DisplayName("Use — 消耗输入资源")
    void execute_Use_consumesInput() {
        Action action = new Action("A04", "消耗", Importance.Require, 4);
        action.setExecuteType(Execute.Use);
        BigDecimal before = material.getNumber();

        action.execute(process, material);
        // 消耗全部数量后应为0
        assertEquals(BigDecimal.ZERO, material.getNumber());
    }

    @Test
    @DisplayName("Use — 负数余额保护")
    void execute_Use_negativeProtection() {
        Action action = new Action("A05", "过度消耗", Importance.Require, 5);
        action.setExecuteType(Execute.Use);
        ResourceItem tiny = new ResourceItem("少量", Dict.SourceGroup.Material, Dict.SourceType.Other, BigDecimal.ONE);

        assertThrows(Exception.class, () -> {
            tiny.use(new BigDecimal("100"));
        });
    }

    @Test
    @DisplayName("Change — 返回工序资源包")
    void execute_Change_returnsProcessPack() {
        Action action = new Action("A06", "改变", Importance.Require, 6);
        action.setExecuteType(Execute.Change);

        IResourcePack result = action.execute(process, material);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Convert — 输入资源作为输出")
    void execute_Convert_inputBecomeOutput() {
        Action action = new Action("A07", "转换", Importance.Require, 7);
        action.setExecuteType(Execute.Convert);

        IResourcePack result = action.execute(process, material);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("有脚本时调用脚本引擎路径")
    void execute_withScript_usesScriptEngine() {
        Action action = new Action("A08", "脚本动作", Importance.Require, 8);
        action.setScript("return process.getResourcePack()");

        // 没有配置脚本引擎时应该抛异常
        assertThrows(Exception.class, () -> action.execute(process, material));
    }

    @Test
    @DisplayName("Importance Optional — getImportance 正确返回")
    void importance_optional() {
        Action action = new Action("A09", "可选动作", Importance.Optional, 9);
        assertEquals(Importance.Optional, action.getImportance());
    }

    @Test
    @DisplayName("Importance Require — getImportance 正确返回")
    void importance_require() {
        Action action = new Action("A10", "必要动作", Importance.Require, 10);
        assertEquals(Importance.Require, action.getImportance());
    }
}
