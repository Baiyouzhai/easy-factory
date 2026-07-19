package com.byz.factory.lims;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 称量任务抽象 — 供 MES/IoT/QMS 等模块编译期引用。
 * <p>
 * 称量任务关联配方和工单，记录每个物料的配方量、实际称量量和允差，
 * 通过 LIMS 模块的 {@code WeighingTask} 实现。
 *
 * @author 苏政
 */
public interface IWeighingTask {

    /** 称量任务号 */
    String getCode();

    /** 关联配方编码 */
    String getFormulaCode();

    /** 关联工单号 */
    String getWorkOrderId();

    /** 批号 */
    String getBatchNo();

    /** 称量项目列表 */
    List<? extends IWeighingItem> getItems();

    /** 任务状态 */
    WeighingTaskStatus getStatus();

    /**
     * 称量项目 — 单个物料的称量明细。
     */
    interface IWeighingItem {

        /** 物料编码 */
        String getMaterialCode();

        /** 配方量 */
        BigDecimal getFormulaQty();

        /** 实际称量量 */
        BigDecimal getActualQty();

        /** 允差（百分比绝对值，如 1.0 表示 ±1%） */
        BigDecimal getTolerance();

        /** 使用天平编号 */
        String getBalance();

        /** 称量人 */
        String getOperator();

        /** 复核人 */
        String getVerifier();

        /** 称量时间 */
        Instant getWeighedAt();

    }

}
