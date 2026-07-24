package com.byz.factory.andon.model;

/**
 * 安灯呼叫来源。
 *
 * @author 苏政
 */
public enum AndonSource {

    /** 人工触发（操作工按 Andon 按钮） */
    MANUAL,
    /** 系统自动触发（设备报警/SPC 超限/工序超时） */
    AUTO;

}
