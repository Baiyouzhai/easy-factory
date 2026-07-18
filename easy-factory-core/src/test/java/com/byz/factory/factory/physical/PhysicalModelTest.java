package com.byz.factory.factory.physical;

import com.byz.factory.factory.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("物理层模型测试")
class PhysicalModelTest {

    private Factory factory;
    private Workstation w01, w02, w03;

    @BeforeEach
    void setUp() {
        factory = new Factory("F01", "测试工厂");

        // 工位是工厂级共享资源
        w01 = new Workstation("W01", "制粒工位");
        w01.addEquipment("WG-001", "A005");
        factory.registerWorkstation(w01);

        w02 = new Workstation("W02", "干燥工位");
        w02.addEquipment("DR-001", "A006");
        factory.registerWorkstation(w02);

        w03 = new Workstation("W03", "整粒工位");
        w03.addEquipment("SL-001", "A007");
        factory.registerWorkstation(w03);
    }

    @Test
    @DisplayName("产线引用工位→查询能力")
    void lineReferencesWorkstations() {
        ProductionLine lineA = new ProductionLine("LINE-A", "制粒线");
        lineA.addProcesses("P001", "P002", "P003");
        lineA.addNode("W01").addNode("W02").addNode("W03");
        factory.addLine(lineA);

        var registry = factory.getWorkstationRegistry();

        // 解析工位
        var resolved = lineA.resolveWorkstations(registry);
        assertEquals(3, resolved.size());
        assertEquals("W01", resolved.get(0).getCode());

        // 查询能力
        assertTrue(lineA.canExecute("A005", registry));
        assertTrue(lineA.canExecute("A006", registry));
        assertFalse(lineA.canExecute("A999", registry));

        // 工厂级别查询
        assertTrue(factory.canExecute("A005"));
        assertEquals("W01", factory.findWorkstationsFor("A005").get(0).getCode());
    }

    @Test
    @DisplayName("工位被多条产线共用")
    void sharedWorkstation() {
        // Line-A: W01 → W02 → W03
        ProductionLine lineA = new ProductionLine("LINE-A", "制粒线");
        lineA.addNode("W01").addNode("W02").addNode("W03");
        factory.addLine(lineA);

        // Line-B: W03 → W04  (W03 被共用)
        Workstation w04 = new Workstation("W04", "压片工位");
        w04.addEquipment("PT-001", "A016");
        factory.registerWorkstation(w04);

        ProductionLine lineB = new ProductionLine("LINE-B", "直接压片线");
        lineB.addProcesses("P005");
        lineB.addNode("W03").addNode("W04");
        factory.addLine(lineB);

        // W03 被两条产线引用
        var linesUsing = factory.findLinesUsing("W03");
        assertEquals(2, linesUsing.size());
        assertTrue(linesUsing.stream().anyMatch(l -> l.getCode().equals("LINE-A")));
        assertTrue(linesUsing.stream().anyMatch(l -> l.getCode().equals("LINE-B")));
    }

    @Test
    @DisplayName("产线报告")
    void report() {
        ProductionLine lineA = new ProductionLine("LINE-A", "制粒线");
        lineA.addProcesses("P001", "P002");
        lineA.addNode("W01").addNode("W02").addNode("W03");
        factory.addLine(lineA);

        String report = lineA.toReport(factory.getWorkstationRegistry());
        assertTrue(report.contains("LINE-A"));
        assertTrue(report.contains("WG-001"));
        assertTrue(report.contains("W01"));
        assertTrue(report.contains("W03"));
        System.out.println(report);
    }
}
