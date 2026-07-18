package com.byz.factory.test.demo;

import com.byz.factory.operation.time.ActionDuration;
import com.byz.factory.operation.time.GmpActionDuration;
import com.byz.factory.operation.time.ITimed;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 构建所有动作的时长映射（基于制药行业经验值）。
 *
 * @author 苏政
 */
public class DemoDurationRegistry {

    /** 标准批量：100,000 片 */
    public static final BigDecimal STANDARD_BATCH = new BigDecimal("100000");

    public static Map<String, ITimed> build() {
        Map<String, ITimed> map = new LinkedHashMap<>();

        // ── 称量配料 ──
        map.put("A001", ActionDuration.forAction("A001")
            .processingMinutes(10).setupMinutes(5).fixedTime().build());   // 领取原料
        map.put("A002", ActionDuration.forAction("A002")
            .processingMinutes(15).setupMinutes(5).fixedTime().build());   // 称量API
        map.put("A003", ActionDuration.forAction("A003")
            .processingMinutes(10).fixedTime().build());                   // 称量辅料
        map.put("A004", ActionDuration.forAction("A004")
            .processingMinutes(5).fixedTime().build());                    // 复核称量

        // ── 制粒 ──
        GmpActionDuration.Builder gmpBuilder = GmpActionDuration.forAction("A005");
        gmpBuilder.processingMinutes(90); gmpBuilder.setupMinutes(15); gmpBuilder.teardownMinutes(10);
        gmpBuilder.maxAllowedHours(4); gmpBuilder.minRequiredMinutes(60);
        map.put("A005", gmpBuilder.build()); // 湿法制粒 (GMP约束)
        map.put("A006", ActionDuration.forAction("A006")
            .processingMinutes(120).setupMinutes(10).build());             // 干燥
        map.put("A007", ActionDuration.forAction("A007")
            .processingMinutes(20).fixedTime().build());                   // 整粒

        // ── 总混 ──
        map.put("A008", ActionDuration.forAction("A008")
            .processingMinutes(10).fixedTime().build());                   // 加入润滑剂
        map.put("A009", ActionDuration.forAction("A009")
            .processingMinutes(30).setupMinutes(5).build());               // 混合

        // ── 中间体检验 ──
        map.put("A010", ActionDuration.forAction("A010")
            .processingMinutes(5).fixedTime().build());                    // 取样
        map.put("A011", ActionDuration.forAction("A011")
            .processingMinutes(15).fixedTime().build());                   // 水分检测
        map.put("A012", ActionDuration.forAction("A012")
            .processingMinutes(60).setupMinutes(10).build());              // 含量检测(HPLC)
        map.put("A013", ActionDuration.forAction("A013")
            .processingMinutes(10).fixedTime().build());                   // 质量判定

        // ── 压片 ──
        map.put("A014", ActionDuration.forAction("A014")
            .processingMinutes(15).setupMinutes(15).build());              // 安装模具
        map.put("A015", ActionDuration.forAction("A015")
            .processingMinutes(10).fixedTime().build());                   // 调试参数
        map.put("A016", ActionDuration.forAction("A016")
            .processing(Duration.ofSeconds(1)).variableTime(STANDARD_BATCH).setupMinutes(5).build()); // 压片
        map.put("A017", ActionDuration.forAction("A017")
            .processingMinutes(30).fixedTime().build());                   // 片重监测

        // ── 包衣 ──
        map.put("A018", ActionDuration.forAction("A018")
            .processingMinutes(15).fixedTime().build());                   // 配制包衣液
        map.put("A019", ActionDuration.forAction("A019")
            .processingMinutes(60).setupMinutes(10).teardownMinutes(5).build()); // 包衣

        // ── 内包装 ──
        map.put("A020", ActionDuration.forAction("A020")
            .processing(Duration.ofMillis(500)).variableTime(STANDARD_BATCH).setupMinutes(10).build()); // 铝塑包装
        map.put("A021", ActionDuration.forAction("A021")
            .processingMinutes(10).setupMinutes(5).build());               // 批号打印

        // ── 外包装 ──
        map.put("A022", ActionDuration.forAction("A022")
            .processing(Duration.ofMillis(300)).variableTime(STANDARD_BATCH).setupMinutes(5).build()); // 装盒
        map.put("A023", ActionDuration.forAction("A023")
            .processing(Duration.ofMillis(200)).variableTime(STANDARD_BATCH).build()); // 赋码
        map.put("A024", ActionDuration.forAction("A024")
            .processingMinutes(20).fixedTime().build());                   // 装箱入库

        return map;
    }
}
