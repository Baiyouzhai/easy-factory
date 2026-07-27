-- ============================================================
-- V1.9__mps.sql — MPS 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: ProductionPlan, PlanItem, DemandSource
-- ============================================================

-- 生产计划主表
CREATE TABLE IF NOT EXISTS mps_production_plan (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    plan_no         VARCHAR(100) NOT NULL UNIQUE,
    period_type     VARCHAR(20) NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    approved_by     VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_mps_plan_status ON mps_production_plan(status);
CREATE INDEX idx_mps_plan_period ON mps_production_plan(period_type, period_start);

-- 计划明细（PlanItem @ElementCollection）
CREATE TABLE IF NOT EXISTS mps_plan_item (
    plan_id         BIGINT NOT NULL REFERENCES mps_production_plan(id),
    product_code    VARCHAR(100) NOT NULL,
    product_name    VARCHAR(255) NOT NULL,
    quantity        NUMERIC(20,6) NOT NULL,
    due_date        DATE NOT NULL,
    priority        INT NOT NULL DEFAULT 5,
    factory_code    VARCHAR(50) NOT NULL
);
CREATE INDEX idx_mpitem_plan ON mps_plan_item(plan_id);
CREATE INDEX idx_mpitem_product ON mps_plan_item(product_code);

-- 需求来源主表
CREATE TABLE IF NOT EXISTS mps_demand_source (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    source_type     VARCHAR(20),
    reference_no    VARCHAR(100) NOT NULL,
    product_code    VARCHAR(100) NOT NULL,
    quantity        NUMERIC(20,6) NOT NULL,
    due_date        DATE NOT NULL,
    priority        INT NOT NULL DEFAULT 5,
    customer        VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_mdemand_product ON mps_demand_source(product_code);
CREATE INDEX idx_mdemand_source_type ON mps_demand_source(source_type);
CREATE INDEX idx_mdemand_due_date ON mps_demand_source(due_date);
