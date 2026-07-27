-- ============================================================
-- V1.3__equip.sql — Equip 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: Equipment, EquipmentParameter, EquipmentRecipe, OEMetrics
-- ============================================================

CREATE TABLE IF NOT EXISTS equip_equipment (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'IDLE',
    model           VARCHAR(100),
    category        VARCHAR(50),
    location        VARCHAR(100),
    asset_code      VARCHAR(50),
    specifications  VARCHAR(500),
    supplier        VARCHAR(100),
    purchase_date   DATE,
    group_name      VARCHAR(30),
    type_name       VARCHAR(30),
    number          NUMERIC(20,4) NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_equip_status ON equip_equipment(status);
CREATE INDEX idx_equip_category ON equip_equipment(category);

-- ============================================================
CREATE TABLE IF NOT EXISTS equip_parameter (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255),
    equipment_code  VARCHAR(100) NOT NULL,
    param_code      VARCHAR(50) NOT NULL,
    param_name      VARCHAR(100) NOT NULL,
    set_value       NUMERIC(20,6),
    actual_value    NUMERIC(20,6),
    upper_limit     NUMERIC(20,6),
    lower_limit     NUMERIC(20,6),
    unit            VARCHAR(20),
    control_method  VARCHAR(20),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_eparam_eq ON equip_parameter(equipment_code);
CREATE INDEX idx_eparam_pc ON equip_parameter(equipment_code, param_code);

-- ============================================================
CREATE TABLE IF NOT EXISTS equip_recipe (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    equipment_code  VARCHAR(100) NOT NULL,
    product_code    VARCHAR(100),
    version         VARCHAR(20) NOT NULL DEFAULT '1.0.0',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_erecipe_eq ON equip_recipe(equipment_code);

-- 配方阶段明细
CREATE TABLE IF NOT EXISTS equip_recipe_phase (
    recipe_id       BIGINT NOT NULL REFERENCES equip_recipe(id),
    param_code      VARCHAR(50),
    phase           VARCHAR(50),
    set_value       NUMERIC(20,6),
    duration        INT NOT NULL DEFAULT 0,
    ramp_rate       NUMERIC(10,4) DEFAULT 0
);
CREATE INDEX idx_erphase_rid ON equip_recipe_phase(recipe_id);

-- ============================================================
CREATE TABLE IF NOT EXISTS equip_oee_metrics (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(200) NOT NULL UNIQUE,
    name            VARCHAR(300),
    equipment_code  VARCHAR(100) NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    availability    NUMERIC(6,4),
    performance     NUMERIC(6,4),
    quality         NUMERIC(6,4),
    oee             NUMERIC(6,4),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_oee_eq ON equip_oee_metrics(equipment_code);
CREATE INDEX idx_oee_period ON equip_oee_metrics(equipment_code, period_start);
