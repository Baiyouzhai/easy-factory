package com.byz.factory.operation.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActionDuration 测试")
class ActionDurationTest {

    BigDecimal batch100k = new BigDecimal("100000");

    @Test
    @DisplayName("FIXED — 时间不随批量变化")
    void fixedTime() {
        ActionDuration d = ActionDuration.forAction("A004")
            .processingMinutes(5).fixedTime().build();

        assertEquals(ITimed.TimeNature.FIXED, d.getTimeNature());
        assertEquals(Duration.ofMinutes(5), d.estimateTotal(BigDecimal.ONE));
        assertEquals(Duration.ofMinutes(5), d.estimateTotal(batch100k));
        assertNull(d.getStandardBatchSize());
    }

    @Test
    @DisplayName("LINEAR — 时间随批量等比缩放")
    void linearTime() {
        ActionDuration d = ActionDuration.forAction("A016")
            .processingSeconds(1).variableTime(batch100k).build();

        assertEquals(ITimed.TimeNature.LINEAR, d.getTimeNature());
        assertEquals(batch100k, d.getStandardBatchSize());
        // 1秒/100000片 × 100000 = 1秒，约等于1秒
        Duration total = d.estimateTotal(batch100k);
        assertTrue(total.toMillis() >= 900 && total.toMillis() <= 1100);
    }

    @Test
    @DisplayName("设置+拆卸时间固定叠加")
    void setupAndTeardown() {
        ActionDuration d = ActionDuration.forAction("A005")
            .processingMinutes(90).setupMinutes(15).teardownMinutes(10).build();

        Duration total = d.estimateTotal(BigDecimal.ONE);
        // 90 + 15 + 10 = 115 min
        assertEquals(115, total.toMinutes());
    }

    @Test
    @DisplayName("ITimed.isFuzzy — FIXED 不是模糊")
    void notFuzzy() {
        ActionDuration d = ActionDuration.forAction("A001").processingMinutes(10).build();
        assertFalse(d.isFuzzy());
        assertFalse(d.isLinear());
    }

    @Test
    @DisplayName("ITimed.isLinear — LINEAR 返回 true")
    void isLinear() {
        ActionDuration d = ActionDuration.forAction("A016")
            .processingSeconds(1).variableTime(batch100k).build();
        assertTrue(d.isLinear());
    }
}
