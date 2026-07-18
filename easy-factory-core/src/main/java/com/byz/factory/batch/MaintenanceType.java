package com.byz.factory.batch;

/**
 * 维护类型。
 *
 * @author 苏政
 */
public enum MaintenanceType {

    /** 预防性维护（按计划周期） */
    PREVENTIVE,
    /** 纠正性维护（故障后维修） */
    CORRECTIVE,
    /** 预测性维护（基于状态监测） */
    PREDICTIVE,
    /** 校准 */
    CALIBRATION

}
