package com.byz.factory.process.action;

import com.byz.factory.process.IActionModel;
import com.byz.factory.resource.IResourcePack;

/**
 * 设备动作 — IAction 在 Equip 子系统的扩展。
 * <p>
 * 执行时：下发工艺参数到设备、检查设备状态、记录运行数据、计算OEE。
 * 伴生结果：{@link EquipmentActionResult}
 *
 * @author 苏政
 */
public interface IEquipmentAction extends IActionModel {

    /** 设备参数（运行时下发） */
    default java.util.Map<String, Object> getEquipmentParameters() {
        return java.util.Collections.emptyMap();
    }

    /** 是否需要在执行前校准 */
    default boolean requiresCalibration() { return false; }

    /** 执行：参数下发 + 状态检查 + 返回设备运行结果 */
    EquipmentActionResult executeEquipment(IResourcePack input);

    /** 设备动作执行结果 */
    record EquipmentActionResult(
        String equipmentCode,
        boolean success,
        java.time.Duration runDuration,
        java.util.Map<String, Object> actualParameters,
        String statusBefore,
        String statusAfter,
        java.util.List<String> alarms
    ) {}
}
