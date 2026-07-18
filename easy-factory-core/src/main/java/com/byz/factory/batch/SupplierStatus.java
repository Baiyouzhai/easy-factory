package com.byz.factory.batch;

/**
 * 供应商状态 — 供应商运营状态分类。
 * <p>
 * ACTIVE=正常合作, INACTIVE=暂停合作, BLACKLISTED=永久拉黑。
 * 非状态机枚举——状态变更由管理操作驱动，不需要生命周期校验。
 *
 * @author 苏政
 */
public enum SupplierStatus {

    /** 正常合作 */
    ACTIVE,
    /** 暂停合作 */
    INACTIVE,
    /** 永久拉黑 */
    BLACKLISTED

}
