package com.byz.factory.andon.model;

import com.byz.factory.batch.AndonStatus;
import com.byz.factory.shared.BaseLifecycleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * 安灯呼叫 — 继承 BaseLifecycleEntity 获得状态机（OPEN→ACKNOWLEDGED→RESOLVED/ESCALATED）。
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AndonCall extends BaseLifecycleEntity<AndonStatus> {

    private String source;
    private String triggerType;
    private String workOrderNo;
    private String processCode;
    private String equipmentCode;
    private String severity;
    private String description;
    private String triggeredBy;
    private Instant triggeredAt;
    private Instant resolvedAt;

    public AndonCall(String code, String triggerType, String severity) {
        super(code, "Andon-" + code, AndonStatus.OPEN);
        this.triggerType = triggerType;
        this.severity = severity;
        this.triggeredAt = Instant.now();
    }

}
