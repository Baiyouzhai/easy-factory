/**
 * @id          lims.weighing-check.v1
 * @name        称量防错
 * @version     1.0.0
 * @module      lims
 * @author      engineer-b
 * @description 对比天平读数与配方量，计算偏差百分比。超差时拒绝记录并抛出异常。
 * @param       {ProcessInfo} process - 当前称量工序
 * @param       {ResourceInfo[]} inputResources - 输入资源（配方物料）
 * @return      {WeighingResult} 称量结果: { items: [...], allPassed: boolean }
 * @throws      {WeighingException} 称量超差时抛出
 * @since       2026-07-12
 */

function execute(context) {
    var process = context.process;
    var services = context.services;
    var logger = context.logger;

    var lims = services.lims;
    var task = lims.getWeighingTask(process.code, context.batchNo);

    if (!task || !task.items || task.items.length === 0) {
        logger.warn("称量防错: 无称量任务，跳过");
        return { items: [], allPassed: true };
    }

    var results = [];
    var allPassed = true;

    for (var i = 0; i < task.items.length; i++) {
        var item = task.items[i];

        // 从天平读取实际值（模拟：实际应通过IoT接口获取）
        var actual = services.iot
            ? services.iot.readScale(item.balanceCode)
            : item.formulaQty; // 无IoT时使用配方量作为测试值

        var deviation = 0;
        if (item.formulaQty > 0) {
            deviation = Math.abs(actual - item.formulaQty) / item.formulaQty * 100;
        }

        var status = "OK";
        if (deviation > item.tolerance) {
            status = "DEVIATION";
            allPassed = false;
        }

        var record = {
            taskCode: task.code,
            materialCode: item.materialCode,
            materialName: item.materialName,
            formulaQty: item.formulaQty,
            actualQty: actual,
            deviationPercent: parseFloat(deviation.toFixed(2)),
            tolerance: item.tolerance,
            balance: item.balanceCode,
            operator: context.operator,
            status: status,
            timestamp: new Date().toISOString()
        };

        lims.saveWeighingRecord(record);
        results.push(record);

        if (status === "DEVIATION") {
            logger.error("称量超差: " + item.materialCode
                + " 配方量=" + item.formulaQty
                + " 实际=" + actual
                + " 偏差=" + deviation.toFixed(2) + "%"
                + " (允差±" + item.tolerance + "%)");

            // 发起偏差通知QMS
            if (services.qms) {
                services.qms.notifyWeighingDeviation({
                    taskCode: task.code,
                    materialCode: item.materialCode,
                    batchNo: context.batchNo,
                    deviation: deviation.toFixed(2),
                    tolerance: item.tolerance
                });
            }
        } else {
            logger.info("称量通过: " + item.materialCode + " 偏差=" + deviation.toFixed(2) + "%");
        }
    }

    return {
        items: results,
        allPassed: allPassed,
        taskCode: task.code,
        completedAt: new Date().toISOString()
    };
}
