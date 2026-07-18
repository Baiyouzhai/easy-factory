package com.byz.factory.test.demo;

import com.byz.factory.factory.*;
import com.byz.factory.factory.physical.*;
import com.byz.factory.operation.capability.*;
import com.byz.factory.operation.capacity.*;
import com.byz.factory.operation.common.ReportPrinter;
import com.byz.factory.operation.resource.*;
import com.byz.factory.operation.time.*;
import com.byz.factory.process.*;
import com.byz.factory.resource.IResourceItem;
import com.byz.factory.shared.Dict;
import com.byz.factory.shared.UOM;
import com.byz.factory.batch.IBatch;
import com.byz.factory.event.AuditTrail;
import com.byz.factory.event.IElectronicSignature;
import com.byz.factory.event.SignatureMeaning;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * 全面演示：动作库→策略对比→GMP合规→标签过滤→产线预设
 *
 * @author 苏政
 */
public class FullDemo {

    static final BigDecimal BATCH = new BigDecimal("100000");

    public static void main(String[] args) {
        System.out.println(ReportPrinter.thickHeader("easy-factory 完整演示"));
        System.out.println("产品: 阿莫西林片剂 500mg | 批量: 100,000片\n");

        // 准备数据
        ActionLibrary library = buildActionLibrary();
        IFactory factory = DemoFactoryBuilder.build();
        IProductInfo product = DemoProductBuilder.build();
        Map<String, ITimed> durations = DemoDurationRegistry.build();

        step1_ActionLibrary(library);
        step2_StrategyComparison(product, factory);
        step3_GmpCompliance(durations);
        step4_TagFilter(product);
        step5_ProductionLinePreset(factory);
        step6_FullPipeline(product, factory, durations);
        step7_AuditTrailAndSignature();
        step8_WipAndProcessParameters();

        System.out.println(ReportPrinter.thickHeader("演示完成"));
    }

    // ─── 1. 动作库 ───
    static ActionLibrary buildActionLibrary() {
        ActionLibrary lib = new ActionLibrary();
        lib.register("A001", "领取原料", Dict.Execute.Add);
        lib.register("A002", "称量API", Dict.Execute.Use);
        lib.register("A005", "湿法制粒", Dict.Execute.Convert);
        lib.register("A006", "干燥", Dict.Execute.Change);
        lib.register("A016", "压片", Dict.Execute.Convert);
        lib.register("A019", "包衣", Dict.Execute.Change);
        return lib;
    }

    static void step1_ActionLibrary(ActionLibrary lib) {
        System.out.println(ReportPrinter.header("1. 动作库"));
        System.out.println("  注册动作: " + lib.size() + " 个");
        lib.get("A005").ifPresent(a ->
            System.out.println("  A005: " + a.getName() + " (" + a.getExecuteType() + ")"));
        System.out.println("  制粒类动作: " + lib.findByPrefix("A00").size() + " 个\n");
    }

    // ─── 2. 策略对比 ───
    static void step2_StrategyComparison(IProductInfo product, IFactory factory) {
        System.out.println(ReportPrinter.header("2. 匹配策略对比"));

        // 直接匹配
        ProcessRouteMatcher direct = new ProcessRouteMatcher(new DirectEquipmentStrategy());
        var r1 = direct.match(product.getBlueprint(), factory);

        // 线优先
        ProcessRouteMatcher lineFirst = new ProcessRouteMatcher(new LineFirstStrategy());
        var r2 = lineFirst.match(product.getBlueprint(), factory);

        System.out.println("  DirectEquipment: " + (r1.feasible() ? "✅可行" : "❌不可行")
            + " (✅"+ count(r1, ProcessRouteMatcher.MatchStatus.EQUIPPED) + "个动作)");
        System.out.println("  LineFirst:       " + (r2.feasible() ? "✅可行" : "❌不可行")
            + " (✅"+ count(r2, ProcessRouteMatcher.MatchStatus.EQUIPPED) + "个动作)");
        System.out.println();
    }

    static long count(ProcessRouteMatcher.RouteMatchResult r, ProcessRouteMatcher.MatchStatus s) {
        return r.processResults().stream()
            .flatMap(p -> p.actionResults().stream())
            .filter(a -> a.status() == s).count();
    }

    // ─── 3. GMP合规 ───
    static void step3_GmpCompliance(Map<String, ITimed> durations) {
        System.out.println(ReportPrinter.header("3. GMP合规校验"));

        // 制粒动作：法规上限4h
        var b = GmpActionDuration.forAction("A005");
        b.processingMinutes(90); b.maxAllowedHours(4); b.minRequiredMinutes(60);
        GmpActionDuration gmpDur = b.build();

        System.out.println("  A005 湿法制粒:");
        System.out.println("    上限: " + gmpDur.getMaxAllowedTime().map(d -> d.toHours()+"h").orElse("无"));
        System.out.println("    下限: " + gmpDur.getMinRequiredTime().map(d -> d.toMinutes()+"min").orElse("无"));

        // 正常执行
        var ok = gmpDur.checkConstraints(Duration.ofMinutes(90));
        System.out.println("    实际90min → " + (ok.passed() ? "✅" : "❌"));

        // 超时
        var fail = gmpDur.checkConstraints(Duration.ofHours(5));
        System.out.println("    实际300min → " + (fail.passed() ? "✅" : "❌ " + fail.violation()));
        System.out.println();
    }

