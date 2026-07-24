package com.byz.factory.batch;

import java.time.Instant;

/**
 * CAPA（纠正与预防措施）— QMS 模块的持续改进实体。
 * <p>
 * 偏差调查完成后，若需根本原因分析和长期改进，则创建 CAPA。
 * 供 DMS/Andon/MES 等模块编译期引用。
 *
 * @author 苏政
 */
public interface ICapa {

    /** CAPA 编号 */
    String getCode();

    /** CAPA 名称 */
    String getName();

    /** 关联偏差编号 */
    String getDeviationCode();

    /** 问题描述 */
    String getProblemDescription();

    /** 根因分析结果 */
    String getRootCause();

    /** 纠正措施 */
    String getCorrectiveAction();

    /** 预防措施 */
    String getPreventiveAction();

    /** 效果验证结果 */
    String getVerification();

    /** CAPA 状态 */
    CapaStatus getStatus();

    /** 负责人 */
    String getAssignTo();

    /** 批准人 */
    String getApprovedBy();

    /** 计划完成日期 */
    Instant getDueDate();

    /** 创建时间 */
    Instant getCreatedAt();

    /** 关闭时间 */
    Instant getClosedAt();

}
