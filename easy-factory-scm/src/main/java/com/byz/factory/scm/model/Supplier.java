package com.byz.factory.scm.model;

import com.byz.factory.scm.ISupplier;
import com.byz.factory.scm.SupplierStatus;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商 — 继承 BaseEntity 获得 code/name/audit，实现 ISupplier 供跨模块引用。
 *
 * <h3>两个独立维度</h3>
 * Supplier 的状态由两个正交维度组成，各自独立变化：
 * <ul>
 *   <li><b>qualification（资质状态，String）</b> — 表示供应商是否通过准入门槛。
 *       UNDER_REVIEW → QUALIFIED / DISQUALIFIED。由 qualify() / disqualify() 操作。</li>
 *   <li><b>status（运营状态，SupplierStatus 枚举）</b> — 表示当前合作状态。
 *       ACTIVE / INACTIVE / BLACKLISTED。由 deactivate() / reactivate() / blacklist() 操作。</li>
 * </ul>
 * 一个已通过资质审核（QUALIFIED）的供应商可能因付款纠纷被暂停合作（INACTIVE），
 * 也可能恢复（ACTIVE）后因质量问题被拉黑（BLACKLISTED）——两个维度互不绑定。
 * <p>
 * qualification 用 String 而非枚举，预留与外部 OA/ERP 资质系统对接时扩展中间状态
 * （如 EXPIRING、SUSPENDED 等），避免修改 core 接口。
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   scm.category       — 供应商类别
 *   scm.qualification  — 资质状态
 *   scm.leadTime       — 交货周期(天)
 *   scm.onTimeRate     — 准时交货率(%)
 *   scm.qualityRate    — 一次合格率(%)
 *   scm.contacts       — 联系人列表(JSON)
 *   scm.status         — 运营状态
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Supplier extends BaseEntity implements ISupplier {

    /** 类别 (RAW_MATERIAL / PACKAGING / EQUIPMENT / SERVICE) */
    private String category;

    /** 资质状态 (QUALIFIED / UNDER_REVIEW / DISQUALIFIED) */
    private String qualification;

    /** 运营状态 */
    private SupplierStatus status;

    /** 平均交货周期（天） */
    private int leadTimeDays;

    /** 准时交货率（%） */
    private double onTimeRate;

    /** 一次合格率（%） */
    private double qualityRate;

    public Supplier(String code, String name, String category) {
        super(code, name);
        this.category = category;
        this.qualification = "UNDER_REVIEW";
        this.status = SupplierStatus.ACTIVE;
    }

    /** 通过资质审核 */
    public void qualify() {
        this.qualification = "QUALIFIED";
        markUpdated();
    }

    /** 取消资质 */
    public void disqualify() {
        this.qualification = "DISQUALIFIED";
        markUpdated();
    }

    /** 暂停合作 */
    public void deactivate() {
        this.status = SupplierStatus.INACTIVE;
        markUpdated();
    }

    /** 恢复合作 */
    public void reactivate() {
        this.status = SupplierStatus.ACTIVE;
        markUpdated();
    }

    /** 永久拉黑 */
    public void blacklist() {
        this.status = SupplierStatus.BLACKLISTED;
        markUpdated();
    }

}
