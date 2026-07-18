package com.byz.factory.test.demo;

import com.byz.factory.factory.IBlueprint;
import com.byz.factory.factory.IProductInfo;
import com.byz.factory.process.Action;
import com.byz.factory.process.IProcess;
import com.byz.factory.process.Process;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.ResourceItem;
import com.byz.factory.shared.Dict;
import com.byz.factory.shared.Dict.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 构建阿莫西林片剂产品蓝图（8道工序，22个动作）。
 * <p>
 * 基于 docs/architecture/walkthrough.md 的真实场景。
 *
 * @author 苏政
 */
public class DemoProductBuilder {

    public static final String PRODUCT_NAME = "阿莫西林片剂 500mg";
    public static final String BLUEPRINT_CODE = "AMX-TAB-v1.0";

    /** 构建完整产品信息（含蓝图） */
    public static IProductInfo build() {
        return new SimpleProductInfo(PRODUCT_NAME, buildBlueprint());
    }

    /** 构建蓝图：8道工序 */
    public static IBlueprint buildBlueprint() {
        return new SimpleBlueprint(BLUEPRINT_CODE, "阿莫西林片剂工艺路线", "1.0.0", buildProcesses());
    }

    /** 构建8道工序 */
    public static List<IProcess> buildProcesses() {
        List<IProcess> processes = new ArrayList<>();

        processes.add(buildProcess001()); // 称量配料
        processes.add(buildProcess002()); // 制粒
        processes.add(buildProcess003()); // 总混
        processes.add(buildProcess004()); // 中间体检验
        processes.add(buildProcess005()); // 压片
        processes.add(buildProcess006()); // 包衣
        processes.add(buildProcess007()); // 内包装
        processes.add(buildProcess008()); // 外包装

        return processes;
    }

    private static IProcess buildProcess001() {
        Process p = new Process("P001", "称量配料");
        p.setActions(List.of(
            action("A001", "领取原料", Execute.Add, Importance.Require,
                resource("原料-阿莫西林API", SourceGroup.Material)),
            action("A002", "称量API", Execute.Use, Importance.Require,
                resource("阿莫西林API 500g", SourceGroup.Material)),
            action("A003", "称量辅料", Execute.Use, Importance.Require,
                resource("淀粉 4500g", SourceGroup.Material),
                resource("硬脂酸镁 25g", SourceGroup.Material)),
            action("A004", "复核称量", Execute.Nothing, Importance.Require,
                resource("操作工", SourceGroup.Personnel))
        ));
        return p;
    }

    private static IProcess buildProcess002() {
        Process p = new Process("P002", "制粒");
        p.setActions(List.of(
            action("A005", "湿法制粒", Execute.Convert, Importance.Require,
                resource("湿法制粒机 WG-001", SourceGroup.Machine)),
            action("A006", "干燥", Execute.Change, Importance.Require,
                resource("干燥机 DR-001", SourceGroup.Machine)),
            action("A007", "整粒", Execute.Change, Importance.Require,
                resource("整粒机 SL-001", SourceGroup.Machine))
        ));
        return p;
    }

    private static IProcess buildProcess003() {
        Process p = new Process("P003", "总混");
        p.setActions(List.of(
            action("A008", "加入润滑剂", Execute.Combine, Importance.Require,
                resource("硬脂酸镁 25g", SourceGroup.Material)),
            action("A009", "混合", Execute.Convert, Importance.Require,
                resource("三维混合机 MX-001", SourceGroup.Machine))
        ));
        return p;
    }

    private static IProcess buildProcess004() {
        Process p = new Process("P004", "中间体检验");
        p.setActions(List.of(
            action("A010", "取样", Execute.Split, Importance.Require,
                resource("质检员", SourceGroup.Personnel)),
            action("A011", "水分检测", Execute.Nothing, Importance.Require,
                resource("水分测定仪 MST-001", SourceGroup.Machine)),
            action("A012", "含量检测", Execute.Nothing, Importance.Require,
                resource("HPLC HPLC-001", SourceGroup.Machine)),
            action("A013", "质量判定", Execute.Nothing, Importance.Require,
                resource("质检员", SourceGroup.Personnel))
        ));
        return p;
    }