    // ─── 4. 标签过滤 ───
    static void step4_TagFilter(IProductInfo product) {
        System.out.println(ReportPrinter.header("4. 标签过滤"));
        var processes = product.getBlueprint().getProductionProcessList();
        if (processes.isEmpty()) return;
        var firstAction = processes.get(0).getActions().get(0);

        System.out.println("  动作: " + firstAction.getCode() + " " + firstAction.getName());
        if (firstAction instanceof IActionModel m) {
            IResourceItem[] required = m.requireResources();
            if (required != null && required.length > 0) {
                System.out.println("  资源总数: " + required.length);
                System.out.println("  物料类: " + TagFilter.of(required).group(Dict.SourceGroup.Material).count() + " 个");
                System.out.println("  人员类: " + TagFilter.of(required).group(Dict.SourceGroup.Personnel).count() + " 个");
            }
        }
        System.out.println();
    }

    // ─── 5. 产线预设 ───
    static void step5_ProductionLinePreset(IFactory factory) {
        System.out.println(ReportPrinter.header("5. 产线预设配置"));
        System.out.println("  产线数: " + factory.getProductionLines().size());
        System.out.println("  设备池: " + factory.getEquipmentPool().size() + " 台设备");
        System.out.println("  工位数: " + factory.getWorkstationRegistry().size());

        // 展示一条产线的预设
        factory.getProductionLines().stream().findFirst().ifPresent(line -> {
            System.out.println("\n  " + line.getCode() + " " + line.getName());
            System.out.println("    工序: " + line.getSupportedProcessCodes());
            System.out.println("    节点: " + line.getNodes().size());
            System.out.println("    衔接: " + line.getConnections().size() + " 条");
        });
        System.out.println();
    }

    // ─── 6. 完整管道 ───
    static void step6_FullPipeline(IProductInfo product, IFactory factory,
                                    Map<String, ITimed> durations) {
        System.out.println(ReportPrinter.header("6. 完整分析管道"));

        // 匹配
        ProcessRouteMatcher matcher = new ProcessRouteMatcher();
        var match = matcher.match(product.getBlueprint(), factory);
        System.out.println("  匹配: " + (match.feasible() ? "✅可行" : "❌不可行"));

        // 提前期
        ProductionLeadTime timeCalc = new ProductionLeadTime(Duration.ofMinutes(10), Duration.ofMinutes(5));
        var lead = timeCalc.calculate(product.getBlueprint(), durations, BATCH);
        System.out.println("  提前期: " + ReportPrinter.formatDuration(lead.totalLeadTime()));

        // 资源需求
        ResourceRequirementExploder exploder = new ResourceRequirementExploder()
            .withYield("P002", new BigDecimal("0.97"));
        var resources = exploder.explode(product.getBlueprint(), BATCH);
        System.out.println("  资源: " + resources.getResourceTypeCount() + " 种");

        // 瓶颈
        FactoryCapacityProfile profile = new FactoryCapacityProfile(factory.getCode(), Duration.ofHours(16));
        profile.withMachine("湿法制粒机 WG-001", 960, 0.85).withMachine("压片机 PT-001", 960, 0.90);
        BottleneckDetector detector = new BottleneckDetector();
        var bottleneck = detector.detect(product.getBlueprint(), durations, profile);
        System.out.println("  瓶颈: " + (bottleneck.hasBottleneck() ? bottleneck.bottleneckProcessCode() : "无"));
        System.out.println("  日产能: " + bottleneck.maxDailyThroughput() + " 件/天\n");
    }

    // ─── 7. 审计追踪 & 电子签名 ───
    static void step7_AuditTrailAndSignature() {
        System.out.println(ReportPrinter.header("7. 审计追踪 & 电子签名"));

        AuditTrail trail = new AuditTrail(
            "Batch", "B20260718-001", "TRANSITION",
            "suzheng", Instant.now(),
            "{\"status\":\"CREATED\"}", "{\"status\":\"IN_PROGRESS\"}",
            "开工确认完成"
        );
        System.out.println("  审计: " + trail.entityType() + "[" + trail.entityId() + "] "
            + trail.action() + " by " + trail.operator());
        System.out.println("    变更: " + trail.before() + " → " + trail.after());

        System.out.println("  签名: " + trail.operator() + " "
            + SignatureMeaning.APPROVED + " → " + trail.reason());
        System.out.println();
    }

    // ─── 8. WIP 位置 & 工艺参数 ───
    static void step8_WipAndProcessParameters() {
        System.out.println(ReportPrinter.header("8. WIP 位置 & 工艺参数"));

        // WIP 位置演示
        System.out.println("  WIP 位置: 批次 B20260718-001");
        System.out.println("    工序: P005 (压片)");
        System.out.println("    工位: W06 (压片工位)");

        // 工艺参数演示
        System.out.println("  工艺参数: 干燥温度");
        System.out.println("    目标: 60°C [55°C - 65°C]");
        System.out.println();
    }

    static String formatDuration(Duration d) {
        long h = d.toHours(); long m = d.toMinutesPart();
        return h > 0 ? h + "h " + m + "m" : m + "m";
    }
}
