package com.byz.factory.andon.service;

import com.byz.factory.andon.model.AndonCall;

public interface AndonService {
    AndonCall trigger(String triggerType, String severity, String workOrderNo, String processCode, String description);
    void acknowledge(String callCode, String operator);
    void resolve(String callCode, String resolution);
    void escalate(String callCode);   // 逐级上报
}
