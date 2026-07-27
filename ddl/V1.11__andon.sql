-- ============================================================
-- V1.11__andon.sql — Andon 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: AndonCall, EscalationRule
-- ============================================================

-- 安灯呼叫主表
CREATE TABLE IF NOT EXISTS andon_call (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    source          VARCHAR(10) NOT NULL,
    trigger_type    VARCHAR(30) NOT NULL,
    work_order_no   VARCHAR(100),
    process_code    VARCHAR(100),
    equipment_code  VARCHAR(100),
    severity        VARCHAR(15) NOT NULL,
    description     VARCHAR(500),
    triggered_by    VARCHAR(100) NOT NULL,
    triggered_at    TIMESTAMPTZ NOT NULL,
    escalation_level INT DEFAULT 0,
    acknowledged_by VARCHAR(100),
    acknowledged_at TIMESTAMPTZ,
    resolution      VARCHAR(1000),
    resolved_at     TIMESTAMPTZ,
    closed_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_acall_code ON andon_call(code);
CREATE INDEX idx_acall_status ON andon_call(status);
CREATE INDEX idx_acall_wo ON andon_call(work_order_no);
CREATE INDEX idx_acall_equip ON andon_call(equipment_code);
CREATE INDEX idx_acall_trigger ON andon_call(trigger_type);

-- 上报规则主表
CREATE TABLE IF NOT EXISTS andon_escalation_rule (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    trigger_type    VARCHAR(30) NOT NULL,
    severity        VARCHAR(15) NOT NULL,
    auto_stop       VARCHAR(20) NOT NULL DEFAULT 'NONE',
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_aerule_type_sev ON andon_escalation_rule(trigger_type, severity);
CREATE INDEX idx_aerule_enabled ON andon_escalation_rule(enabled);

-- 上报级别（EscalationRule.EscalationLevel，暂存内存不持久化）
-- 生产环境使用时按需增加此表
