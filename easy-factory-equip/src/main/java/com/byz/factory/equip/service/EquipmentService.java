package com.byz.factory.equip.service;

import com.byz.factory.equip.model.Equipment;

/**
 * 设备服务 — Equip 模块核心。
 * <p>
 * TODO 待实现：设备台账、设备配方管理、OEE计算、参数下发
 *
 * @author 苏政
 */
public interface EquipmentService {

    /** 查询设备状态 */
    Equipment.Status getStatus(String equipmentCode);

    /** 占用设备 */
    void occupy(String equipmentCode, String workOrderNo);

    /** 释放设备 */
    void release(String equipmentCode);

    /** 计算 OEE */
    double calculateOee(String equipmentCode, String periodStart, String periodEnd);

}
