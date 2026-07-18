package com.byz.factory.test.demo;

import com.byz.factory.factory.IFactory;
import com.byz.factory.factory.IProductInfo;
import com.byz.factory.operation.capability.ProcessRouteMatcher;
import com.byz.factory.operation.capability.ProcessRouteMatcher.RouteMatchResult;
import com.byz.factory.operation.capacity.BottleneckDetector;
import com.byz.factory.operation.capacity.BottleneckDetector.BottleneckResult;
import com.byz.factory.operation.capacity.FactoryCapacityProfile;
import com.byz.factory.operation.common.ReportPrinter;
import com.byz.factory.operation.resource.ResourceRequirementExploder;
import com.byz.factory.operation.resource.ResourceRequirementExploder.ExploderResult;
import com.byz.factory.operation.time.ITimed;
import com.byz.factory.operation.time.ProductionLeadTime;
import com.byz.factory.operation.time.ProductionLeadTime.LeadTimeResult;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

/**
 * 阿莫西林片剂 500mg — 产品可行性分析完整演示。
 * <p>
 * 串行执行：可制造性检查 → 生产提前期 → 资源需求 → 瓶颈分析
 * <p>
 * 直接运行 main() 查看完整报告。
 *
 * @author 苏政
 */
public class AmoxicillinTabletDemo {

    public static final BigDecimal BATCH_SIZE = new BigDecimal("100000");

    public static void main(String[] args) {
        System.out.println(ReportPrinter.thickHeader("阿莫西林片剂 500mg — 产品可行性分析"));
        System.out.println(ReportPrinter.kv("产品", DemoProductBuilder.PRODUCT_NAME));
        System.out.println(ReportPrinter.kv("工厂", DemoFactoryBuilder.FACTORY_NAME));
        System.out.println(ReportPrinter.kv("批量", String.format("%,d 片", BATCH_SIZE.longValue())));
        System.out.println();

        // 准备数据
        IProductInfo product = DemoProductBuilder.build();
        IFactory factory = DemoFactoryBuilder.build();
        Map<String, ITimed> durationMap = DemoDurationRegistry.build();

        // ━━━ 1. 可制造性检查 ━━━
        step1_CapabilityCheck(product, factory);

        // ━━━ 2. 生产提前期 ━━━
        step2_LeadTime(product, durationMap);

        // ━━━ 3. 资源需求 ━━━
        step3_ResourceRequirement(product);

        // ━━━ 4. 瓶颈分析 ━━━
        step4_Bottleneck(product, durationMap);

        // 结论
        System.out.println(ReportPrinter.thickHeader("结论"));
        System.out.println("  ✅ FACTORY-01（口服固体制剂车间）可以生产阿莫西林片剂 500mg");
        System.out.println("  总生产提前期约 10 小时，日产能约 192 件（瓶颈：制粒工序）");
        System.out.println();
    }

    // ──── Step 1 ────

    private static void step1_CapabilityCheck(IProductInfo product, IFactory factory) {
        ProcessRouteMatcher matcher = new ProcessRouteMatcher();
        RouteMatchResult result = matcher.run(
            new ProcessRouteMatcher.MatchInput(product.getBlueprint(), factory));

        // 同时展示物理层结构
        System.out.println(ReportPrinter.header("物理层结构"));
        for (var line : factory.getProductionLines()) {
            System.out.println(line.toReport(factory.getWorkstationRegistry()));
        }

        System.out.println(result.toReport());

        if (!result.feasible()) {
            System.out.println(ReportPrinter.red("❌ 路线不可行！"));
            System.exit(1);
        }
    }

    // ──── Step 2 ────

    private static void step2_LeadTime(IProductInfo product, Map<String, ITimed> durationMap) {
        ProductionLeadTime calculator = new ProductionLeadTime(
            Duration.ofMinutes(10),  // 默认排队时间
            Duration.ofMinutes(5)    // 默认转运时间
        );

        long start = System.currentTimeMillis();
        LeadTimeResult result = calculator.calculate(
            product.getBlueprint(), durationMap, BATCH_SIZE);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println(ReportPrinter.header("生产提前期"));
        System.out.println("  总提前期: " + ReportPrinter.formatDuration(result.totalLeadTime()));
        System.out.println("  计算耗时: " + elapsed + "ms\n");

        // 工序明细表格
        var headers = java.util.List.of("工序", "周期时间", "排队", "转运", "小计");
        var rows = new java.util.ArrayList<java.util.List<String>>();
        for (var p : result.breakdown()) {
            rows.add(java.util.List.of(
                p.processCode() + " " + p.processName(),
                ReportPrinter.formatDuration(p.cycleTime()),
                ReportPrinter.formatDuration(p.queueTime()),
                ReportPrinter.formatDuration(p.transferTime()),
                ReportPrinter.formatDuration(p.total())
            ));
        }
        System.out.println(ReportPrinter.table(headers, rows));
        System.out.println();
    }

    // ──── Step 3 ────

    private static void step3_ResourceRequirement(IProductInfo product) {
        ResourceRequirementExploder exploder = new ResourceRequirementExploder()
            .withYield("P002", new BigDecimal("0.97"))  // 制粒良率97%
            .withYield("P005", new BigDecimal("0.98"))  // 压片良率98%
            .withYield("P006", new BigDecimal("0.99")); // 包衣良率99%

        long start = System.currentTimeMillis();
        ExploderResult result = exploder.explode(product.getBlueprint(), BATCH_SIZE);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println(ReportPrinter.header("资源需求"));
        System.out.println("  批量: " + String.format("%,d 片", BATCH_SIZE.longValue()));
        System.out.println("  资源种类: " + result.getResourceTypeCount());
        System.out.println("  计算耗时: " + elapsed + "ms\n");

        var items = result.totalResources().getResources();
        if (items != null) {
            for (var item : items) {
                String qty;
                if (item.getNumber().compareTo(BigDecimal.ONE) < 0) {
                    qty = item.getNumber().toPlainString();
                } else {
                    qty = String.format("%,.2f", item.getNumber());
                }
                System.out.println("  ├─ " + item.getName() + ": " + qty
                    + " [" + item.getGroup() + "]");
            }
        }
        System.out.println();
    }

    // ──── Step 4 ────

    private static void step4_Bottleneck(IProductInfo product,
                                          Map<String, ITimed> durationMap) {
        // 构建产能画像
        FactoryCapacityProfile profile = new FactoryCapacityProfile(
            DemoFactoryBuilder.FACTORY_CODE, Duration.ofHours(16)); // 双班16h
        profile.withMachine("湿法制粒机 WG-001", 960, 0.85)   // 16h×60min, OEE 85%
               .withMachine("压片机 PT-001", 960, 0.90)
               .withMachine("包衣机 CY-001", 960, 0.88)
               .withLabor("操作工", Duration.ofHours(32))     // 2班×2人×8h
               .withLabor("质检员", Duration.ofHours(16));    // 1班×2人×8h

        BottleneckDetector detector = new BottleneckDetector();
        long start = System.currentTimeMillis();
        BottleneckResult result = detector.detect(
            product.getBlueprint(), durationMap, profile);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println(ReportPrinter.header("瓶颈分析"));
        System.out.println("  约束工序: " + (result.hasBottleneck() ? result.bottleneckProcessCode() : "无"));
        System.out.println("  最大日产能: " + result.maxDailyThroughput() + " 件/天");
        System.out.println("  计算耗时: " + elapsed + "ms\n");

        System.out.println(result.toReport());
    }

}
