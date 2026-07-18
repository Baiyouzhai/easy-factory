package com.byz.factory.test.demo;

import com.byz.factory.factory.Factory;
import com.byz.factory.factory.IFactory;
import com.byz.factory.factory.physical.*;
import com.byz.factory.process.Process;
import com.byz.factory.process.IProcess;

import java.util.List;

/**
 * 构建 FACTORY-01 测试工厂 — 含完整物理层（工位注册表 + 3条产线）。
 *
 * @author 苏政
 */
public class DemoFactoryBuilder {

    public static final String FACTORY_CODE = "FACTORY-01";
    public static final String FACTORY_NAME = "口服固体制剂车间";

    // 工位编码常量
    public static final String W01 = "W01", W02 = "W02", W03 = "W03", W04 = "W04";
    public static final String W05 = "W05", W06 = "W06", W07 = "W07", W08 = "W08";
    public static final String W09 = "W09", W10 = "W10", W11 = "W11", W12 = "W12";

    public static IFactory build() {
        Factory factory = new Factory(FACTORY_CODE, FACTORY_NAME);

        // ── 注册工位 ──
        registerWorkstations(factory);

        // ── 构建产线 ──
        factory.addLine(buildGranulationLine())   // 制粒线
               .addLine(buildCompressionLine())   // 压片线
               .addLine(buildPackagingLine());    // 包装线

        // ── 工艺层(兼容旧的Process查询) ──
        factory.setProcesses(DemoProductBuilder.buildProcesses());

        return factory;
    }

    /** 注册全部12个工位 */
    private static void registerWorkstations(Factory factory) {
        // ── 制粒线工位 ──
        factory.registerWorkstation(new Workstation(W01, "称量工位", StationType.SINGLE)
            .addEquipment("WG-BAL-001", "A001", "A002", "A003", "A004"));

        factory.registerWorkstation(new Workstation(W02, "制粒工位", StationType.SINGLE)
            .addEquipment("WG-001", "A005"));   // 湿法制粒机

        factory.registerWorkstation(new Workstation(W03, "干燥工位", StationType.SINGLE)
            .addEquipment("DR-001", "A006"));    // 干燥机

        factory.registerWorkstation(new Workstation(W04, "整粒总混工位", StationType.SINGLE)
            .addEquipment("SL-001", "A007")       // 整粒机
            .addEquipment("MX-001", "A008", "A009")); // 混合机(一台设备多动作)

        // ── 压片线工位 ──
        factory.registerWorkstation(new Workstation(W05, "检验工位", StationType.SINGLE)
            .addEquipment("QC-KIT-001", "A010")      // 取样工具
            .addEquipment("MST-001", "A011")          // 水分测定仪
            .addEquipment("HPLC-001", "A012")         // HPLC
            .addEquipment("QC-DEC-001", "A013"));     // 质量判定(人工)

        factory.registerWorkstation(new Workstation(W06, "压片工位", StationType.PARALLEL)
            .addEquipment(new EquipmentBinding("PT-001").addAction("A014").addAction("A015").addAction("A016").addAction("A017").setPrimary(true))
            .addEquipment(new EquipmentBinding("PT-002").addAction("A016").setPrimary(false))); // 备选压片机

        factory.registerWorkstation(new Workstation(W07, "包衣工位", StationType.SINGLE)
            .addEquipment("CY-001", "A018", "A019"));

        // ── 包装线工位 ──
        factory.registerWorkstation(new Workstation(W08, "内包工位", StationType.SINGLE)
            .addEquipment("PK-001", "A020"));       // 泡罩包装机

        factory.registerWorkstation(new Workstation(W09, "打码工位", StationType.SINGLE)
            .addEquipment("DM-001", "A021"));

        factory.registerWorkstation(new Workstation(W10, "装盒工位", StationType.SINGLE)
            .addEquipment("ZH-001", "A022"));

        factory.registerWorkstation(new Workstation(W11, "赋码工位", StationType.SINGLE)
            .addEquipment("COD-001", "A023"));

        factory.registerWorkstation(new Workstation(W12, "装箱工位", StationType.SINGLE)
            .addEquipment("PKL-001", "A024"));
    }

    /** 制粒线: W01→W02→W03→W04 */
    private static ProductionLine buildGranulationLine() {
        return new ProductionLine("LINE-G", "制粒线")
            .addProcesses("P001", "P002", "P003")
            .addNode(W01).addNode(W02).addNode(W03).addNode(W04);
    }

    /** 压片线: W05→W06→W07 */
    private static ProductionLine buildCompressionLine() {
        return new ProductionLine("LINE-C", "压片线")
            .addProcesses("P004", "P005", "P006")
            .addNode(W05).addNode(W06).addNode(W07);
    }

    /** 包装线: W08→W09→W10→W11→W12 */
    private static ProductionLine buildPackagingLine() {
        return new ProductionLine("LINE-P", "包装线")
            .addProcesses("P007", "P008")
            .addNode(W08).addNode(W09).addNode(W10).addNode(W11).addNode(W12);
    }
}
