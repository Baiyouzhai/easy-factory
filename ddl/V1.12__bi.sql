-- ============================================================
-- V1.12__bi.sql — BI 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: KpiSnapshot
-- ============================================================

-- KPI 快照主表
CREATE TABLE IF NOT EXISTS kpi_snapshot (
    id                      BIGSERIAL PRIMARY KEY,
    code                    VARCHAR(100) NOT NULL UNIQUE,
    name                    VARCHAR(255) NOT NULL,
    period                  VARCHAR(20) NOT NULL,
    factory_code            VARCHAR(100) NOT NULL,
    plan_completion_rate    NUMERIC(10,4) DEFAULT 0,
    first_pass_rate         NUMERIC(10,4) DEFAULT 0,
    oee                     NUMERIC(10,4) DEFAULT 0,
    mttr                    NUMERIC(10,4) DEFAULT 0,
    mtbf                    NUMERIC(10,4) DEFAULT 0,
    batch_yield             NUMERIC(10,4) DEFAULT 0,
    inventory_turnover      NUMERIC(10,4) DEFAULT 0,
    deviation_closure_rate  NUMERIC(10,4) DEFAULT 0,
    capa_closure_rate       NUMERIC(10,4) DEFAULT 0,
    andon_response_time     NUMERIC(10,4) DEFAULT 0,
    custom_kpis             TEXT,
    computed_at             VARCHAR(50),
    computed_by             VARCHAR(100),
    remark                  VARCHAR(1000),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_kpi_code ON kpi_snapshot(code);
CREATE INDEX idx_kpi_period ON kpi_snapshot(period);
CREATE INDEX idx_kpi_factory ON kpi_snapshot(factory_code);
CREATE INDEX idx_kpi_factory_period ON kpi_snapshot(factory_code, period);
