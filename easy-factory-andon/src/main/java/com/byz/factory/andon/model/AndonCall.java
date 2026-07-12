package com.byz.factory.andon.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class AndonCall extends DataExpand {
    private String code;
    private String source;           // MANUAL/AUTO
    private String triggerType;      // EQUIPMENT_FAULT/QUALITY_ISSUE/MATERIAL_SHORTAGE/SAFETY/PROCESS_DELAY
    private String workOrderNo;
    private String processCode;
    private String equipmentCode;
    private String severity;         // INFO/WARNING/CRITICAL/EMERGENCY
    private String description;
    private String status;           // OPEN/ACKNOWLEDGED/IN_PROGRESS/RESOLVED/CLOSED
    private String triggeredBy;
    private Instant triggeredAt;
    private Instant resolvedAt;

    public AndonCall(String code, String triggerType, String severity) {
        this.code = code; this.triggerType = triggerType; this.severity = severity;
        this.status = "OPEN"; this.triggeredAt = Instant.now();
    }
}
