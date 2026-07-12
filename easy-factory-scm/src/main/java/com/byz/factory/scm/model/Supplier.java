package com.byz.factory.scm.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Supplier extends DataExpand {
    private String code;
    private String name;
    private String category;         // RAW_MATERIAL/PACKAGING/EQUIPMENT/SERVICE
    private String qualification;    // QUALIFIED/UNDER_REVIEW/DISQUALIFIED
    private int leadTimeDays;
    private double onTimeRate;
    private double qualityRate;
    private String status;           // ACTIVE/INACTIVE/BLACKLISTED
}
