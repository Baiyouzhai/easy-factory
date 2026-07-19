package com.byz.factory.eam;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AssetStatus 资产状态转换测试")
class AssetStatusTest {

    @Test
    @DisplayName("IDLE → IN_USE 允许")
    void idle_toInUse_allowed() {
        assertTrue(AssetStatus.IDLE.allowedTransitions().contains(AssetStatus.IN_USE));
    }

    @Test
    @DisplayName("IDLE → UNDER_MAINTENANCE 允许")
    void idle_toUnderMaintenance_allowed() {
        assertTrue(AssetStatus.IDLE.allowedTransitions().contains(AssetStatus.UNDER_MAINTENANCE));
    }

    @Test
    @DisplayName("IDLE → SCRAPPED 允许（直接报废）")
    void idle_toScrapped_allowed() {
        assertTrue(AssetStatus.IDLE.allowedTransitions().contains(AssetStatus.SCRAPPED));
    }

    @Test
    @DisplayName("IN_USE → IDLE 允许（停用）")
    void inUse_toIdle_allowed() {
        assertTrue(AssetStatus.IN_USE.allowedTransitions().contains(AssetStatus.IDLE));
    }

    @Test
    @DisplayName("IN_USE → UNDER_MAINTENANCE 允许")
    void inUse_toUnderMaintenance_allowed() {
        assertTrue(AssetStatus.IN_USE.allowedTransitions().contains(AssetStatus.UNDER_MAINTENANCE));
    }

    @Test
    @DisplayName("IN_USE → SCRAPPED 允许")
    void inUse_toScrapped_allowed() {
        assertTrue(AssetStatus.IN_USE.allowedTransitions().contains(AssetStatus.SCRAPPED));
    }

    @Test
    @DisplayName("UNDER_MAINTENANCE → IDLE 允许（维修完成）")
    void underMaintenance_toIdle_allowed() {
        assertTrue(AssetStatus.UNDER_MAINTENANCE.allowedTransitions().contains(AssetStatus.IDLE));
    }

    @Test
    @DisplayName("UNDER_MAINTENANCE → SCRAPPED 允许（维修后仍报废）")
    void underMaintenance_toScrapped_allowed() {
        assertTrue(AssetStatus.UNDER_MAINTENANCE.allowedTransitions().contains(AssetStatus.SCRAPPED));
    }

    @Test
    @DisplayName("SCRAPPED 终端状态 — 无出口")
    void scrapped_isTerminal() {
        assertTrue(AssetStatus.SCRAPPED.allowedTransitions().isEmpty());
    }

    @Test
    @DisplayName("IN_USE → UNDER_MAINTENANCE 非法（重复）不通过")
    void inUse_toUnderMaintenance_duplicateCheck() {
        // UNDER_MAINTENANCE 没有到 IN_USE 的出口
        assertFalse(AssetStatus.UNDER_MAINTENANCE.allowedTransitions().contains(AssetStatus.IN_USE));
    }
}
