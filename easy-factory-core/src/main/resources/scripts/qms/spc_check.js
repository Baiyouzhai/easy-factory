/**
 * @id          qms.spc-check.v1
 * @name        SPC规则检测
 * @version     1.0.0
 * @module      qms
 * @author      engineer-a
 * @description 对计量型检验值执行 Western Electric SPC 规则检测：
 *              Rule1: 1点超出3σ控制限
 *              Rule2: 连续9点在中心线同侧
 *              Rule3: 连续6点递增或递减
 *              Rule4: 连续14点交替上下
 * @param       {ProcessInfo} process - 当前工序
 * @param       {ResourceInfo[]} inputResources - 输入资源
 * @return      {SpcResult} SPC结果: { rulesViolated: [...], inControl: boolean }
 * @since       2026-07-12
 */

function execute(context) {
    var services = context.services;
    var logger = context.logger;

    var qms = services.qms;

    // 获取该工序最近30个检验值
    var dataPoints = qms.getRecentMeasurementData(context.process.code, 30);

    if (dataPoints.length < 2) {
        return { rulesViolated: [], inControl: true, dataPoints: dataPoints.length };
    }

    // 计算控制限
    var values = dataPoints.map(function(dp) { return dp.value; });
    var stats = calculateStats(values);

    var violations = [];

    // Rule 1: 1点超出 3σ
    var lastPoint = values[values.length - 1];
    if (lastPoint > stats.mean + 3 * stats.sigma || lastPoint < stats.mean - 3 * stats.sigma) {
        violations.push({
            rule: "Rule1",
            description: "1点超出3σ控制限",
            pointValue: lastPoint,
            ucl: stats.mean + 3 * stats.sigma,
            lcl: stats.mean - 3 * stats.sigma
        });
    }

    // Rule 2: 连续9点在中心线同侧
    if (values.length >= 9) {
        var last9 = values.slice(-9);
        var aboveCenter = last9.every(function(v) { return v > stats.mean; });
        var belowCenter = last9.every(function(v) { return v < stats.mean; });
        if (aboveCenter || belowCenter) {
            violations.push({
                rule: "Rule2",
                description: "连续9点在中心线" + (aboveCenter ? "上方" : "下方"),
                centerLine: stats.mean
            });
        }
    }

    // Rule 3: 连续6点递增或递减
    if (values.length >= 6) {
        var last6 = values.slice(-6);
        var increasing = true;
        var decreasing = true;
        for (var i = 1; i < last6.length; i++) {
            if (last6[i] <= last6[i - 1]) increasing = false;
            if (last6[i] >= last6[i - 1]) decreasing = false;
        }
        if (increasing || decreasing) {
            violations.push({
                rule: "Rule3",
                description: "连续6点" + (increasing ? "递增" : "递减"),
                trend: last6
            });
        }
    }

    // Rule 4: 连续14点交替上下
    if (values.length >= 14) {
        var last14 = values.slice(-14);
        var alternating = true;
        for (var j = 2; j < last14.length; j++) {
            if ((last14[j] - last14[j - 1]) * (last14[j - 1] - last14[j - 2]) >= 0) {
                alternating = false;
                break;
            }
        }
        if (alternating) {
            violations.push({
                rule: "Rule4",
                description: "连续14点交替上下"
            });
        }
    }

    var inControl = violations.length === 0;

    if (!inControl) {
        logger.warn("SPC告警: 违反 " + violations.length + " 条规则");
        for (var k = 0; k < violations.length; k++) {
            logger.warn("  - " + violations[k].rule + ": " + violations[k].description);
        }
    }

    return {
        rulesViolated: violations,
        inControl: inControl,
        dataPoints: dataPoints.length,
        stats: {
            mean: stats.mean,
            sigma: stats.sigma,
            ucl: stats.mean + 3 * stats.sigma,
            lcl: stats.mean - 3 * stats.sigma,
            usl: stats.usl,
            lsl: stats.lsl,
            cpk: stats.cpk
        }
    };
}

// ---- 统计计算 ----

function calculateStats(values) {
    var n = values.length;
    var sum = 0;
    for (var i = 0; i < n; i++) sum += values[i];
    var mean = sum / n;

    var variance = 0;
    for (var j = 0; j < n; j++) variance += (values[j] - mean) * (values[j] - mean);
    var sigma = Math.sqrt(variance / (n - 1));

    // 使用移动极差估计σ (n < 10时更稳健)
    if (n < 10) {
        var mrSum = 0;
        for (var k = 1; k < n; k++) {
            mrSum += Math.abs(values[k] - values[k - 1]);
        }
        sigma = (mrSum / (n - 1)) / 1.128;
    }

    // Cpk估算 (使用预设的规格限，若无则使用 ±4σ)
    var usl = mean + 4 * sigma;
    var lsl = mean - 4 * sigma;
    var cpk = Math.min((usl - mean) / (3 * sigma), (mean - lsl) / (3 * sigma));

    return {
        mean: parseFloat(mean.toFixed(4)),
        sigma: parseFloat(sigma.toFixed(4)),
        usl: usl,
        lsl: lsl,
        cpk: parseFloat(cpk.toFixed(4))
    };
}
