package com.byz.factory.factory;

import com.byz.factory.factory.physical.EquipmentBinding;
import com.byz.factory.factory.physical.IEquipmentBinding;
import com.byz.factory.factory.physical.ProductionLine;
import com.byz.factory.factory.physical.Workstation;
import com.byz.factory.process.Action;
import com.byz.factory.process.IAction;
import com.byz.factory.process.IProcess;
import com.byz.factory.process.Process;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.Dict;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductChecker 可制造性检查器测试")
class ProductCheckerTest {

    // ---- test helpers for building factory and product ----

    private static class TestFactory implements IFactory {
        private final String code;
        private final String name;
        private final List<IProcess> processes;
        private final TestWorkstation workstation;

        TestFactory(String code, String name, List<IProcess> processes, TestWorkstation ws) {
            this.code = code;
            this.name = name;
            this.processes = processes;
            this.workstation = ws;
        }

        @Override public String getCode() { return code; }
        @Override public String getName() { return name; }
        @Override public List<IProcess> getProcesses() { return processes; }
        @Override public java.util.Map<String, com.byz.factory.factory.physical.IWorkstation> getWorkstationRegistry() {
            return java.util.Map.of(workstation.getCode(), workstation);
        }
    }

    private static class TestWorkstation extends Workstation {
        private final List<IEquipmentBinding> bindings;

        TestWorkstation(String code, String name, List<IEquipmentBinding> bindings) {
            super(code, name);
            this.bindings = bindings;
        }
        @Override public List<IEquipmentBinding> getEquipmentBindings() { return bindings; }
    }

    private static class TestProduct implements IProductInfo {
        private final String name;
        private final IBlueprint blueprint;
        TestProduct(String name, IBlueprint blueprint) {
            this.name = name;
            this.blueprint = blueprint;
        }
        @Override public String getName() { return name; }
        @Override public IBlueprint getBlueprint() { return blueprint; }
        @Override public Dict.SourceGroup getGroup() { return Dict.SourceGroup.Product; }
        @Override public Dict.SourceType getType() { return Dict.SourceType.Other; }
        @Override public java.math.BigDecimal getNumber() { return java.math.BigDecimal.ONE; }
        @Override public com.byz.factory.resource.IResourceItem setNumber(Number number) { return this; }
        @Override public com.byz.factory.resource.IResourceItem copy() { return this; }
    }

    private static class TestBlueprint implements IBlueprint {
        private final List<IProcess> processes;
        TestBlueprint(List<IProcess> processes) { this.processes = processes; }
        @Override public String getCode() { return "BP-TEST"; }
        @Override public String getName() { return "Test Blueprint"; }
        @Override public String getVersion() { return "1.0.0"; }
        @Override public List<IProcess> getProductionProcessList() { return processes; }
    }

    private final ProductChecker checker = new ProductChecker();

    @Test
    @DisplayName("check — 所有工序通过")
    void check_allPassed() {
        Process process = new Process("P001", "工序一");
        Action action = new Action("A01", "动作一", Dict.Importance.Require, 1);
        process.setActions(List.of(action));

        EquipmentBinding binding = new EquipmentBinding("EQ-001", Set.of("A01"), true);
        TestWorkstation ws = new TestWorkstation("W01", "工位一", List.of(binding));
        TestFactory factory = new TestFactory("F001", "工厂", List.of(process), ws);
        TestProduct product = new TestProduct("产品A", new TestBlueprint(List.of(process)));

        ProductFactoryResult result = checker.check(product, factory);
        assertTrue(result.isPassed());
        assertEquals(1, result.getResults().size());
        assertTrue(result.getResults().get(0).passed());
    }

    @Test
    @DisplayName("check — 工厂缺少工序时失败")
    void check_missingProcess_fails() {
        Process bpProcess = new Process("P001", "工序一");
        Process factoryProcess = new Process("P002", "工序二");

        EquipmentBinding binding = new EquipmentBinding("EQ-001", Set.of("A01"), true);
        TestWorkstation ws = new TestWorkstation("W01", "工位一", List.of(binding));
        TestFactory factory = new TestFactory("F001", "工厂", List.of(factoryProcess), ws);
        TestProduct product = new TestProduct("产品A", new TestBlueprint(List.of(bpProcess)));

        ProductFactoryResult result = checker.check(product, factory);
        assertFalse(result.isPassed());
        assertTrue(result.getResults().get(0).reason().contains("缺少工序"));
    }

    @Test
    @DisplayName("check — 工序存在但动作不匹配时失败")
    void check_actionMismatch_fails() {
        Process process = new Process("P001", "工序一");
        Action action = new Action("A01", "动作一", Dict.Importance.Require, 1);
        process.setActions(List.of(action));

        // 工位只支持 A02, 不支持 A01
        EquipmentBinding binding = new EquipmentBinding("EQ-001", Set.of("A02"), true);
        TestWorkstation ws = new TestWorkstation("W01", "工位一", List.of(binding));
        TestFactory factory = new TestFactory("F001", "工厂", List.of(process), ws);
        TestProduct product = new TestProduct("产品A", new TestBlueprint(List.of(process)));

        ProductFactoryResult result = checker.check(product, factory);
        assertFalse(result.isPassed());
        assertTrue(result.getResults().get(0).reason().contains("无法执行的动作"));
    }

    @Test
    @DisplayName("check — product为null抛异常")
    void check_nullProduct_throws() {
        assertThrows(IllegalArgumentException.class, () -> checker.check(null, null));
    }

    @Test
    @DisplayName("check — 产品无蓝图时失败")
    void check_noBlueprint_fails() {
        TestFactory factory = new TestFactory("F", "厂", List.of(), null);
        TestProduct product = new TestProduct("产品A", null);

        ProductFactoryResult result = checker.check(product, factory);
        assertFalse(result.isPassed());
        assertTrue(result.getResults().get(0).reason().contains("未关联蓝图"));
    }

    @Test
    @DisplayName("check — 蓝图为空工序时失败")
    void check_emptyBlueprintProcesses_fails() {
        EquipmentBinding binding = new EquipmentBinding("EQ-001", Set.of("A01"), true);
        TestWorkstation ws = new TestWorkstation("W01", "工位一", List.of(binding));
        TestFactory factory = new TestFactory("F001", "工厂", List.of(), ws);
        TestProduct product = new TestProduct("产品A", new TestBlueprint(List.of()));

        ProductFactoryResult result = checker.check(product, factory);
        assertFalse(result.isPassed());
        assertTrue(result.getResults().get(0).reason().contains("空工序"));
    }

}
