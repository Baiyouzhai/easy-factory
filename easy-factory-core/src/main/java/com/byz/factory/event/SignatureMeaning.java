package com.byz.factory.event;

/**
 * 签名含义 — 电子签名的语义类型。
 * <p>
 * 遵循 21 CFR Part 11 / EU Annex 11 对电子签名的要求。
 *
 * @author 苏政
 */
public enum SignatureMeaning {

    /** 已审核 — 确认内容正确无误 */
    REVIEWED,

    /** 已批准 — 授权执行或放行 */
    APPROVED,

    /** 已验证 — 双人复核/见证确认 */
    VERIFIED
}
