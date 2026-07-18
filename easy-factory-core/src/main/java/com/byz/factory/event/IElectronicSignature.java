package com.byz.factory.event;

import java.time.Instant;

/**
 * 电子签名 — 批记录、检验报告、偏差处置等 GxP 操作的签名记录。
 * <p>
 * 每个签名绑定签名人、含义（审核/批准/验证）、时间戳和理由。
 * 与 {@link AuditTrail} 配合使用，实现 21 CFR Part 11 合规。
 *
 * @author 苏政
 */
public interface IElectronicSignature {

    /** 签名人标识 */
    String getSignerId();

    /** 签名含义（审核/批准/验证） */
    SignatureMeaning getMeaning();

    /** 签名时间 */
    Instant getTimestamp();

    /** 签名理由 */
    String getReason();
}
