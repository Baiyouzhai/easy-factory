package com.byz.factory.aps.service;

import com.byz.factory.aps.model.Schedule;

public interface ApsService {
    Schedule create(String planNo, String strategy, String factoryCode);
    Schedule optimize(String scheduleCode);
    Schedule reschedule(String scheduleCode, String reason);
    void dispatch(String scheduleCode);   // 下发到 MES
    Object getGanttData(String scheduleCode);
}
