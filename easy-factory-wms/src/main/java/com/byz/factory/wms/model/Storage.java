package com.byz.factory.wms.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Storage extends DataExpand {
    private String locationCode;
    private String warehouse;
    private String zone;        // RAW/PACKAGING/FINISHED/QUARANTINE/REJECT
    private String storageType; // AMBIENT/COLD/FROZEN/HAZARDOUS
    private double capacity;
}
