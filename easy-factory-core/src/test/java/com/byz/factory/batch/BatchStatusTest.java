package com.byz.factory.batch;

import com.byz.factory.lifecycle.ILifecycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BatchStatus 批次状态转换测试")
class BatchStatusTest {

    @Test
    @DisplayName("CREATED → IN_PROGRESS 允许")
    void created_toInProgress_allowed() {
        assertTrue(BatchStatus.CREATED.allowedTransitions().contains(BatchStatus.IN_PROGRESS));
        assertTrue(BatchStatus.CREATED.allowedTransitions().contains(BatchStatus.CANCELLED));
    }

    @Test
    @DisplayName("IN_PROGRESS → COMPLETED 允许")
    void inProgress_toCompleted_allowed() {
        assertTrue(BatchStatus.IN_PROGRESS.allowedTransitions().contains(BatchStatus.COMPLETED));
    }

    @Test
    @DisplayName("UNDER_REVIEW → APPROVED/REJECTED 允许")
    void underReview_transitions() {
        assertTrue(BatchStatus.UNDER_REVIEW.allowedTransitions().contains(BatchStatus.APPROVED));
        assertTrue(BatchStatus.UNDER_REVIEW.allowedTransitions().contains(BatchStatus.REJECTED));
    }

    @Test
    @DisplayName("REJECTED → CANCELLED/IN_PROGRESS(返工) 允许")
    void rejected_canRework() {
        assertTrue(BatchStatus.REJECTED.allowedTransitions().contains(BatchStatus.CANCELLED));
        assertTrue(BatchStatus.REJECTED.allowedTransitions().contains(BatchStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("RELEASED → ARCHIVED 允许")
    void released_toArchived_allowed() {
        assertTrue(BatchStatus.RELEASED.allowedTransitions().contains(BatchStatus.ARCHIVED));
    }

    @Test
    @DisplayName("ARCHIVED 终端状态 — 无出口")
    void archived_isTerminal() {
        assertTrue(BatchStatus.ARCHIVED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("CANCELLED 终端状态 — 无出口")
    void cancelled_isTerminal() {
        assertTrue(BatchStatus.CANCELLED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("CREATED → COMPLETED 不允许")
    void created_toCompleted_notAllowed() {
        assertFalse(BatchStatus.CREATED.allowedTransitions().contains(BatchStatus.COMPLETED));
    }
}
