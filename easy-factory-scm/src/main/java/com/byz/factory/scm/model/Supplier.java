package com.byz.factory.scm.model;

import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商 — 继承 BaseEntity 获得 code/name/audit。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Supplier extends BaseEntity {

    private String category;
    private String qualification;
    private int leadTimeDays;
    private double onTimeRate;
    private double qualityRate;

    public Supplier(String code, String name, String category) {
        super(code, name);
        this.category = category;
    }

}
