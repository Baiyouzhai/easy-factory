/**
 * @id          lims.batch-record-generate.v1
 * @name        批记录生成
 * @version     1.0.0
 * @module      lims
 * @author      system
 * @description 根据称量任务数据和工序执行记录，自动生成批记录。
 *              收集称量结果、工序执行日志、检验记录，计算实际收率，
 *              生成符合 GMP 要求的批记录文档。
 * @param       {Object} context - 执行上下文
 * @param       {string} context.batchNo - 批号
 * @param       {string} context.workOrderId - 工单号
 * @param       {string} context.formulaCode - 配方编码
 * @param       {string} context.productCode - 产品编码
 * @return      {BatchRecordInfo} 生成的批记录信息
 * @since       2026-07-19
 */

function execute(context) {
    var services = context.services;
    var logger = context.logger;

    var batchNo = context.batchNo;
    var workOrderId = context.workOrderId;
    var formulaCode = context.formulaCode;
    var productCode = context.productCode;

    logger.info("开始生成批记录: batchNo=" + batchNo + ", workOrderId=" + workOrderId);

    // 1. 收集称量任务数据
    var weighingTasks = [];
    var totalInputQty = 0;
    var hasDeviation = false;

    if (services.lims) {
        var tasks = services.lims.getWeighingTasksByWorkOrder(workOrderId);
        if (tasks && tasks.length > 0) {
            for (var i = 0; i < tasks.length; i++) {
                var task = tasks[i];
                weighingTasks.push(task.code);

                if (task.items) {
                    for (var j = 0; j < task.items.length; j++) {
                        var item = task.items[j];
                        if (item.actualQty) {
                            totalInputQty += item.actualQty;
                        }
                        if (item.status === "DEVIATION") {
                            hasDeviation = true;
                        }
                    }
                }
            }
        }
    }

    // 2. 收集工序执行记录
    var processRecords = [];
    if (services.mes) {
        var records = services.mes.getProcessRecords(workOrderId);
        if (records) {
            for (var k = 0; k < records.length; k++) {
                processRecords.push(records[k].code);
            }
        }
    }

    // 3. 收集检验记录
    var inspectionRecords = [];
    if (services.qms) {
        var inspections = services.qms.getInspectionRecordsByBatch(batchNo);
        if (inspections) {
            for (var m = 0; m < inspections.length; m++) {
                inspectionRecords.push(inspections[m].code);
            }
        }
    }

    // 4. 收集偏差记录
    var deviations = [];
    if (hasDeviation && services.qms) {
        var devs = services.qms.getDeviationsByBatch(batchNo);
        if (devs) {
            for (var n = 0; n < devs.length; n++) {
                deviations.push(devs[n].code);
            }
        }
    }

    // 5. 计算实际收率
    var actualYield = null;
    if (services.mes) {
        var outputQty = services.mes.getOutputQuantity(workOrderId);
        if (outputQty && totalInputQty > 0) {
            actualYield = (outputQty / totalInputQty * 100).toFixed(2);
        }
    }

    // 6. 构建批记录
    var batchRecord = {
        batchNo: batchNo,
        workOrderId: workOrderId,
        formulaCode: formulaCode,
        productCode: productCode,
        weighingTasks: weighingTasks,
        processRecords: processRecords,
        inspectionRecords: inspectionRecords,
        deviations: deviations,
        totalInputQty: totalInputQty,
        actualYield: actualYield,
        hasDeviation: hasDeviation,
        generatedAt: new Date().toISOString(),
        status: "IN_PROGRESS"
    };

    // 7. 保存批记录
    if (services.lims) {
        services.lims.saveBatchRecord(batchRecord);
    }

    logger.info("批记录生成完成: batchNo=" + batchNo
        + ", 称量任务=" + weighingTasks.length
        + ", 工序记录=" + processRecords.length
        + ", 检验记录=" + inspectionRecords.length
        + ", 偏差=" + deviations.length
        + ", 收率=" + (actualYield || "N/A") + "%"
        + ", 存在偏差=" + hasDeviation);

    return batchRecord;
}
