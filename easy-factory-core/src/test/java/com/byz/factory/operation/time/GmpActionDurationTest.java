package com.byz.factory.operation.time;

import com.byz.factory.operation.time.IConstrainedTimed.ConstraintResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GmpActionDuration 合规测试")
class GmpActionDurationTest {

    @Test
    @DisplayName("约束校验 — 实际耗时在范围内")
    void withinConstraints() {
        var b = GmpActionDuration.forAction("A005");
        b.processingMinutes(90);
        b.maxAllowedHours(4);
        b.minRequiredMinutes(60);
        GmpActionDuration d = b.build();

        assertTrue(d.isTimeCritical());
        assertEquals(Duration.ofHours(4), d.getMaxAllowedTime().get());
        assertEquals(Duration.ofMinutes(60), d.getMinRequiredTime().get());

        ConstraintResult r = d.checkConstraints(Duration.ofMinutes(90));
        assertTrue(r.passed());
    }

    @Test
    @DisplayName("约束校验 — 超时")
    void exceedsMax() {
        var b = GmpActionDuration.forAction("A005");
        b.processingMinutes(90); b.maxAllowedHours(4);
        GmpActionDuration d = b.build();

        ConstraintResult r = d.checkConstraints(Duration.ofHours(5));
        assertTrue(r.isViolated());
    }

    @Test
    @DisplayName("约束校验 — 不足")
    void belowMin() {
        var b = GmpActionDuration.forAction("A009");
        b.processingMinutes(30); b.minRequiredMinutes(30);
        GmpActionDuration d = b.build();

        ConstraintResult r = d.checkConstraints(Duration.ofMinutes(10));
        assertTrue(r.isViolated());
    }

    @Test
    @DisplayName("保质期 — 中间体有时限")
    void shelfLife() {
        var b = GmpActionDuration.forAction("A005");
        b.processingMinutes(90); b.shelfLifeAfterActionHours(72);
        GmpActionDuration d = b.build();

        assertTrue(d.isTimeCritical());
        assertEquals(Duration.ofHours(72), d.getShelfLifeAfterAction().get());
    }
}
