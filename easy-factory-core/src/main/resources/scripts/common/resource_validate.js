/**
 * @id          common.resource-validate.v1
 * @name        资源校验
 * @version     1.0.0
 * @module      common
 * @author      engineer-a
 * @description 校验输入资源是否满足工序的 requireResources 要求。
 *              检查所有必要资源是否存在且数量充足。
 * @param       {ProcessInfo} process - 当前工序
 * @param       {ResourceInfo[]} inputResources - 输入资源列表
 * @return      {ValidationResult} 校验结果: { valid: boolean, missing: [...], insufficient: [...] }
 * @since       2026-07-12
 */

function execute(context) {
    var process = context.process;
    var inputResources = context.inputResources;
    var logger = context.logger;

    var required = process.requireResources();
    if (!required || required.length === 0) {
        return { valid: true, missing: [], insufficient: [] };
    }

    // 构建输入资源索引: { "group-name": number }
    var inputMap = {};
    for (var i = 0; i < inputResources.length; i++) {
        var r = inputResources[i];
        var key = r.group + "-" + r.name;
        inputMap[key] = r.number;
    }

    var missing = [];
    var insufficient = [];

    for (var j = 0; j < required.length; j++) {
        var req = required[j];
        var reqKey = req.group + "-" + req.name;
        var available = inputMap[reqKey];

        if (available === undefined || available === null) {
            missing.push({
                name: req.name,
                group: req.group,
                required: req.number
            });
            logger.error("资源校验: 缺少必要资源 " + req.name + " (需要 " + req.number + ")");
        } else if (available < req.number) {
            insufficient.push({
                name: req.name,
                group: req.group,
                required: req.number,
                available: available,
                shortage: req.number - available
            });
            logger.error("资源校验: " + req.name + " 数量不足 (需要 " + req.number
                + ", 可用 " + available + ", 短缺 " + (req.number - available) + ")");
        } else {
            logger.info("资源校验: " + req.name + " 通过 (需要 " + req.number
                + ", 可用 " + available + ")");
        }
    }

    var valid = missing.length === 0 && insufficient.length === 0;

    if (valid) {
        logger.info("资源校验: 所有必要资源满足要求");
    }

    return {
        valid: valid,
        missing: missing,
        insufficient: insufficient,
        checkedAt: new Date().toISOString()
    };
}
