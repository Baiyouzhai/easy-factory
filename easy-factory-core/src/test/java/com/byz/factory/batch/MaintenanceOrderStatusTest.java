package com.byz.factory.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MaintenanceOrderStatus 维护工单状态转换测试")
class MaintenanceOrderStatusTest {

    @Test
    @DisplayName("OPEN → IN_PROGRESS 允许")
    void open_toInProgress_allowed() {
        assertTrue(MaintenanceOrderStatus.OPEN.allowedTransitions()
                .contains(MaintenanceOrderStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("OPEN → CANCELLED 允许")
    void open_toCancelled_allowed() {
        assertTrue(MaintenanceOrderStatus.OPEN.allowedTransitions()
                .contains(MaintenanceOrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("IN_PROGRESS → COMPLETED 允许")
    void inProgress_toCompleted_allowed() {
        assertTrue(MaintenanceOrderStatus.IN_PROGRESS.allowedTransitions()
                .contains(MaintenanceOrderStatus.COMPLETED));
    }

    @Test
    @DisplayName("COMPLETED → VERIFIED 允许")
    void completed_toVerified_allowed() {
        assertTrue(MaintenanceOrderStatus.COMPLETED.allowedTransitions()
                .contains(MaintenanceOrderStatus.VERIFIED));
    }

    @Test
    @DisplayName("VERIFIED 终端状态 — 无出口")
    void verified_isTerminal() {
        assertTrue(MaintenanceOrderStatus.VERIFIED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("CANCELLED 终端状态 — 无出口")
    void cancelled_isTerminal() {
        assertTrue(MaintenanceOrderStatus.CANCELLED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("OPEN → COMPLETED 不允许（跳步骤）")
    void open_toCompleted_notAllowed() {
        assertFalse(MaintenanceOrderStatus.OPEN.allowedTransitions()
                .contains(MaintenanceOrderStatus.COMPLETED));
    }

    @Test
    @DisplayName("OPEN → VERIFIED 不允许（跳步骤）")
    void open_toVerified_notAllowed() {
        assertFalse(MaintenanceOrderStatus.OPEN.allowedTransitions()
                .contains(MaintenanceOrderStatus.VERIFIED));
    }

    @Test
    @DisplayName("IN_PROGRESS → VERIFIED 不允许（跳步骤）")
    void inProgress_toVerified_notAllowed() {
        assertFalse(MaintenanceOrderStatus.IN_PROGRESS.allowedTransitions()
                .contains(MaintenanceOrderStatus.VERIFIED));
    }
}
