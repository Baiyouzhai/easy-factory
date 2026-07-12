package com.byz.factory.model;

/**
 * 检验指令 — QMS 模块的核心实体，定义对某个工序/产品的检验要求。
 * <p>
 * 关联检验方案和具体的检验项目，在工序到达检验点时由 QMS 创建。
 *
 * @author 苏政
 */
public interface IInspectionOrder {

    /** 检验指令编号 */
    String getInspectionNo();

    /** 工单号 */
    String getWorkOrderNo();

    /** 批号 */
    String getBatchNo();

    /** 工序编码（检验发生的工序） */
    String getProcessCode();

    /** 检验类型: IQC | IPQC | FQC | OQC */
    String getInspectionType();

    /** 检验方案编码 */
    String getPlanCode();

    /** 检验状态: PENDING | IN_PROGRESS | COMPLETED */
    String getStatus();

    /** 检验项目数 */
    int getTotalItems();

    /** 已完成项目数 */
    int getCompletedItems();

    /** 合格项目数 */
    int getPassedItems();

}
