package com.byz.factory.lims;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配方抽象 — 供 MES/QMS/ERP 等模块编译期引用。
 * <p>
 * 配方 = 产品编码 + 版本 + 批量规模 + 资源组分 + 投料阶段，
 * 通过 LIMS 模块的 {@code Formula} 实现。
 *
 * @author 苏政
 */
public interface IFormula {

    /** 配方编码 */
    String getCode();

    /** 配方名称 */
    String getName();

    /** 对应产品编码 */
    String getProductCode();

    /** 版本号（语义版本，如 "1.0.0"） */
    String getVersion();

    /** 标准批量规模 */
    BigDecimal getBatchSize();

    /** 配方状态 */
    FormulaStatus getStatus();

    /** 批准人 */
    String getApprovedBy();

    /** 收率范围（如 "95-102%"） */
    String getYield();

    /** 投料阶段列表 */
    List<? extends IFormulaPhase> getPhases();

    /**
     * 投料阶段 — 配方中按工艺阶段划分的物料投放分组。
     */
    interface IFormulaPhase {

        /** 阶段号（从 1 开始） */
        int getPhaseNo();

        /** 阶段名称（如 "预处理"、"主反应"、"后处理"） */
        String getPhaseName();

    }

}
