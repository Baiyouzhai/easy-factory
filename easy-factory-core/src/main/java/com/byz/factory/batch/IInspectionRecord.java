package com.byz.factory.batch;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 检验记录 — QMS 模块的检验执行记录。
 * <p>
 * 每次检验动作执行后生成一条检验记录，记录实测值和判定结果。
 * 供 MES/LIMS/DMS 等模块编译期引用。
 *
 * @author 苏政
 */
public interface IInspectionRecord {

    /** 记录编号 */
    String getCode();

    /** 关联检验单编号 */
    String getInspectionNo();

    /** 关联检验方案编码 */
    String getPlanCode();

    /** 检验项目编码 */
    String getItemCode();

    /** 检验项目名称 */
    String getItemName();

    /** 规格上限 */
    BigDecimal getUsl();

    /** 规格下限 */
    BigDecimal getLsl();

    /** 目标值 */
    BigDecimal getTarget();

    /** 计量单位 */
    String getUnit();

    /** 实测值 */
    BigDecimal getMeasuredValue();

    /** 判定结果：PASS / FAIL / CONCESSION */
    String getJudgement();

    /** 不良代码 */
    String getDefectCode();

    /** 使用的量检具编码 */
    String getGaugeCode();

    /** 检验员 */
    String getInspector();

    /** 备注 */
    String getRemark();

    /** 检验时间 */
    Instant getInspectedAt();

}
