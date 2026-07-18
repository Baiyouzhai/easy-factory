package com.byz.factory.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ILifecycle 生命周期测试")
class ILifecycleTest {

    // 简单Stub实现
    enum TestStatus implements ILifecycle.StatusEnum {
        START, MIDDLE, END;

        @Override
        public Set<TestStatus> allowedTransitions() {
            return switch (this) {
                case START  -> Set.of(MIDDLE);
                case MIDDLE -> Set.of(END);
                case END    -> Set.of();
            };
        }
    }

    static class TestEntity implements ILifecycle<TestStatus> {
        private TestStatus status = TestStatus.START;
        @Override public TestStatus getStatus() { return status; }
        @Override public void setStatus(TestStatus s) { this.status = s; }
    }

    @Test
    @DisplayName("合法转换成功")
    void transition_valid_succeeds() {
        TestEntity entity = new TestEntity();
        entity.transition(TestStatus.MIDDLE);
        assertEquals(TestStatus.MIDDLE, entity.getStatus());
    }

    @Test
    @DisplayName("非法转换抛异常")
    void transition_invalid_throws() {
        TestEntity entity = new TestEntity();
        assertThrows(IllegalStateException.class, () -> entity.transition(TestStatus.END));
    }

    @Test
    @DisplayName("同状态转换不报错")
    void transition_sameState_noOp() {
        TestEntity entity = new TestEntity();
        assertDoesNotThrow(() -> entity.transition(TestStatus.START));
        assertEquals(TestStatus.START, entity.getStatus());
    }

    @Test
    @DisplayName("canTransition — 合法返回 true")
    void canTransition_valid_true() {
        TestEntity entity = new TestEntity();
        assertTrue(entity.canTransition(TestStatus.MIDDLE));
    }

    @Test
    @DisplayName("canTransition — 非法返回 false")
    void canTransition_invalid_false() {
        TestEntity entity = new TestEntity();
        assertFalse(entity.canTransition(TestStatus.END));
    }

    @Test
    @DisplayName("完整链: START→MIDDLE→END")
    void fullChain() {
        TestEntity entity = new TestEntity();
        entity.transition(TestStatus.MIDDLE);
        entity.transition(TestStatus.END);
        assertEquals(TestStatus.END, entity.getStatus());
    }
}
