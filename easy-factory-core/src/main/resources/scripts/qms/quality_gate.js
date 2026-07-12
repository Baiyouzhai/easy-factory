/**
 * @id          qms.quality-gate.v1
 * @name        质量门禁判定
 * @version     1.0.0
 * @module      qms
 * @author      engineer-a
 * @description 根据检验记录判定放行/让步/拒收。不合格时触发偏差流程，中断工序。
 * @param       {ProcessInfo} process - 当前工序上下文
 * @param       {ResourceInfo[]} inputResources - 输入资源列表
 * @return      {JudgementResult} 判定结果: { judgement: "PASS"|"CONCESSION"|"FAIL", ... }
 * @throws      {JudgementFailedException} 判定不合格时抛出，中断工序流转
 * @since       2026-07-12
 */

function execute(context) {
    var process = context.process;
    var services = context.services;
    var logger = context.logger;

    // 1. 获取该工序的最近一次检验记录
    var qms = services.qms;
    var inspectionRecord = qms.getLastInspection(process.code, context.batchNo);

    if (!inspectionRecord) {
        logger.warn("质量门禁: 工序 " + process.code + " 无检验记录，默认放行");
        return { judgement: "PASS", reason: "无检验记录，默认放行" };
    }

    var judgement = inspectionRecord.judgement;

    if (judgement === "PASS") {
        // 合格 → 放行
        logger.info("质量门禁: 工序 " + process.code + " 检验合格，放行");
        return { judgement: "PASS", inspectionId: inspectionRecord.id };
    }

    if (judgement === "CONCESSION") {
        // 让步接收 → 记录审批，继续流转
        logger.warn("质量门禁: 工序 " + process.code + " 让步接收");
        qms.recordConcession({
            inspectionId: inspectionRecord.id,
            reason: inspectionRecord.remark,
            approver: context.operator,
            timestamp: new Date().toISOString()
        });
        return {
            judgement: "CONCESSION",
            inspectionId: inspectionRecord.id,
            concessionRecorded: true
        };
    }

    if (judgement === "FAIL") {
        // 不合格 → 发起偏差，中断工序
        logger.error("质量门禁: 工序 " + process.code + " 检验不合格，发起偏差");

        var deviation = qms.createDeviation({
            source: "INSPECTION",
            inspectionId: inspectionRecord.id,
            batchNo: context.batchNo,
            processCode: process.code,
            severity: inspectionRecord.severity || "MAJOR",
            description: "工序 " + process.code + " 检验不合格: "
                + inspectionRecord.itemName + " = " + inspectionRecord.measuredValue
                + " (标准: " + inspectionRecord.lsl + " ~ " + inspectionRecord.usl + ")",
            triggeredBy: context.operator,
            timestamp: new Date().toISOString()
        });

        // 抛出异常中断流程（Control.Interrupt 语义）
        throw {
            type: "JudgementFailedException",
            message: "质量门禁拒绝: 工序 " + process.code + " 检验不合格",
            deviationCode: deviation.code,
            inspectionId: inspectionRecord.id
        };
    }

    // 未知判定 → 保守处理，发起偏差
    logger.error("质量门禁: 未知判定类型 " + judgement + "，按不合格处理");
    throw {
        type: "JudgementFailedException",
        message: "质量门禁: 未知判定类型 " + judgement
    };
}
