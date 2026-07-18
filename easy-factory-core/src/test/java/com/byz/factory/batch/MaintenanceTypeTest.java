package com.byz.factory.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MaintenanceType 维护类型枚举测试")
class MaintenanceTypeTest {

    @Test
    @DisplayName("枚举包含全部 4 个值")
    void shouldHaveFourValues() {
        assertEquals(4, MaintenanceType.values().length);
    }

    @Test
    @DisplayName("PREVENTIVE 值存在")
    void preventive_exists() {
        assertNotNull(MaintenanceType.valueOf("PREVENTIVE"));
    }

    @Test
    @DisplayName("CORRECTIVE 值存在")
    void corrective_exists() {
        assertNotNull(MaintenanceType.valueOf("CORRECTIVE"));
    }

    @Test
    @DisplayName("PREDICTIVE 值存在")
    void predictive_exists() {
        assertNotNull(MaintenanceType.valueOf("PREDICTIVE"));
    }

    @Test
    @DisplayName("CALIBRATION 值存在")
    void calibration_exists() {
        assertNotNull(MaintenanceType.valueOf("CALIBRATION"));
    }
}