    private static IProcess buildProcess005() {
        Process p = new Process("P005", "压片");
        p.setActions(List.of(
            action("A014", "安装模具", Execute.Add, Importance.Require,
                resource("模具 Φ12mm", SourceGroup.Machine)),
            action("A015", "调试参数", Execute.Change, Importance.Require,
                resource("操作工", SourceGroup.Personnel)),
            action("A016", "压片", Execute.Convert, Importance.Require,
                resource("压片机 PT-001", SourceGroup.Machine)),
            action("A017", "片重监测", Execute.Nothing, Importance.Require,
                resource("质检员", SourceGroup.Personnel))
        ));
        return p;
    }

    private static IProcess buildProcess006() {
        Process p = new Process("P006", "包衣");
        p.setActions(List.of(
            action("A018", "配制包衣液", Execute.Create, Importance.Require,
                resource("包衣粉 200g", SourceGroup.Material)),
            action("A019", "包衣", Execute.Change, Importance.Require,
                resource("包衣机 CY-001", SourceGroup.Machine))
        ));
        return p;
    }

    private static IProcess buildProcess007() {
        Process p = new Process("P007", "内包装");
        p.setActions(List.of(
            action("A020", "铝塑包装", Execute.Convert, Importance.Require,
                resource("泡罩包装机 PK-001", SourceGroup.Machine)),
            action("A021", "批号打印", Execute.Change, Importance.Require,
                resource("打码机 DM-001", SourceGroup.Machine))
        ));
        return p;
    }

    private static IProcess buildProcess008() {
        Process p = new Process("P008", "外包装");
        p.setActions(List.of(
            action("A022", "装盒", Execute.Convert, Importance.Require,
                resource("装盒机 ZH-001", SourceGroup.Machine)),
            action("A023", "赋码", Execute.Change, Importance.Require,
                resource("追溯码打印机", SourceGroup.Machine)),
            action("A024", "装箱入库", Execute.Transfer, Importance.Require,
                resource("操作工", SourceGroup.Personnel))
        ));
        return p;
    }

    // ---- 辅助方法 ----

    private static Action action(String code, String name, Execute type, Importance importance,
                                  IResourceItem... required) {
        Action a = new Action(code, name, importance, System.currentTimeMillis());
        a.setExecuteType(type);
        a.setRequireResources(required);
        return a;
    }

    private static ResourceItem resource(String name, SourceGroup group) {
        return new ResourceItem(name, group, Dict.SourceType.Other, BigDecimal.ONE);
    }

    // ---- 简单实现类 ----

    static class SimpleProductInfo implements IProductInfo {
        private final String name;
        private final IBlueprint blueprint;
        SimpleProductInfo(String name, IBlueprint blueprint) { this.name = name; this.blueprint = blueprint; }
        @Override public String getName() { return name; }
        @Override public IBlueprint getBlueprint() { return blueprint; }
        @Override public BigDecimal getNumber() { return BigDecimal.ONE; }
        @Override public IResourceItem setNumber(Number n) { return this; }
        @Override public Dict.SourceGroup getGroup() { return SourceGroup.Product; }
        @Override public Dict.SourceType getType() { return Dict.SourceType.Other; }
        @Override public IResourceItem copy() { return this; }
    }

    static class SimpleBlueprint implements IBlueprint {
        private final String code, name, version;
        private final List<IProcess> processes;
        SimpleBlueprint(String code, String name, String version, List<IProcess> processes) {
            this.code = code; this.name = name; this.version = version; this.processes = processes;
        }
        @Override public String getCode() { return code; }
        @Override public String getName() { return name; }
        @Override public String getVersion() { return version; }
        @Override public List<IProcess> getProductionProcessList() { return processes; }
    }
}
